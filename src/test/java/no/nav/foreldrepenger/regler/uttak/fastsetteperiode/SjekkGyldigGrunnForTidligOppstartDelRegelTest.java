package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class SjekkGyldigGrunnForTidligOppstartDelRegelTest {
    private final FastsettePerioderRegelOrkestrering regelOrkestrering = new FastsettePerioderRegelOrkestrering();

    //TODO PFP-8743 endre til å sjekke på GyldigGrunnPerioder og ikke om periodeverudering er OK

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_gyldig_grunn_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = DelRegelTestUtil.oppgittPeriode(FEDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6),
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medSøknad(
                new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(oppgittPeriode)).medKontoer(kontoer).build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(2);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());
        assertThat(perioder.stream().map(UttakPeriode::getPerioderesultattype).collect(toList())).containsExactly(INNVILGET,
                INNVILGET);
        assertThat(perioder.stream().map(UttakPeriode::getStønadskontotype).collect(toList())).containsExactly(FEDREKVOTE, FEDREKVOTE);
    }

    @Test
    public void fellesperiode_med_tidlig_oppstart_og_gyldig_grunn_hele_perioden_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = DelRegelTestUtil.oppgittPeriode(FELLESPERIODE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1),
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = enKonto(FELLESPERIODE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medSøknad(
                new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode)).medKontoer(kontoer).build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(1);
        assertThat(periodeResultater.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(periodeResultater.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_uten_gyldig_grunn_deler_av_perioden_skal_behandles_manuelt() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10), PeriodeVurderingType.UAVKLART_PERIODE);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medSøknad(
                new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode)).medKontoer(kontoer).build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(2);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        var ugyldigPeriode = perioder.get(0);
        assertThat(ugyldigPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(ugyldigPeriode.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(ugyldigPeriode.getStønadskontotype()).isEqualTo(FEDREKVOTE);

        var gyldigPeriode = perioder.get(1);
        assertThat(gyldigPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(gyldigPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(gyldigPeriode.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(gyldigPeriode.getStønadskontotype()).isEqualTo(FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget_med_knekk() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(10), PeriodeVurderingType.PERIODE_OK);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode))
                .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(2);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(1), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), INNVILGET, FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1),
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medSøknad(
                new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode)).medKontoer(kontoer).build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(1);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), INNVILGET, FEDREKVOTE);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurdering) {
        return OppgittPeriode.forVanligPeriode(FEDREKVOTE, fom, tom, null, false, vurdering, null, null);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_uavklart_av_saksbehandler_går_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1),
                PeriodeVurderingType.UAVKLART_PERIODE);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medSøknad(
                new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode)).medKontoer(kontoer).build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(1);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), MANUELL_BEHANDLING,
                FEDREKVOTE);
    }

    @Test
    public void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget_med_knekk_som_saksbehandler_har_registrert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode1 = oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1),
                PeriodeVurderingType.ENDRE_PERIODE);
        var uttakPeriode2 = oppgittPeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4).minusDays(1),
                PeriodeVurderingType.PERIODE_OK);
        var uttakPeriode3 = oppgittPeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1),
                PeriodeVurderingType.UAVKLART_PERIODE);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(uttakPeriode1)
                .leggTilOppgittPeriode(uttakPeriode2)
                .leggTilOppgittPeriode(uttakPeriode3)).medKontoer(kontoer).build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag,
                new FeatureTogglesForTester());
        assertThat(periodeResultater).hasSize(3);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(1), fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(2), fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING,
                FEDREKVOTE);
    }


    private void verifiserPeriode(UttakPeriode periode,
                                  LocalDate forventetFom,
                                  LocalDate forventetTom,
                                  Perioderesultattype forventetResultat,
                                  Stønadskontotype stønadskontotype) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(forventetResultat);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true).medMorHarRett(true).medFarHarRett(true))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private Kontoer.Builder enKonto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(stønadskontotype).medTrekkdager(trekkdager));
    }
}
