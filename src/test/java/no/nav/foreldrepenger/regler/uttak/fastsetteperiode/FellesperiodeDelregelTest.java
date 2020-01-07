package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FellesperiodeDelregelTest {

    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = fødselsdato.minusMonths(3);

    @Test
    public void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_ved_3_uker_før_fødsel_blir_innvilget() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();

    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_før_fødsel_slutter_før_7_uker_blir_avslått() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(3), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_før_fødsel_slutter_før_fødsel_blir_avslått() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(1), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_7_uker_etter_fødsel_blir_innvilget() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(10), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void fellesperiode_far_før_fødsel_slutter_før_fødsel_blir_avslått_uten_knekk() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(10), fødselsdato.minusWeeks(5), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    private RegelGrunnlag.Builder basicGrunnlagFar() {
        return basicGrunnlag().medBehandling(new Behandling.Builder().medSøkerErMor(false));
    }

    @Test
    public void fellesperiode_blir_avslått_etter_uke_7_når_mor_ikke_har_omsorg() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode, new PeriodeUtenOmsorg(fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void fellesperiode_før_fødsel_innvilges_uavhengig_av_om_søker_har_omsorg_da_det_ikke_er_mulig_å_ha_omsorg_fordi_barnet_ikke_er_født() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(10), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode, new PeriodeUtenOmsorg(fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(10))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void UT1041_mor_før3UkerFørFamilieHendelse_ikkeGradert() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(4), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1064_mor_før3UkerFørFamilieHendelse_gradert() {
        UttakPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(4), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(13 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilgetAvslåttGradering(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1219_mor_tidligstUke7_omsorg_disponibleStønadsdager_gradert_avklart() {
        UttakPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1047_mor_fellesperioder_etter_uke7() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1048_mor_fellesperiode_før_uke7() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    public void UT1220_far_førUke7_etterFamileHendelse_gyldigGrunn_omsorg_disponibleStønadsdager_gradert_avklart() {
        UttakPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1054_far_førUke7_etterFamileHendelse_utenGyldigGrunn_flerbarnsdager_ikkeOmsorg() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5), PeriodeVurderingType.PERIODE_OK, null, true);
        var kontoer = fellesperiodeOgFlerbarnsdagerKonto(100, 85);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
            .medSøknad(søknad(søknadsperiode, new PeriodeUtenOmsorg(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4))))
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
            .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertAvslått(regelresultat, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false);
    }

    @Test
    public void UT1055_far_førUke7_etterFamileHendelse_gyldigGrunn_omsorg_disponibleStønadsdager_utenGradering() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void opphold_fellesperiode_annenforelder() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        OppholdPeriode søknadsperiode = new OppholdPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD,
                Oppholdårsaktype.FELLESPERIODE_ANNEN_FORELDER, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15), null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isNull();
    }

    @Test
    public void opphold_fellesperiode_annenforelder_tom_for_konto() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        OppholdPeriode søknadsperiode = new OppholdPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD,
                Oppholdårsaktype.FELLESPERIODE_ANNEN_FORELDER, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15), null, false);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FELLESPERIODE, 0))
                .leggTilKonto(konto(MØDREKVOTE, 100));
        RegelGrunnlag grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void UT1233_far_etterUke7_omsorg_disponibleStønadsdager_gradering_ikkeFlerbarnsdager() {
        UttakPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
            .medSøknad(søknad(søknadsperiode))
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
            .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1270_far_etterUke7_omsorg_disponibleStønadsdager_gradering_flerbarnsdager() {
        UttakPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK, null, true);
        var kontoer = fellesperiodeOgFlerbarnsdagerKonto(100, 85);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
            .medSøknad(søknad(søknadsperiode))
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
            .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1061_far_etterUke7_omsorg_disponibleStønadsdager_utenGradering_ikkeFlerbarnsdager() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK, null, false);
        var kontoer = enFellesperiodeKonto(100);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
            .medSøknad(søknad(søknadsperiode))
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
            .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1271_far_etterUke7_omsorg_disponibleStønadsdager_utenGradering_flerbarnsdager() {
        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK, null, true);
        var kontoer = fellesperiodeOgFlerbarnsdagerKonto(100, 85);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
            .medSøknad(søknad(søknadsperiode))
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
            .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
    }

    @Test
    public void UT1293_fellesperiode_uten_at_mor_har_rett_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FELLESPERIODE, 10 * 5));

        UttakPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9).minusDays(1), PeriodeVurderingType.PERIODE_OK, null, false);
        RegelGrunnlag grunnlag = basicGrunnlagFar()
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(false)
                        .medFarHarRett(true)
                        .medSamtykke(true))
                .medSøknad(søknad(søknadsperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_IKKE_RETT_FP);
    }

    private UttakPeriode gradertSøknadsperiode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType,  SamtidigUttak samtidigUttak, boolean flerbarnsdager) {
        return StønadsPeriode.medGradering(FELLESPERIODE, PeriodeKilde.SØKNAD, fom, tom,
                Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, vurderingType, samtidigUttak, flerbarnsdager);
    }

    private Søknad.Builder søknad(UttakPeriode søknadsperiode, PeriodeUtenOmsorg periodeUtenOmsorg) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .leggTilSøknadsperiode(søknadsperiode)
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggPerioderUtenOmsorg(periodeUtenOmsorg));
    }

    private Søknad.Builder søknad(UttakPeriode søknadsperiode) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .medMottattDato(søknadsperiode.getFom().minusWeeks(1))
                .leggTilSøknadsperiode(søknadsperiode);
    }

    private Kontoer.Builder enFellesperiodeKonto(int trekkdager) {
        return new Kontoer.Builder().leggTilKonto(konto(FELLESPERIODE, trekkdager));
    }

    private Kontoer.Builder fellesperiodeOgFlerbarnsdagerKonto(int fellesperiodeTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder()
            .leggTilKonto(new Konto.Builder()
                .medType(FELLESPERIODE)
                .medTrekkdager(fellesperiodeTrekkdager))
            .leggTilKonto(new Konto.Builder()
                .medType(FLERBARNSDAGER)
                .medTrekkdager(flerbarnsdagerTrekkdager));
    }

    private Konto.Builder konto(Stønadskontotype type, int trekkdager) {
        return new Konto.Builder()
                .medType(type)
                .medTrekkdager(trekkdager);
    }

    private UttakPeriode søknadsperiode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType, SamtidigUttak samtidigUttak, boolean flerbarnsdager) {
        StønadsPeriode stønadsPeriode = new StønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fom, tom, samtidigUttak, flerbarnsdager);
        stønadsPeriode.setPeriodeVurderingType(vurderingType);
        return stønadsPeriode;
    }

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private void assertInnvilgetAvslåttGradering(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak, GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak);
    }

    private void assertManuellBehandling(Regelresultat regelresultat,
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

    private void assertAvslått(Regelresultat regelresultat,
                               IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                               boolean trekkdager,
                               boolean utbetal) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkdager);
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }


    private RegelGrunnlag.Builder basicGrunnlagMor() {
        return basicGrunnlag()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true));
    }

    private RegelGrunnlag.Builder basicGrunnlag() {
        return RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                        .medFødsel(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }
}
