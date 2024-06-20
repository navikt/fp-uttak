package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import org.junit.jupiter.api.Test;

class UtbetalingsgradUtenGraderingUtregningTest {

    @Test
    void utbetaling_skal_være_100_prosent() {
        var utregning = utregning();
        assertThat(utregning.resultat()).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    private UtbetalingsgradUtenGraderingUtregning utregning() {
        return new UtbetalingsgradUtenGraderingUtregning(SamtidigUttaksprosent.ZERO);
    }
}
