package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.UtbetalingsprosentUtenGraderingUtregning;

public class UtbetalingsprosentUtenGraderingUtregningTest {

    @Test
    public void utbetaling_skal_være_100_prosent() {
        var utregning = utregning();
        assertThat(utregning.resultat()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    private UtbetalingsprosentUtenGraderingUtregning utregning() {
        return new UtbetalingsprosentUtenGraderingUtregning();
    }
}
