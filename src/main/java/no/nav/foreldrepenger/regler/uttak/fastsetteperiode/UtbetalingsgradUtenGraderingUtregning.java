package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;
    // TODO avklare om bruk av ord stillingsprosent/arbeidsprosent/stillingsdel, etc.
    private final BigDecimal annenpartStillingsprosent;

    public UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent annenpartSamtidigUttaksprosent, BigDecimal annenpartStillingsprosent) {
        Objects.requireNonNull(annenpartSamtidigUttaksprosent);
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
        this.annenpartStillingsprosent = annenpartStillingsprosent;
    }

    // TODO lage en Stillingsprosent klasse med validering av stillingsprosent
    @Override
    public Utbetalingsgrad resultat() {
        if (annenpartStillingsprosent != null) {
            return new Utbetalingsgrad(annenpartStillingsprosent);
        }
        return Utbetalingsgrad.HUNDRED.subtract(annenpartSamtidigUttaksprosent.decimalValue());
    }
}
