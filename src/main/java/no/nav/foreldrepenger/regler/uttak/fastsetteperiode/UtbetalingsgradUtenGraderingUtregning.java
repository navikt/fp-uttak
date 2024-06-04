package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;
    private final AnnenpartStillingsprosent annenpartStillingsprosent;

    public UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent annenpartSamtidigUttaksprosent,
                                                 AnnenpartStillingsprosent annenpartStillingsprosent) {
        Objects.requireNonNull(annenpartSamtidigUttaksprosent);
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
        this.annenpartStillingsprosent = annenpartStillingsprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        if (annenpartStillingsprosent != null) {
            return new Utbetalingsgrad(annenpartStillingsprosent.decimalValue());
        }
        return Utbetalingsgrad.HUNDRED.subtract(annenpartSamtidigUttaksprosent.decimalValue());
    }
}
