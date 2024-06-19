package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Objects;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;

    public UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        Objects.requireNonNull(annenpartSamtidigUttaksprosent);
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        return Utbetalingsgrad.HUNDRED.subtract(annenpartSamtidigUttaksprosent.decimalValue());
    }
}
