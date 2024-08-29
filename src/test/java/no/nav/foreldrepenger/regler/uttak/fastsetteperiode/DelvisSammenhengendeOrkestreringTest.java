package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;

class DelvisSammenhengendeOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_håndtere_behandlinger_med_delvis_sammenhengende_uttak_uttak_overlapper() {

        var fødselsdato = LocalDate.of(2021, 6, 1);
        var sammenhengendeUttakTomDato = fødselsdato.plusWeeks(20).minusDays(1);
        var annenpartUttakPeriode = AnnenpartUttakPeriode.Builder.uttak(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)).innvilget(true).build();
        var utsettelsePåTversAvRegelendring = utsettelsePeriode(annenpartUttakPeriode.getTom().plusWeeks(1), sammenhengendeUttakTomDato.plusWeeks(2),
            UtsettelseÅrsak.SYKDOM_SKADE, null);
        var fedrekvote = oppgittPeriode(Stønadskontotype.FEDREKVOTE, utsettelsePåTversAvRegelendring.getTom().plusWeeks(2),
            utsettelsePåTversAvRegelendring.getTom().plusWeeks(4));
        var grunnlag = basicGrunnlagFar(fødselsdato).behandling(farBehandling().sammenhengendeUttakTomDato(sammenhengendeUttakTomDato))
            .kontoer(defaultKontoer().konto(Stønadskontotype.FEDREKVOTE, 1000))
            .annenPart(new AnnenPart.Builder().uttaksperiode(annenpartUttakPeriode))
            .søknad(søknad(Søknadstype.FØDSEL, utsettelsePåTversAvRegelendring, fedrekvote));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING); //msp i sammenhengende
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(
            Perioderesultattype.MANUELL_BEHANDLING); //utsettelse i sammenhengende
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(
            Perioderesultattype.INNVILGET); //utsettelse etter sammenhengende
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(
            Perioderesultattype.INNVILGET); //fedrekvote etter sammenhengende
    }

    @Test
    void skal_håndtere_behandlinger_med_delvis_sammenhengende_uttak_msp_overlapper() {

        var fødselsdato = LocalDate.of(2024, 8, 28);
        var sammenhengendeUttakTomDato = fødselsdato.plusWeeks(8).minusDays(1);
        var mødrekvote1 = oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var mødrekvote2 = oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(10).minusDays(1));
        var grunnlag = basicGrunnlagMor(fødselsdato).behandling(morBehandling().sammenhengendeUttakTomDato(sammenhengendeUttakTomDato))
            .søknad(søknad(Søknadstype.FØDSEL, mødrekvote1, mødrekvote2));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET); //mødrekvote første 6
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(
            Perioderesultattype.MANUELL_BEHANDLING); //msp fram til sammenhengende
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET); //mødrekvote i sammenhengende
    }
}
