package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_3;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.create;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;

class GraderingOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    protected final FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
            .konto(konto(MØDREKVOTE, 50))
            .konto(konto(FEDREKVOTE, 50))
            .konto(konto(FELLESPERIODE, 130));
        return builder.kontoer(kontoer);
    }

    @Test
    void gradering_av_foreldrepenger_før_fødsel_skal_innvilges_med_avslått_gradering_med_fulle_trekkdager_og_redusert_utbetalingsgrad_UT1072() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var arbeidsprosent = BigDecimal.TEN;
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
            .oppgittPeriode(gradertoppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), arbeidsprosent))
            .oppgittPeriode(gradertoppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), arbeidsprosent))
            ).build();

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        var fastsattFFFPeriode = resultat.get(0).uttakPeriode();
        assertThat(fastsattFFFPeriode.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(fastsattFFFPeriode.getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(fastsattFFFPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new Utbetalingsgrad(90)); // Redusert i henhold til arbeidsprosenten 100 - 10 = 90
        assertThat(fastsattFFFPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(15)); // Fulle trekkdager
    }

    @Test
    void periode_med_gradering_og_10_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var graderingFom = fødselsdato.plusWeeks(10);
        var graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        var arbeidsprosent = BigDecimal.TEN;
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
            .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
            .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
            .oppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))).build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        var uttakPeriode = resultat.get(0).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(45));
    }

    @Test
    void når_graderingsperiode_går_tom_skal_perioden_dras_til_søkers_fordel() {
        var kontoer = new Kontoer.Builder().flerbarnsdager(17 * 5).konto(konto(FORELDREPENGER, 57 * 5));
        var søknadsperiode1 = oppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 1, 23), LocalDate.of(2019, 3, 1));
        //Søker vil gå tom for dager i løpet av 19. sept, derfor får søker en ekstra trekkdager (Søkers fordel)
        var søknadsperiode2 = gradertoppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 3, 4), LocalDate.of(2019, 9, 19), BigDecimal.valueOf(60));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(LocalDate.of(2019, 1, 23)))
            .rettOgOmsorg(aleneomsorg())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(søknadsperiode1).oppgittPeriode(søknadsperiode2))
            .build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void samtidig_uttaksprosent_skal_settes_til_100_minus_gradering_arbeidstidsprosent_hvis_samtidig_uttak_og_gradering() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var arbeidstidsprosent = BigDecimal.TEN;
        var samtidigUttaksprosent = new SamtidigUttaksprosent(50);
        var gradertMedSamtidigUttak = OppgittPeriode.forGradering(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
            arbeidstidsprosent, samtidigUttaksprosent, false, Set.of(ARBEIDSFORHOLD_1), null, null, null, null, null);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
            .konto(konto(MØDREKVOTE, 50))
            .konto(konto(FEDREKVOTE, 50))
            .konto(konto(FELLESPERIODE, 130));
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradertMedSamtidigUttak))
            .kontoer(kontoer)
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)).arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)));

        var fastsettePeriodeGrunnlag = grunnlag.build();
        var resultat = fastsettPerioder(fastsettePeriodeGrunnlag);

        assertThat(resultat.get(2).uttakPeriode().erSamtidigUttak()).isTrue();
        assertThat(resultat.get(2).uttakPeriode().getSamtidigUttaksprosent()).isEqualTo(SamtidigUttaksprosent.HUNDRED.subtract(arbeidstidsprosent));
    }

    @Test
    void uttak_på_to_arbeidsforhold_hvor_den_ene_går_tom_for_fellesperiode_skal_føre_0_utbetaling_og_0_trekkdager_for_den_som_går_tom() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var prosent50 = new BigDecimal("50.00");
        var grunnlag = RegelGrunnlagTestBuilder.create();
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
            .konto(konto(MØDREKVOTE, 15 * 5))
            .konto(konto(FEDREKVOTE, 15 * 5))
            .konto(konto(FELLESPERIODE, 16 * 5));
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1)))
                .oppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(33).minusDays(1), prosent50,
                    Set.of(ARBEIDSFORHOLD_1))))
            .kontoer(kontoer)
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)).arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)));

        var fastsettePeriodeGrunnlag = grunnlag.build();
        var resultat = fastsettPerioder(fastsettePeriodeGrunnlag);


        assertThat(resultat).hasSize(5);
        assertKontoOgResultat(resultat.get(0), FORELDREPENGER_FØR_FØDSEL, INNVILGET);
        assertTrekkdager(resultat.get(0), ARBEIDSFORHOLD_1, new Trekkdager(3 * 5));
        assertTrekkdager(resultat.get(0), ARBEIDSFORHOLD_2, new Trekkdager(3 * 5));
        assertKontoOgResultat(resultat.get(1), MØDREKVOTE, INNVILGET);
        assertTrekkdager(resultat.get(1), ARBEIDSFORHOLD_1, new Trekkdager(6 * 5));
        assertTrekkdager(resultat.get(1), ARBEIDSFORHOLD_2, new Trekkdager(6 * 5));
        assertKontoOgResultat(resultat.get(2), MØDREKVOTE, INNVILGET);
        assertTrekkdager(resultat.get(2), ARBEIDSFORHOLD_1, new Trekkdager(9 * 5));
        assertTrekkdager(resultat.get(2), ARBEIDSFORHOLD_2, new Trekkdager(9 * 5));
        assertKontoOgResultat(resultat.get(3), FELLESPERIODE, INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isNull();
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_1, new Trekkdager((16 * 5) / 2));
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_2, new Trekkdager(16 * 5));
        assertKontoOgResultat(resultat.get(4), FELLESPERIODE, INNVILGET);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_1, new Trekkdager((2 * 5) / 2));
        assertThat(resultat.get(4).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(Utbetalingsgrad.ZERO);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_2, Trekkdager.ZERO);
    }

    @Test
    void uttak_på_to_arbeidsforhold_hvor_den_ene_går_tom_for_mødrekvote_skal_føre_0_utbetaling_og_0_trekkdager_for_den_som_går_tom() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var prosent50 = new BigDecimal("50.00");
        var grunnlag = RegelGrunnlagTestBuilder.create();
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
            .konto(konto(MØDREKVOTE, 15 * 5))
            .konto(konto(FEDREKVOTE, 15 * 5))
            .konto(konto(FELLESPERIODE, 16 * 5));
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)).arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
            .kontoer(kontoer)
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(33).minusDays(1), prosent50,
                    Set.of(ARBEIDSFORHOLD_1))));

        var fastsettePeriodeGrunnlag = grunnlag.build();
        var resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag);


        assertThat(resultat).hasSize(5);
        assertKontoOgResultat(resultat.get(0), FORELDREPENGER_FØR_FØDSEL, INNVILGET);
        assertTrekkdager(resultat.get(0), ARBEIDSFORHOLD_1, new Trekkdager(3 * 5));
        assertTrekkdager(resultat.get(0), ARBEIDSFORHOLD_2, new Trekkdager(3 * 5));
        assertKontoOgResultat(resultat.get(1), MØDREKVOTE, INNVILGET);
        assertTrekkdager(resultat.get(1), ARBEIDSFORHOLD_1, new Trekkdager(6 * 5));
        assertTrekkdager(resultat.get(1), ARBEIDSFORHOLD_2, new Trekkdager(6 * 5));
        assertKontoOgResultat(resultat.get(2), MØDREKVOTE, INNVILGET);
        assertTrekkdager(resultat.get(2), ARBEIDSFORHOLD_1, new Trekkdager(new BigDecimal("22.50")));
        assertTrekkdager(resultat.get(2), ARBEIDSFORHOLD_2, new Trekkdager(9 * 5));
        assertKontoOgResultat(resultat.get(3), MØDREKVOTE, INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isNull();
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_1, new Trekkdager(new BigDecimal("22.50")));
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_2, Trekkdager.ZERO);
        assertKontoOgResultat(resultat.get(4), MØDREKVOTE, MANUELL_BEHANDLING);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_1, new Trekkdager(new BigDecimal("22.50")));
        assertThat(resultat.get(4).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(Utbetalingsgrad.ZERO);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_2, new Trekkdager(45));
    }

    private void assertKontoOgResultat(FastsettePeriodeResultat fastsettePeriodeResultat,
                                       Stønadskontotype stønadskontotype,
                                       Perioderesultattype perioderesultattype) {
        assertThat(fastsettePeriodeResultat.uttakPeriode().getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(fastsettePeriodeResultat.uttakPeriode().getPerioderesultattype()).isEqualTo(perioderesultattype);
    }

    private void assertTrekkdager(FastsettePeriodeResultat fastsettePeriodeResultat,
                                  AktivitetIdentifikator aktivitetIdentifikator,
                                  Trekkdager trekkdager) {
        assertThat(fastsettePeriodeResultat.uttakPeriode().getTrekkdager(aktivitetIdentifikator)).isEqualTo(trekkdager);
    }


    @Test
    void utbetalingsgrad_og_trekkdager_skal_ta_utgangspunkt_samtidig_uttaksprosent_for_aktiviteter_uten_gradering_hvis_det_finnes_gradering() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        var arbeidstidsprosent = BigDecimal.TEN;
        var samtidigUttaksprosent = new SamtidigUttaksprosent(50);
        //10 virkedager
        var gradertMedSamtidigUttak = OppgittPeriode.forGradering(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
            arbeidstidsprosent, samtidigUttaksprosent, false, Set.of(ARBEIDSFORHOLD_1), null, null, null, null, null);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
            .konto(konto(MØDREKVOTE, 50))
            .konto(konto(FEDREKVOTE, 50))
            .konto(konto(FELLESPERIODE, 130));
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)).arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
            .kontoer(kontoer)
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradertMedSamtidigUttak));

        var fastsettePeriodeGrunnlag = grunnlag.build();
        var resultat = fastsettPerioder(fastsettePeriodeGrunnlag);

        assertThat(resultat.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(9));
        assertThat(resultat.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2).decimalValue()).isEqualTo(
            BigDecimal.valueOf(100).subtract(arbeidstidsprosent).setScale(2, RoundingMode.DOWN));
    }

    @Test
    void trekkdager_med_desimaler_når_en_periode_er_en_dag() {
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 1));
        var søknadsperiode = gradertoppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 4, 19), LocalDate.of(2019, 4, 19), BigDecimal.valueOf(75));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .behandling(new Behandling.Builder().søkerErMor(true))
            .datoer(new Datoer.Builder().omsorgsovertakelse(søknadsperiode.getFom()))
            .adopsjon(new Adopsjon.Builder().ankomstNorge(søknadsperiode.getFom()))
            .rettOgOmsorg(aleneomsorg())
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON).oppgittPeriode(søknadsperiode))
            .build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(1);
        //Runder ned fra 0.25 til 0.2
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(0.2));
    }

    @Test
    void periode_med_gradering_og_90_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var graderingFom = fødselsdato.plusWeeks(10);
        var graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        var arbeidsprosent = BigDecimal.valueOf(90);
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
            .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
            .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
            .oppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))).build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        var uttakPeriode = resultat.get(0).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
    }

    @Test
    void periode_med_gradering_og_90_prosent_arbeid_og_lite_igjen_på_saldo_skal_bli_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var graderingFom = fødselsdato.plusWeeks(10);
        var graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        var arbeidsprosent = BigDecimal.valueOf(90);
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 5)) // bare 5 igjen
            .konto(konto(FORELDREPENGER_FØR_FØDSEL, 1000)).konto(konto(MØDREKVOTE, 1000));
        var grunnlag = basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                .oppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent)))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        var uttakPeriode = resultat.get(0).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
    }

    @Test
    void periode_med_gradering_og_80_prosent_arbeid_og_det_er_for_lite_igjen_på_saldo_slik_at_perioden_knekkes() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var graderingFom = fødselsdato.plusWeeks(10);
        var graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 5)) //bare 5 dager felles igjen
            .konto(konto(FORELDREPENGER_FØR_FØDSEL, 15)).konto(konto(MØDREKVOTE, 50));
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                .oppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, BigDecimal.valueOf(80))))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .inngangsvilkår(oppfyltAlleVilkår());

        var fastsettePeriodeGrunnlag = grunnlag.build();
        var resultat = fastsettPerioder(fastsettePeriodeGrunnlag);
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        var uttakPeriode = resultat.get(0).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //5 neste uker fellesperiode innvilges
        uttakPeriode = resultat.get(3).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(uttakPeriode.erGraderingInnvilget(ARBEIDSFORHOLD_1)).isTrue();
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));

        //5 siste uker fellesperiode avslås
        uttakPeriode = resultat.get(4).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(15));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(uttakPeriode.erGraderingInnvilget(ARBEIDSFORHOLD_1)).isTrue();
    }

    @Test
    void fellesperiode_med_gradering_før_fødsel_skal_innvilges_automatisk_for_mor() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = create();
        leggPåKvoter(grunnlag);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(
                    gradertoppgittPeriode(FELLESPERIODE, fødselsdato.minusWeeks(6), fødselsdato.minusWeeks(3).minusDays(1), BigDecimal.valueOf(50)))
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))))
            .inngangsvilkår(oppfyltAlleVilkår());
        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        //Foreldrepenger før fødsel innvilges
        var uttakPeriode = resultat.get(0).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();

        //3 uker før fødsel innvilges
        uttakPeriode = resultat.get(1).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).uttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();
    }

    @Test
    void skal_ha_en_desimal_på_trekkdager_ved_gradering() {
        var omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .adopsjon(new Adopsjon.Builder().ankomstNorge(omsorgsovertakelse))
            .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
            .rettOgOmsorg(aleneomsorg())
            .behandling(farBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4), BigDecimal.valueOf(50)))
                .oppgittPeriode(
                    gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1), omsorgsovertakelse.plusWeeks(1), BigDecimal.TEN)))
            .inngangsvilkår(oppfyltAlleVilkår());
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(2.5));
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(0.9));
    }

    @Test
    void skal_knekke_riktig_ved_flere_graderingsperioder_og_flere_arbeidsforhold() {
        var omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2);
        var arbeidsforhold3 = new Arbeidsforhold(ARBEIDSFORHOLD_3);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        //Fastsatt periode før å få ulik saldo
        var fastsattAktiviter = List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(96), FORELDREPENGER, ARBEIDSFORHOLD_1),
            new FastsattUttakPeriodeAktivitet(new Trekkdager(96), FORELDREPENGER, ARBEIDSFORHOLD_2),
            new FastsattUttakPeriodeAktivitet(Trekkdager.ZERO, FORELDREPENGER, ARBEIDSFORHOLD_3));
        var fastsattPeriode = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
            .tidsperiode(omsorgsovertakelse.minusDays(1), omsorgsovertakelse.minusDays(1))
            .aktiviteter(fastsattAktiviter);
        var vedtak = new Vedtak.Builder().leggTilPeriode(fastsattPeriode);
        var grunnlag = new RegelGrunnlag.Builder().arbeid(
                new Arbeid.Builder().arbeidsforhold(arbeidsforhold1).arbeidsforhold(arbeidsforhold2).arbeidsforhold(arbeidsforhold3))
            .kontoer(kontoer)
            .adopsjon(new Adopsjon.Builder().ankomstNorge(omsorgsovertakelse))
            .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
            .rettOgOmsorg(aleneomsorg())
            .behandling(farBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4), BigDecimal.valueOf(50),
                    Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                .oppgittPeriode(
                    gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1), omsorgsovertakelse.plusWeeks(1), BigDecimal.TEN,
                        Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                .oppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1).plusDays(1), omsorgsovertakelse.plusWeeks(10),
                    BigDecimal.valueOf(75), Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2))))
            .inngangsvilkår(oppfyltAlleVilkår())
            .revurdering(new Revurdering.Builder().gjeldendeVedtak(vedtak));
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        //Resten av dagene 4 - 2.5 - 0.9.
        assertThat(resultat.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(1.5));
        assertThat(resultat.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(1.5));
        assertThat(resultat.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_3)).isEqualTo(new Trekkdager(6));
        assertThat(resultat.get(2).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2019, 4, 23));
    }

    @Test
    void skal_knekke_riktig_når_arbeidsforhold_som_ikke_er_gradert_går_tom_for_dager() {
        var omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2);
        //Fastsatt periode før å få ulik saldo
        var fastsattPeriode = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
            .tidsperiode(omsorgsovertakelse.minusDays(1), omsorgsovertakelse.minusDays(1))
            .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(96), FORELDREPENGER, ARBEIDSFORHOLD_2),
                new FastsattUttakPeriodeAktivitet(Trekkdager.ZERO, FORELDREPENGER, ARBEIDSFORHOLD_1)));
        var vedtak = new Vedtak.Builder().leggTilPeriode(fastsattPeriode);
        var grunnlag = new RegelGrunnlag.Builder().arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold1).arbeidsforhold(arbeidsforhold2))
            .adopsjon(new Adopsjon.Builder().ankomstNorge(omsorgsovertakelse))
            .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
            .rettOgOmsorg(aleneomsorg())
            .behandling(farBehandling())
            .kontoer(kontoer)
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4), BigDecimal.valueOf(50),
                    Set.of(ARBEIDSFORHOLD_1))))
            .inngangsvilkår(oppfyltAlleVilkår())
            .revurdering(new Revurdering.Builder().gjeldendeVedtak(vedtak));
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(2));
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(4));
        assertThat(resultat.get(0).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2019, 4, 11));
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(new BigDecimal("0.5")));
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void skal_sette_trekkdager_lik_lengden_på_perioden_før_knekk() {
        var omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .adopsjon(new Adopsjon.Builder().ankomstNorge(omsorgsovertakelse))
            .kontoer(kontoer)
            .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
            .rettOgOmsorg(aleneomsorg())
            .behandling(farBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusWeeks(1), BigDecimal.valueOf(75)))
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1).plusDays(1), omsorgsovertakelse.plusWeeks(100))))
            .inngangsvilkår(oppfyltAlleVilkår());
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(1.5));
        assertThat(resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(99));
    }

    @Test
    void skal_ikke_få_innvilget_en_hel_ekstra_trekkdag() {
        var fødselsdato = LocalDate.of(2019, 2, 11);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
            .konto(konto(FELLESPERIODE, 80))
            .konto(konto(FEDREKVOTE, 75))
            .konto(konto(MØDREKVOTE, 75));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(farBehandling())
            //Søker så at konto akkurat går går opp i 76 trekkdager
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(gradertoppgittPeriode(FEDREKVOTE, LocalDate.of(2019, 5, 3), LocalDate.of(2019, 7, 5), BigDecimal.valueOf(60)))
                .oppgittPeriode(utsettelsePeriode(LocalDate.of(2019, 7, 8), LocalDate.of(2019, 7, 26), UtsettelseÅrsak.FERIE, null))
                .oppgittPeriode(gradertoppgittPeriode(FEDREKVOTE, LocalDate.of(2019, 7, 29), LocalDate.of(2020, 2, 13), BigDecimal.valueOf(60))))
            .inngangsvilkår(oppfyltAlleVilkår());
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        var trekkdager1 = resultat.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        var trekkdager2 = resultat.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        var trekkdager3 = resultat.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        assertThat(resultat.get(3).isManuellBehandling()).isTrue();
        assertThat(trekkdager1).isEqualTo(new Trekkdager(18.4));
        assertThat(trekkdager2).isEqualTo(Trekkdager.ZERO);
        assertThat(trekkdager3).isEqualTo(new Trekkdager(57.2));
    }

    @Test
    void graderingsperiode_før_søknad_mottatt_dato_skal_innvilges_ved_sammenhengende() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var mottattDato = fødselsdato.plusWeeks(8).minusDays(1);
        var gradering = OppgittPeriode.forGradering(MØDREKVOTE, fødselsdato.plusWeeks(6), mottattDato.plusWeeks(1), BigDecimal.TEN, null, false,
            Set.of(ARBEIDSFORHOLD_1), mottattDato, mottattDato, null, null, null);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling().sammenhengendeUttakTomDato(LocalDate.of(9999, 1, 1)))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradering));
        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void graderingsperiode_før_søknad_mottatt_dato_skal_godtas_manuell_hvis_fritt_uttak() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var mottattDato = fødselsdato.plusWeeks(8).minusDays(1);
        var gradering = OppgittPeriode.forGradering(MØDREKVOTE, fødselsdato.plusWeeks(6), mottattDato.plusWeeks(1), BigDecimal.TEN, null, false,
            Set.of(ARBEIDSFORHOLD_1), mottattDato, mottattDato, null, null, null);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradering));
        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isNull();
    }

    @Test
    void skal_bruke_tidligst_mottatt_dato_når_søknadsfrist_vurderes() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var senestMottattDato = fødselsdato.plusWeeks(8).minusDays(1);
        var tidligstMottattDato = fødselsdato.plusWeeks(6);
        var gradering = OppgittPeriode.forGradering(MØDREKVOTE, fødselsdato.plusWeeks(6), senestMottattDato.plusWeeks(1), BigDecimal.TEN, null, false,
            Set.of(ARBEIDSFORHOLD_1), senestMottattDato, tidligstMottattDato, null, null, null);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .behandling(morBehandling())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                .oppgittPeriode(gradering));
        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void opphevet_gradering_skal_ikke_endre_på_hvilket_arbeidsforhold_det_er_søkt_gradering_for() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var mottattDato = fødselsdato.plusWeeks(8).minusDays(1);
        var kontoer = new Kontoer.Builder().konto(konto(MØDREKVOTE, 200));
        var gradering = OppgittPeriode.forGradering(MØDREKVOTE, fødselsdato.plusWeeks(5), mottattDato.plusWeeks(1), BigDecimal.TEN, null, false,
            Set.of(ARBEIDSFORHOLD_1), mottattDato, mottattDato, null, null, null);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)).arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
            .kontoer(kontoer)
            .behandling(morBehandling().sammenhengendeUttakTomDato(LocalDate.of(9999, 1, 1)))
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(5).minusDays(1)))
                .oppgittPeriode(gradering))
            .inngangsvilkår(oppfyltAlleVilkår());
        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isEqualTo(
            GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
        assertThat(resultat.get(1).uttakPeriode().erGraderingInnvilget()).isFalse();
        assertThat(resultat.get(1).uttakPeriode().erGraderingInnvilget(ARBEIDSFORHOLD_1)).isFalse();
        assertThat(resultat.get(1).uttakPeriode().erGraderingInnvilget(ARBEIDSFORHOLD_1)).isFalse();
        assertThat(resultat.get(1)
            .uttakPeriode()
            .getAktiviteter()
            .stream()
            .anyMatch(a -> a.isSøktGradering() && a.getIdentifikator().equals(ARBEIDSFORHOLD_1))).isTrue();
        assertThat(resultat.get(1)
            .uttakPeriode()
            .getAktiviteter()
            .stream()
            .anyMatch(a -> a.isSøktGradering() && a.getIdentifikator().equals(ARBEIDSFORHOLD_2))).isFalse();
    }
}
