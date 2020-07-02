package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradSamtidigUttakUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent samtidigUttaksprosent;
    private final BigDecimal graderingArbeidstidsprosent;

    UtbetalingsgradSamtidigUttakUtregning(SamtidigUttaksprosent samtidigUttaksprosent, BigDecimal graderingArbeidstidsprosent) {
        Objects.requireNonNull(samtidigUttaksprosent);
        this.graderingArbeidstidsprosent = graderingArbeidstidsprosent;
        this.samtidigUttaksprosent = samtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        //Ikke gradering på noen aktiviteter i perioden
        if (graderingArbeidstidsprosent == null) {
            return new Utbetalingsgrad(samtidigUttaksprosent.decimalValue());
        }
        return Utbetalingsgrad.HUNDRED.subtract(graderingArbeidstidsprosent);
    }
}
