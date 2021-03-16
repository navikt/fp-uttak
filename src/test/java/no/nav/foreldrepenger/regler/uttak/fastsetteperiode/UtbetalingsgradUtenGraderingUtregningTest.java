package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtenGraderingUtregningTest {

    @Test
    void utbetaling_skal_være_100_prosent() {
        var utregning = utregning();
        assertThat(utregning.resultat()).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    private UtbetalingsgradUtenGraderingUtregning utregning() {
        return new UtbetalingsgradUtenGraderingUtregning();
    }
}
