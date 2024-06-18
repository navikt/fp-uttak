package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;
    private final MorsStillingsprosent morsStillingsprosent;

    public UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent annenpartSamtidigUttaksprosent, MorsStillingsprosent morsStillingsprosent) {
        Objects.requireNonNull(annenpartSamtidigUttaksprosent);
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
        this.morsStillingsprosent = morsStillingsprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        if (morsStillingsprosent != null) {
            // TODO: Annenparts samtidig uttaksprosent sammen med mor i arbeid
            return new Utbetalingsgrad(morsStillingsprosent.decimalValue());
        }
        return Utbetalingsgrad.HUNDRED.subtract(annenpartSamtidigUttaksprosent.decimalValue());
    }
}
