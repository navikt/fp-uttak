package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

class UtbetalingsprosentUtenGraderingUtregning implements UtbetalingsprosentUtregning {

    private final Arbeidsprosenter arbeidsprosenter;
    private final AktivitetIdentifikator aktivitet;
    private final LukketPeriode periode;

    UtbetalingsprosentUtenGraderingUtregning(Arbeidsprosenter arbeidsprosenter,
                                             AktivitetIdentifikator aktivitet,
                                             LukketPeriode periode) {
        this.arbeidsprosenter = arbeidsprosenter;
        this.aktivitet = aktivitet;
        this.periode = periode;
    }

    @Override
    public BigDecimal resultat() {
        BigDecimal arbeidsprosent = arbeidsprosenter.getArbeidsprosent(aktivitet, periode);
        BigDecimal stillingsprosent = arbeidsprosenter.getStillingsprosent(aktivitet, periode);
        if (stillingsprosent == null) {
            throw new IllegalArgumentException("Stillingsprosent kan ikke være null");
        }
        if (arbeidsprosent == null) {
            throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
        }
        if (stillingsprosent.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100.00");
        }
        if (arbeidsprosent.compareTo(stillingsprosent) >= 0) {
            return BigDecimal.ZERO;
        }
        // Utbetalingsgrad (i %) = (stillingsprosent – arbeidsprosent) x 100 / stillingsprosent
        return stillingsprosent.subtract(arbeidsprosent).multiply(BigDecimal.valueOf(100)).divide(stillingsprosent, 2, RoundingMode.HALF_UP);
    }
}
