package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

public final class TrekkdagerUtregningUtil {

    private static final BigDecimal BIG_DECIMAL_100 = BigDecimal.valueOf(100);

    private TrekkdagerUtregningUtil() {
    }


    public static Trekkdager trekkdagerFor(OppgittPeriode oppgittPeriode, boolean graderingSøktOgInnvilget, Utbetalingsgrad utbetalingsgrad) {
        var virkedagerIPerioden = Virkedager.beregnAntallVirkedager(oppgittPeriode);

        if (graderingSøktOgInnvilget) {
            return beregnetTrekkdager(virkedagerIPerioden, BIG_DECIMAL_100.subtract(oppgittPeriode.getArbeidsprosent()));
        }

        if (!Utbetalingsgrad.ZERO.equals(utbetalingsgrad)) {
            return beregnetTrekkdager(virkedagerIPerioden, utbetalingsgrad.decimalValue());
        }

        return new Trekkdager(virkedagerIPerioden);
    }

    private static Trekkdager beregnetTrekkdager(int virkedagerIPerioden, BigDecimal trekkProsent) {
        return new Trekkdager(BigDecimal.valueOf(virkedagerIPerioden)
            .multiply(trekkProsent)
            .divide(BIG_DECIMAL_100, 1, RoundingMode.DOWN));
    }
}
