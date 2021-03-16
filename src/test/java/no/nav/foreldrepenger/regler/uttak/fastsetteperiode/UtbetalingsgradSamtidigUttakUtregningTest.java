package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradSamtidigUttakUtregningTest {

    @Test
    void hvis_ugradert_periode_skal_utbetalingsgrad_være_lik_samtidig_uttaksprosent() {
        var samtidigUttaksprosent = SamtidigUttaksprosent.TEN;
        var resultat = new UtbetalingsgradSamtidigUttakUtregning(samtidigUttaksprosent, null).resultat();

        assertThat(resultat.decimalValue()).isEqualTo(samtidigUttaksprosent.decimalValue());
    }

    @Test
    void hvis_gradert_periode_skal_utbetalingsgrad_være_gradering_arbeidstidsprosent() {
        var samtidigUttaksprosent = SamtidigUttaksprosent.TEN;
        var graderingArbeidstidsprosent = BigDecimal.ONE;
        var resultat = new UtbetalingsgradSamtidigUttakUtregning(samtidigUttaksprosent, graderingArbeidstidsprosent).resultat();

        assertThat(resultat).isEqualByComparingTo(Utbetalingsgrad.HUNDRED.subtract(graderingArbeidstidsprosent));
    }
}
