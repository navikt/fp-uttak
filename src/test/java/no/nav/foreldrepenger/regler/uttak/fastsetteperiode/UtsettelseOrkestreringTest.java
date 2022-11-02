package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static java.time.LocalDate.of;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_3;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode.Builder.utsettelse;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode.Builder.uttak;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_BARN_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_SØKER_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_DOKUMENTERT_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_DOKUMENTERT_IKKE_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.SYKDOM_SØKER_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.FERIE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.FRI;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_BARN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_SØKER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.SYKDOM_SKADE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.FRATREKK_PLEIEPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.Pleiepenger;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.PleiepengerPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.Ytelser;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class UtsettelseOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void periode_med_dokumentert_utsettelse_pga_barn_innlagt_innenfor_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
                        INNLAGT_BARN, INNLEGGELSE_BARN_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_dokumentert_utsettelse_pga_barn_innlagt_etter_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                        INNLAGT_BARN, INNLEGGELSE_BARN_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_dokumentert_utsettelse_pga_søker_innlagt_innenfor_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad()
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
                        INNLAGT_SØKER, INNLEGGELSE_SØKER_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_SØKER);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_dokumentert_utsettelse_pga_søker_innlagt_etter_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad()
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                        INNLAGT_SØKER, INNLEGGELSE_SØKER_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_SØKER);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_dokumentert_utsettelse_pga_søker_syk_innenfor_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
                        SYKDOM_SKADE, SYKDOM_SØKER_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_dokumentert_utsettelse_pga_søker_syk_etter_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad()
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), SYKDOM_SKADE, SYKDOM_SØKER_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_utsettelse_pga_arbeid_etter_uke_6_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var utsettelseFom = fødselsdato.plusWeeks(6);
        var utsettelseTom = fødselsdato.plusWeeks(10).minusDays(1);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, ARBEID, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);

        var uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(ARBEID);
        assertThat(uttakPeriode.getFom()).isEqualTo(utsettelseFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(utsettelseTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_utsettelse_pga_arbeid_før_uke_6_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var utsettelseFom = fødselsdato.plusWeeks(4);
        var utsettelseTom = fødselsdato.plusWeeks(10).minusDays(1);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, ARBEID, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var innenfor6Uker = resultat.get(1).getUttakPeriode();
        assertThat(innenfor6Uker.getFom()).isEqualTo(utsettelseFom);
        assertThat(innenfor6Uker.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(innenfor6Uker.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        var etter6Uker = resultat.get(2).getUttakPeriode();
        assertThat(etter6Uker.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(etter6Uker.getTom()).isEqualTo(utsettelseTom);
        assertThat(etter6Uker.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void periode_med_utsettelse_pga_ferie_etter_uke_6_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var utsettelseFom = fødselsdato.plusWeeks(6);
        var utsettelseTom = fødselsdato.plusWeeks(10).minusDays(1);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);

        var uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(utsettelseFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(utsettelseTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    void skal_ikke_utlede_stønadskontotype_ved_innvilgelse_av_utsettelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad()
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN, INNLEGGELSE_BARN_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);

        var uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(uttakPeriode.getStønadskontotype()).isNull();
    }

    @Test
    void periode_med_utsettelse_sykdom_som_ikke_er_dokumentert_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad()
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(2).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(3).minusDays(1), SYKDOM_SKADE, null))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(12).minusDays(1), SYKDOM_SKADE, SYKDOM_SØKER_DOKUMENTERT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        var utsettelseFørsteUkeneSomIkkeErDokumentert = resultat.get(1).getUttakPeriode();
        assertThat(utsettelseFørsteUkeneSomIkkeErDokumentert.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(utsettelseFørsteUkeneSomIkkeErDokumentert.getFom()).isEqualTo(fødselsdato.plusWeeks(2));
        assertThat(utsettelseFørsteUkeneSomIkkeErDokumentert.getTom()).isEqualTo(fødselsdato.plusWeeks(3).minusDays(1));
        assertThat(utsettelseFørsteUkeneSomIkkeErDokumentert.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        var utsettelseFørsteUkeneSomErDokumentert = resultat.get(2).getUttakPeriode();
        assertThat(utsettelseFørsteUkeneSomErDokumentert.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(utsettelseFørsteUkeneSomErDokumentert.getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(utsettelseFørsteUkeneSomErDokumentert.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(utsettelseFørsteUkeneSomErDokumentert.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        var utsettelseEtterFørsteUkeneDokumentert = resultat.get(3).getUttakPeriode();
        assertThat(utsettelseEtterFørsteUkeneDokumentert.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(utsettelseEtterFørsteUkeneDokumentert.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(utsettelseEtterFørsteUkeneDokumentert.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelseEtterFørsteUkeneDokumentert.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void utsettelse_periode_med_ukjent_kontotype_må_settes_til_neste_tilgjengelig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad()
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1),
                        SYKDOM_SKADE, null)));

        //Her skal det gis avslag (mangler dok) og trekke dager, skal velge konto å trekke fra
        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);


        var utsettelse = resultat.get(1).getUttakPeriode();
        assertThat(utsettelse.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(utsettelse.getFom()).isEqualTo(fødselsdato.plusWeeks(4));
        assertThat(utsettelse.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(utsettelse.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(utsettelse.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(utsettelse.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    void pleiepenger_utsettelse_skal_trekke_fra_fellesperiode() {
        //Over 7 uker for tidlig, får pleiepenger. Utsettelsen skal avlås og det skal trekkes dager
        var termindato = LocalDate.of(2019, 9, 1);
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .datoer(new Datoer.Builder().termin(termindato).fødsel(fødselsdato))
                .søknad(fødselSøknad().dokumentasjon(new Dokumentasjon.Builder())
                        //Starter med pleiepenger
                        .oppgittPeriode(utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(4).minusDays(1), INNLAGT_BARN, INNLEGGELSE_BARN_DOKUMENTERT))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(4), termindato)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FELLESPERIODE);
    }

    @Test
    void pleiepenger_utsettelse_skal_trekke_fra_foreldrepenger() {
        //Over 7 uker for tidlig, får pleiepenger. Utsettelsen skal avlås og det skal trekkes dager
        var termindato = LocalDate.of(2019, 9, 1);
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(100));
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).kontoer(kontoer)
                .datoer(new Datoer.Builder().termin(termindato).fødsel(fødselsdato))
                .søknad(fødselSøknad()
                        //Starter med pleiepenger
                        .oppgittPeriode(utsettelsePeriode(fødselsdato, termindato.minusDays(1), INNLAGT_BARN, INNLEGGELSE_BARN_DOKUMENTERT))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, termindato, termindato.plusWeeks(10))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void pleiepenger_med_overlappende_uttaksperiode_skal_gå_til_manuell() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var innleggelse = new PleiepengerPeriode(fødselsdato, fødselsdato.plusWeeks(3), true);
        var utenInnleggelse = new PleiepengerPeriode(fødselsdato.plusWeeks(3).plusDays(1), fødselsdato.plusWeeks(6).minusDays(1), false);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))))
                .ytelser(new Ytelser(new Pleiepenger(List.of(innleggelse, utenInnleggelse))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(innleggelse.getFom());
        assertThat(resultat.get(0).getUttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.OVERLAPPENDE_PLEIEPENGER_MED_INNLEGGELSE);
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(utenInnleggelse.getFom());
        assertThat(resultat.get(1).getUttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.OVERLAPPENDE_PLEIEPENGER_UTEN_INNLEGGELSE);
    }

    @Test
    void utsettelse_pga_sykdom_før_søknad_mottatt_dato_skal_innvilges() {
        //Mottatt dato skal ikke være relevant for utsettelse første 6 ukene hvis det er dokumentert
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var utsettelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1),
            SYKDOM_SKADE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(8), null, SYKDOM_SØKER_DOKUMENTERT);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                        .oppgittPeriode(utsettelse));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void skal_avslå_periode_hvis_overlapp_med_innvilget_utsettelse_i_tidsperiode_forbeholdt_mor_i_berørt_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(uttak(fødselsdato, fødselsdato.plusWeeks(2).minusDays(1)).build())
                        .uttaksperiode(utsettelse(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(6)).build()))
                .behandling(farBehandling().berørtBehandling(true))
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(4).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(1);

        var uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(OPPHOLD_UTSETTELSE);
    }

    @Test
    void skal_innvilge_periode_hvis_overlapp_med_innvilget_utsettelse_etter_tidsperiode_forbeholdt_mor_i_berørt_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(uttak(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)).build())
                        .uttaksperiode(utsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10)).build()))
                .behandling(farBehandling().berørtBehandling(true))
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(1);

        var uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    //FAGSYSTEM-151437
    @Test
    void utsettelse_innvilges_tilbake_i_tid_for_bare_far_har_rett_hvis_mor_er_i_aktivitet() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var utsettelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(50),
            ARBEID, fødselsdato.plusWeeks(100), fødselsdato.plusWeeks(100), MorsAktivitet.UTDANNING, MORS_AKTIVITET_DOKUMENTERT_AKTIVITET);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .behandling(farBehandling())
                .kontoer(new Kontoer.Builder().konto(konto(FORELDREPENGER, 100)))
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad(FØDSEL, utsettelse));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void innvilge_eller_manuell_behandling_basert_på_pleiepenger_med_innleggelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .søknad(fødselSøknad()
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN,
                            null)))
                .ytelser(new Ytelser(new Pleiepenger(Set.of(new PleiepengerPeriode(fødselsdato.plusWeeks(3),
                        fødselsdato.plusWeeks(5).minusDays(1), true)))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        var innvilgetPeriode = resultat.get(2).getUttakPeriode();
        assertThat(innvilgetPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(innvilgetPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(innvilgetPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(5).minusDays(1));
        assertThat(innvilgetPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(innvilgetPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        var manuellPeriode = resultat.get(3).getUttakPeriode();
        assertThat(manuellPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(manuellPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(5));
        assertThat(manuellPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(manuellPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void avslag_utsettelse_med_trekkdager_skal_knekkes_når_saldo_går_tom() {
        var fødselsdato = LocalDate.of(2021, 1, 20);
        //Skal få avslag pga mor ikke er i aktivitet
        var fom = fødselsdato.plusWeeks(6);
        var tom = fødselsdato.plusWeeks(9);
        //Skal gå tom for dager
        var utsettelse = OppgittPeriode.forUtsettelse(fom, tom, FRI, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, MORS_AKTIVITET_DOKUMENTERT_IKKE_AKTIVITET);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder().konto(new Konto.Builder().trekkdager(10).type(FORELDREPENGER)))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(utsettelse));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fom);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fom.plusWeeks(2).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fom.plusWeeks(2));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(tom);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void utsettelse_akt_krav_bare_far_rett_innvilget_uten_trekk() {
        var fødselsdato = LocalDate.of(2021, 1, 20);
        //Skal få avslag pga mor ikke er i aktivitet
        var fom = fødselsdato.plusWeeks(6);
        var tom = fødselsdato.plusWeeks(9);
        //Skal gå tom for dager
        var utsettelse = OppgittPeriode.forUtsettelse(fom, tom, FRI, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, MORS_AKTIVITET_DOKUMENTERT_AKTIVITET);
        var grunnlag = basicGrunnlagFar(fødselsdato).datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder().konto(new Konto.Builder().trekkdager(10).type(FORELDREPENGER)))
                .søknad(new Søknad.Builder().type(FØDSEL).oppgittPeriode(utsettelse));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG_BFR_AKT_KRAV_OPPFYLT);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fom);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(tom);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isFalse();
    }

    @Test
    void fri_utsettelse_før_fødsel_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2021, 1, 20);
        var fom = fødselsdato.minusWeeks(5);
        var tom = fødselsdato.minusWeeks(3).minusDays(1);
        var utsettelse = utsettelsePeriode(fom, tom, FRI, null);
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(FØDSEL, utsettelse,
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isNull();
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fom);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(tom);
    }

    @Test
    void prematur_fødsel_pleiepenger_skal_gi_utsettelse_med_trekkdager_fram_til_termindato() {
        var fødselsdato = LocalDate.of(2021, 11, 22);
        var termindato = fødselsdato.plusWeeks(8);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termindato))
                .søknad(fødselSøknad()
                .oppgittPeriode(utsettelsePeriode(fødselsdato, termindato.minusDays(1), INNLAGT_BARN, INNLEGGELSE_BARN_DOKUMENTERT))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, termindato, termindato.plusWeeks(6))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        var p1 = resultat.get(0).getUttakPeriode();
        var p2 = resultat.get(1).getUttakPeriode();
        var p3 = resultat.get(2).getUttakPeriode();
        assertThat(p1.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(p1.getFom()).isEqualTo(fødselsdato);
        assertThat(p1.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(p1.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p1.getPeriodeResultatÅrsak()).isEqualTo(FRATREKK_PLEIEPENGER);
        assertThat(p1.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));

        assertThat(p2.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(p2.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(p2.getTom()).isEqualTo(termindato.minusDays(1));
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p2.getPeriodeResultatÅrsak()).isEqualTo(FRATREKK_PLEIEPENGER);
        assertThat(p2.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));

        assertThat(p3.getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    void fri_utsettelse_første_6_ukene_skal_gå_til_manuell_pre_wlb() {
        var fødselsdato = LocalDate.of(2022, 6, 28);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .kontoer(defaultKontoer())
                .søknad(søknad(FØDSEL,
                        utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), FRI, null),
                        oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2).minusDays(1), false, SamtidigUttaksprosent.HUNDRED)
                ));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isNotEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isNotEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void fri_utsettelse_første_6_ukene_skal_innvilges_for_far_begge_rett() {
        var fødselsdato = LocalDate.of(2022, 6, 28);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .kontoer(defaultKontoer().farUttakRundtFødselDager(10))
                .søknad(søknad(FØDSEL,
                        utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), FRI, null),
                        oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2).minusDays(1), false, SamtidigUttaksprosent.HUNDRED),
                        utsettelsePeriode(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(3).minusDays(1), FRI, null),
                        oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(5).minusDays(1), false, new SamtidigUttaksprosent(50)),
                        utsettelsePeriode(fødselsdato.plusWeeks(5), fødselsdato.plusWeeks(8).minusDays(1), FRI, null),
                        oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15))
                        ));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(7);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(4).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(4).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(4).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        // Knekk ved fødsel + 6uker
        assertThat(perioder.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(5).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(5).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(5).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(6).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void fri_utsettelse_første_6_ukene_skal_innvilges_for_bfhr() {
        var fødselsdato = LocalDate.of(2022, 6, 28);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder().konto(FORELDREPENGER, 100).minsterettDager(40).farUttakRundtFødselDager(10))
                .søknad(søknad(FØDSEL,
                        utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), FRI, null),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2).minusDays(1)),
                        utsettelsePeriode(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(3).minusDays(1), FRI, null),
                        gradertoppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(5).minusDays(1), BigDecimal.valueOf(50)),
                        utsettelsePeriode(fødselsdato.plusWeeks(5), fødselsdato.plusWeeks(6).minusDays(1), FRI, MorsAktivitet.ARBEID, null),
                        utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), FRI, MorsAktivitet.ARBEID, MORS_AKTIVITET_DOKUMENTERT_AKTIVITET),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(10), MORS_AKTIVITET_DOKUMENTERT_AKTIVITET)
                ));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(7);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(4).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(4).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(4).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        assertThat(perioder.get(6).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void fri_utsettelse_før_termin_far_begge_rett() {
        var fødselsdato = LocalDate.of(2022, 6, 28);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .kontoer(defaultKontoer().farUttakRundtFødselDager(10))
                .søknad(søknad(FØDSEL,
                        utsettelsePeriode(fødselsdato.minusWeeks(1), fødselsdato.plusWeeks(1).minusDays(1), FRI, null),
                        oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(5).minusDays(1), false, SamtidigUttaksprosent.HUNDRED)
                ));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void fri_utsettelse_før_termin_bfhr() {
        var fødselsdato = LocalDate.of(2022, 6, 28);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder().konto(FORELDREPENGER, 100).minsterettDager(40).farUttakRundtFødselDager(10))
                .søknad(søknad(FØDSEL,
                        utsettelsePeriode(fødselsdato.minusWeeks(1), fødselsdato.plusWeeks(1).minusDays(1), FRI, null),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1))
                ));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @ParameterizedTest
    @EnumSource(UtsettelseÅrsak.class)
    void annen_parts_periode_skal_trekke_dager_selv_om_de_overlapper_med_søkers_utsettelse(UtsettelseÅrsak utsettelseÅrsak) {
        //FAGSYSTEM-243708
        var fødselsdato = of(2018, 1, 1);
        var mottattDatoFar = fødselsdato.plusWeeks(5);
        var utsettelsePeriode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), utsettelseÅrsak, mottattDatoFar,
            mottattDatoFar, null, null);
        var fellesperiodeFom = utsettelsePeriode.getTom().plusDays(1);
        var fellesperiode = OppgittPeriode.forVanligPeriode(FELLESPERIODE, fellesperiodeFom, fellesperiodeFom.plusWeeks(1).minusDays(1),
            null, false, mottattDatoFar, mottattDatoFar, MorsAktivitet.ARBEID, MORS_AKTIVITET_DOKUMENTERT_AKTIVITET);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
            .behandling(farBehandling())
            .søknad(søknad(Søknadstype.FØDSEL, utsettelsePeriode, fellesperiode))
            .annenPart(new AnnenPart.Builder()
                .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                    .innvilget(true)
                    .senestMottattDato(fødselsdato)
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(ARBEIDSFORHOLD_3, MØDREKVOTE, new Trekkdager(30), Utbetalingsgrad.HUNDRED))
                    .build())
                .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(utsettelsePeriode.getFom(), utsettelsePeriode.getTom())
                    .innvilget(true)
                    .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(ARBEIDSFORHOLD_3, FELLESPERIODE, new Trekkdager(130), Utbetalingsgrad.HUNDRED))
                    .senestMottattDato(fødselsdato)
                    .build())
            );

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(resultat.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IKKE_STØNADSDAGER_IGJEN);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().fødsel(fødselsdato);
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato) {
        return basicUtsettelseGrunnlag(fødselsdato, morBehandling());
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato, Behandling.Builder behandling) {
        return basicGrunnlag().datoer(datoer(fødselsdato)).behandling(behandling).rettOgOmsorg(beggeRett());
    }

    private Søknad.Builder fødselSøknad() {
        return new Søknad.Builder().type(FØDSEL);
    }
}
