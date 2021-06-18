package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.FERIE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_BARN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_SØKER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.SYKDOM_SKADE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class UtsettelseOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void periode_med_dokumentert_utsettelse_pga_barn_innlagt_innenfor_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
                        INNLAGT_BARN))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(
                        new PeriodeMedBarnInnlagt(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1)))));

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
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                        INNLAGT_BARN))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(
                        new PeriodeMedBarnInnlagt(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1)))));

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
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad()
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
                        INNLAGT_SØKER))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedInnleggelse(
                        new PeriodeMedInnleggelse(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1)))));

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
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad()
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                        INNLAGT_SØKER))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedInnleggelse(
                        new PeriodeMedInnleggelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1)))));

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
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
                        SYKDOM_SKADE))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedSykdomEllerSkade(
                        new PeriodeMedSykdomEllerSkade(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1)))));

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
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad()
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), SYKDOM_SKADE))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedSykdomEllerSkade(
                        new PeriodeMedSykdomEllerSkade(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1)))));

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
                .medSøknad(fødselSøknad()
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, ARBEID)));

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
                .medSøknad(fødselSøknad()
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                        .leggTilOppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, ARBEID)));

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
                .medSøknad(fødselSøknad()
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, FERIE)));

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
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad()
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN))
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fødselsdato, fødselsdato.plusWeeks(10)))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);

        var uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(uttakPeriode.getStønadskontotype()).isNull();
    }

    @Test
    void periode_med_utsettelse_sykdom_som_ikke_er_dokumentert_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad()
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(2).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(12).minusDays(1), SYKDOM_SKADE))
        .medDokumentasjon(new Dokumentasjon.Builder()
                .leggPeriodeMedSykdomEllerSkade(new PeriodeMedSykdomEllerSkade(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(8)))));

        var resultat = fastsettPerioder(grunnlag);
        //Her trenger vi egentlig ikke knapp på tom på dokumentasjon ettersom den ligger etter uke 6
        assertThat(resultat).hasSize(5);

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
        assertThat(utsettelseEtterFørsteUkeneDokumentert.getTom()).isEqualTo(fødselsdato.plusWeeks(8));
        assertThat(utsettelseEtterFørsteUkeneDokumentert.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        var utsettelseEtterFørsteUkeneIkkeDokumentert = resultat.get(4).getUttakPeriode();
        assertThat(utsettelseEtterFørsteUkeneIkkeDokumentert.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(utsettelseEtterFørsteUkeneIkkeDokumentert.getFom()).isEqualTo(fødselsdato.plusWeeks(8).plusDays(1));
        assertThat(utsettelseEtterFørsteUkeneIkkeDokumentert.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelseEtterFørsteUkeneIkkeDokumentert.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void utsettelse_periode_med_ukjent_kontotype_må_settes_til_neste_tilgjengelig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad()
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1),
                        SYKDOM_SKADE)));

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
                .medDatoer(new Datoer.Builder().medTermin(termindato).medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().medDokumentasjon(new Dokumentasjon.Builder()
                        .leggPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fødselsdato, termindato.minusWeeks(2).minusDays(1))))
                        //Starter med pleiepenger
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), termindato)));

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
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(100));
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medTermin(termindato).medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().medDokumentasjon(new Dokumentasjon.Builder()
                        .leggPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fødselsdato, termindato.minusWeeks(2).minusDays(1))))
                        //Starter med pleiepenger
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN))
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), termindato)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void utsettelse_pga_sykdom_før_søknad_mottatt_dato_skal_innvilges() {
        //Mottatt dato skal ikke være relevant for utsettelse første 6 ukene hvis det er dokumentert
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var utsettelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(6).minusDays(1),
                PeriodeVurderingType.PERIODE_OK, SYKDOM_SKADE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(8), null);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .medSøknad(fødselSøknad()
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4).minusDays(1)))
                        .leggTilOppgittPeriode(utsettelse)
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggPeriodeMedSykdomEllerSkade(new PeriodeMedSykdomEllerSkade(utsettelse.getFom(), utsettelse.getTom()))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    //FAGSYSTEM-151437
    @Disabled
    @Test
    void utsettelse_skal_ikke_avslås_pga_periode_før_gyldig_dato_men_gå_til_manuell() {
        //TODO fritt uttak
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .medRettOgOmsorg(bareFarRett())
                .medSøknad(søknad(FØDSEL, OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8),
                        PeriodeVurderingType.IKKE_VURDERT, ARBEID, fødselsdato.plusWeeks(80), fødselsdato.plusWeeks(80),
                        MorsAktivitet.UTDANNING)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
    }

    //TODO fritt uttak. Hvilke caser kan må gå tom for dager ved avslag utsettelse? Kanskje bare far har rett og utsettelse uten årsak
    @Disabled
    @Test
    void avslag_utsettelse_med_trekkdager_skal_knekkes_når_saldo_går_tom() {
        var fødselsdato = LocalDate.of(2021, 1, 20);
        //Skal få avslag pga mor ikke er i aktivitet
        var fom = fødselsdato.plusWeeks(6);
        var tom = fødselsdato.plusWeeks(9);
        var dokumentasjon = new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_DOKUMENTERT));
        //Skal gå tom for dager
        var utsettelse = OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.PERIODE_OK,
                ARBEID, fødselsdato, fødselsdato, MorsAktivitet.ARBEID);
        basicGrunnlagFar(fødselsdato)
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(bareFarRett())
                .medKontoer(new Kontoer.Builder().leggTilKonto(new Konto.Builder().medTrekkdager(10).medType(FORELDREPENGER)))
                .medSøknad(new Søknad.Builder().medType(FØDSEL)
                        .leggTilOppgittPeriode(utsettelse)
                .medDokumentasjon(dokumentasjon));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fom);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fom.plusWeeks(2).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fom.plusWeeks(2));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(tom);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    //TODO fritt uttak. Lage tester på bare far har rett. Avhenger av om det løses med utsettelse uten årsak eller ikke

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().medFødsel(fødselsdato);
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato) {
        return basicUtsettelseGrunnlag(fødselsdato, morBehandling());
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato, Behandling.Builder behandling) {
        return grunnlag.medDatoer(datoer(fødselsdato)).medBehandling(behandling).medRettOgOmsorg(beggeRett());
    }

    private Søknad.Builder fødselSøknad() {
        return new Søknad.Builder().medType(FØDSEL);
    }
}
