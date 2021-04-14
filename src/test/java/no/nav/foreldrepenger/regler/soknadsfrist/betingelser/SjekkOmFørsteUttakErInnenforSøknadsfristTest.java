package no.nav.foreldrepenger.regler.soknadsfrist.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmFørsteUttakErInnenforSøknadsfristTest {

    @Test
    void startAvUttakErInnenforSøknadsfrist() {
        var grunnlag = SøknadsfristGrunnlag.builder()
                .medFørsteUttaksdato(LocalDate.of(2017, Month.FEBRUARY, 1))
                .medSøknadMottattDato(LocalDate.of(2017, Month.MAY, 31))
                .build();

        var betingelse = new SjekkOmFørsteUttakErInnenforSøknadsfrist();
        var evaluation = betingelse.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void startAvUttakErIkkeInnenforSøknadsfrist() {
        var grunnlag = SøknadsfristGrunnlag.builder()
                .medFørsteUttaksdato(LocalDate.of(2017, Month.JANUARY, 31))
                .medSøknadMottattDato(LocalDate.of(2017, Month.MAY, 31))
                .build();

        var betingelse = new SjekkOmFørsteUttakErInnenforSøknadsfrist();
        var evaluation = betingelse.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

}
