package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_ANNEN_FORELDER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Rettighetstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

class SjekkGyldigGrunnForTidligOppstartDelRegelTest {
    private final FastsettePerioderRegelOrkestrering regelOrkestrering = new FastsettePerioderRegelOrkestrering();

    @Test
    void fedrekvote_med_tidlig_oppstart_og_gyldig_grunn_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = DelRegelTestUtil.oppgittPeriode(FEDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6), TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(2);
        var perioder = periodeResultater.stream().map(FastsettePeriodeResultat::uttakPeriode).sorted(comparing(UttakPeriode::getFom)).toList();
        assertThat(perioder.stream().map(UttakPeriode::getPerioderesultattype).collect(toList())).containsExactly(INNVILGET, INNVILGET);
        assertThat(perioder.stream().map(UttakPeriode::getStønadskontotype).collect(toList())).containsExactly(FEDREKVOTE, FEDREKVOTE);
    }

    @Test
    void fellesperiode_med_tidlig_oppstart_mor_er_i_aktivitet_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = DelRegelTestUtil.oppgittPeriode(FELLESPERIODE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1),
            MORS_AKTIVITET_GODKJENT);
        var kontoer = enKonto(FELLESPERIODE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        assertThat(periodeResultater.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(periodeResultater.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FELLESPERIODE);
    }

    @Test
    void fellesperiode_med_tidlig_oppstart_og_gyldig_grunn_hele_perioden_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = DelRegelTestUtil.oppgittPeriode(FELLESPERIODE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1),
            INNLEGGELSE_ANNEN_FORELDER_GODKJENT);
        var kontoer = enKonto(FELLESPERIODE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        assertThat(periodeResultater.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(periodeResultater.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
    }

    @Test
    void fedrekvote_med_tidlig_oppstart_uten_gyldig_grunn_deler_av_perioden_skal_behandles_manuelt() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10), null);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(2);
        var perioder = periodeResultater.stream().map(FastsettePeriodeResultat::uttakPeriode).sorted(comparing(UttakPeriode::getFom)).toList();

        var ugyldigPeriode = perioder.get(0);
        assertThat(ugyldigPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(ugyldigPeriode.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(ugyldigPeriode.getStønadskontotype()).isEqualTo(FEDREKVOTE);

        var gyldigPeriode = perioder.get(1);
        assertThat(gyldigPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(gyldigPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(gyldigPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(gyldigPeriode.getStønadskontotype()).isEqualTo(FEDREKVOTE);
    }

    @Test
    void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget_med_knekk() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(10), TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).kontoer(kontoer)
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode))
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(2);
        var perioder = periodeResultater.stream().map(FastsettePeriodeResultat::uttakPeriode).sorted(comparing(UttakPeriode::getFom)).toList();

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(1), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), INNVILGET, FEDREKVOTE);
    }

    @Test
    void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        var perioder = periodeResultater.stream().map(FastsettePeriodeResultat::uttakPeriode).sorted(comparing(UttakPeriode::getFom)).toList();

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), INNVILGET, FEDREKVOTE);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(FEDREKVOTE, fom, tom, null, false, null, null, null, null, dokumentasjonVurdering);
    }

    @Test
    void fedrekvote_med_tidlig_oppstart_og_vurdert_uavklart_av_saksbehandler_går_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), null);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(1);
        var perioder = periodeResultater.stream().map(FastsettePeriodeResultat::uttakPeriode).sorted(comparing(UttakPeriode::getFom)).toList();

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }

    @Test
    void fedrekvote_med_tidlig_oppstart_og_vurdert_OK_av_saksbehandler_blir_innvilget_med_knekk_som_saksbehandler_har_registrert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var uttakPeriode1 = oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT);
        var uttakPeriode2 = oppgittPeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4).minusDays(1), TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT);
        var uttakPeriode3 = oppgittPeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), null);
        var kontoer = enKonto(FEDREKVOTE, 10 * 5);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(
                new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode1).oppgittPeriode(uttakPeriode2).oppgittPeriode(uttakPeriode3))
            .kontoer(kontoer)
            .build();

        var periodeResultater = regelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(periodeResultater).hasSize(3);
        var perioder = periodeResultater.stream().map(FastsettePeriodeResultat::uttakPeriode).sorted(comparing(UttakPeriode::getFom)).toList();

        verifiserPeriode(perioder.get(0), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(1), fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(perioder.get(2), fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
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
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).rettighetstype(Rettighetstype.BEGGE_RETT))
            .behandling(new Behandling.Builder().søkerErMor(false))
            .inngangsvilkår(
                new Inngangsvilkår.Builder().adopsjonOppfylt(true).foreldreansvarnOppfylt(true).fødselOppfylt(true).opptjeningOppfylt(true).medlemskapOppfylt(true));
    }

    private Kontoer.Builder enKonto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Kontoer.Builder().konto(new Konto.Builder().type(stønadskontotype).trekkdager(trekkdager));
    }
}
