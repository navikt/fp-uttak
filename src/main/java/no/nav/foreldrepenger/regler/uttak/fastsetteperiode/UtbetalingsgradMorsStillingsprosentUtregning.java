package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradMorsStillingsprosentUtregning implements UtbetalingsgradUtregning {

    private final MorsStillingsprosent morsStillingsprosent;
    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;

    public UtbetalingsgradMorsStillingsprosentUtregning(MorsStillingsprosent morsStillingsprosent,
                                                        SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        Objects.requireNonNull(annenpartSamtidigUttaksprosent);
        this.morsStillingsprosent = morsStillingsprosent;
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        var samtidigUttakPgaMor = BigDecimal.valueOf(100).subtract(annenpartSamtidigUttaksprosent.decimalValue());
        return new Utbetalingsgrad(samtidigUttakPgaMor.min(morsStillingsprosent.decimalValue()));
    }
}
