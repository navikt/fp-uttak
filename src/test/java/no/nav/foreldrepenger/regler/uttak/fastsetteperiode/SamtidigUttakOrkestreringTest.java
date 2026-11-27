package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forFrilans;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class SamtidigUttakOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_lik_100_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(40)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(60))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(60));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_under_100_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(30)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(30))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(30));
    }

    @Test
    void samtidig_uttak_til_manuell_i_berørt_hvis_samlet_over_100_prosent_uttak_ene_over_80_prosent() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(85)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    @Test
    void samtidig_uttak_automatisk_reduksjon_i_berørt_hvis_samlet_over_100_prosent_uttak() {
        var forventetRedusertTil = 40;
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(60)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getSamtidigUttaksprosent()).isEqualTo(new SamtidigUttaksprosent(forventetRedusertTil));
        assertThat(resultat.get(1).uttakPeriode().getAktiviteter().stream().findFirst().orElseThrow().getUtbetalingsgrad()).isEqualTo(
            new Utbetalingsgrad(forventetRedusertTil));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_lik_150_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(50)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(100))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(100));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_lik_150_prosent_uttak_byttom() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(100)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(50))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(50));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_under_150_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(30)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(30))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(30));
    }

    @Test
    void samtidig_uttak_til_manuell_i_berørt_hvis_samlet_over_150_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(60)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void samtidig_uttak_til_manuell_i_berørt_hvis_samlet_over_150_prosent_uttak_byttom() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(60)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), false, new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void overlappende_gradering_avslås_i_berørt_selv_om_samlet_under_100_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh).annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(30)))
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradertoppgittPeriode(MØDREKVOTE, fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1), BigDecimal.valueOf(30),
                    Set.of(ARBEIDSFORHOLD))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void skal_ikke_stykke_opp_periode_når_berørt_behandling_reduserer_utbetaling_og_kontoen_går_tom() {
        var fh = LocalDate.of(2022, 4, 1);
        var fom = fh.plusWeeks(28);
        var tom = fom.plusWeeks(2).minusDays(1);
        var grunnlag = basicGrunnlagMor(fh)
            .kontoer(new Kontoer.Builder()
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(10)))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(50)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(FELLESPERIODE, fom, tom)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(50));
        assertThat(resultat.get(1).uttakPeriode().getAktiviteter().stream().findFirst().orElseThrow().getTrekkdager()).isEqualTo(new Trekkdager(5));
    }

    @Test
    void kontroll_stykker_ikke_opp_når_mor_selv_har_søkt_samtidig_uttak_50_prosent() {
        var fh = LocalDate.of(2022, 4, 1);
        var fom = fh.plusWeeks(28);
        var tom = fom.plusWeeks(2).minusDays(1);
        var grunnlag = basicGrunnlagMor(fh)
            .kontoer(new Kontoer.Builder()
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(10)))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(50)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(FELLESPERIODE, fom, tom, false, new SamtidigUttaksprosent(50))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getAktiviteter().stream().findFirst().orElseThrow().getTrekkdager()).isEqualTo(new Trekkdager(5));
    }

    @Test
    void skal_knekke_på_riktig_dato_når_berørt_behandling_reduserer_utbetaling_og_kontoen_går_tom_midt_i_perioden() {
        var fh = LocalDate.of(2022, 4, 1);
        var fom = fh.plusWeeks(28);
        var tom = fom.plusWeeks(2).minusDays(1);
        var grunnlag = basicGrunnlagMor(fh)
            .kontoer(new Kontoer.Builder()
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(8)))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(50)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(FELLESPERIODE, fom, tom)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        var innvilgetPeriode = resultat.get(1).uttakPeriode();
        assertThat(innvilgetPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(innvilgetPeriode.getFom()).isEqualTo(fom);
        assertThat(innvilgetPeriode.getTom()).isEqualTo(Virkedager.plusVirkedager(fom, 6).minusDays(1));
        assertThat(innvilgetPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(50));
        assertThat(innvilgetPeriode.getAktiviteter().stream().findFirst().orElseThrow().getTrekkdager()).isEqualTo(new Trekkdager(3));

        var etterKnekkPeriode = resultat.get(2).uttakPeriode();
        assertThat(etterKnekkPeriode.getFom()).isEqualTo(Virkedager.plusVirkedager(fom, 6));
        assertThat(etterKnekkPeriode.getTom()).isEqualTo(tom);
        assertThat(etterKnekkPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(etterKnekkPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    void skal_bruke_redusert_prosent_for_knekkpunkt_når_mor_selv_søkte_høyere_samtidig_uttaksprosent() {
        var fh = LocalDate.of(2022, 4, 1);
        var fom = fh.plusWeeks(28);
        var tom = fom.plusWeeks(2).minusDays(1);
        var grunnlag = basicGrunnlagMor(fh)
            .kontoer(new Kontoer.Builder()
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(9)))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(60)))
                    .samtidigUttak(true)
                    .build()))
            .behandling(morBehandling().berørtBehandling(true))
            .søknad(søknad(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode(FELLESPERIODE, fom, tom, false, new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getSamtidigUttaksprosent()).isEqualTo(new SamtidigUttaksprosent(40));
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
        assertThat(resultat.get(1).uttakPeriode().getAktiviteter().stream().findFirst().orElseThrow().getTrekkdager()).isEqualTo(new Trekkdager(4));
    }
}
