package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeRegelOrkestreringAdopsjonTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void adopsjon() {

    }

    // MØDREKVOTE
    @Test
    public void UT1230_adopsjon_mor_søker_mødrekvote_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        søknadsperiode(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // MØDREKVOTE
    @Test
    public void UT1006_adopsjon_mor_søker_mødrekvote_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // MØDREKVOTE
    @Test
    public void UT1230_adopsjon_far_søker_overført_mødrekvote_pga_innleggelse_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(MØDREKVOTE, PeriodeKilde.SØKNAD, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // MØDREKVOTE
    @Test
    public void UT1230_adopsjon_far_søker_overført_mødrekvote_pga_sykdom_skade_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(MØDREKVOTE, PeriodeKilde.SØKNAD, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }


    // FEDREKVOTE
    @Test
    public void UT1231_adopsjon_far_søker_fedrekvote_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        søknadsperiode(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FEDREKVOTE
    @Test
    public void UT1030_adopsjon_far_søker_fedrekvote_før_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(Stønadskontotype.FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(3)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FEDREKVOTE
    @Test
    public void UT1231_adopsjon_mor_søker_overført_fedrekvote_pga_innleggelse_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FEDREKVOTE
    @Test
    public void UT1231_adopsjon_mor_søker_overført_fedrekvote_pga_sykdom_skade_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(FEDREKVOTE, PeriodeKilde.SØKNAD, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }


    // FELLESPERIODE
    @Test
    public void UT1235_adopsjon_mor_søker_fellesperiode_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        søknadsperiode(FELLESPERIODE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FELLESPERIODE
    @Test
    public void UT1046_adopsjon_mor_søker_fellesperiode_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FELLESPERIODE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // FELLESPERIODE
    @Test
    public void UT1232_adopsjon_far_søker_fellesperiode_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        søknadsperiode(FELLESPERIODE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FELLESPERIODE
    @Test
    public void UT1060_adopsjon_far_søker_fellesperiode_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FELLESPERIODE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    public void UT1236_adopsjon_mor_søker_foreldrepenger_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(false)
                        .medMorHarRett(true)
                        .medSamtykke(true))
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        søknadsperiode(FORELDREPENGER, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FORELDREPENGER
    @Test
    public void UT1191_adopsjon_mor_søker_foreldrepenger_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    public void UT1234_adopsjon_far_søker_foreldrepenger_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .medSamtykke(true))
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.ADOPSJON,
                        søknadsperiode(FORELDREPENGER, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FORELDREPENGER
    @Test
    public void UT1199_adopsjon_far_søker_foreldrepenger_etter_omsorgsovertakelse_men_uten_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .medSamtykke(true))
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    public void UT1201_adopsjon_far_søker_foreldrepenger_etter_omsorgsovertakelse_men_før_etter_uke_7() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        var grunnlag = grunnlagAdopsjon
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .medSamtykke(true))
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    // STEBARNSADOPSJON
    @Test
    public void UT1240_stebarnsadopsjon_far_ikke_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1241_stebarnsadopsjon_far_omsorg_disponible_dager_og_ingen_gradering() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    public void UT1242_stebarnsadopsjon_far_omsorg_disponible_dager_gradering_og_avklart_periode() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        RegelGrunnlag grunnlag = grunnlagAdopsjon
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(StønadsPeriode.medGradering(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1),
                                Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK))
                        .medMottattDato(omsorgsovertakelseDato.minusWeeks(1)))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    public void UT1244_stebarnsadopsjon_far_omsorg_ikke_disponible_stønadsdager() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(0))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(1300));
        var grunnlag = grunnlagAdopsjon
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelseDato)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelseDato.minusMonths(3)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode(FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .medStebarnsadopsjon(true))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }


}