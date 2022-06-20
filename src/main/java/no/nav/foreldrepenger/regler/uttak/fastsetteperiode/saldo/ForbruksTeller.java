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
                                                    List<FastsattUttakPeriode> søkersPerioder, Predicate<FastsattUttakPeriode> tellPeriode) {
        return forbruksTeller(stønadskonto, aktivitet, søkersPerioder, tellPeriode,
                (s,p) -> Trekkdager.ZERO, (p, a) -> Objects.equals(stønadskonto, a.getStønadskontotype()));
    }

    static Trekkdager forbruksTellerKontoMedUnntak(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet,
                                                    List<FastsattUttakPeriode> søkersPerioder, Predicate<FastsattUttakPeriode> tellPeriode,
                                                   BiFunction<Stønadskontotype, FastsattUttakPeriode, Trekkdager> unntaksTeller) {
        return forbruksTeller(stønadskonto, aktivitet, søkersPerioder, tellPeriode,
                unntaksTeller, (p, a) -> Objects.equals(stønadskonto, a.getStønadskontotype()));
    }


    static Trekkdager forbruksTeller(Stønadskontotype stønadskonto,
                                     AktivitetIdentifikator aktivitet,
                                     List<FastsattUttakPeriode> søkersPerioder,
                                     Predicate<FastsattUttakPeriode> tellPeriode,
                                     BiFunction<Stønadskontotype, FastsattUttakPeriode, Trekkdager> unntaksTeller,
                                     BiPredicate<FastsattUttakPeriode, FastsattUttakPeriodeAktivitet> aktivitetsfilter) {
        var sum = Trekkdager.ZERO;
        var startindex = førstePeriodeSomTellesMedAktivitet(aktivitet, søkersPerioder, tellPeriode);
        // Tilkommet aktivitet. Bruk minste forbruk inntil første forekomst - evt ZERO dersom ingen forekomster.
        if (startindex > 0) {
            var perioderTomPeriode = søkersPerioder.subList(0, startindex);
            var minForbrukteDagerEksisterendeAktiviteter = aktiviteterIPerioder(perioderTomPeriode).stream()
                    .filter(a -> førstePeriodeSomTellesMedAktivitet(a, perioderTomPeriode, tellPeriode) == 0) // Tell aktiviteter som finnes fra start
                    .map(a -> forbruksTeller(stønadskonto, a, perioderTomPeriode, tellPeriode, unntaksTeller, aktivitetsfilter))
                    .min(Trekkdager::compareTo)
                    .orElse(Trekkdager.ZERO);
            sum = sum.add(minForbrukteDagerEksisterendeAktiviteter);
        }

        for (var i = startindex; i < søkersPerioder.size(); i++) {
            var periodeSøker = søkersPerioder.get(i);
            if (tellPeriode.test(periodeSøker)) {
                sum = sum.add(trekkdagerForUttaksperiode(aktivitet, periodeSøker, aktivitetsfilter));
            } else {
                sum = sum.add(unntaksTeller.apply(stønadskonto, periodeSøker));
            }
        }

        return sum;
    }

    // Index til første periode som inneholder aktivitet, 0 dersom finnes fra start, max dersom ikke funnet
    private static int førstePeriodeSomTellesMedAktivitet(AktivitetIdentifikator aktivitet,
                                                          List<FastsattUttakPeriode> perioder,
                                                          Predicate<FastsattUttakPeriode> tellPeriode) {
        var førstePeriodeSomTelles = -1;
        for (var i = 0; i < perioder.size(); i++) {
            var periode = perioder.get(i);
            if (tellPeriode.test(periode)) {
                if (førstePeriodeSomTelles == -1) {
                    førstePeriodeSomTelles = i;
                }
                if (aktivitetIPeriode(periode, aktivitet)) {
                    return i == førstePeriodeSomTelles ? 0 : i;
                }
            }
        }
        return perioder.size();
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
