package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;

class UtbetalingsprosentSamtidigUttakUtregning implements UtbetalingsprosentUtregning {

    private final SamtidigUttak samtidigUttak;
    private final BigDecimal graderingArbeidstidsprosent;

    UtbetalingsprosentSamtidigUttakUtregning(SamtidigUttak samtidigUttak, BigDecimal graderingArbeidstidsprosent) {
        Objects.requireNonNull(samtidigUttak);
        this.graderingArbeidstidsprosent = graderingArbeidstidsprosent;
        this.samtidigUttak = samtidigUttak;
    }

    @Override
    public BigDecimal resultat() {
        //Ikke gradering på noen aktiviteter i perioden
        if (graderingArbeidstidsprosent == null) {
            return samtidigUttak.getProsent();
        }
        return new BigDecimal("100.00").subtract(graderingArbeidstidsprosent);
    }
}
