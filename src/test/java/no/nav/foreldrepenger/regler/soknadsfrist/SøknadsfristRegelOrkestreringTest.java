package no.nav.foreldrepenger.regler.soknadsfrist;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePerioderRegelOrkestrering;

class SøknadsfristRegelOrkestreringTest {

    @Test
    void søknadOmUttakInnenforSøknadsfristSkalGiOppfyltResultat() {
        var nå = LocalDate.now();
        var grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.with(lastDayOfMonth()))
                .medFørsteUttaksdato(nå.minusMonths(3).withDayOfMonth(1))
                .build();

        var regelOrkestrering = new SøknadsfristRegelOrkestrering();
        var resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);
        assertThat(resultat.isRegelOppfylt()).isTrue();
        assertThat(resultat.getTidligsteLovligeUttak()).isEqualTo(nå.withDayOfMonth(1).minusMonths(3));
    }

    @Test
    void resultat_skal_inneholde_sporing_i_json() throws IOException {
        var nå = LocalDate.now();
        var grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.with(lastDayOfMonth()))
                .medFørsteUttaksdato(nå.minusMonths(3).withDayOfMonth(1))
                .build();

        var regelOrkestrering = new SøknadsfristRegelOrkestrering();
        var resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);

        assertThat(new ObjectMapper().readValue(resultat.getInnsendtGrunnlag(), HashMap.class)).isNotNull().isNotEmpty();
        assertThat(new ObjectMapper().readValue(resultat.getEvalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();
    }

    @Test
    void søknadOmUttakUtenforSøknadsfristSkalGiIkkeOppfyltOgAksjonskode() {
        var nå = LocalDate.now();
        var grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.plusMonths(1).with(firstDayOfMonth()))
                .medFørsteUttaksdato(nå.minusMonths(3).withDayOfMonth(1))
                .build();

        var regelOrkestrering = new SøknadsfristRegelOrkestrering();
        var resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);
        assertThat(resultat.isRegelOppfylt()).isFalse();
        assertThat(resultat.getÅrsakKodeIkkeVurdert()).isPresent();
        assertThat(resultat.getÅrsakKodeIkkeVurdert().get()).isEqualTo("5043");
    }

    /**
     * Bruker har ikke noe første uttaksdag ettersom det bare er søkt om utsettelse
     * Avslag på utsettelse tilbake i tid håndteres av {@link FastsettePerioderRegelOrkestrering}
     */
    @Test
    void søknadOmBareUtsettelseSkalGiOppfyltResultat() {
        var nå = LocalDate.now();
        var grunnlag = SøknadsfristGrunnlag.builder()
                .medSøknadMottattDato(nå.with(lastDayOfMonth()))
                .medFørsteUttaksdato(null)
                .build();

        var regelOrkestrering = new SøknadsfristRegelOrkestrering();
        var resultat = regelOrkestrering.vurderSøknadsfrist(grunnlag);
        assertThat(resultat.isRegelOppfylt()).isTrue();
        assertThat(resultat.getTidligsteLovligeUttak()).isEqualTo(nå.withDayOfMonth(1).minusMonths(3));
    }
}
