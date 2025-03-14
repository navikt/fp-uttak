package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_ANNEN_FORELDER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetskravArbeidPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetskravGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class FellesperiodeOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    private final LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    @Test
    void fellesperiode_mor_etter_uke_7_etter_fødsel_uten_nok_dager_blir_innvilget_med_knekk_og_avslått_periode_på_resten() {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER_FØR_FØDSEL).trekkdager(1000))
            .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(1000))
            .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(4 * 5));
        var grunnlag = basicGrunnlagMor().søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15).minusDays(1))))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).uttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(1).uttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        verifiserPeriode(resultater.get(2).uttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET,
            FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(3).uttakPeriode(), fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15).minusDays(1),
            FELLESPERIODE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    private Kontoer.Builder fellesperiodeKonto(int trekkdager) {
        return new Kontoer.Builder().konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(trekkdager));
    }

    @Test
    void fellesperiode_far_etter_uke_6_blir_innvilget_pga_oppfyller_aktivitetskravet_dokumentert() {
        var kontoer = fellesperiodeKonto(4 * 5);
        var oppgittPeriode = oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15), MORS_AKTIVITET_GODKJENT);
        var søknad = søknad(Søknadstype.FØDSEL, oppgittPeriode);
        var grunnlag = basicGrunnlagFar().søknad(søknad)
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);

        var resultater = fastsettPerioder(grunnlag);

        //Siste del av søknadsperioden blir avslått pga tom for dager
        assertThat(resultater).hasSize(2);
        verifiserPeriode(resultater.getFirst().uttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET,
            FELLESPERIODE);
    }

    @Test
    void fellesperiode_far_blir_innvilget_pga_oppfyller_aktivitetskravet_register() {
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
            null, false, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, null, null);
        var søknad = søknad(Søknadstype.FØDSEL, oppgittPeriode);
        var annenPart = new AnnenPart.Builder().aktivitetskravGrunnlag(new AktivitetskravGrunnlag(List.of(
            new AktivitetskravArbeidPeriode(oppgittPeriode.getFom().minusYears(1), oppgittPeriode.getFom().plusWeeks(1).minusDays(1),
                new BigDecimal(100)))));
        var grunnlag = basicGrunnlagFar().søknad(søknad)
            .annenPart(annenPart);

        var resultater = fastsettPerioder(grunnlag);
        assertThat(resultater).hasSize(2);

        var periode1 = resultater.getFirst().uttakPeriode();
        assertThat(periode1.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(periode1.getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_FELLESPERIODE_TIL_FAR);
        assertThat(periode1.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(periode1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var periode2 = resultater.get(1).uttakPeriode();
        assertThat(periode2.getPerioderesultattype()).isEqualTo(AVSLÅTT); //Ikke nok stillingsprosent på mor
        assertThat(periode2.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT);
        assertThat(periode2.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO); //Trekker ikke dager fritt uttak
        assertThat(periode2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    void fellesperiode_far_blir_avslått_hvis_oppfyller_aktivitetskravet_register_men_har_annen_dokvurdering() {
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), null,
            false, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, null, DokumentasjonVurdering.MORS_AKTIVITET_IKKE_GODKJENT);
        var søknad = søknad(Søknadstype.FØDSEL, oppgittPeriode);
        var annenPart = new AnnenPart.Builder().aktivitetskravGrunnlag(new AktivitetskravGrunnlag(
            List.of(new AktivitetskravArbeidPeriode(oppgittPeriode.getFom().minusYears(1), oppgittPeriode.getFom().plusYears(1).minusDays(1), 100))));
        var grunnlag = basicGrunnlagFar().søknad(søknad).annenPart(annenPart);

        var resultater = fastsettPerioder(grunnlag);
        assertThat(resultater).hasSize(1);

        var periode1 = resultater.getFirst().uttakPeriode();
        assertThat(periode1.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(periode1.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);
    }

    @Test
    void fellesperiode_far_blir_innvilget_pga_oppfyller_aktivitetskravet_register_mor_flere_arbeidsforhold() {
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), null,
            false, fødselsdato, fødselsdato, MorsAktivitet.ARBEID, null, null);
        var søknad = søknad(Søknadstype.FØDSEL, oppgittPeriode);
        var annenPart = new AnnenPart.Builder().aktivitetskravGrunnlag(new AktivitetskravGrunnlag(
            List.of(new AktivitetskravArbeidPeriode(oppgittPeriode.getFom().minusYears(1), oppgittPeriode.getTom().plusYears(1), 40),
                new AktivitetskravArbeidPeriode(oppgittPeriode.getFom().minusYears(1), oppgittPeriode.getTom().plusYears(1), 40))));
        var grunnlag = basicGrunnlagFar().søknad(søknad).annenPart(annenPart);

        var resultater = fastsettPerioder(grunnlag);
        assertThat(resultater).hasSize(1);

        var periode1 = resultater.getFirst().uttakPeriode();
        assertThat(periode1.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(periode1.getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_FELLESPERIODE_TIL_FAR);
        assertThat(periode1.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
        assertThat(periode1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void for_tidlig_fellesperiode_far_blir_knekt_og_må_behandles_manuelt() {
        var periode = oppgittPeriode(FELLESPERIODE, fødselsdato.minusWeeks(5), fødselsdato.plusWeeks(1), false, null,
            INNLEGGELSE_ANNEN_FORELDER_GODKJENT);
        var grunnlag = basicGrunnlagFar().søknad(søknad(Søknadstype.FØDSEL, periode));

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(3);
        verifiserManuellBehandlingPeriode(resultater.get(0).uttakPeriode(), fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(3).minusDays(1),
            FELLESPERIODE, IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL, Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL);
        verifiserManuellBehandlingPeriode(resultater.get(1).uttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), FELLESPERIODE,
            IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL, Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(2).uttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1), Perioderesultattype.INNVILGET, FELLESPERIODE);
    }

    @Test
    void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_etter_3_uker_før_fødsel_og_blir_innvilget() {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER_FØR_FØDSEL).trekkdager(3 * 5))
            .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(15 * 5))
            .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(16 * 5));

        var grunnlag = basicGrunnlagMor().søknad(
                søknad(Søknadstype.FØDSEL, oppgittPeriode(FELLESPERIODE, fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(1).minusDays(1)),
                    oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(1), fødselsdato.minusDays(1)),
                    oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).uttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1),
            Perioderesultattype.INNVILGET, FELLESPERIODE);
        verifiserPeriode(resultater.get(1).uttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(1).minusDays(1),
            Perioderesultattype.INNVILGET, FELLESPERIODE);
        verifiserPeriode(resultater.get(2).uttakPeriode(), fødselsdato.minusWeeks(1), fødselsdato.minusDays(1), Perioderesultattype.INNVILGET,
            FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(3).uttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), Perioderesultattype.INNVILGET,
            MØDREKVOTE);
    }

    @Test
    void fellesperiode_mor_uttak_starter_ved_3_uker_etter_fødsel_blir_knekt_ved_6_uker_og_må_behandles_manuelt() {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().trekkdager(15).type(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))
            .konto(new Konto.Builder().trekkdager(50).type(Stønadskontotype.MØDREKVOTE))
            .konto(new Konto.Builder().trekkdager(13 * 5).type(Stønadskontotype.FELLESPERIODE));
        var søknad = søknad(Søknadstype.FØDSEL, oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10).minusDays(1)));
        var grunnlag = basicGrunnlagMor().søknad(søknad).kontoer(kontoer);

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(3);
        verifiserAvslåttPeriode(resultater.get(0).uttakPeriode(), fødselsdato, fødselsdato.plusWeeks(3).minusDays(3), MØDREKVOTE,
            IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        verifiserManuellBehandlingPeriode(resultater.get(1).uttakPeriode(), fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1),
            FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserPeriode(resultater.get(2).uttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
            Perioderesultattype.INNVILGET, FELLESPERIODE);
    }

    @Test
    void fellesperiode_mor_uttak_starter_før_12_uker_blir_avslått_med_knekk_ved_12_uker_før_fødsel() {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(1000))
            .konto(new Konto.Builder().type(Stønadskontotype.FELLESPERIODE).trekkdager(13 * 5));
        var grunnlag = basicGrunnlagMor().søknad(
            søknad(Søknadstype.FØDSEL, oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.minusWeeks(13), fødselsdato))).kontoer(kontoer);

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(5);
        verifiserPeriode(resultater.get(0).uttakPeriode(), fødselsdato.minusWeeks(13), fødselsdato.minusWeeks(12).minusDays(1),
            Perioderesultattype.AVSLÅTT, Stønadskontotype.FELLESPERIODE);
        verifiserPeriode(resultater.get(1).uttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1),
            Perioderesultattype.INNVILGET, Stønadskontotype.FELLESPERIODE);
        verifiserPeriode(resultater.get(2).uttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), Perioderesultattype.INNVILGET,
            Stønadskontotype.FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(3).uttakPeriode(), fødselsdato, fødselsdato, FELLESPERIODE, null,
            Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserAvslåttPeriode(resultater.get(4).uttakPeriode(), fødselsdato.plusDays(1), fødselsdato.plusWeeks(6).minusDays(3), MØDREKVOTE,
            IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
    }

    @Test
    void innvilge_fellesperiode_14_uker_før_fødsel_men_ikke_12_uker_før_termin_ved_terminsøknad() {
        var termin = LocalDate.of(2020, 6, 10);
        var grunnlag = basicGrunnlagMor().datoer(new Datoer.Builder().termin(termin).fødsel(termin.plusWeeks(2)))
            .søknad(new Søknad.Builder().type(Søknadstype.TERMIN)
                .oppgittePerioder(List.of(
                    OppgittPeriode.forVanligPeriode(FELLESPERIODE, termin.minusWeeks(15), termin.minusWeeks(3).minusDays(1), null, false, null, null,
                        null, null, null),
                    OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, termin.minusWeeks(3), termin.minusDays(1), null, false, null, null,
                        null, null, null),
                    OppgittPeriode.forVanligPeriode(MØDREKVOTE, termin, termin.plusWeeks(4), null, false, null, null, null, null, null))));

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultater.getFirst().uttakPeriode().getFom()).isEqualTo(termin.minusWeeks(15));
        assertThat(resultater.getFirst().uttakPeriode().getTom()).isEqualTo(termin.minusWeeks(12).minusDays(1));
        assertThat(resultater.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultater.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(resultater.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void avslå_fellesperiode_mer_enn_12_uker_før_fødsel_ved_fødselsøknad() {
        var termin = LocalDate.of(2020, 6, 10);
        var grunnlag = basicGrunnlagMor().datoer(new Datoer.Builder().termin(termin).fødsel(termin.plusWeeks(2)))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(
                    OppgittPeriode.forVanligPeriode(FELLESPERIODE, termin.minusWeeks(12), termin.minusWeeks(3).minusDays(1), null, false, null, null,
                        null, null, null),
                    OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, termin.minusWeeks(3), termin.minusDays(1), null, false, null, null,
                        null, null, null),
                    OppgittPeriode.forVanligPeriode(MØDREKVOTE, termin, termin.plusWeeks(4), null, false, null, null, null, null, null))));

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    private RegelGrunnlag.Builder basicGrunnlagMor() {
        return basicGrunnlag(fødselsdato).behandling(morBehandling());
    }

    private RegelGrunnlag.Builder basicGrunnlagFar() {
        return basicGrunnlag(fødselsdato).behandling(farBehandling());
    }
}
