package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;

class UtbetalingsprosentSamtidigUttakUtregning implements UtbetalingsprosentUtregning {

    private final BigDecimal samtidigUttaksprosent;
    private final BigDecimal graderingArbeidstidsprosent;

    UtbetalingsprosentSamtidigUttakUtregning(BigDecimal samtidigUttaksprosent, BigDecimal graderingArbeidstidsprosent) {
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
