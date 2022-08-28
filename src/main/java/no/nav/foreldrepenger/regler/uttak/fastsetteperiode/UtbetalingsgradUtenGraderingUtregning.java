package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregning implements UtbetalingsgradUtregning {

    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;

    public UtbetalingsgradUtenGraderingUtregning() {
        annenpartSamtidigUttaksprosent = SamtidigUttaksprosent.ZERO;
    }

    public UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        return Utbetalingsgrad.HUNDRED.subtract(annenpartSamtidigUttaksprosent.decimalValue());
    }
}
