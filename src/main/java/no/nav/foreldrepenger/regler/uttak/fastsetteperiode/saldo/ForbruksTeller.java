package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktivitetIPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktiviteterIPerioder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

final class ForbruksTeller {

    static BigDecimal forbruksTellerKontoKunForbruk(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet,
                                                    List<FastsattUttakPeriode> søkersPerioder, Predicate<FastsattUttakPeriode> unntak) {
        return forbruksTeller(stønadskonto, aktivitet, søkersPerioder, unntak,
                (s,p) -> BigDecimal.ZERO, (p, a) -> Objects.equals(stønadskonto, a.getStønadskontotype()));
    }


    static BigDecimal forbruksTeller(Stønadskontotype stønadskonto,
                                     AktivitetIdentifikator aktivitet,
                                     List<FastsattUttakPeriode> søkersPerioder,
                                     Predicate<FastsattUttakPeriode> unntak,
                                     BiFunction<Stønadskontotype, FastsattUttakPeriode, BigDecimal> unntaksTeller,
                                     BiPredicate<FastsattUttakPeriode, FastsattUttakPeriodeAktivitet> aktivitetsvelger) {
        var sum = BigDecimal.ZERO;

        for (var i = 0; i < søkersPerioder.size(); i++) {
            var periodeSøker = søkersPerioder.get(i);
            if (unntak.test(periodeSøker)) {
                sum = sum.add(unntaksTeller.apply(stønadskonto, periodeSøker));
            } else {
                var nestePeriodeSomIkkeErOpphold = nestePeriodeFinner(søkersPerioder, i, unntak);
                if (!aktivitetIPeriode(periodeSøker, aktivitet) &&
                        (nestePeriodeSomIkkeErOpphold.isEmpty() || aktivitetIPeriode(nestePeriodeSomIkkeErOpphold.get(), aktivitet))) {
                    var perioderTomPeriode = søkersPerioder.subList(0, i + 1);
                    var eksisterendeAktiviteter = aktiviteterIPerioder(perioderTomPeriode);
                    eksisterendeAktiviteter.remove(aktivitet);
                    var minForbrukteDagerEksisterendeAktiviteter = eksisterendeAktiviteter.stream()
                            .map(a -> forbruksTeller(stønadskonto, a, perioderTomPeriode, unntak, unntaksTeller, aktivitetsvelger))
                            .min(BigDecimal::compareTo)
                            .orElseThrow();
                    sum = sum.add(minForbrukteDagerEksisterendeAktiviteter);
                } else {
                    sum = sum.add(trekkdagerForUttaksperiode(aktivitet, periodeSøker, aktivitetsvelger));
                }
            }
        }

        return sum;
    }

    private static Optional<FastsattUttakPeriode> nestePeriodeFinner(List<FastsattUttakPeriode> perioder, int index, Predicate<FastsattUttakPeriode> unntak) {
        for (var i = index + 1; i < perioder.size(); i++) {
            var periode = perioder.get(i);
            if (!unntak.test(periode)) {
                return Optional.of(periode);
            }
        }
        return Optional.empty();
    }


    private static BigDecimal trekkdagerForUttaksperiode(AktivitetIdentifikator aktivitet,
                                                         FastsattUttakPeriode periode,
                                                         BiPredicate<FastsattUttakPeriode, FastsattUttakPeriodeAktivitet> aktivitetsvelger) {
        for (var periodeAktivitet : periode.getAktiviteter()) {
            if (periodeAktivitet.getAktivitetIdentifikator().equals(aktivitet) && aktivitetsvelger.test(periode, periodeAktivitet)) {
                return periodeAktivitet.getTrekkdager().decimalValue();
            }
        }
        return BigDecimal.ZERO;
    }
}
