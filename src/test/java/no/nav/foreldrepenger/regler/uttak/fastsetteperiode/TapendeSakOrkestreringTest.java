package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

class TapendeSakOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    private static final AktivitetIdentifikator MOR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;

    private final LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    @Test
    void skal_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_innvilget_samme_tidsrom() {
        /*
           Far søker fedrekvote samtidig som mor tar fellesperiode. Far har ikke omsorg i denne perioden og skal få avslag og egentlig trukket dager.
           Skal ikke trekke dager siden mor har innvilget i samme tidsrom
         */
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), MOR_ARBEIDSFORHOLD, true))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD, true))
                .uttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16), MOR_ARBEIDSFORHOLD, true)))
            .behandling(farBehandling())
            .rettOgOmsorg(beggeRett().harOmsorg(false))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16))));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isZero();
    }

    @Test
    void skal_ikke_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_avslått_samme_tidsrom() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), MOR_ARBEIDSFORHOLD, true))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD, true))
                .uttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16), MOR_ARBEIDSFORHOLD, false)))
            .behandling(farBehandling())
            .rettOgOmsorg(beggeRett().harOmsorg(false))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16))));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
    }

    @Test
    void skal_ikke_tape_hvis_søknad_mottatt_i_etterkant() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(10)))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(10))))
            .behandling(farBehandling())
            .rettOgOmsorg(beggeRett())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), null, false,
                    fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skal_tape_hvis_søknad_mottatt_i_forkant() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(14)))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(14))))
            .behandling(farBehandling())
            .rettOgOmsorg(beggeRett())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), null, false,
                    fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void skal_tape_hvis_mor_søker_etter_far_samme_dag() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(13)))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(13)))
                .sisteSøknadMottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(12, 12, 12))))
            .behandling(farBehandling())
            .rettOgOmsorg(beggeRett())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), null, false,
                    fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null))
                .mottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(10, 10, 10))));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void skal_ikke_tape_hvis_mor_søker_før_far_samme_dag() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(13)))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(13)))
                .sisteSøknadMottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(10, 10, 10))))
            .behandling(farBehandling())
            .rettOgOmsorg(beggeRett())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), null, false,
                    fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null))
                .mottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(12, 12, 12))));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skal_tape_hvis_far_har_søkt_etter_mor_men_det_er_berørt_behandling() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(10)))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(10))))
            .behandling(farBehandling().berørtBehandling(true))
            .rettOgOmsorg(beggeRett())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), null, false,
                    fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void skal_evaluere_normal_flyt_dersom_flerbarnsdager_far_har_søkt_etter_mor_berørt_behandling() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(10)))
                .uttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(10))))
            .behandling(farBehandling().berørtBehandling(true))
            .rettOgOmsorg(beggeRett())
            .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(5 * 15))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(5 * 16 + 5 * 17))
                .flerbarnsdager(5 * 17))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12),
                    new SamtidigUttaksprosent(100), true, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skal_evaluere_normal_flyt_dersom_flerbarnsdager_far_har_søkt_etter_mor_berørt_behandling_annen_part_samtidig() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato.plusWeeks(10)))
                .uttaksperiode(annenpartsPeriodeSamtidig(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                    fødselsdato.plusWeeks(10))))
            .behandling(farBehandling().berørtBehandling(true))
            .rettOgOmsorg(beggeRett())
            .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(5 * 15))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(5 * 16 + 5 * 17))
                .flerbarnsdager(5 * 17))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), null, true,
                    fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13), null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).uttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skal_evaluere_normal_flyt_dersom_far_rundt_fødsel_berørt_med_samtidig() {
        //Søkt samme dag, men mor har søkt etter far
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 10, 3));
        var termindato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 10, 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termindato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD, true, fødselsdato))
                .uttaksperiode(
                    annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true, fødselsdato)))
            .behandling(farBehandling().berørtBehandling(true))
            .rettOgOmsorg(beggeRett())
            .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(5 * 15))
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(5 * 15))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(5 * 16))
                .farUttakRundtFødselDager(10))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, termindato.minusWeeks(1), termindato.plusWeeks(1).minusDays(1),
                    new SamtidigUttaksprosent(100), false, fødselsdato, fødselsdato, null, null, null))
                .oppgittPeriode(
                    OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(35), fødselsdato.plusWeeks(42).minusDays(1), null, false,
                        fødselsdato, fødselsdato, null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.stream()
            .map(FastsettePeriodeResultat::uttakPeriode)
            .map(UttakPeriode::getPerioderesultattype)
            .allMatch(INNVILGET::equals)).isTrue();
    }

    @Test
    void skal_evaluere_normal_flyt_dersom_far_rundt_fødsel_mor_berørt_behandling_annen_part_samtidig() {
        //Søkt samme dag, men mor har søkt etter far
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 10, 3));
        var termindato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 10, 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termindato))
            .annenPart(new AnnenPart.Builder().uttaksperiode(
                    annenpartsSamtidigPeriode(FEDREKVOTE, termindato.minusWeeks(1), termindato.plusWeeks(1).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato))
                .uttaksperiode(
                    annenpartsSamtidigPeriode(FEDREKVOTE, fødselsdato.plusWeeks(41), fødselsdato.plusWeeks(50).minusDays(1), MOR_ARBEIDSFORHOLD, true,
                        fødselsdato)))
            .behandling(morBehandling().berørtBehandling(true))
            .rettOgOmsorg(beggeRett())
            .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(5 * 15))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(5 * 16))
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(5 * 15))
                .farUttakRundtFødselDager(10))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(
                    OppgittPeriode.forVanligPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(3), null, false, fødselsdato,
                        fødselsdato, null, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.stream()
            .map(FastsettePeriodeResultat::uttakPeriode)
            .map(UttakPeriode::getPerioderesultattype)
            .allMatch(INNVILGET::equals)).isTrue();
    }

    static AnnenpartUttakPeriode annenpartsPeriode(Stønadskontotype stønadskontotype,
                                                   LocalDate fom,
                                                   LocalDate tom,
                                                   AktivitetIdentifikator aktivitet,
                                                   boolean innvilget) {
        return annenpartsPeriode(stønadskontotype, fom, tom, aktivitet, innvilget, null);
    }

    static AnnenpartUttakPeriode annenpartsPeriode(Stønadskontotype stønadskontotype,
                                                   LocalDate fom,
                                                   LocalDate tom,
                                                   AktivitetIdentifikator aktivitet,
                                                   boolean innvilget,
                                                   LocalDate senestMottattDato) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
            .uttakPeriodeAktivitet(
                new AnnenpartUttakPeriodeAktivitet(aktivitet, stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)),
                    Utbetalingsgrad.TEN))
            .innvilget(innvilget)
            .senestMottattDato(senestMottattDato)
            .build();
    }

    static AnnenpartUttakPeriode annenpartsSamtidigPeriode(Stønadskontotype stønadskontotype,
                                                           LocalDate fom,
                                                           LocalDate tom,
                                                           AktivitetIdentifikator aktivitet,
                                                           boolean innvilget,
                                                           LocalDate senestMottattDato) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
            .uttakPeriodeAktivitet(
                new AnnenpartUttakPeriodeAktivitet(aktivitet, stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)),
                    Utbetalingsgrad.TEN))
            .innvilget(innvilget)
            .samtidigUttak(true)
            .senestMottattDato(senestMottattDato)
            .build();
    }

    static AnnenpartUttakPeriode annenpartsPeriodeSamtidig(Stønadskontotype stønadskontotype,
                                                           LocalDate fom,
                                                           LocalDate tom,
                                                           AktivitetIdentifikator aktivitet,
                                                           boolean innvilget,
                                                           LocalDate senestMottattDato) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
            .uttakPeriodeAktivitet(
                new AnnenpartUttakPeriodeAktivitet(aktivitet, stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)),
                    Utbetalingsgrad.TEN))
            .innvilget(innvilget)
            .senestMottattDato(senestMottattDato)
            .samtidigUttak(true)
            .build();
    }

}
