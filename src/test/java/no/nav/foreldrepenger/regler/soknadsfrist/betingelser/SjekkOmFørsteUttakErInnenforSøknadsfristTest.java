package no.nav.foreldrepenger.regler.soknadsfrist.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Test;

import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmFørsteUttakErInnenforSøknadsfristTest {

    @Test
    public void startAvUttakErInnenforSøknadsfrist() {
        SøknadsfristGrunnlag grunnlag = SøknadsfristGrunnlag.builder()
            .medFørsteUttaksdato(LocalDate.of(2017, Month.FEBRUARY, 1))
            .medSøknadMottattDato(LocalDate.of(2017, Month.MAY, 31))
            .build();

        SjekkOmFørsteUttakErInnenforSøknadsfrist betingelse = new SjekkOmFørsteUttakErInnenforSøknadsfrist();
        Evaluation evaluation = betingelse.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void startAvUttakErIkkeInnenforSøknadsfrist() {
        SøknadsfristGrunnlag grunnlag = SøknadsfristGrunnlag.builder()
            .medFørsteUttaksdato(LocalDate.of(2017, Month.JANUARY, 31))
            .medSøknadMottattDato(LocalDate.of(2017, Month.MAY, 31))
            .build();

        SjekkOmFørsteUttakErInnenforSøknadsfrist betingelse = new SjekkOmFørsteUttakErInnenforSøknadsfrist();
        Evaluation evaluation = betingelse.evaluate(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

}
