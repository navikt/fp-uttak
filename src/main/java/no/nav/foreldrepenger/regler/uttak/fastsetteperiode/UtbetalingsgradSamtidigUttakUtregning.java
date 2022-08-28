package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradSamtidigUttakUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent samtidigUttaksprosent;
    private final BigDecimal graderingArbeidstidsprosent;
    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;

    UtbetalingsgradSamtidigUttakUtregning(SamtidigUttaksprosent samtidigUttaksprosent,
                                          BigDecimal graderingArbeidstidsprosent) {
        Objects.requireNonNull(samtidigUttaksprosent);
        this.graderingArbeidstidsprosent = graderingArbeidstidsprosent;
        this.samtidigUttaksprosent = samtidigUttaksprosent;
        this.annenpartSamtidigUttaksprosent = SamtidigUttaksprosent.ZERO;
    }

    UtbetalingsgradSamtidigUttakUtregning(SamtidigUttaksprosent samtidigUttaksprosent,
                                          BigDecimal graderingArbeidstidsprosent,
                                          SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        Objects.requireNonNull(samtidigUttaksprosent);
        Objects.requireNonNull(annenpartSamtidigUttaksprosent);
        this.graderingArbeidstidsprosent = graderingArbeidstidsprosent;
        this.samtidigUttaksprosent = samtidigUttaksprosent;
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        // Samtidiguttaksprosent med mindre gradering på noen aktiviteter i perioden
        var lokalSamtidigUttaksprosent = Optional.ofNullable(graderingArbeidstidsprosent)
            .map(SamtidigUttaksprosent.HUNDRED::subtract)
            .orElse(samtidigUttaksprosent);
        var maksSamtidigUttakUtFraAnnenpart = SamtidigUttaksprosent.HUNDRED.subtract(annenpartSamtidigUttaksprosent);
        // Reduser utbetaling dersom annenpart > 0 og det ligger an til mer enn 100 prosent
        if (lokalSamtidigUttaksprosent.subtract(maksSamtidigUttakUtFraAnnenpart).merEnn0()) {
            return new Utbetalingsgrad(maksSamtidigUttakUtFraAnnenpart.decimalValue());
        }
        return new Utbetalingsgrad(lokalSamtidigUttaksprosent.decimalValue());
    }
}
