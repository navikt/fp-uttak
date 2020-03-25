package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;

class UtbetalingsgradSamtidigUttakUtregning implements UtbetalingsgradUtregning {

    private final BigDecimal samtidigUttaksprosent;
    private final BigDecimal graderingArbeidstidsprosent;

    UtbetalingsgradSamtidigUttakUtregning(BigDecimal samtidigUttaksprosent, BigDecimal graderingArbeidstidsprosent) {
        Objects.requireNonNull(samtidigUttaksprosent);
        this.graderingArbeidstidsprosent = graderingArbeidstidsprosent;
        this.samtidigUttaksprosent = samtidigUttaksprosent;
    }

    @Override
    public BigDecimal resultat() {
        //Ikke gradering på noen aktiviteter i perioden
        if (graderingArbeidstidsprosent == null) {
            return samtidigUttaksprosent;
        }
        return new BigDecimal("100.00").subtract(graderingArbeidstidsprosent);
    }
}
