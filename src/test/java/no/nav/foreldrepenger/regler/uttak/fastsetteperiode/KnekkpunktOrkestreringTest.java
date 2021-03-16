package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class KnekkpunktOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void foreldrepengerFørFødssel_søknad_fra_3_uker_før_fødsel_til_og_med_fødsel_blir_knekt_ved_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).medTrekkdager(3 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer)
                .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(
                Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato);
    }


    @Test
    void foreldrepengerFørFødsel_periode_dekker_alle_ukene_før_fødsel_slutter_etter_fødsel_blir_hvor_ukene_etter_fødsel_blir_avslått() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).medTrekkdager(3 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.plusWeeks(4))))
                .medKontoer(kontoer)
                .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(
                Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4));
    }

    @Test
    void riktig_knekk_ved_tom_for_dager_ifm_helg() {
        var fødselsdato = LocalDate.of(2018, 5, 15);
        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).medTrekkdager(15))
                .leggTilKonto(new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(15 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1)),
                        oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, LocalDate.of(2018, 6, 30)),
                        oppgittPeriode(Stønadskontotype.MØDREKVOTE, LocalDate.of(2018, 7, 1), LocalDate.of(2018, 12, 21))))
                .medKontoer(kontoer)
                .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 6, 26));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 6, 30));

        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 7, 1));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 8, 27));

        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(4).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(4).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 8, 28));
        assertThat(perioder.get(4).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 12, 21));
    }

    @Test
    void riktig_knekk_ved_tom_for_dager_ifm_helg2() {
        var fødselsdato = LocalDate.of(2020, 9, 20);
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(Stønadskontotype.FEDREKVOTE).medTrekkdager(10));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        //Starter søndag, slutter lørdag
                        oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))))
                .medKontoer(kontoer)
                .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2020, 11, 15));
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2020, 11, 16));
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void periode_etter_knekk_skal_gå_videre() {
        var fødselsdato = LocalDate.of(2018, 5, 15);
        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).medTrekkdager(15))
                .leggTilKonto(new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(15 * 5))
                .leggTilKonto(new Konto.Builder().medType(Stønadskontotype.FELLESPERIODE).medTrekkdager(2));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1)),
                        oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15)),
                        oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(15).plusDays(1),
                                fødselsdato.plusWeeks(15).plusDays(2))))
                .medKontoer(kontoer)
                .build();

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //tom for mødrekvote
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void en_lang_manglende_søkt_før_første_uttaksperiode_skal_gå_til_manuell_når_alle_dager_brukes_opp() {
        var fødselsdato = LocalDate.of(2018, 2, 13);
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(200));
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(bareFarRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FORELDREPENGER, LocalDate.of(2019, 3, 20),
                                LocalDate.of(2019, 12, 24))))
                .medKontoer(kontoer);

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }
}
