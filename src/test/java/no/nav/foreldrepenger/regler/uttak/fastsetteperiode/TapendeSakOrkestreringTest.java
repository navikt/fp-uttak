package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class TapendeSakOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    private static final AktivitetIdentifikator MOR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;

    private final LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    @Test
    void skal_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_innvilget_samme_tidsrom() {
        /*
           Far søker fedrekvote samtidig som mor tar fellesperiode. Far har ikke omsorg i denne perioden og skal få avslag og egentlig trukket dager.
           Skal ikke trekke dager siden mor har innvilget i samme tidsrom
         */
        var periodeUtenOmsorg = new PeriodeUtenOmsorg(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                                MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(
                                annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD,
                                        true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16),
                                MOR_ARBEIDSFORHOLD, true)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(periodeUtenOmsorg)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isZero();
    }

    @Test
    void skal_ikke_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_avslått_samme_tidsrom() {
        var periodeUtenOmsorg = new PeriodeUtenOmsorg(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                                MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(
                                annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD,
                                        true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16),
                                MOR_ARBEIDSFORHOLD, false)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(periodeUtenOmsorg)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
    }

    @Test
    void skal_ikke_tape_hvis_søknad_mottatt_i_etterkant() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD,
                                true, fødselsdato.plusWeeks(10)))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                                fødselsdato.plusWeeks(10))))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13),
                                null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skal_tape_hvis_søknad_mottatt_i_forkant() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD,
                                true, fødselsdato.plusWeeks(14)))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                                fødselsdato.plusWeeks(14))))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13),
                                null)));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void skal_tape_hvis_mor_søker_etter_far_samme_dag() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD,
                                true, fødselsdato.plusWeeks(13)))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                                fødselsdato.plusWeeks(13)))
                        .medSisteSøknadMottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(12, 12, 12)))
                )
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13),
                                null))
                .medMottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(10, 10, 10))));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void skal_ikke_tape_hvis_mor_søker_før_far_samme_dag() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD,
                                true, fødselsdato.plusWeeks(13)))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                                fødselsdato.plusWeeks(13)))
                        .medSisteSøknadMottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(10, 10, 10)))
                )
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13),
                                null))
                        .medMottattTidspunkt(fødselsdato.plusWeeks(13).atTime(LocalTime.of(12, 12, 12))));

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skal_tape_hvis_far_har_søkt_etter_mor_men_det_er_berørt_behandling() {
        //Søkt samme dag, men mor har søkt etter far
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), MOR_ARBEIDSFORHOLD,
                                true, fødselsdato.plusWeeks(10)))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12), MOR_ARBEIDSFORHOLD, true,
                                fødselsdato.plusWeeks(10)))
                )
                .medBehandling(farBehandling().medErBerørtBehandling(true))
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(13),
                                null))
                );

        var resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
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
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitet, stønadskontotype,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), Utbetalingsgrad.TEN))
                .medInnvilget(innvilget)
                .medSenestMottattDato(senestMottattDato)
                .build();
    }

}
