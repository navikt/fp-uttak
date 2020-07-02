package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

public class UtbetalingsgradUtenGraderingUtregningTest {

    @Test
    public void utbetaling_skal_være_100_prosent() {
        var utregning = utregning();
        assertThat(utregning.resultat()).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    private UtbetalingsgradUtenGraderingUtregning utregning() {
        return new UtbetalingsgradUtenGraderingUtregning();
    }
}
