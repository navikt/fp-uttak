package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktivitetIPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktiviteterIPerioder;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

final class ForbruksTeller {

    static Trekkdager forbruksTellerKontoKunForbruk(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet,
                                                    List<FastsattUttakPeriode> søkersPerioder, Predicate<FastsattUttakPeriode> unntak) {
        return forbruksTeller(stønadskonto, aktivitet, søkersPerioder, unntak,
                (s,p) -> Trekkdager.ZERO, (p, a) -> Objects.equals(stønadskonto, a.getStønadskontotype()));
    }


    static Trekkdager forbruksTeller(Stønadskontotype stønadskonto,
                                     AktivitetIdentifikator aktivitet,
                                     List<FastsattUttakPeriode> søkersPerioder,
                                     Predicate<FastsattUttakPeriode> unntak,
                                     BiFunction<Stønadskontotype, FastsattUttakPeriode, Trekkdager> unntaksTeller,
                                     BiPredicate<FastsattUttakPeriode, FastsattUttakPeriodeAktivitet> aktivitetsvelger) {
        var sum = Trekkdager.ZERO;

        for (var i = 0; i < søkersPerioder.size(); i++) {
            var periodeSøker = søkersPerioder.get(i);
            if (unntak.test(periodeSøker)) {
                sum = sum.add(unntaksTeller.apply(stønadskonto, periodeSøker));
            } else {
                var nestePeriodeSomIkkeErUnntak = nestePeriodeIkkeUnntakFinner(søkersPerioder, i, unntak);
                // Hvis ikke aktivitet i perioden (tilkommet aktivitet) - bruk minste forbruk opp til periode.
                if (!aktivitetIPeriode(periodeSøker, aktivitet) &&
                        (nestePeriodeSomIkkeErUnntak == 0 || aktivitetIPeriode(søkersPerioder.get(nestePeriodeSomIkkeErUnntak), aktivitet))) {
                    var perioderTomPeriode = søkersPerioder.subList(0, i + 1);
                    var eksisterendeAktiviteter = aktiviteterIPerioder(perioderTomPeriode);
                    eksisterendeAktiviteter.remove(aktivitet);
                    var minForbrukteDagerEksisterendeAktiviteter = eksisterendeAktiviteter.stream()
                            .map(a -> forbruksTeller(stønadskonto, a, perioderTomPeriode, unntak, unntaksTeller, aktivitetsvelger))
                            .min(Trekkdager::compareTo)
                            .orElseThrow();
                    sum = sum.add(minForbrukteDagerEksisterendeAktiviteter);
                } else {
                    sum = sum.add(trekkdagerForUttaksperiode(aktivitet, periodeSøker, aktivitetsvelger));
                }
            }
        }

        return sum;
    }

    private static int nestePeriodeIkkeUnntakFinner(List<FastsattUttakPeriode> perioder,
                                                    int index, Predicate<FastsattUttakPeriode> unntak) {
        for (var i = index + 1; i < perioder.size(); i++) {
            var periode = perioder.get(i);
            if (!unntak.test(periode)) {
                return i;
            }
        }
        return 0;
    }


    private static Trekkdager trekkdagerForUttaksperiode(AktivitetIdentifikator aktivitet,
                                                         FastsattUttakPeriode periode,
                                                         BiPredicate<FastsattUttakPeriode, FastsattUttakPeriodeAktivitet> aktivitetsvelger) {
        return periode.getAktiviteter().stream()
                .filter(a -> a.getAktivitetIdentifikator().equals(aktivitet))
                .filter(a -> aktivitetsvelger.test(periode,a))
                .findFirst()
                .map(FastsattUttakPeriodeAktivitet::getTrekkdager)
                .orElse(Trekkdager.ZERO);
    }
}
