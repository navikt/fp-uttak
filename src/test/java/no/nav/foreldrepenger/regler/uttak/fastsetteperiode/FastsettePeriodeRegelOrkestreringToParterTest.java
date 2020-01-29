package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePerioderRegelOrkestreringTapendeSakTest.annenpartsPeriode;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeRegelOrkestreringToParterTest extends FastsettePerioderRegelOrkestreringTestBase {

    private static final int UKER_FPFF = 3;
    private static final int UKER_MK = 15;
    private static final int UKER_FK = 15;
    private static final int UKER_FP = 16;
    private static final AktivitetIdentifikator FAR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;

    private LocalDate førsteLovligeDato = LocalDate.of(2017, 10, 1);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(kvote(FORELDREPENGER_FØR_FØDSEL, UKER_FPFF))
                .leggTilKonto(kvote(MØDREKVOTE, UKER_MK))
                .leggTilKonto(kvote(FEDREKVOTE, UKER_FK))
                .leggTilKonto(kvote(FELLESPERIODE, UKER_FP));
        return builder.medKontoer(kontoer);
    }

    private Konto.Builder kvote(Stønadskontotype foreldrepengerFørFødsel, int uker) {
        return new Konto.Builder().medType(foreldrepengerFørFødsel).medTrekkdager(uker * 5);
    }


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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        var p0 = resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1a = resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1b = resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p2 = resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        var p3 = resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFPorginal, true)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        var p0 = resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1a = resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1b = resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p2 = resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        var p3 = resultat.get(4).getUttakPeriode();
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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(fomFarsFP, tomFarsFP)
                                .medSamtidigUttak(true)
                                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(FAR_ARBEIDSFORHOLD, FELLESPERIODE,
                                        new Trekkdager(Virkedager.beregnAntallVirkedager(fomFarsFP, tomFarsFP)), BigDecimal.TEN))
                                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(farArbeidsforhold2, FELLESPERIODE, new Trekkdager(5), BigDecimal.valueOf(87.5)))
                                .build()))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        var p0 = resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1a = resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1b = resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p2 = resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        var p3 = resultat.get(4).getUttakPeriode();
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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
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
        assertThat(resultat.get(7).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder_medSamtidigUttak() {
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fødselsdato.plusWeeks(UKER_MK), fødselsdato.plusWeeks(UKER_MK).plusWeeks(20).minusDays(1), true)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(UKER_MK).plusWeeks(2), fødselsdato.plusWeeks(UKER_MK).plusWeeks(3).minusDays(1)))
                        //Når denne perioden skal inn i reglene skal er det igjen 6 uke på fellesperioden
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(UKER_MK).plusWeeks(6), fødselsdato.plusWeeks(UKER_MK).plusWeeks(17).minusDays(1))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(6);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //Tom for dager
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    public void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder_utenSamtidigUttak() {
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fødselsdato.plusWeeks(UKER_MK), fødselsdato.plusWeeks(UKER_MK).plusWeeks(20).minusDays(1), false)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(UKER_MK).plusWeeks(2), fødselsdato.plusWeeks(UKER_MK).plusWeeks(3).minusDays(1)))
                        //Når denne perioden skal inn i reglene skal er det igjen 6 uke på fellesperioden
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(UKER_MK).plusWeeks(6), fødselsdato.plusWeeks(UKER_MK).plusWeeks(17).minusDays(1))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(6);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //Tom for dager
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void når_far_har_brukt_all_fellesperiode_må_mor_avslås_i_tapende_behandling() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(22).minusDays(1);
        LocalDate fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6).minusDays(1);

        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medErTapende(true))
                .medRevurdering(new Revurdering.Builder())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void far_skal_gå_tom_for_dager_når_mor_har_oppholdsperiode_men_far_tar_ikke_uttak_i_perioden() {

        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(80));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartPeriodeInnvilget(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE, new Trekkdager(30)))
                        .leggTilUttaksperiode(annenpartPeriodeOpphold(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)))
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(23).minusDays(1))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    public void skal_ikke_trekke_for_opphold_i_tapende_behandling_hvis_søker_har_søkt_i_perioden() {
        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(80));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartPeriodeInnvilget(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), FEDREKVOTE, new Trekkdager(20)))
                        .leggTilUttaksperiode(annenpartPeriodeOpphold(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(19).minusDays(1), OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medErTapende(true))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(fødselsdato.plusWeeks(10)))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(19).minusDays(1))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void skal_trekke_for_overlappende_avslått_hos_annenpart_i_tapende_behandling() {
        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(80));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))
                                .medInnvilget(false)
                                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), FELLESPERIODE, new Trekkdager(80), BigDecimal.ZERO))
                                .build()))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medErTapende(true))
                .medRevurdering(new Revurdering.Builder().medEndringsdato(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //tom på dager
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    public void skal_ikke_trekke_for_opphold_hvis_søker_har_søkt_i_perioden() {
        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(80));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartPeriodeInnvilget(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE, new Trekkdager(30)))
                        .leggTilUttaksperiode(annenpartPeriodeOpphold(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(21).minusDays(1), OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER))
                        .leggTilUttaksperiode(annenpartPeriodeInnvilget(fødselsdato.plusWeeks(21), fødselsdato.plusWeeks(30).minusDays(1), MØDREKVOTE, new Trekkdager(45))))
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(21).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(21), fødselsdato.plusWeeks(22).minusDays(1))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        //skal gå tom for dager
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void oppholdsperioder_som_overlapper_med_annenpart_skal_fjernes() {
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(17), AktivitetIdentifikator.forFrilans(), true)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(14), fødselsdato.plusWeeks(15).minusDays(1)))
                        .leggTilOppgittPeriode(OppgittPeriode.forOpphold(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16).minusDays(1), PeriodeKilde.SØKNAD, OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER))
                        .leggTilOppgittPeriode(OppgittPeriode.forOpphold(fødselsdato.plusWeeks(16), fødselsdato.plusWeeks(20), PeriodeKilde.SØKNAD, OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        //Skal ligge igjen opphold på slutten som ikke overlapper med annenpart, alt som overlapper skal fjernes
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(resultat.get(1).getUttakPeriode().getOppholdÅrsak()).isEqualTo(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER);
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(20));
    }

    @Test
    public void oppholdsperioder_som_overlapper_med_annenpart_uten_trekkdager_skal_ikke_fjernes() {
        var annenpartAktivitet = new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), FELLESPERIODE, Trekkdager.ZERO, BigDecimal.ZERO);
        var annenpartPeriodeUtenTrekkdager = AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)).medInnvilget(false)
                .medUttakPeriodeAktivitet(annenpartAktivitet).build();
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartPeriodeUtenTrekkdager))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(14), fødselsdato.plusWeeks(15).minusDays(1)))
                        .leggTilOppgittPeriode(OppgittPeriode.forOpphold(annenpartPeriodeUtenTrekkdager.getFom(), annenpartPeriodeUtenTrekkdager.getTom(), PeriodeKilde.SØKNAD, OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(resultat.get(1).getUttakPeriode().getOppholdÅrsak()).isEqualTo(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER);
    }

    @Test
    public void oppholdsperioder_som_overlapper_med_annenpart_innvilget_utsettelse_skal_fjernes() {
        var annenpartAktivitet = new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), FELLESPERIODE, Trekkdager.ZERO, BigDecimal.ZERO);
        var annenpartPeriodeUtsettelse = AnnenpartUttakPeriode.Builder.utsettelse(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)).medInnvilget(true)
                .medUttakPeriodeAktivitet(annenpartAktivitet)
                .build();
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartPeriodeUtsettelse))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(14), fødselsdato.plusWeeks(15).minusDays(1)))
                        .leggTilOppgittPeriode(OppgittPeriode.forOpphold(annenpartPeriodeUtsettelse.getFom(), annenpartPeriodeUtsettelse.getTom(),
                                PeriodeKilde.SØKNAD, OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FEDREKVOTE);
    }

    //FAGSYSTEM-85385
    @Test
    public void tapende_behandling_som_har_annenpart_med_overlappende_oppholdsperiode_skal_ikke_tape_perrioden() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), AktivitetIdentifikator.forFrilans(), true))
                        .leggTilUttaksperiode(annenpartPeriodeOpphold(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(30).minusDays(1), OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER))
                )
                .medBehandling(farBehandling().medErTapende(true))
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(30).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    private AnnenpartUttakPeriode annenpartPeriodeOpphold(LocalDate fom, LocalDate tom, OppholdÅrsak oppholdÅrsak) {
        return AnnenpartUttakPeriode.Builder.opphold(fom, tom, oppholdÅrsak)
                .medInnvilget(true)
                .build();
    }

    private AnnenpartUttakPeriode annenpartPeriodeInnvilget(LocalDate fom, LocalDate tom, Stønadskontotype stønadskontotype, Trekkdager trekkdager) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), stønadskontotype, trekkdager, BigDecimal.valueOf(100)))
                .build();
    }

    private AnnenpartUttakPeriode lagPeriodeForFar(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, boolean samtidigUttak) {
        return lagPeriode(stønadskontotype, fom, tom, FAR_ARBEIDSFORHOLD, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), samtidigUttak);
    }

    private AnnenpartUttakPeriode lagPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitet, Trekkdager trekkdager, boolean samtidigUttak) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .medSamtidigUttak(samtidigUttak)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitet, stønadskontotype, trekkdager, BigDecimal.TEN))
                .build();
    }


}
