package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregningTest {

    @Test
    void utbetaling_skal_være_100_prosent() {
        var utregning = new UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent.ZERO, null);
        assertThat(utregning.resultat()).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void utbetaling_skal_være_justert_til_annenparts_stillingsprosent() {
        var utregning = new UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent.ZERO, AnnenpartStillingsprosent.TEN);
        assertThat(utregning.resultat()).isEqualTo(Utbetalingsgrad.TEN);
    }
}
