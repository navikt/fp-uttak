package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.oppholdPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_ANNEN_FORELDER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.SYKDOM_ANNEN_FORELDER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class FellesperiodeDelregelTest {

    private final static LocalDate FØDSELSDATO = LocalDate.of(2018, 1, 1);

    @Test
    void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_ved_3_uker_før_fødsel_blir_innvilget() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.minusWeeks(12), FØDSELSDATO.minusWeeks(3), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();

    }

    @Test
    void fellesperiode_mor_uttak_starter_etter_fødsel_og_som_slutter_før_7_uker_blir_avslått() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO, FØDSELSDATO.plusWeeks(3), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    void fellesperiode_mor_uttak_starter_ved_3_uker_før_fødsel_slutter_før_fødsel_blir_innvilget() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.minusWeeks(3), FØDSELSDATO.minusWeeks(1), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    void fellesperiode_mor_uttak_starter_ved_7_uker_etter_fødsel_blir_innvilget() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(7), FØDSELSDATO.plusWeeks(10), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    void fellesperiode_far_før_fødsel_slutter_før_fødsel_blir_avslått_uten_knekk() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.minusWeeks(10), FØDSELSDATO.minusWeeks(5), null, false);
        var kontoer = enFellesperiodeKonto(10 * 5);
        var grunnlag = basicGrunnlagFar().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    private RegelGrunnlag.Builder basicGrunnlagFar() {
        return basicGrunnlag().behandling(new Behandling.Builder().søkerErMor(false));
    }

    @Test
    void fellesperiode_blir_avslått_etter_uke_7_når_mor_ikke_har_omsorg() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(12), FØDSELSDATO.plusWeeks(14), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void fellesperiode_blir_avslått_etter_uke_7_når_mor_ikke_har_omsorg_sammenhengede_uttak() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(12), FØDSELSDATO.plusWeeks(14), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .behandling(new Behandling.Builder().søkerErMor(true).kreverSammenhengendeUttak(true))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void fellesperiode_blir_avslått_etter_uke_7_når_far_ikke_har_omsorg() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(12), FØDSELSDATO.plusWeeks(14), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagFar().rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void fellesperiode_blir_avslått_etter_uke_7_når_far_ikke_har_omsorg_sammenhengede_uttak() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(12), FØDSELSDATO.plusWeeks(14), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagFar().rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .behandling(new Behandling.Builder().søkerErMor(false).kreverSammenhengendeUttak(true))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void fellesperiode_før_fødsel_innvilges_uavhengig_av_om_søker_har_omsorg_da_det_ikke_er_mulig_å_ha_omsorg_fordi_barnet_ikke_er_født() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.minusWeeks(12), FØDSELSDATO.minusWeeks(10), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    void UT1041_mor_før3UkerFørFamilieHendelse_ikkeGradert() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.minusWeeks(5), FØDSELSDATO.minusWeeks(4), null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1064_mor_før3UkerFørFamilieHendelse_gradert() {
        var søknadsperiode = gradertoppgittPeriode(FØDSELSDATO.minusWeeks(5), FØDSELSDATO.minusWeeks(4), null, false, null);
        var kontoer = enFellesperiodeKonto(13 * 5);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1219_mor_tidligstUke7_omsorg_disponibleStønadsdager_gradert_avklart() {
        var søknadsperiode = gradertoppgittPeriode(FØDSELSDATO.plusWeeks(8), FØDSELSDATO.plusWeeks(9), null, false, null);
        var kontoer = enFellesperiodeKonto(100);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1047_mor_fellesperioder_etter_uke7() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(8), FØDSELSDATO.plusWeeks(9), null, false);
        var kontoer = enFellesperiodeKonto(100);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1048_mor_fellesperiode_før_uke7() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO, FØDSELSDATO.plusWeeks(6).minusDays(1), null, false);
        var kontoer = enFellesperiodeKonto(100);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    void UT1255_far_førUke7_etterFamileHendelse_gyldigGrunn_omsorg_disponibleStønadsdager_gradert_avklart() {
        var søknadsperiode = gradertoppgittPeriode(FØDSELSDATO.plusWeeks(4), FØDSELSDATO.plusWeeks(5), null, false, SYKDOM_ANNEN_FORELDER_GODKJENT);
        var kontoer = enFellesperiodeKonto(100);
        var grunnlag = basicGrunnlagFar().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1054_far_førUke7_etterFamileHendelse_utenGyldigGrunn_flerbarnsdager_ikkeOmsorg() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(4), FØDSELSDATO.plusWeeks(5), null, true);
        var kontoer = fellesperiodeOgFlerbarnsdagerKonto(100, 85);
        var grunnlag = basicGrunnlagFar().rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertAvslått(regelresultat, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false);
    }

    @Test
    void UT1256_far_førUke7_etterFamileHendelse_gyldigGrunn_omsorg_disponibleStønadsdager_utenGradering() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(4), FØDSELSDATO.plusWeeks(5), null, false, INNLEGGELSE_ANNEN_FORELDER_GODKJENT);
        var kontoer = enFellesperiodeKonto(100);
        var grunnlag = basicGrunnlagFar().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void opphold_fellesperiode_annenforelder() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var søknadsperiode = oppholdPeriode(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15),
            OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER);
        var kontoer = enFellesperiodeKonto(100);
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
    }

    @Test
    void opphold_fellesperiode_annenforelder_tom_for_konto() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var søknadsperiode = oppholdPeriode(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15),
            OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER);
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 0)).konto(konto(MØDREKVOTE, 100));
        var grunnlag = basicGrunnlagMor().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void far_etterUke7_omsorg_disponibleStønadsdager_gradering_ikkeFlerbarnsdager() {
        var søknadsperiode = gradertoppgittPeriode(FØDSELSDATO.plusWeeks(7), FØDSELSDATO.plusWeeks(9), null, false, MORS_AKTIVITET_GODKJENT);
        var kontoer = enFellesperiodeKonto(100);
        var søknad = søknad(søknadsperiode);
        var grunnlag = basicGrunnlagFar().søknad(søknad)
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1270_far_etterUke7_omsorg_disponibleStønadsdager_gradering_flerbarnsdager() {
        var søknadsperiode = gradertoppgittPeriode(FØDSELSDATO.plusWeeks(7), FØDSELSDATO.plusWeeks(9), null, true, null);
        var kontoer = fellesperiodeOgFlerbarnsdagerKonto(100, 85);
        var grunnlag = basicGrunnlagFar().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void far_etterUke7_omsorg_disponibleStønadsdager_utenGradering_ikkeFlerbarnsdager() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(7), FØDSELSDATO.plusWeeks(9), null, false, MORS_AKTIVITET_GODKJENT);
        var kontoer = enFellesperiodeKonto(100);
        var søknad = søknad(søknadsperiode);
        var grunnlag = basicGrunnlagFar().søknad(søknad)
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FELLESPERIODE_TIL_FAR);
    }

    @Test
    void UT1271_far_etterUke7_omsorg_disponibleStønadsdager_utenGradering_flerbarnsdager() {
        var søknadsperiode = oppgittPeriode(FØDSELSDATO.plusWeeks(7), FØDSELSDATO.plusWeeks(9), null, true);
        var kontoer = fellesperiodeOgFlerbarnsdagerKonto(100, 85);
        var grunnlag = basicGrunnlagFar().søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    void UT1293_fellesperiode_uten_at_mor_har_rett_blir_avslått() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 10 * 5));

        var søknadsperiode = oppgittPeriode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9).minusDays(1), null, false);
        var grunnlag = basicGrunnlagFar().rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(false).farHarRett(true).samtykke(true))
            .søknad(søknad(søknadsperiode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_IKKE_RETT_FP);
    }

    private OppgittPeriode gradertoppgittPeriode(LocalDate fom,
                                                 LocalDate tom,
                                                 SamtidigUttaksprosent samtidigUttaksprosent,
                                                 boolean flerbarnsdager,
                                                 DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forGradering(FELLESPERIODE, fom, tom, BigDecimal.TEN, samtidigUttaksprosent, flerbarnsdager,
            Set.of(AktivitetIdentifikator.forFrilans()), null, null, null, null, dokumentasjonVurdering);
    }

    private Søknad.Builder søknad(OppgittPeriode søknadsperiode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(søknadsperiode);
    }

    private Kontoer.Builder enFellesperiodeKonto(int trekkdager) {
        return new Kontoer.Builder().konto(konto(FELLESPERIODE, trekkdager));
    }

    private Kontoer.Builder fellesperiodeOgFlerbarnsdagerKonto(int fellesperiodeTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder().konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(fellesperiodeTrekkdager))
            .flerbarnsdager(flerbarnsdagerTrekkdager);
    }

    private Konto.Builder konto(Stønadskontotype type, int trekkdager) {
        return new Konto.Builder().type(type).trekkdager(trekkdager);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom, SamtidigUttaksprosent samtidigUttaksprosent, boolean flerbarnsdager) {
        return oppgittPeriode(fom, tom, samtidigUttaksprosent, flerbarnsdager, null);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom,
                                          LocalDate tom,
                                          SamtidigUttaksprosent samtidigUttaksprosent,
                                          boolean flerbarnsdager,
                                          DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(FELLESPERIODE, fom, tom, samtidigUttaksprosent, flerbarnsdager, null, null, null, null,
            dokumentasjonVurdering);
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(innvilgetÅrsak);
    }


    private void assertManuellBehandling(FastsettePerioderRegelresultat regelresultat,
                                         IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak,
                                         boolean trekkdager,
                                         boolean utbetal) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkdager);
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(manuellBehandlingÅrsak);
    }

    private void assertAvslått(FastsettePerioderRegelresultat regelresultat, IkkeOppfyltÅrsak ikkeOppfyltÅrsak, boolean trekkdager, boolean utbetal) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkdager);
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }


    private RegelGrunnlag.Builder basicGrunnlagMor() {
        return basicGrunnlag().behandling(new Behandling.Builder().søkerErMor(true));
    }

    private RegelGrunnlag.Builder basicGrunnlag() {
        return RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(FØDSELSDATO))
            .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).farHarRett(true).morHarRett(true))
            .inngangsvilkår(
                new Inngangsvilkår.Builder().adopsjonOppfylt(true).foreldreansvarnOppfylt(true).fødselOppfylt(true).opptjeningOppfylt(true));
    }
}
