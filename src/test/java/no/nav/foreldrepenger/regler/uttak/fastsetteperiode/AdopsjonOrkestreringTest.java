package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class AdopsjonOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    // MØDREKVOTE
    @Test
    void UT1230_adopsjon_mor_søker_mødrekvote_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // MØDREKVOTE
    @Test
    void UT1006_adopsjon_mor_søker_mødrekvote_etter_omsorgsovertakelse_men_uten_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // MØDREKVOTE
    @Test
    void UT1230_adopsjon_far_søker_overført_mødrekvote_pga_innleggelse_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(OppgittPeriode.forOverføring(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK, OverføringÅrsak.INNLEGGELSE,
                                null, null))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // MØDREKVOTE
    @Test
    void UT1230_adopsjon_far_søker_overført_mødrekvote_pga_sykdom_skade_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(OppgittPeriode.forOverføring(MØDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null, null))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }


    // FEDREKVOTE
    @Test
    void UT1231_adopsjon_far_søker_fedrekvote_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FEDREKVOTE
    @Test
    void UT1030_adopsjon_far_søker_fedrekvote_før_omsorgsovertakelse_men_uten_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(3)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FEDREKVOTE
    @Test
    void UT1231_adopsjon_mor_søker_overført_fedrekvote_pga_innleggelse_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(OppgittPeriode.forOverføring(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK, OverføringÅrsak.INNLEGGELSE,
                                null, null))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FEDREKVOTE
    @Test
    void UT1231_adopsjon_mor_søker_overført_fedrekvote_pga_sykdom_skade_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(OppgittPeriode.forOverføring(FEDREKVOTE, omsorgsovertakelseDato.minusWeeks(1),
                                omsorgsovertakelseDato.minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null, null))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(omsorgsovertakelseDato.minusWeeks(2), omsorgsovertakelseDato.plusWeeks(1)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }


    // FELLESPERIODE
    @Test
    void UT1235_adopsjon_mor_søker_fellesperiode_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FELLESPERIODE
    @Test
    void UT1046_adopsjon_mor_søker_fellesperiode_etter_omsorgsovertakelse_men_uten_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // FELLESPERIODE
    @Test
    void UT1232_adopsjon_far_søker_fellesperiode_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FELLESPERIODE
    @Test
    void UT1060_adopsjon_far_søker_fellesperiode_etter_omsorgsovertakelse_men_uten_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    void UT1236_adopsjon_mor_søker_foreldrepenger_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(130));
        var grunnlag = grunnlagAdopsjon.kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(false).morHarRett(true).samtykke(true))
                .behandling(morBehandling())
                .søknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FORELDREPENGER
    @Test
    void UT1191_adopsjon_mor_søker_foreldrepenger_etter_omsorgsovertakelse_men_uten_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(130));
        var grunnlag = grunnlagAdopsjon.kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    void UT1234_adopsjon_far_søker_foreldrepenger_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(130));
        var grunnlag = grunnlagAdopsjon.kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(bareFarRett())
                .behandling(farBehandling())
                .søknad(søknad(Søknadstype.ADOPSJON,
                        oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato.minusWeeks(1), omsorgsovertakelseDato.minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato.minusWeeks(1),
                omsorgsovertakelseDato.minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    // FORELDREPENGER
    @Test
    void UT1199_adopsjon_far_søker_foreldrepenger_etter_omsorgsovertakelse_men_uten_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(130));
        var grunnlag = grunnlagAdopsjon.arbeid(
                new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(bareFarRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    // FORELDREPENGER
    @Test
    void adopsjon_far_søker_foreldrepenger_etter_omsorgsovertakelse_men_før_uke_7_skal_innvilges() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(130));
        var oppgittPeriode = oppgittPeriode(FORELDREPENGER, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1));
        var periodeMedAvklartMorsAktivitet = new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(),
                I_AKTIVITET);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(periodeMedAvklartMorsAktivitet);
        var søknad = new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode)
                .dokumentasjon(dokumentasjon);
        var grunnlag = grunnlagAdopsjon.arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(bareFarRett())
                .behandling(farBehandling())
                .søknad(søknad)
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1),
                Perioderesultattype.INNVILGET, FORELDREPENGER);
    }

    @Test
    void UT1082_adopsjon_avslag_perioder_forut_for_førsteLovligeUttaksdag() {
        var omsorgsovertakelseDato = LocalDate.of(2020, 1, 1);

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(100));
        var testGrunnlag = grunnlagAdopsjon.arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true).samtykke(true))
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        //Mottatt mer enn 3 mnd etter start
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(1).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT,
                                omsorgsovertakelseDato.plusMonths(4), omsorgsovertakelseDato.plusMonths(4), null)))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(omsorgsovertakelseDato))
                .build();

        var resultater = fastsettPerioder(testGrunnlag);
        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(1).minusDays(1), FORELDREPENGER, IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    // STEBARNSADOPSJON
    @Test
    void UT1240_stebarnsadopsjon_far_ikke_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null).stebarnsadopsjon(true))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    void UT1241_stebarnsadopsjon_far_omsorg_disponible_dager_og_ingen_gradering() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(
                                oppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null).stebarnsadopsjon(true))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    void UT1242_stebarnsadopsjon_far_omsorg_disponible_dager_gradering_og_avklart_periode() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var grunnlag = grunnlagAdopsjon.datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(gradertoppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato,
                                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), BigDecimal.TEN)))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null).stebarnsadopsjon(true))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, null, Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    void UT1244_stebarnsadopsjon_far_omsorg_ikke_disponible_stønadsdager() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);

        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(50))
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(0))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(1300));
        var grunnlag = grunnlagAdopsjon.kontoer(kontoer)
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelseDato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(
                                oppgittPeriode(FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2).minusDays(1))))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null).stebarnsadopsjon(true))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), omsorgsovertakelseDato,
                omsorgsovertakelseDato.plusWeeks(2).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }
}
