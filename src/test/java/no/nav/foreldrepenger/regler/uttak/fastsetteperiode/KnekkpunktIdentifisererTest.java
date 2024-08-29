package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Medlemskap;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.Pleiepenger;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.PleiepengerPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.Ytelser;

class KnekkpunktIdentifisererTest {

    @Test
    void skal_finne_knekkpunkter_for_søknad_ved_fødsel() {
        var fødselsdato = LocalDate.of(2018, 2, 22);
        var grunnlag = RegelGrunnlagTestBuilder.create().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)).datoer(datoer(fødselsdato)).build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).containsOnly(fødselsdato.minusWeeks(12), //tidligste mulige uttak
            fødselsdato.minusWeeks(3),  //foreldrepenger før fødsel
            fødselsdato, fødselsdato.plusWeeks(6),   //slutt på periode reservert mor
            fødselsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    void skal_finne_knekkpunkter_for_opphørsdato_for_medlemskap() {
        var fødselsdato = LocalDate.of(2018, 2, 22);
        var opphørsdato = fødselsdato.plusWeeks(1);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
            .datoer(datoer(fødselsdato))
            .medlemskap(new Medlemskap.Builder().opphørsdato(opphørsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(opphørsdato);
    }

    @Test
    void skal_finne_knekkpunkter_ved_adopsjon() {
        var adopsjonsdato = LocalDate.of(2018, 2, 22);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON))
            .datoer(new Datoer.Builder().omsorgsovertakelse(adopsjonsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).containsOnly(adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
            adopsjonsdato, adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    void skal_lage_knekkpunkt_ved_start_og_dagen_etter_periode_medslutt_av_() {
        var adopsjonsdato = LocalDate.of(2018, 2, 22);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON))
            .datoer(new Datoer.Builder().omsorgsovertakelse(adopsjonsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).containsOnly(adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
            adopsjonsdato,

            adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart1() {
        final var uttakStartdato = LocalDate.of(2018, 6, 1);
        var fødselsdato = uttakStartdato.minusMonths(1);
        var førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        var knekkdato = uttakStartdato.plusDays(1);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .annenPart(
                new AnnenPart.Builder().uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(uttakStartdato, uttakStartdato.plusDays(10)).build()))
            .søknad(new Søknad.Builder().oppgittPeriode(
                OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, knekkdato, uttakStartdato.plusDays(10), SamtidigUttaksprosent.TEN,
                    true, null, null, null, null, null)))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(uttakStartdato, knekkdato);
    }

    @Test
    void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart2() {
        final var uttakStartdato = LocalDate.of(2018, 10, 1);
        var fødselsdato = uttakStartdato.minusWeeks(7);
        var førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        var knekkdato = uttakStartdato.plusDays(1);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(uttakStartdato.plusDays(1), uttakStartdato.plusDays(10)).build()))
            .søknad(new Søknad.Builder().oppgittPeriode(
                OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, uttakStartdato, knekkdato, SamtidigUttaksprosent.TEN, true, null,
                    null, null, null, null)))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(knekkdato, uttakStartdato, uttakStartdato.plusDays(11), knekkdato.plusDays(1));
    }

    @Test
    void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperMidtI() {
        final var uttakStartdato = LocalDate.of(2018, 10, 1);
        var fødselsdato = uttakStartdato.minusWeeks(7);
        var førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                AnnenpartUttakPeriode.Builder.uttak(uttakStartdato.plusDays(1), uttakStartdato.plusDays(5)).build()))
            .søknad(new Søknad.Builder().oppgittPeriode(
                OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, uttakStartdato, uttakStartdato.plusDays(6), SamtidigUttaksprosent.TEN,
                    true, null, null, null, null, null)))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(uttakStartdato, uttakStartdato.plusDays(7), uttakStartdato.plusDays(1),
            uttakStartdato.plusDays(6));
    }

    @Test
    void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperSluttAvPeriode() {
        final var stønadsperiodeFom = LocalDate.of(2018, 10, 1);
        var stønadsperiodeTom = stønadsperiodeFom.plusDays(6);
        var fødselsdato = stønadsperiodeFom.minusWeeks(7);
        var førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);

        var annenPartPeriodeFom = stønadsperiodeFom.plusDays(4);
        var annenPartPeriodeTom = stønadsperiodeFom.plusDays(12);

        var grunnlag = RegelGrunnlagTestBuilder.create()
            .annenPart(new AnnenPart.Builder().uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(annenPartPeriodeFom, annenPartPeriodeTom).build()))
            .søknad(new Søknad.Builder().oppgittPeriode(
                OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, stønadsperiodeFom, stønadsperiodeTom, SamtidigUttaksprosent.TEN, true,
                    null, null, null, null, null)))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(stønadsperiodeFom, stønadsperiodeTom.plusDays(1), annenPartPeriodeFom,
            annenPartPeriodeTom.plusDays(1));
    }

    @Test
    void finnerKnekkPåEndringssøknadMottattdatoHvisGraderingStarterPåMottattdato() {
        var mottattdato = LocalDate.of(2018, 10, 10);
        var gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        var gradering = OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, mottattdato, mottattdato.plusWeeks(2), BigDecimal.valueOf(30), null,
            false, Set.of(gradertArbeidsforhold), mottattdato, mottattdato, null, null, null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(LocalDate.of(2018, 5, 5)))
            .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2018, 5, 5)))
            .søknad(new Søknad.Builder().oppgittPeriode(gradering))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    void finnerIkkeKnekkPåEndringssøknadMottattdatoHvisGraderingStarterEtterMottattdato() {
        var mottattdato = LocalDate.of(2018, 10, 10);
        var gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        var gradering = OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, mottattdato.plusWeeks(1), mottattdato.plusWeeks(2),
            BigDecimal.valueOf(30), null, false, Set.of(gradertArbeidsforhold), mottattdato, mottattdato, null, null, null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(LocalDate.of(2018, 5, 5)))
            .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2018, 5, 5)))
            .søknad(new Søknad.Builder().oppgittPeriode(gradering))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).isNotEmpty().doesNotContain(mottattdato);
    }

    @Test
    void finnerKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterPåMottattdato() {
        var mottattdato = LocalDate.of(2018, 10, 10);
        var utsettelse = OppgittPeriode.forUtsettelse(mottattdato, mottattdato.plusWeeks(2), UtsettelseÅrsak.ARBEID, mottattdato, mottattdato, null,
            null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(LocalDate.of(2018, 5, 5)))
            .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2018, 5, 5)))
            .søknad(new Søknad.Builder().oppgittPeriode(utsettelse))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    void finnerIkkeKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterEtterMottattdato() {
        var mottattdato = LocalDate.of(2018, 10, 10);
        var utsettelse = OppgittPeriode.forUtsettelse(mottattdato.plusWeeks(1), mottattdato.plusWeeks(2), UtsettelseÅrsak.FERIE, mottattdato,
            mottattdato, null, null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(LocalDate.of(2018, 5, 5)))
            .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2018, 5, 5)))
            .søknad(new Søknad.Builder().oppgittPeriode(utsettelse))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).isNotEmpty().doesNotContain(mottattdato);
    }

    @Test
    void skal_knekke_på_bevegelige_helligdager() {
        var fødselsdato = LocalDate.of(2019, 5, 1);
        var tom = LocalDate.of(2019, 5, 25);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forUtsettelse(fødselsdato, tom, UtsettelseÅrsak.FERIE, null, null, null, null)))
            .datoer(datoer(fødselsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(LocalDate.of(2019, 5, 17), LocalDate.of(2019, 5, 18));
    }

    @Test
    void skal_knekke_både_på_termindato_og_fødselsdato_ved_prematuruker() {
        var fødselsdato = LocalDate.of(2019, 7, 22);
        var termin = LocalDate.of(2019, 9, 23);
        var førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
            .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termin))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(termin, fødselsdato);
    }

    @Test
    void skal_knekke_på_startdato_hos_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 9, 23);
        var startdato1 = fødselsdato.plusWeeks(8);
        var startdato2 = fødselsdato.plusWeeks(10);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
            .datoer(datoer(fødselsdato))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans(), startdato1))
                .arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), startdato2)))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(startdato1, startdato2);
    }

    @Test
    void skal_ikke_knekke_på_startdato_hos_arbeidsforhold_hvis_bare_ett_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 9, 23);
        var startdato = fødselsdato.plusWeeks(8);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
            .datoer(datoer(fødselsdato))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans(), startdato)))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).isNotEmpty().doesNotContain(startdato);
    }

    @Test
    void skal_knekke_på_første_lovlige_uttaksdag_for_hver_søknadsperiode_der_første_lovlige_uttaksdag_overlapper_med_periode() {
        var fødselsdato = LocalDate.of(2020, 7, 20);
        var mottattDatoPeriode1 = LocalDate.of(2020, 11, 1);
        var mottattDatoPeriode2 = LocalDate.of(2021, 2, 1);
        var periode1 = OppgittPeriode.forVanligPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, LocalDate.of(2020, 11, 15), null, false,
            mottattDatoPeriode1, mottattDatoPeriode1, null, null, null);
        var periode2 = OppgittPeriode.forVanligPeriode(Stønadskontotype.MØDREKVOTE, LocalDate.of(2020, 11, 16), LocalDate.of(2020, 12, 15), null,
            false, mottattDatoPeriode2, mottattDatoPeriode2, null, null, null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().oppgittePerioder(List.of(periode1, periode2)).type(Søknadstype.FØDSEL))
            .datoer(datoer(fødselsdato))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(
                LocalDate.of(2020, 8, 1))//Første lovlige dato for første periode ligger midt i perioden. Skal derfor knekkes for å avlås
            .doesNotContain(LocalDate.of(2020, 11, 1)); //Første lovlige dato for andre periode ligger før start på perioden
    }

    @Test
    void skal_knekke_på_perioder_med_pleiepenger() {
        var innleggelseFom = LocalDate.of(2020, 10, 10);
        var innleggelseTom = LocalDate.of(2020, 10, 15);
        var pleiepenger = new Pleiepenger(Set.of(new PleiepengerPeriode(innleggelseFom, innleggelseTom, true)));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
            .datoer(new Datoer.Builder().fødsel(innleggelseFom))
            .ytelser(new Ytelser(pleiepenger))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(innleggelseFom, innleggelseTom.plusDays(1));
    }

    @Test
    void ikkeKnekkPåMottattDatoForFriUtsettelse() {
        var mottattdato = LocalDate.of(2018, 10, 10);
        var utsettelse = OppgittPeriode.forUtsettelse(mottattdato.minusWeeks(2), mottattdato.minusWeeks(1), UtsettelseÅrsak.FRI, mottattdato,
            mottattdato, null, null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(LocalDate.of(2018, 5, 5)))
            .søknad(new Søknad.Builder().oppgittPeriode(utsettelse))
            .behandling(new Behandling.Builder())
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).isNotEmpty().doesNotContain(mottattdato);
    }

    @Test
    void skalKnekkePåSammenhengendeUttakTom() {
        var dato = LocalDate.of(2024, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .behandling(new Behandling.Builder().sammenhengendeUttakTomDato(dato))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
            .datoer(new Datoer.Builder().fødsel(dato.minusWeeks(2)))
            .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);

        assertThat(knekkpunkter).contains(dato.plusDays(1));
    }

    private List<LocalDate> standardKnekkpunktFødsel(LocalDate fødselsdato, LocalDate førsteLovligeSøknadsperiode) {
        return List.of(fødselsdato.minusWeeks(12), //tidligste mulige uttak
            førsteLovligeSøknadsperiode,//ifbm søknadsfrist
            fødselsdato.minusWeeks(3),  //foreldrepenger før fødsel
            fødselsdato, fødselsdato.plusWeeks(6),   //slutt på periode reservert mor
            fødselsdato.plusYears(3));  //siste mulige uttak for foreldrepenger
    }


    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().fødsel(fødselsdato);
    }

}


