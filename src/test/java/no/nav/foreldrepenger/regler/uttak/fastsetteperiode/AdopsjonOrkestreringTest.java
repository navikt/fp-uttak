package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

;

public class AdopsjonOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    // MØDREKVOTE
    @Test
    public void UT1230_adopsjon_mor_søker_mødrekvote_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // MØDREKVOTE
    @Test
    public void UT1006_adopsjon_mor_søker_mødrekvote_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // MØDREKVOTE
    @Test
    public void UT1230_adopsjon_far_søker_overført_mødrekvote_pga_innleggelse_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK, OverføringÅrsak.INNLEGGELSE,
                                null))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // MØDREKVOTE
    @Test
    public void UT1230_adopsjon_far_søker_overført_mødrekvote_pga_sykdom_skade_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }


    // FEDREKVOTE
    @Test
    public void UT1231_adopsjon_far_søker_fedrekvote_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FEDREKVOTE
    @Test
    public void UT1030_adopsjon_far_søker_fedrekvote_før_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(3)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FEDREKVOTE
    @Test
    public void UT1231_adopsjon_mor_søker_overført_fedrekvote_pga_innleggelse_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK, OverføringÅrsak.INNLEGGELSE,
                                null))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FEDREKVOTE
    @Test
    public void UT1231_adopsjon_mor_søker_overført_fedrekvote_pga_sykdom_skade_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }


    // FELLESPERIODE
    @Test
    public void UT1235_adopsjon_mor_søker_fellesperiode_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FELLESPERIODE
    @Test
    public void UT1046_adopsjon_mor_søker_fellesperiode_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // FELLESPERIODE
    @Test
    public void UT1232_adopsjon_far_søker_fellesperiode_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FELLESPERIODE
    @Test
    public void UT1060_adopsjon_far_søker_fellesperiode_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    public void UT1236_adopsjon_mor_søker_foreldrepenger_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon.medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(false).medMorHarRett(true).medSamtykke(true))
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FORELDREPENGER
    @Test
    public void UT1191_adopsjon_mor_søker_foreldrepenger_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon.medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    public void UT1234_adopsjon_far_søker_foreldrepenger_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon.medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false).medSamtykke(true))
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FORELDREPENGER
    @Test
    public void UT1199_adopsjon_far_søker_foreldrepenger_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon.medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false).medSamtykke(true))
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    public void adopsjon_far_søker_foreldrepenger_etter_omsorgsovertakelse_men_før_uke_7_skal_innvilges() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        var oppgittPeriode = oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1));
        var periodeMedAvklartMorsAktivitet = new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(),
                I_AKTIVITET);
        var dokumentasjon = new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(periodeMedAvklartMorsAktivitet);
        var søknad = new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode)
                .medDokumentasjon(dokumentasjon);
        var grunnlag = grunnlagAdopsjon.medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false).medSamtykke(true))
                .medBehandling(farBehandling())
                .medSøknad(søknad)
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1),
                Perioderesultattype.INNVILGET, FORELDREPENGER);
    }

    @Test
    public void UT1082_adopsjon_avslag_perioder_forut_for_førsteLovligeUttaksdag() {
        var omsorgsovertakelseDato = LocalDate.of(2020, 1, 1);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(100));
        var testGrunnlag = grunnlagAdopsjon.medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true).medSamtykke(true))
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        //Mottatt mer enn 3 mnd etter start
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(1).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT,
                                omsorgsovertakelseDato.plusMonths(4), null)))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(omsorgsovertakelseDato))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(testGrunnlag);
        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(1).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    // STEBARNSADOPSJON
    @Test
    public void UT1240_stebarnsadopsjon_far_ikke_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null).medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1241_stebarnsadopsjon_far_omsorg_disponible_dager_og_ingen_gradering() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null).medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    public void UT1242_stebarnsadopsjon_far_omsorg_disponible_dager_gradering_og_avklart_periode() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon.medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(gradertoppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), BigDecimal.TEN)))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null).medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    public void UT1244_stebarnsadopsjon_far_omsorg_ikke_disponible_stønadsdager() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(0))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(1300));
        var grunnlag = grunnlagAdopsjon.medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelseDato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null).medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }
}
