package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringToParterTest {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();
    private static final int UKER_FPFF = 3;
    private static final int UKER_MK = 15;
    private static final int UKER_FK = 15;
    private static final int UKER_FP = 16;
    private static final AktivitetIdentifikator FAR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;


    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        Kontoer kontoer = new Kontoer.Builder()
                .leggTilKonto(kvote(FORELDREPENGER_FØR_FØDSEL, UKER_FPFF))
                .leggTilKonto(kvote(MØDREKVOTE, UKER_MK))
                .leggTilKonto(kvote(FEDREKVOTE, UKER_FK))
                .leggTilKonto(kvote(FELLESPERIODE, UKER_FP))
                .build();
        return builder.leggTilKontoer(FAR_ARBEIDSFORHOLD, kontoer);
    }

    private Konto kvote(Stønadskontotype foreldrepengerFørFødsel, int ukerFpff) {
        return new Konto.Builder().medType(foreldrepengerFørFødsel).medTrekkdager(ukerFpff * 5).build();
    }
    private LocalDate førsteLovligeDato = LocalDate.of(2017, 10, 1);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);


    @Test
    public void far_har_uttak_og_mor_søker_før_og_etter_fars_uttak() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        LocalDate fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        LocalDate tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP).minusDays(1);
        LocalDate tomMorsFPsøknad = fødselsdato.plusWeeks(50);

        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(5);
        UttakPeriode p0 = resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p1a = resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p1b = resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p2 = resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        UttakPeriode p3 = resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    private UttakPeriode uttakPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    @Test
    public void far_har_uttak_og_mor_søker_om_uttak_før_fars_uttak_slutter_slik_at_mor_tar_dager_fra_far() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFPorginal = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        LocalDate fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        LocalDate tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP).minusDays(1);
        LocalDate tomMorsFPsøknad = fødselsdato.plusWeeks(50);

        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFPorginal))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(5);
        UttakPeriode p0 = resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p1a = resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p1b = resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p2 = resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        UttakPeriode p3 = resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    public void når_far_har_uttak_og_gradert_i_ett_arbeidsforhold_ser_mors_tilgjengelige_dager_kun_det_arbeidsforholdet_for_far_med_minst_forbruk() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        LocalDate fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        LocalDate tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP + 7).minusDays(1); //7 ekstra uker pga fars gradering
        LocalDate tomMorsFPsøknad = fødselsdato.plusWeeks(50);
        AktivitetIdentifikator farArbeidsforhold2 = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_3;

        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(new FastsattPeriodeAnnenPart.Builder(fomFarsFP, tomFarsFP)
                                .medSamtidigUttak(true)
                                .medInnvilgetUtsettelse(false)
                                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(FAR_ARBEIDSFORHOLD, FELLESPERIODE,
                                        new Trekkdager(Virkedager.beregnAntallVirkedager(fomFarsFP, tomFarsFP)), BigDecimal.TEN))
                                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(farArbeidsforhold2, FELLESPERIODE, new Trekkdager(5), BigDecimal.valueOf(87.5)))
                                .build())
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(5);
        UttakPeriode p0 = resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p1a = resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p1b = resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        UttakPeriode p2 = resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        UttakPeriode p3 = resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    public void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(1);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(5).minusDays(1);
        LocalDate fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2).minusDays(1);
        LocalDate fomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2);
        LocalDate tomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(UKER_FP).minusWeeks(4).plusWeeks(1);


        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(8);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(6).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(7).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(7).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder2() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(13).minusDays(1);
        LocalDate fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2);
        LocalDate tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(4).minusDays(1);
        LocalDate fomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6);
        LocalDate tomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(8).minusDays(1);


        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(5);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioderPeriodenTilSluttSkalTrekkesAvIOverlapp() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(20).minusDays(1);
        LocalDate fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2);
        LocalDate tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(4).minusDays(1);
        LocalDate fomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6);
        //en dag for mye
        LocalDate tomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(14);


        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(6);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(5).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioderPeriodenTilSluttSkalIkkeTrekkesFra() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(10);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(40).minusDays(1);
        LocalDate fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(10).minusDays(1);


        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void når_far_har_hele_fellesperioder_mor_sin_fellesperiode_må_avslått_i_berørt_saken() {
        LocalDate endringssøknadMottattdato = fødselsdato.plusWeeks(UKER_MK);
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(22).minusDays(1);
        LocalDate fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6).minusDays(1);

        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medType(Behandlingtype.REVURDERING_BERØRT_SAK)
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(endringssøknadMottattdato)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilSøknadsperiode(uttakPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    private FastsattPeriodeAnnenPart lagPeriodeForFar(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return lagPeriode(stønadskontotype, fom, tom, FAR_ARBEIDSFORHOLD, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)));
    }

    private FastsattPeriodeAnnenPart lagPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitet, Trekkdager trekkdager) {
        return new FastsattPeriodeAnnenPart.Builder(fom, tom)
                .medSamtidigUttak(true)
                .medInnvilgetUtsettelse(false)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitet, stønadskontotype, trekkdager, BigDecimal.TEN))
                .build();
    }


}
