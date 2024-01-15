package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.HV_OVELSE_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_BARN_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_SØKER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_IKKE_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.NAV_TILTAK_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.SYKDOM_SØKER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.FERIE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.HV_OVELSE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_BARN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_SØKER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.NAV_TILTAK;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.SYKDOM_SKADE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.EndringAvStilling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class UtsettelseSammenhengendeUttakOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void periode_med_gyldig_utsettelse_pga_barn_innlagt_i_helseinstitusjon_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        INNLAGT_BARN, INNLEGGELSE_BARN_GODKJENT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_gyldig_utsettelse_pga_søker_innlagt_i_helseinstitusjon_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        INNLAGT_SØKER, INNLEGGELSE_SØKER_GODKJENT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_SØKER);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_gyldig_utsettelse_pga_søkers_sykdom_eller_skade_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        SYKDOM_SKADE, SYKDOM_SØKER_GODKJENT)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_gyldig_utsettelse_pga_arbeid_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var utsettelseFom = fødselsdato.plusWeeks(10);
        var utsettelseTom = fødselsdato.plusWeeks(12).minusDays(1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 75))
                .konto(konto(FEDREKVOTE, 75))
                .konto(konto(FELLESPERIODE, 80));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato)
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold).endringAvStilling(new EndringAvStilling(fødselsdato, BigDecimal.valueOf(100))))
                .kontoer(kontoer)
                .søknad(fødselSøknad().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, utsettelseFom.minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, ARBEID, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(ARBEID);
        assertThat(uttakPeriode.getFom()).isEqualTo(utsettelseFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(utsettelseTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(arbeidsforhold.identifikator())).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void UT1110_periode_med_utsettelse_pga_arbeid_med_50_prosent_stilling_skal_manuell_behandles() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var utsettelseFom = fødselsdato.plusWeeks(10);
        var utsettelseTom = fødselsdato.plusWeeks(12).minusDays(1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 75))
                .konto(konto(FELLESPERIODE, 80))
                .konto(konto(FEDREKVOTE, 75));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).kontoer(kontoer)
                .søknad(fødselSøknad().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, utsettelseFom.minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, ARBEID, null)))
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold).endringAvStilling(new EndringAvStilling(utsettelseFom, BigDecimal.valueOf(50))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(ARBEID);
        assertThat(uttakPeriode.getFom()).isEqualTo(utsettelseFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(utsettelseTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_HELTIDSARBEID);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    void periode_med_utsettelse_pga_ferie_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void skal_ikke_utlede_stønadskontotype_ved_innvilgelse_av_utsettelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getStønadskontotype()).isNull();
    }

    @Test
    void periode_med_utsettelse_pga_ferie_skal_til_manuell_behandling_grunnet_bevegelige_helligdager() {
        var fødselsdato = LocalDate.of(2018, 1, 15);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(8);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        // 26.03 - 28.03 er en periode grunnet helligdag den 29.03
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(2));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        //2 neste uker med ugyldig utsettelse grunnet bevegelig helligdag

        // 29.03 - 29.03 er en periode fordi 29.mars er skjærtorsdag
        uttakPeriode = resultat.get(4).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 30.03 - 30.03 er en periode fordi 30.mars er en helligdag
        uttakPeriode = resultat.get(5).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 02.04 - 02.04 er en periode fordi 02.april er 2.påskedag
        uttakPeriode = resultat.get(6).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 03.04 - 08.04 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(7).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(1));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(6));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void flere_perioder_med_utsettelse_pga_ferie_skal_til_manuell_behandling_grunnet_bevegelige_helligdager() {
        var fødselsdato = LocalDate.of(2018, 1, 15);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), FERIE, null))
                .oppgittPeriode(
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(17).minusDays(1)))
                .oppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(17), fødselsdato.plusWeeks(18).minusDays(1), FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(12);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        // 26.03 - 28.03 er en periode grunnet helligdag den 29.03
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(2));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        //2 neste uker med ugyldig utsettelse grunnet bevegelig helligdag

        // 29.03 - 29.03 er en periode fordi 29.mars er skjærtorsdag
        uttakPeriode = resultat.get(4).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 30.03 - 30.03 er en periode fordi 30.mars er en helligdag (langfredag)
        uttakPeriode = resultat.get(5).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 02.04 - 02.04 er en periode fordi 02.april er 2.påskedag
        uttakPeriode = resultat.get(6).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 03.04 - 08.04 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(7).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(1));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        // 09.04 - 13.05 er en periode fordi det er søkt om vanlig fellesperiode, ingen utsettelse
        uttakPeriode = resultat.get(8).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(12));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(25));

        // 14.05 - 16.05 er en periode grunnet helligdag den 17.05
        uttakPeriode = resultat.get(9).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(2));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        // 17.05 - 17.05 er en periode pga 17.mai
        uttakPeriode = resultat.get(10).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(3));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 18.05 - 20.05 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(11).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(4));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(18).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void periode_med_ugyldig_utsettelse_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        INNLAGT_BARN, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med forsøkt utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    void utsettelse_uten_stønadskonto_på_helligdag_skal_gi_ugyldig_opphold() {
        var fødselsdato = LocalDate.of(2018, 11, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, LocalDate.of(2018, 12, 24)))
                .oppgittPeriode(
                        utsettelsePeriode(LocalDate.of(2018, 12, 25), LocalDate.of(2018, 12, 25), FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //2 neste uker med gyldig utsettelse
        var utsettelse = resultat.get(3).uttakPeriode();
        assertThat(utsettelse.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(utsettelse.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(utsettelse.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        //Utsettelse periode med UKJENT stønadskontotype er settes til neste tilgjengelige stønadskontotype
        assertThat(utsettelse.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(utsettelse.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));
    }

    @Test
    void utsettelse_periode_med_ukjent_kontotype_må_settes_til_neste_tilgjengelig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(fødselSøknad().oppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        SYKDOM_SKADE, null)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med forsøkt utsettelse
        var uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(SYKDOM_SKADE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    void pleiepenger_utsettelse_skal_trekke_fra_fellesperiode() {
        //Over 7 uker for tidlig, får pleiepenger. Utsettelsen skal avlås og det skal trekkes dager
        var termindato = LocalDate.of(2019, 9, 1);
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).datoer(new Datoer.Builder().termin(termindato).fødsel(fødselsdato))
                .søknad(fødselSøknad()
                        //Starter med pleiepenger
                        .oppgittPeriode(utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN, INNLEGGELSE_BARN_GODKJENT))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), termindato)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FELLESPERIODE);
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
                        .oppgittPeriode(
                                utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNLAGT_BARN, INNLEGGELSE_BARN_GODKJENT))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), termindato)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getUtsettelseÅrsak()).isEqualTo(INNLAGT_BARN);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skal_trekke_fra_mødrekvote_ved_avslag_ferie_innenfor_første_seks_uker() {
        //Søkt om ferieutsettelse innenfor seks uker etter fødsel. Utsettelsen skal avlås og det skal trekkes dager fra mødrekvote
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(fødselSøknad().oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4)))
                        .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4).plusDays(1), fødselsdato.plusWeeks(8),
                                FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).uttakPeriode().getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isNotEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    void skal_trekke_fra_foreldrepengekvote_ved_avslag_ferie_innenfor_første_seks_uker_ved_aleneomsorg_far() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = aleneomsorgUtsettelseGrunnlag(fødselsdato, farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(fødselSøknad().oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(4)))
                        .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4).plusDays(1), fødselsdato.plusWeeks(8),
                                FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).uttakPeriode().getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isNotEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skal_trekke_fra_foreldrepengekvote_ved_avslag_ferie_innenfor_første_seks_uker_ved_aleneomsorg_mor() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = aleneomsorgUtsettelseGrunnlag(fødselsdato, morBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(fødselSøknad().oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(4)))
                        .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4).plusDays(1), fødselsdato.plusWeeks(8),
                                FERIE, null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).uttakPeriode().getUtsettelseÅrsak()).isEqualTo(FERIE);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isNotEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void utsettelse_før_søknad_mottatt_dato_skal_innvilges() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var utsettelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10),
            FERIE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(8), null, null);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(
                fødselSøknad().oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(utsettelse));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void skal_bruke_tidligst_mottatt_dato_når_søknadsfrist_vurderes() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var tidligstMottattDato = fødselsdato;
        var senestMottattDato = fødselsdato.plusWeeks(8);
        var utsettelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10),
            FERIE, senestMottattDato, tidligstMottattDato, null, null);
        var grunnlag = basicUtsettelseGrunnlag(fødselsdato).søknad(
                fødselSøknad().oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(utsettelse));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void utsettelse_ved_tomme_dager_skal_også_avslås() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var uttakAvsluttetMedUtsettelse = basicUtsettelseGrunnlag(fødselsdato).søknad(
                fødselSøknad().oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                        .oppgittPeriode(
                                oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(36).minusDays(1)))
                        .oppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(36), fødselsdato.plusWeeks(37).minusDays(1),
                                FERIE, null))
                        .oppgittPeriode(
                                utsettelsePeriode(fødselsdato.plusWeeks(37), fødselsdato.plusWeeks(100), ARBEID, null)))
                .build();

        var resultat = fastsettPerioder(uttakAvsluttetMedUtsettelse);

        assertThat(resultat).hasSize(5);
        assertThat(resultat.get(3).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(3).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE);

        assertThat(resultat.get(4).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(4).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void skal_knekke_riktig_på_helligdager_i_jula_ved_utsettelse_pga_ferie() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                                .build()))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(
                                utsettelsePeriode(fødselsdato.plusWeeks(6), LocalDate.of(2020, 1, 1), FERIE, null)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);
        //Før jul ok
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //25
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        //26
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        //Romjul
        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //1
        assertThat(perioder.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void utsettelse_pga_heimevernet_skal_gå_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var hvFom = fødselsdato.plusWeeks(6);
        var hvTom = fødselsdato.plusWeeks(7).minusDays(1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, hvFom.minusDays(1))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                                .build()))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(utsettelsePeriode(hvFom, hvTom, HV_OVELSE, null)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
    }

    @Test
    void utsettelse_pga_heimevernet_skal_innvilges_hvis_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var hvFom = fødselsdato.plusWeeks(6);
        var hvTom = fødselsdato.plusWeeks(7).minusDays(1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, hvFom.minusDays(1))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                                .build()))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(utsettelsePeriode(hvFom, hvTom, HV_OVELSE, HV_OVELSE_GODKJENT)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void utsettelse_pga_heimevernet_skal_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var hvFom = fødselsdato.plusWeeks(6);
        var hvTom = fødselsdato.plusWeeks(7).minusDays(1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fødselsdato, hvFom.minusDays(1))
                    .innvilget(true)
                    .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                            Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                    .build()))
            .søknad(new Søknad.Builder().type(FØDSEL)
                .oppgittPeriode(utsettelsePeriode(hvFom, hvTom, HV_OVELSE, null)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void utsettelse_pga_tiltak_i_regi_av_nav_skal_gå_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var tiltakFom = fødselsdato.plusWeeks(6);
        var tiltakTom = fødselsdato.plusWeeks(7).minusDays(1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, tiltakFom.minusDays(1))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                                .build()))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(utsettelsePeriode(tiltakFom, tiltakTom, NAV_TILTAK, null)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
    }

    @Test
    void utsettelse_pga_tiltak_i_regi_av_nav_skal_innvilges_hvis_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var tiltakFom = fødselsdato.plusWeeks(6);
        var tiltakTom = fødselsdato.plusWeeks(7).minusDays(1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, tiltakFom.minusDays(1))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                                .build()))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(utsettelsePeriode(tiltakFom, tiltakTom, NAV_TILTAK, NAV_TILTAK_GODKJENT)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void utsettelse_pga_tiltak_i_regi_av_nav_skal_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var tiltakFom = fødselsdato.plusWeeks(6);
        var tiltakTom = fødselsdato.plusWeeks(7).minusDays(1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(fødselsdato, tiltakFom.minusDays(1))
                    .innvilget(true)
                    .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                    .build()))
            .søknad(new Søknad.Builder().type(FØDSEL)
                .oppgittPeriode(utsettelsePeriode(tiltakFom, tiltakTom, NAV_TILTAK, null)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void avslag_utsettelse_med_trekkdager_skal_knekkes_når_saldo_går_tom() {
        var fødselsdato = LocalDate.of(2021, 1, 20);
        //Skal få avslag pga mor ikke er i aktivitet
        var fom = fødselsdato.plusWeeks(6);
        var tom = fødselsdato.plusWeeks(9);
        //Skal gå tom for dager
        var utsettelse = OppgittPeriode.forUtsettelse(fom, tom, ARBEID, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, MORS_AKTIVITET_IKKE_GODKJENT);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder().konto(new Konto.Builder().trekkdager(10).type(FORELDREPENGER)))
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(utsettelse));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fom);
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fom.plusWeeks(2).minusDays(1));
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fom.plusWeeks(2));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(tom);
        assertThat(perioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void bfhr_utsettelse_som_trenger_dok_og_aktivitetskrav_vurdering_skal_gå_til_manuell() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var periode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
            null, false, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, MORS_AKTIVITET_GODKJENT);
        var sykdom = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(11).minusDays(1),
            SYKDOM_SKADE, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, SYKDOM_SØKER_GODKJENT);

        var innleggelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(11), fødselsdato.plusWeeks(12).minusDays(1),
            INNLAGT_SØKER, fødselsdato, fødselsdato, MorsAktivitet.UTDANNING, INNLEGGELSE_SØKER_GODKJENT);

        var sykdomBarn = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(13).minusDays(1),
            INNLAGT_BARN, fødselsdato, fødselsdato, MorsAktivitet.INTROPROG, INNLEGGELSE_BARN_GODKJENT);

        var grunnlag = basicGrunnlagFar(fødselsdato)
            .kontoer(new Kontoer.Builder().konto(konto(FORELDREPENGER, 100)))
            .rettOgOmsorg(bareFarRett())
            .søknad(søknad(FØDSEL, periode, sykdom, innleggelse, sykdomBarn));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(2).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);

        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(3).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    private void assertDeTreFørstePeriodene(List<FastsettePeriodeResultat> resultat, LocalDate fødselsdato) {
        //3 uker før fødsel - innvilges
        var uttakPeriode = resultat.get(0).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().fødsel(fødselsdato);
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato) {
        return basicUtsettelseGrunnlag(fødselsdato, morBehandling());
    }

    private RegelGrunnlag.Builder aleneomsorgUtsettelseGrunnlag(LocalDate fødselsdato, Behandling.Builder behandling) {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(130));
        return basicGrunnlag().datoer(datoer(fødselsdato)).behandling(behandling).kontoer(kontoer).rettOgOmsorg(aleneomsorg());
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato, Behandling.Builder behandling) {
        return basicGrunnlag().datoer(datoer(fødselsdato)).behandling(behandling).rettOgOmsorg(beggeRett());
    }

    private Søknad.Builder fødselSøknad() {
        return new Søknad.Builder().type(FØDSEL);
    }

    @Override
    Behandling.Builder morBehandling() {
        return super.morBehandling().kreverSammenhengendeUttak(true);
    }

    @Override
    Behandling.Builder farBehandling() {
        return super.farBehandling().kreverSammenhengendeUttak(true);
    }
}
