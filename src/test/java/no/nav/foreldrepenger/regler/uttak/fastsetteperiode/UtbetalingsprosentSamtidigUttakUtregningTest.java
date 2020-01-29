package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class UtbetalingsprosentSamtidigUttakUtregningTest {

    @Test
    public void hvis_ugradert_periode_skal_utbetalingsgrad_være_lik_samtidig_uttaksprosent() {
        BigDecimal samtidigUttaksprosent = BigDecimal.TEN;
        BigDecimal resultat = new UtbetalingsprosentSamtidigUttakUtregning(samtidigUttaksprosent, null).resultat();

        assertThat(resultat).isEqualTo(samtidigUttaksprosent);
    }

    @Test
    public void hvis_gradert_periode_skal_utbetalingsgrad_være_gradering_arbeidstidsprosent() {
        BigDecimal samtidigUttaksprosent = BigDecimal.TEN;
        BigDecimal graderingArbeidstidsprosent = BigDecimal.ONE;
        BigDecimal resultat = new UtbetalingsprosentSamtidigUttakUtregning(samtidigUttaksprosent, graderingArbeidstidsprosent).resultat();

        assertThat(resultat).isEqualTo(new BigDecimal("100.00").subtract(graderingArbeidstidsprosent));
    }
}
