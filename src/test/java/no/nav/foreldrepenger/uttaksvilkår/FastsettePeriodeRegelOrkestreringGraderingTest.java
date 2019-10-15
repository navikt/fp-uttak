package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringGraderingTest {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        return builder
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 130))
                        .build());
    }

    private Konto konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager)
                .build();
    }

    @Test
    public void periode_med_gradering_og_10_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.TEN;
        RegelGrunnlag grunnlag = basicGrunnlag(graderingFom, graderingTom, fødselsdato, arbeidsprosent)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //4 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(arbeidsprosent);
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(45));
    }

    @Test
    public void når_graderingsperiode_går_tom_skal_perioden_dras_til_søkers_fordel() {
        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FLERBARNSDAGER, 17 * 5)).leggTilKonto(konto(FORELDREPENGER, 57 * 5)).build());
        UttakPeriode søknadsperiode1 = new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, LocalDate.of(2019, 1, 23),
                LocalDate.of(2019, 3, 1), null, true);
        //Søker vil gå tom for dager i løpet av 19. sept, derfor får søker en ekstra trekkdager (Søkers fordel)
        UttakPeriode søknadsperiode2 = StønadsPeriode.medGradering(FORELDREPENGER, PeriodeKilde.SØKNAD, LocalDate.of(2019, 3, 4),
                LocalDate.of(2019, 9, 19), Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(60),
                PeriodeVurderingType.PERIODE_OK, null, true);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medKontoer(kontoer)
                .medBehandling(new Behandling.Builder().medSøkerErMor(false).build())
                .medDatoer(new Datoer.Builder().medFødsel(LocalDate.of(2019, 1, 23)).medFørsteLovligeUttaksdag(LocalDate.MIN).build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(false).medFarHarRett(true).medAleneomsorg(true).build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(søknadsperiode1)
                        .leggTilSøknadsperiode(søknadsperiode2)
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void samtidig_uttaksprosent_skal_settes_til_100_minus_gradering_arbeidstidsprosent_hvis_samtidig_uttak_og_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.normal();
        leggPåKvoter(grunnlag);
        BigDecimal arbeidstidsprosent = BigDecimal.TEN;
        BigDecimal samtidigUttaksprosent = BigDecimal.valueOf(50);
        UttakPeriode gradertMedSamtidigUttak = StønadsPeriode.medGradering(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), Collections.singletonList(ARBEIDSFORHOLD_1), arbeidstidsprosent, PeriodeVurderingType.PERIODE_OK,
                new SamtidigUttak(samtidigUttaksprosent), false);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFødsel(fødselsdato)
                .build())
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                                .leggTil(ARBEIDSFORHOLD_2, new ArbeidTidslinje.Builder().build())
                        )
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilSøknadsperiode(gradertMedSamtidigUttak)
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 130))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_2, new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 130))
                        .build());

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag, new FeatureTogglesForTester());

        assertThat(resultat.get(2).getUttakPeriode().isSamtidigUttak()).isTrue();
        assertThat(resultat.get(2).getUttakPeriode().getSamtidigUttaksprosent().get()).isEqualTo(BigDecimal.valueOf(100).subtract(arbeidstidsprosent));
    }

    @Test
    public void utbetalingsgrad_og_trekkdager_skal_ta_utgangspunkt_samtidig_uttaksprosent_for_aktiviteter_uten_gradering_hvis_det_finnes_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.normal();
        BigDecimal arbeidstidsprosent = BigDecimal.TEN;
        BigDecimal samtidigUttaksprosent = BigDecimal.valueOf(50);
        //10 virkedager
        UttakPeriode gradertMedSamtidigUttak = StønadsPeriode.medGradering(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), Collections.singletonList(ARBEIDSFORHOLD_1), arbeidstidsprosent, PeriodeVurderingType.PERIODE_OK,
                new SamtidigUttak(samtidigUttaksprosent), false);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFødsel(fødselsdato)
                .build())
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().medArbeid(fødselsdato, fødselsdato.plusYears(2),
                                        Arbeid.forOrdinærtArbeid(arbeidstidsprosent, BigDecimal.valueOf(100))).build())
                                .leggTil(ARBEIDSFORHOLD_2, new ArbeidTidslinje.Builder().medArbeid(fødselsdato, fødselsdato.plusYears(2),
                                        Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100))).build())
                        )
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilSøknadsperiode(gradertMedSamtidigUttak)
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 130))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_2, new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 50))
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 130))
                        .build());

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag, new FeatureTogglesForTester());

        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(9));
        assertThat(resultat.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_2))
                .isEqualTo(BigDecimal.valueOf(100).subtract(arbeidstidsprosent).setScale(2, RoundingMode.DOWN));
    }

    @Test
    public void trekkdager_med_desimaler_når_en_periode_er_en_dag() {
        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 1)).build());
        UttakPeriode søknadsperiode = gradertSøknadsperiode(FORELDREPENGER, LocalDate.of(2019, 4, 19), LocalDate.of(2019, 4, 19),
                BigDecimal.valueOf(75));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medKontoer(kontoer)
                .medBehandling(new Behandling.Builder().medSøkerErMor(true).build())
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(søknadsperiode.getFom()).medFørsteLovligeUttaksdag(LocalDate.MIN).build())
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(søknadsperiode.getFom()).build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true).medFarHarRett(true).medAleneomsorg(true).build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(søknadsperiode)
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(1);
        //Runder ned fra 0.25 til 0.2
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(0.2)));
    }

    private UttakPeriode gradertSøknadsperiode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, BigDecimal arbeidsprosent) {
        return gradertSøknadsperiode(stønadskontotype, fom, tom, arbeidsprosent, Collections.singletonList(ARBEIDSFORHOLD_1));
    }

    private UttakPeriode gradertSøknadsperiode(Stønadskontotype stønadskontotype,
                                               LocalDate fom,
                                               LocalDate tom,
                                               BigDecimal arbeidsprosent,
                                               List<AktivitetIdentifikator> gradertAktiviteter) {
        return StønadsPeriode.medGradering(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom,
                gradertAktiviteter, arbeidsprosent, PeriodeVurderingType.PERIODE_OK);
    }

    private StønadsPeriode ugradertSøknadsperiode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate graderingFom, LocalDate graderingTom, LocalDate fødselsdato, BigDecimal arbeidsprosent) {
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, arbeidsprosent);
        leggPåKvoter(grunnlag);
        return grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
    }

    @Test
    public void periode_med_gradering_og_90_prosent_arbeid_skal_få_riktig_antall_trekkdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(90);
        RegelGrunnlag grunnlag = basicGrunnlag(graderingFom, graderingTom, fødselsdato, arbeidsprosent)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(arbeidsprosent);
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
    }

    @Test
    public void periode_med_gradering_og_90_prosent_arbeid_og_lite_igjen_på_saldo_skal_bli_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(90);
        RegelGrunnlag grunnlag = basicGrunnlag(graderingFom, graderingTom, fødselsdato, arbeidsprosent)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FELLESPERIODE, graderingFom, graderingTom, arbeidsprosent))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FELLESPERIODE, 5)) // bare 5 igjen
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 1000))
                        .leggTilKonto(konto(MØDREKVOTE, 1000))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();

        //10 neste uker mødrekvote innvilges og gradering beholdes
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isTrue();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(arbeidsprosent);
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
    }

    @Test
    public void periode_med_gradering_og_80_prosent_arbeid_og_det_er_for_lite_igjen_på_saldo_slik_at_perioden_knekkes() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(80));
        grunnlag.medDatoer(new Datoer.Builder()
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFødsel(fødselsdato)
                .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, graderingFom.minusDays(1)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FELLESPERIODE, graderingFom, graderingTom, BigDecimal.valueOf(80)))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FELLESPERIODE, 5)) //bare 5 dager felles igjen
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(MØDREKVOTE, 50))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.isGradering()).isFalse();

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.isGradering()).isFalse();

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingFom.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.isGradering()).isFalse();

        //5 neste uker fellesperiode innvilges
        uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(graderingFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.isGradering()).isTrue();
        assertThat(uttakPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(80));
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));

        //5 siste uker fellesperiode avslås
        uttakPeriode = resultat.get(4).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(15));
        assertThat(uttakPeriode.getTom()).isEqualTo(graderingTom);
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(uttakPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(uttakPeriode.isGradering()).isTrue();
        assertThat(uttakPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(80));
    }

    @Test
    public void fellesperiode_før_fødsel_skal_ikke_kunne_ha_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.minusWeeks(6), fødselsdato.minusWeeks(3).minusDays(1), BigDecimal.valueOf(50));
        leggPåKvoter(grunnlag);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FELLESPERIODE, fødselsdato.minusWeeks(6), fødselsdato.minusWeeks(3).minusDays(1), BigDecimal.valueOf(50)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(3);

        //Foreldrepenger før fødsel innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(50));

        //3 uker før fødsel innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(0));

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(stønadsPeriode.isGradering()).isFalse();
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    public void skal_ha_en_desimal_på_trekkdager_ved_gradering() {
        LocalDate omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        Map<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100)).build());
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(omsorgsovertakelse)
                        .build())
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())).build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .medAleneomsorg(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4), BigDecimal.valueOf(50)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1), omsorgsovertakelse.plusWeeks(1), BigDecimal.TEN))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(2.5)));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(0.9)));
    }

    @Test
    public void skal_knekke_riktig_ved_flere_graderingsperioder_og_flere_arbeidsforhold() {
        LocalDate omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        Map<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 4)).build());
        kontoer.put(ARBEIDSFORHOLD_2, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 4)).build());
        kontoer.put(ARBEIDSFORHOLD_3, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100)).build());
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(omsorgsovertakelse)
                        .build())
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter()
                        .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                        .leggTil(ARBEIDSFORHOLD_2, new ArbeidTidslinje.Builder().build())
                        .leggTil(ARBEIDSFORHOLD_3, new ArbeidTidslinje.Builder().build()))
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .medAleneomsorg(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4),
                                BigDecimal.valueOf(50), Arrays.asList(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1), omsorgsovertakelse.plusWeeks(1),
                                BigDecimal.TEN, Arrays.asList(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1).plusDays(1),
                                omsorgsovertakelse.plusWeeks(10), BigDecimal.valueOf(75), Arrays.asList(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultat).hasSize(4);
        //Resten av dagene 4 - 2.5 - 0.9.
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(1.5)));
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(BigDecimal.valueOf(1.5)));
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_3)).isEqualTo(new Trekkdager(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2019, 4, 23));
    }

    @Test
    public void skal_knekke_riktig_når_arbeidsforhold_som_ikke_er_gradert_går_tom_for_dager() {
        LocalDate omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        Map<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100)).build());
        kontoer.put(ARBEIDSFORHOLD_2, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 4)).build());
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(omsorgsovertakelse)
                        .build())
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter()
                        .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                        .leggTil(ARBEIDSFORHOLD_2, new ArbeidTidslinje.Builder().build()))
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .medAleneomsorg(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusDays(4),
                                BigDecimal.valueOf(50), Collections.singletonList(ARBEIDSFORHOLD_1)))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(2));
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(new Trekkdager(4));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2019, 4, 11));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void skal_sette_trekkdager_lik_lengden_på_perioden_før_knekk() {
        LocalDate omsorgsovertakelse = LocalDate.of(2019, 4, 8);
        Map<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100)).build());
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(omsorgsovertakelse)
                        .build())
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())).build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .medAleneomsorg(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse, omsorgsovertakelse.plusWeeks(1), BigDecimal.valueOf(75)))
                        .leggTilSøknadsperiode(ugradertSøknadsperiode(FORELDREPENGER, omsorgsovertakelse.plusWeeks(1).plusDays(1), omsorgsovertakelse.plusWeeks(100)))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(1.5)));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(99)));
    }

    @Test
    public void skal_ikke_få_innvilget_en_hel_ekstra_trekkdag() {
        LocalDate fødselsdato = LocalDate.of(2019, 2, 11);
        Map<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(FELLESPERIODE, 80))
                .leggTilKonto(konto(FEDREKVOTE, 75))
                .leggTilKonto(konto(MØDREKVOTE, 75))
                .build());
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())).build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                //Søker så at konto akkurat går går opp i 76 trekkdager
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FEDREKVOTE, LocalDate.of(2019, 5, 3),
                                LocalDate.of(2019, 7, 5), BigDecimal.valueOf(60)))
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, LocalDate.of(2019, 7, 8),
                                LocalDate.of(2019, 7, 26), Utsettelseårsaktype.FERIE, PeriodeVurderingType.IKKE_VURDERT, null,
                                false))
                        .leggTilSøknadsperiode(gradertSøknadsperiode(FEDREKVOTE, LocalDate.of(2019, 7, 29),
                                LocalDate.of(2020, 2, 13), BigDecimal.valueOf(60)))
                        .build())
                .medInngangsvilkår(oppfyltInngangsvilkår());
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultat).hasSize(4);
        Trekkdager trekkdager1 = resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        Trekkdager trekkdager2 = resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        Trekkdager trekkdager3 = resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1);
        assertThat(resultat.get(3).isManuellBehandling()).isTrue();
        assertThat(trekkdager1).isEqualTo(new Trekkdager(BigDecimal.valueOf(18.4)));
        assertThat(trekkdager2).isEqualTo(new Trekkdager(BigDecimal.ZERO));
        assertThat(trekkdager3).isEqualTo(new Trekkdager(BigDecimal.valueOf(57.2)));
    }

    private Inngangsvilkår oppfyltInngangsvilkår() {
        return new Inngangsvilkår.Builder()
                .medAdopsjonOppfylt(true)
                .medForeldreansvarnOppfylt(true)
                .medFødselOppfylt(true)
                .medOpptjeningOppfylt(true)
                .build();
    }
}
