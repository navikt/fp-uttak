package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregning implements UtbetalingsgradUtregning {

    @Override
    public Utbetalingsgrad resultat() {
        return Utbetalingsgrad.FULL;
    }
}
