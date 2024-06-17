package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class KnekkpunktOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void foreldrepengerFørFødssel_søknad_fra_3_uker_før_fødsel_til_og_med_fødsel_blir_knekt_ved_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).trekkdager(3 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .behandling(morBehandling())
            .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
            .søknad(søknad(Søknadstype.FØDSEL, oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato)))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer)
            .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato);
    }


    @Test
    void foreldrepengerFørFødsel_periode_dekker_alle_ukene_før_fødsel_slutter_etter_fødsel_blir_hvor_ukene_etter_fødsel_blir_avslått() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).trekkdager(3 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .behandling(morBehandling())
            .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(4))))
            .kontoer(kontoer)
            .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4));
    }

    @Test
    void riktig_knekk_ved_tom_for_dager_ifm_helg() {
        var fødselsdato = LocalDate.of(2018, 5, 15);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).trekkdager(15))
            .konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(15 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling())
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, LocalDate.of(2018, 6, 30)),
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, LocalDate.of(2018, 7, 1), LocalDate.of(2018, 12, 21))))
            .kontoer(kontoer)
            .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 6, 26));
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 6, 30));

        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).uttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 7, 1));
        assertThat(perioder.get(3).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 8, 27));

        assertThat(perioder.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(4).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(4).uttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 8, 28));
        assertThat(perioder.get(4).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 12, 21));
    }

    @Test
    void riktig_knekk_ved_tom_for_dager_ifm_helg2() {
        var fødselsdato = LocalDate.of(2020, 9, 20);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.FEDREKVOTE).trekkdager(10));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(farBehandling())
            .søknad(søknad(Søknadstype.FØDSEL,
                //Starter søndag, slutter lørdag
                oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))))
            .kontoer(kontoer)
            .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2020, 11, 15));
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(LocalDate.of(2020, 11, 16));
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void periode_etter_knekk_skal_gå_videre() {
        var fødselsdato = LocalDate.of(2018, 5, 15);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).trekkdager(15))
            .konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(15 * 5))
            .konto(new Konto.Builder().type(Stønadskontotype.FELLESPERIODE).trekkdager(2));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling())
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15)),
                oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(15).plusDays(1), fødselsdato.plusWeeks(15).plusDays(2))))
            .kontoer(kontoer)
            .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //tom for mødrekvote
        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void en_lang_manglende_søkt_før_første_uttaksperiode_skal_gå_til_manuell_når_alle_dager_brukes_opp() {
        var fødselsdato = LocalDate.of(2018, 2, 13);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200));
        var grunnlag = basicGrunnlag(fødselsdato).behandling(farBehandling())
            .rettOgOmsorg(bareFarRett())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(Stønadskontotype.FORELDREPENGER, LocalDate.of(2019, 3, 20), LocalDate.of(2019, 12, 24))))
            .kontoer(kontoer);

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void tilfelle_med_lang_overføring_sykdom() {
        var termindato = LocalDate.of(2022, 9, 24);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(95))
            .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(95))
            .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(90))
            .konto(new Konto.Builder().type(FORELDREPENGER_FØR_FØDSEL).trekkdager(15))
            .farUttakRundtFødselDager(10);
        var overføring = OppgittPeriode.forOverføring(MØDREKVOTE, LocalDate.of(2022, 9, 26), LocalDate.of(2023, 2, 3),
            OverføringÅrsak.SYKDOM_ELLER_SKADE, LocalDate.of(2022, 8, 22), LocalDate.of(2022, 8, 22),
            DokumentasjonVurdering.SYKDOM_ANNEN_FORELDER_GODKJENT);
        var grunnlag = basicGrunnlag(termindato).behandling(farBehandling())
            .rettOgOmsorg(beggeRett())
            .datoer(new Datoer.Builder().termin(termindato))
            .søknad(new Søknad.Builder().type(Søknadstype.TERMIN)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, LocalDate.of(2022, 9, 12), LocalDate.of(2022, 9, 23)))
                .oppgittPeriode(overføring))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
            .kontoer(kontoer);

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

}
