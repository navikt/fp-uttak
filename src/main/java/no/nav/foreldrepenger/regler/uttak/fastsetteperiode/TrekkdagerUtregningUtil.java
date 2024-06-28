package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;

public final class TrekkdagerUtregningUtil {

    private static final BigDecimal BIG_DECIMAL_100 = BigDecimal.valueOf(100);

    private TrekkdagerUtregningUtil() {
    }

    public static Trekkdager beregnTrekkdagerFor(OppgittPeriode oppgittPeriode,
                                                 AktivitetIdentifikator aktivitet,
                                                 Utbetalingsgrad utbetalingsgrad,
                                                 boolean skalUtbetale,
                                                 GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        var virkedagerIPerioden = Virkedager.beregnAntallVirkedager(oppgittPeriode);

        if (skalUtbetale) {
            if (graderingIkkeInnvilgetÅrsak != null) {
                return new Trekkdager(virkedagerIPerioden);
            }
            return beregnetTrekkdager(virkedagerIPerioden, utbetalingsgrad.decimalValue());
        } else {
            if (oppgittPeriode.erSøktGradering(aktivitet)) {
                return beregnetTrekkdager(virkedagerIPerioden, BIG_DECIMAL_100.subtract(oppgittPeriode.getArbeidsprosent()));
            }
            return new Trekkdager(virkedagerIPerioden);
        }
    }

    private static Trekkdager beregnetTrekkdager(int virkedagerIPerioden, BigDecimal trekkProsent) {
        return new Trekkdager(BigDecimal.valueOf(virkedagerIPerioden)
            .multiply(trekkProsent)
            .divide(BIG_DECIMAL_100, 1, RoundingMode.DOWN));
    }
}
