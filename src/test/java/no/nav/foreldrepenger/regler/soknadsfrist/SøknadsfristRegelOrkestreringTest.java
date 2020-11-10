package no.nav.foreldrepenger.regler.soknadsfrist;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;

public class SøknadsfristRegelOrkestreringTest {

    @Test
    public void søknadOmUttakInnenforSøknadsfristSkalGiOppfyltResultat() {
        LocalDate nå = LocalDate.now();
        SøknadsfristGrunnlag grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.with(lastDayOfMonth()))
                .medFørsteUttaksdato(nå.minusMonths(3).withDayOfMonth(1))
                .build();

        SøknadsfristRegelOrkestrering regelOrkestrering = new SøknadsfristRegelOrkestrering();
        SøknadsfristResultat resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);
        assertThat(resultat.isRegelOppfylt()).isTrue();
        assertThat(resultat.getTidligsteLovligeUttak()).isEqualTo(nå.withDayOfMonth(1).minusMonths(3));
    }

    @Test
    public void resultat_skal_inneholde_sporing_i_json() throws IOException {
        LocalDate nå = LocalDate.now();
        SøknadsfristGrunnlag grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.with(lastDayOfMonth()))
                .medFørsteUttaksdato(nå.minusMonths(3).withDayOfMonth(1))
                .build();

        SøknadsfristRegelOrkestrering regelOrkestrering = new SøknadsfristRegelOrkestrering();
        SøknadsfristResultat resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);

        assertThat(new ObjectMapper().readValue(resultat.getInnsendtGrunnlag(), HashMap.class)).isNotNull().isNotEmpty();
        assertThat(new ObjectMapper().readValue(resultat.getEvalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();
    }

    @Test
    public void søknadOmUttakUtenforSøknadsfristSkalGiIkkeOppfyltOgAksjonskode() {
        LocalDate nå = LocalDate.now();
        SøknadsfristGrunnlag grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.plusMonths(1).with(firstDayOfMonth()))
                .medFørsteUttaksdato(nå.minusMonths(3).withDayOfMonth(1))
                .build();

        SøknadsfristRegelOrkestrering regelOrkestrering = new SøknadsfristRegelOrkestrering();
        SøknadsfristResultat resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);
        assertThat(resultat.isRegelOppfylt()).isFalse();
        assertThat(resultat.getÅrsakKodeIkkeVurdert()).isPresent();
        assertThat(resultat.getÅrsakKodeIkkeVurdert().get()).isEqualTo("5043");
    }
}
