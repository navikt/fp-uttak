package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_3;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.create;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class GraderingOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .leggTilKonto(konto(FEDREKVOTE, 50))
                .leggTilKonto(konto(FELLESPERIODE, 130));
        return builder.medKontoer(kontoer);
    }

    @Test
    public void periode_med_gradering_og_10_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.TEN;
        RegelGrunnlag grunnlag = basicGrunnlag(fødselsdato).medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                .leggTilOppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))).build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(45));
    }

    @Test
    public void når_graderingsperiode_går_tom_skal_perioden_dras_til_søkers_fordel() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FLERBARNSDAGER, 17 * 5)).leggTilKonto(konto(FORELDREPENGER, 57 * 5));
        OppgittPeriode søknadsperiode1 = oppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 1, 23), LocalDate.of(2019, 3, 1));
        //Søker vil gå tom for dager i løpet av 19. sept, derfor får søker en ekstra trekkdager (Søkers fordel)
        OppgittPeriode søknadsperiode2 = gradertoppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 3, 4), LocalDate.of(2019, 9, 19),
                BigDecimal.valueOf(60));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medBehandling(farBehandling())
                .medDatoer(new Datoer.Builder().medFødsel(LocalDate.of(2019, 1, 23)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(false).medFarHarRett(true).medAleneomsorg(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(søknadsperiode1)
                        .leggTilOppgittPeriode(søknadsperiode2))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void samtidig_uttaksprosent_skal_settes_til_100_minus_gradering_arbeidstidsprosent_hvis_samtidig_uttak_og_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        BigDecimal arbeidstidsprosent = BigDecimal.TEN;
        var samtidigUttaksprosent = new SamtidigUttaksprosent(50);
        OppgittPeriode gradertMedSamtidigUttak = OppgittPeriode.forGradering(FELLESPERIODE, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), arbeidstidsprosent, samtidigUttaksprosent, false, Set.of(ARBEIDSFORHOLD_1),
                PeriodeVurderingType.IKKE_VURDERT, null, null);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .leggTilKonto(konto(FEDREKVOTE, 50))
                .leggTilKonto(konto(FELLESPERIODE, 130));
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(gradertMedSamtidigUttak))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)));

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(fastsettePeriodeGrunnlag);

        assertThat(resultat.get(2).getUttakPeriode().erSamtidigUttak()).isTrue();
        assertThat(resultat.get(2).getUttakPeriode().getSamtidigUttaksprosent()).isEqualTo(
                SamtidigUttaksprosent.HUNDRED.subtract(arbeidstidsprosent));
    }

    @Test
    public void uttak_på_to_arbeidsforhold_hvor_den_ene_går_tom_for_fellesperiode_skal_føre_0_utbetaling_og_0_trekkdager_for_den_som_går_tom() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var prosent50 = new BigDecimal("50.00");
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 15 * 5))
                .leggTilKonto(konto(FEDREKVOTE, 15 * 5))
                .leggTilKonto(konto(FELLESPERIODE, 16 * 5));
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1)))
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(33).minusDays(1),
                                        prosent50, Set.of(ARBEIDSFORHOLD_1))))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)));

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(fastsettePeriodeGrunnlag);


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
        assertThat(resultat.get(3).getUttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isNull();
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_1, new Trekkdager((16 * 5) / 2));
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_2, new Trekkdager(16 * 5));
        assertKontoOgResultat(resultat.get(4), FELLESPERIODE, INNVILGET);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_1, new Trekkdager((2 * 5) / 2));
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(Utbetalingsgrad.ZERO);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_2, Trekkdager.ZERO);
    }

    @Test
    public void uttak_på_to_arbeidsforhold_hvor_den_ene_går_tom_for_mødrekvote_skal_føre_0_utbetaling_og_0_trekkdager_for_den_som_går_tom() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var prosent50 = new BigDecimal("50.00");
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 15 * 5))
                .leggTilKonto(konto(FEDREKVOTE, 15 * 5))
                .leggTilKonto(konto(FELLESPERIODE, 16 * 5));
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(33).minusDays(1),
                                        prosent50, Set.of(ARBEIDSFORHOLD_1))));

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag,
                new FeatureTogglesForTester());


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
        assertThat(resultat.get(3).getUttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isNull();
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_1, new Trekkdager(new BigDecimal("22.50")));
        assertTrekkdager(resultat.get(3), ARBEIDSFORHOLD_2, Trekkdager.ZERO);
        assertKontoOgResultat(resultat.get(4), MØDREKVOTE, MANUELL_BEHANDLING);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_1, new Trekkdager(new BigDecimal("22.50")));
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(Utbetalingsgrad.ZERO);
        assertTrekkdager(resultat.get(4), ARBEIDSFORHOLD_2, new Trekkdager(45));
    }

    private void assertKontoOgResultat(FastsettePeriodeResultat fastsettePeriodeResultat,
                                       Stønadskontotype stønadskontotype,
                                       Perioderesultattype perioderesultattype) {
        assertThat(fastsettePeriodeResultat.getUttakPeriode().getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(fastsettePeriodeResultat.getUttakPeriode().getPerioderesultattype()).isEqualTo(perioderesultattype);
    }

    private void assertTrekkdager(FastsettePeriodeResultat fastsettePeriodeResultat,
                                  AktivitetIdentifikator aktivitetIdentifikator,
                                  Trekkdager trekkdager) {
        assertThat(fastsettePeriodeResultat.getUttakPeriode().getTrekkdager(aktivitetIdentifikator)).isEqualTo(trekkdager);
    }


    @Test
    public void utbetalingsgrad_og_trekkdager_skal_ta_utgangspunkt_samtidig_uttaksprosent_for_aktiviteter_uten_gradering_hvis_det_finnes_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        BigDecimal arbeidstidsprosent = BigDecimal.TEN;
        var samtidigUttaksprosent = new SamtidigUttaksprosent(50);
        //10 virkedager
        OppgittPeriode gradertMedSamtidigUttak = OppgittPeriode.forGradering(FELLESPERIODE, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), arbeidstidsprosent, samtidigUttaksprosent, false, Set.of(ARBEIDSFORHOLD_1),
                PeriodeVurderingType.IKKE_VURDERT, null, null);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .leggTilKonto(konto(FEDREKVOTE, 50))
                .leggTilKonto(konto(FELLESPERIODE, 130));
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(gradertMedSamtidigUttak));

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(fastsettePeriodeGrunnlag);

        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(9));
        assertThat(resultat.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2).decimalValue()).isEqualTo(
                BigDecimal.valueOf(100).subtract(arbeidstidsprosent).setScale(2, RoundingMode.DOWN));
    }

    @Test
    public void trekkdager_med_desimaler_når_en_periode_er_en_dag() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 1));
        OppgittPeriode søknadsperiode = gradertoppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 4, 19), LocalDate.of(2019, 4, 19),
                BigDecimal.valueOf(75));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(søknadsperiode.getFom()))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(søknadsperiode.getFom()))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true).medFarHarRett(true).medAleneomsorg(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON).leggTilOppgittPeriode(søknadsperiode))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(1);
        //Runder ned fra 0.25 til 0.2
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(0.2));
    }

    @Test
    public void periode_med_gradering_og_90_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(90);
        RegelGrunnlag grunnlag = basicGrunnlag(fødselsdato).medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))).build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
    }

    @Test
    public void periode_med_gradering_og_90_prosent_arbeid_og_lite_igjen_på_saldo_skal_bli_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(90);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FELLESPERIODE, 5)) // bare 5 igjen
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 1000)).leggTilKonto(konto(MØDREKVOTE, 1000));
        RegelGrunnlag grunnlag = basicGrunnlag(fødselsdato).medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                .leggTilOppgittPeriode(gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
    }

    @Test
    public void periode_med_gradering_og_80_prosent_arbeid_og_det_er_for_lite_igjen_på_saldo_slik_at_perioden_knekkes() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FELLESPERIODE, 5)) //bare 5 dager felles igjen
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15)).leggTilKonto(konto(MØDREKVOTE, 50));
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FELLESPERIODE, graderingFom, graderingTom, BigDecimal.valueOf(80))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medInngangsvilkår(oppfyltAlleVilkår());

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(fastsettePeriodeGrunnlag);
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //5 neste uker fellesperiode innvilges
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isTrue();
        assertThat(uttakPeriode.getArbeidsprosent()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(uttakPeriode.erGraderingInnvilget(ARBEIDSFORHOLD_1)).isTrue();
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));

        //5 siste uker fellesperiode avslås
        uttakPeriode = resultat.get(4).getUttakPeriode();
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
    public void fellesperiode_før_fødsel_skal_ikke_kunne_ha_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = create();
        leggPåKvoter(grunnlag);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FELLESPERIODE, fødselsdato.minusWeeks(6), fødselsdato.minusWeeks(3).minusDays(1),
                                        BigDecimal.valueOf(50)))
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))))
                .medInngangsvilkår(oppfyltAlleVilkår());
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);

        //Foreldrepenger før fødsel innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //3 uker før fødsel innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.erGraderingInnvilget()).isFalse();
    }

    @Test
    public void skal_ha_en_desimal_på_trekkdager_ved_gradering() {
        LocalDate omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100));
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(omsorgsovertakelse))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medRettOgOmsorg(aleneomsorg())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4),
                                        BigDecimal.valueOf(50)))
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1), omsorgsovertakelse.plusWeeks(1),
                                        BigDecimal.TEN)))
                .medInngangsvilkår(oppfyltAlleVilkår());
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(2.5));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(0.9));
    }

    @Test
    public void skal_knekke_riktig_ved_flere_graderingsperioder_og_flere_arbeidsforhold() {
        var omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2);
        var arbeidsforhold3 = new Arbeidsforhold(ARBEIDSFORHOLD_3);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100));
        //Fastsatt periode før å få ulik saldo
        var fastsattAktiviter = List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(96), FORELDREPENGER, ARBEIDSFORHOLD_1),
                new FastsattUttakPeriodeAktivitet(new Trekkdager(96), FORELDREPENGER, ARBEIDSFORHOLD_2),
                new FastsattUttakPeriodeAktivitet(Trekkdager.ZERO, FORELDREPENGER, ARBEIDSFORHOLD_3));
        var fastsattPeriode = new FastsattUttakPeriode.Builder().medPeriodeResultatType(INNVILGET)
                .medTidsperiode(omsorgsovertakelse.minusDays(1), omsorgsovertakelse.minusDays(1))
                .medAktiviteter(fastsattAktiviter);
        var vedtak = new Vedtak.Builder().leggTilPeriode(fastsattPeriode);
        var grunnlag = new RegelGrunnlag.Builder().medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold1)
                .leggTilArbeidsforhold(arbeidsforhold2)
                .leggTilArbeidsforhold(arbeidsforhold3))
                .medKontoer(kontoer)
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(omsorgsovertakelse))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medRettOgOmsorg(aleneomsorg())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4),
                                        BigDecimal.valueOf(50), Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1), omsorgsovertakelse.plusWeeks(1),
                                        BigDecimal.TEN, Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                        .leggTilOppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1).plusDays(1),
                                omsorgsovertakelse.plusWeeks(10), BigDecimal.valueOf(75), Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2))))
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medRevurdering(new Revurdering.Builder().medGjeldendeVedtak(vedtak));
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        //Resten av dagene 4 - 2.5 - 0.9.
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(1.5));
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(1.5));
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_3)).isEqualTo(new Trekkdager(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2019, 4, 23));
    }

    @Test
    public void skal_knekke_riktig_når_arbeidsforhold_som_ikke_er_gradert_går_tom_for_dager() {
        var omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100));
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2);
        //Fastsatt periode før å få ulik saldo
        var fastsattPeriode = new FastsattUttakPeriode.Builder().medPeriodeResultatType(INNVILGET)
                .medTidsperiode(omsorgsovertakelse.minusDays(1), omsorgsovertakelse.minusDays(1))
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(96), FORELDREPENGER, ARBEIDSFORHOLD_2),
                        new FastsattUttakPeriodeAktivitet(Trekkdager.ZERO, FORELDREPENGER, ARBEIDSFORHOLD_1)));
        var vedtak = new Vedtak.Builder().leggTilPeriode(fastsattPeriode);
        var grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold1).leggTilArbeidsforhold(arbeidsforhold2))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(omsorgsovertakelse))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medRettOgOmsorg(aleneomsorg())
                .medBehandling(farBehandling())
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4),
                                        BigDecimal.valueOf(50), Set.of(ARBEIDSFORHOLD_1))))
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medRevurdering(new Revurdering.Builder().medGjeldendeVedtak(vedtak));
        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(2));
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(4));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2019, 4, 11));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(new BigDecimal("0.5")));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void skal_sette_trekkdager_lik_lengden_på_perioden_før_knekk() {
        LocalDate omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100));
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(omsorgsovertakelse))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medRettOgOmsorg(aleneomsorg())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusWeeks(1),
                                        BigDecimal.valueOf(75)))
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1).plusDays(1),
                                omsorgsovertakelse.plusWeeks(100))))
                .medInngangsvilkår(oppfyltAlleVilkår());
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(1.5));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(99));
    }

    @Test
    public void skal_ikke_få_innvilget_en_hel_ekstra_trekkdag() {
        LocalDate fødselsdato = LocalDate.of(2019, 2, 11);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(FELLESPERIODE, 80))
                .leggTilKonto(konto(FEDREKVOTE, 75))
                .leggTilKonto(konto(MØDREKVOTE, 75));
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(farBehandling())
                //Søker så at konto akkurat går går opp i 76 trekkdager
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(gradertoppgittPeriode(FEDREKVOTE, LocalDate.of(2019, 5, 3), LocalDate.of(2019, 7, 5),
                                BigDecimal.valueOf(60)))
                        .leggTilOppgittPeriode(
                                utsettelsePeriode(LocalDate.of(2019, 7, 8), LocalDate.of(2019, 7, 26), UtsettelseÅrsak.FERIE))
                        .leggTilOppgittPeriode(gradertoppgittPeriode(FEDREKVOTE, LocalDate.of(2019, 7, 29), LocalDate.of(2020, 2, 13),
                                BigDecimal.valueOf(60))))
                .medInngangsvilkår(oppfyltAlleVilkår());
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        Trekkdager trekkdager1 = resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        Trekkdager trekkdager2 = resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        Trekkdager trekkdager3 = resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        assertThat(resultat.get(3).isManuellBehandling()).isTrue();
        assertThat(trekkdager1).isEqualTo(new Trekkdager(18.4));
        assertThat(trekkdager2).isEqualTo(Trekkdager.ZERO);
        assertThat(trekkdager3).isEqualTo(new Trekkdager(57.2));
    }

    @Test
    public void graderingsperiode_før_søknad_mottatt_dato_skal_gå_til_manuell() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var mottattDato = fødselsdato.plusWeeks(8).minusDays(1);
        var gradering = OppgittPeriode.forGradering(MØDREKVOTE, fødselsdato.plusWeeks(6), mottattDato.plusWeeks(1), BigDecimal.TEN,
                null, false, Set.of(ARBEIDSFORHOLD_1), PeriodeVurderingType.IKKE_VURDERT, mottattDato, null);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(gradering));
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(resultat.get(1).getUttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isEqualTo(
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_SEN_SØKNAD);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void opphevet_gradering_skal_ikke_endre_på_hvilket_arbeidsforhold_det_er_søkt_gradering_for() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag);
        var mottattDato = fødselsdato.plusWeeks(8).minusDays(1);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(MØDREKVOTE, 200));
        var gradering = OppgittPeriode.forGradering(MØDREKVOTE, fødselsdato.plusWeeks(6), mottattDato.plusWeeks(1), BigDecimal.TEN,
                null, false, Set.of(ARBEIDSFORHOLD_1), PeriodeVurderingType.IKKE_VURDERT, mottattDato, null);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
                .medKontoer(kontoer)
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(gradering))
                .medInngangsvilkår(oppfyltAlleVilkår());
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isEqualTo(
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_SEN_SØKNAD);
        assertThat(resultat.get(1).getUttakPeriode().erGraderingInnvilget()).isFalse();
        assertThat(resultat.get(1).getUttakPeriode().erGraderingInnvilget(ARBEIDSFORHOLD_1)).isFalse();
        assertThat(resultat.get(1).getUttakPeriode().erGraderingInnvilget(ARBEIDSFORHOLD_1)).isFalse();
        assertThat(resultat.get(1)
                .getUttakPeriode()
                .getAktiviteter()
                .stream()
                .anyMatch(a -> a.isSøktGradering() && a.getIdentifikator().equals(ARBEIDSFORHOLD_1))).isTrue();
        assertThat(resultat.get(1)
                .getUttakPeriode()
                .getAktiviteter()
                .stream()
                .anyMatch(a -> a.isSøktGradering() && a.getIdentifikator().equals(ARBEIDSFORHOLD_2))).isFalse();
    }
}
