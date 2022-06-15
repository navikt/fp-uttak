package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

final class SaldoUtregningUtil {

    private SaldoUtregningUtil() {
    }

    static boolean aktivitetIPeriode(FastsattUttakPeriode periode, AktivitetIdentifikator aktivitet) {
        return periode.getAktiviteter()
                .stream()
                .map(a -> a.getAktivitetIdentifikator())
                .anyMatch(aktivitetIdentifikator -> aktivitetIdentifikator.equals(aktivitet));
    }

    static Set<AktivitetIdentifikator> aktiviteterIPerioder(List<FastsattUttakPeriode> perioder) {
        return perioder.stream()
                .flatMap(p -> p.getAktiviteter().stream())
                .map(FastsattUttakPeriodeAktivitet::getAktivitetIdentifikator)
                .collect(Collectors.toSet());
    }

    static List<FastsattUttakPeriode> overlappendePeriode(FastsattUttakPeriode periode, List<FastsattUttakPeriode> perioder) {
        var resultat = new ArrayList<FastsattUttakPeriode>();
        for (var periode2 : perioder) {
            if (overlapper(periode, periode2)) {
                resultat.add(periode2);
            }
        }
        return resultat;
    }

    static boolean overlapper(FastsattUttakPeriode periode, FastsattUttakPeriode periode2) {
        return !periode2.getFom().isAfter(periode.getTom()) && !periode2.getTom().isBefore(periode.getFom());
    }

    static boolean innvilgetMedTrekkdager(FastsattUttakPeriode periode) {
        return !periode.getPerioderesultattype().equals(Perioderesultattype.AVSLÅTT) || periode.getAktiviteter()
                .stream()
                .anyMatch(aktivitet -> aktivitet.getTrekkdager().merEnn0());
    }

    static Trekkdager trekkDagerFraDelAvPeriode(LocalDate delFom,
                                         LocalDate delTom,
                                         LocalDate periodeFom,
                                         LocalDate periodeTom,
                                         Trekkdager periodeTrekkdager) {
        var virkedagerInnenfor = Virkedager.beregnAntallVirkedager(delFom, delTom);
        var virkedagerHele = Virkedager.beregnAntallVirkedager(periodeFom, periodeTom);
        if (virkedagerHele == 0) {
            return Trekkdager.ZERO;
        }
        var utregning = periodeTrekkdager.decimalValue().multiply(BigDecimal.valueOf(virkedagerInnenfor))
                .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN);
        return new Trekkdager(utregning);
    }

}
