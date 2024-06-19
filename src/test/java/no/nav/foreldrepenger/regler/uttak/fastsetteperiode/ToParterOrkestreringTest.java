package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.TapendeSakOrkestreringTest.annenpartsPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forFrilans;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import org.junit.jupiter.api.Test;

class ToParterOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    private static final int UKER_FPFF = 3;
    private static final int UKER_MK = 15;
    private static final int UKER_FK = 15;
    private static final int UKER_FP = 16;
    private static final AktivitetIdentifikator FAR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;

    private final LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        var kontoer = new Kontoer.Builder()
                .konto(kvote(FORELDREPENGER_FØR_FØDSEL, UKER_FPFF))
                .konto(kvote(MØDREKVOTE, UKER_MK))
                .konto(kvote(FEDREKVOTE, UKER_FK))
                .konto(kvote(FELLESPERIODE, UKER_FP));
        return builder.kontoer(kontoer);
    }

    private Konto.Builder kvote(Stønadskontotype foreldrepengerFørFødsel, int uker) {
        return new Konto.Builder().type(foreldrepengerFørFødsel).trekkdager(uker * 5);
    }

    @Test
    void far_har_uttak_og_mor_søker_før_og_etter_fars_uttak() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        var tomFarsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        var fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        var tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP).minusDays(1);
        var tomMorsFPsøknad = fødselsdato.plusWeeks(50);

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        var p0 = resultat.get(0).uttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1a = resultat.get(1).uttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1b = resultat.get(2).uttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p2 = resultat.get(3).uttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        var p3 = resultat.get(4).uttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    void far_har_uttak_og_mor_søker_om_uttak_før_fars_uttak_slutter_slik_at_mor_tar_dager_fra_far() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        var tomFarsFPorginal = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        var fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        var tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP).minusDays(1);
        var tomMorsFPsøknad = fødselsdato.plusWeeks(50);

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFPorginal, true)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        var p0 = resultat.get(0).uttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1a = resultat.get(1).uttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1b = resultat.get(2).uttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p2 = resultat.get(3).uttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        var p3 = resultat.get(4).uttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    void
            når_far_har_uttak_og_gradert_i_ett_arbeidsforhold_ser_mors_tilgjengelige_dager_kun_det_arbeidsforholdet_for_far_med_minst_forbruk() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        var tomFarsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        var fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        var tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP + 7).minusDays(1); // 7 ekstra uker pga fars gradering
        var tomMorsFPsøknad = fødselsdato.plusWeeks(50);
        var farArbeidsforhold2 = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_3;

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(fomFarsFP, tomFarsFP)
                                .samtidigUttak(true)
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        FAR_ARBEIDSFORHOLD,
                                        FELLESPERIODE,
                                        new Trekkdager(Virkedager.beregnAntallVirkedager(fomFarsFP, tomFarsFP)),
                                        Utbetalingsgrad.TEN))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        farArbeidsforhold2,
                                        FELLESPERIODE,
                                        new Trekkdager(5),
                                        new Utbetalingsgrad(87.5)))
                                .build()))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP, tomMorsFPsøknad)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        var p0 = resultat.get(0).uttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1a = resultat.get(1).uttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p1b = resultat.get(2).uttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        var p2 = resultat.get(3).uttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        var p3 = resultat.get(4).uttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(1);
        var tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(5).minusDays(1);
        var fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        var tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2).minusDays(1);
        var fomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2);
        var tomMorsFP2 =
                fødselsdato.plusWeeks(UKER_MK).plusWeeks(UKER_FP).minusWeeks(4).plusWeeks(1);

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(8);
        // skal gå tom for dager
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(5).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(6).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(7).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(7).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder2() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        var tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(13).minusDays(1);
        var fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(2);
        var tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(4).minusDays(1);
        var fomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6);
        var tomMorsFP2 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(8).minusDays(1);

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP2, tomMorsFP2)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder_medSamtidigUttak() {
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(UKER_MK),
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(20).minusDays(1),
                                true)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(2),
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(3).minusDays(1)))
                        // Når denne perioden skal inn i reglene skal er det igjen 6 uke på
                        // fellesperioden
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(6),
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(17).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(6);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        // Tom for dager
        assertThat(resultat.get(5).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioder_utenSamtidigUttak() {
        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(UKER_MK),
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(20).minusDays(1),
                                false)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(2),
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(3).minusDays(1)))
                        // Når denne perioden skal inn i reglene skal er det igjen 6 uke på
                        // fellesperioden
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(6),
                                fødselsdato.plusWeeks(UKER_MK).plusWeeks(17).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(6);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        // Tom for dager
        assertThat(resultat.get(5).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void skalHåndtereAnnenPartPeriodeOverlapperMedFlereSøknadsperioderPeriodenTilSluttSkalIkkeTrekkesFra() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(10);
        var tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(40).minusDays(1);
        var fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        var tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(10).minusDays(1);

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void når_far_har_brukt_all_fellesperiode_må_mor_avslås_i_berørt_behandling() {
        var fomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6);
        var tomFarsFP = fødselsdato.plusWeeks(UKER_MK).plusWeeks(22).minusDays(1);
        var fomMorsFP1 = fødselsdato.plusWeeks(UKER_MK);
        var tomMorsFP1 = fødselsdato.plusWeeks(UKER_MK).plusWeeks(6).minusDays(1);

        var grunnlag = RegelGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP, true)))
                .behandling(new Behandling.Builder().søkerErMor(true).berørtBehandling(true))
                .revurdering(new Revurdering.Builder())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(UKER_MK).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, fomMorsFP1, tomMorsFP1)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);
        // skal gå tom for dager
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void far_skal_gå_tom_for_dager_når_mor_har_oppholdsperiode_men_far_tar_ikke_uttak_i_perioden() {

        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(80));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartPeriodeInnvilget(
                                fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE, new Trekkdager(30)))
                        .uttaksperiode(annenpartPeriodeOpphold(
                                fødselsdato.plusWeeks(6),
                                fødselsdato.plusWeeks(8).minusDays(1),
                                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)))
                .behandling(farBehandling().kreverSammenhengendeUttak(true))
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fødselsdato.plusWeeks(8),
                                fødselsdato.plusWeeks(23).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        // skal gå tom for dager
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).uttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    void skal_ikke_trekke_for_opphold_i_berørt_behandling_hvis_søker_har_søkt_i_perioden() {
        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(80));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartPeriodeInnvilget(
                                fødselsdato.plusWeeks(6),
                                fødselsdato.plusWeeks(10).minusDays(1),
                                FEDREKVOTE,
                                new Trekkdager(20)))
                        .uttaksperiode(annenpartPeriodeOpphold(
                                fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(19).minusDays(1),
                                OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)))
                .behandling(new Behandling.Builder().søkerErMor(true).berørtBehandling(true))
                .revurdering(new Revurdering.Builder().endringsdato(fødselsdato.plusWeeks(10)))
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(19).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        // skal gå tom for dager
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void skal_trekke_for_overlappende_avslått_hos_annenpart_i_berørt_behandling() {
        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(80));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fødselsdato.plusWeeks(6),
                                        fødselsdato.plusWeeks(10).minusDays(1))
                                .innvilget(false)
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FELLESPERIODE, new Trekkdager(80), Utbetalingsgrad.ZERO))
                                .build()))
                .behandling(new Behandling.Builder().søkerErMor(true).berørtBehandling(true))
                .revurdering(new Revurdering.Builder().endringsdato(fødselsdato))
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(6),
                                fødselsdato.plusWeeks(10).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        // skal gå tom for dager
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        // tom på dager
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).uttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    void skal_ikke_trekke_for_opphold_hvis_søker_har_søkt_i_perioden() {
        var fødselsdato = LocalDate.of(2019, 9, 25);

        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(80));
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(
                        FEDREKVOTE,
                        fødselsdato.plusWeeks(6),
                        fødselsdato.plusWeeks(21).minusDays(1),
                        MORS_AKTIVITET_GODKJENT))
                .oppgittPeriode(oppgittPeriode(
                        FELLESPERIODE,
                        fødselsdato.plusWeeks(21),
                        fødselsdato.plusWeeks(22).minusDays(1),
                        MORS_AKTIVITET_GODKJENT));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(FAR_ARBEIDSFORHOLD)))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartPeriodeInnvilget(
                                fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE, new Trekkdager(30)))
                        .uttaksperiode(annenpartPeriodeOpphold(
                                fødselsdato.plusWeeks(6),
                                fødselsdato.plusWeeks(21).minusDays(1),
                                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER))
                        .uttaksperiode(annenpartPeriodeInnvilget(
                                fødselsdato.plusWeeks(21),
                                fødselsdato.plusWeeks(30).minusDays(1),
                                MØDREKVOTE,
                                new Trekkdager(45))))
                .behandling(farBehandling())
                .søknad(søknad);

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        // skal gå tom for dager
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void oppholdsperioder_som_overlapper_med_annenpart_skal_fjernes() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartsPeriode(
                                FORELDREPENGER_FØR_FØDSEL,
                                fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartsPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(15).minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartsPeriode(
                                FELLESPERIODE,
                                fødselsdato.plusWeeks(15),
                                fødselsdato.plusWeeks(17),
                                forFrilans(),
                                true)))
                .behandling(farBehandling().kreverSammenhengendeUttak(true))
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fødselsdato.plusWeeks(14),
                                fødselsdato.plusWeeks(15).minusDays(1)))
                        .oppgittPeriode(OppgittPeriode.forOpphold(
                                fødselsdato.plusWeeks(15),
                                fødselsdato.plusWeeks(16).minusDays(1),
                                OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER,
                                null,
                                null))
                        .oppgittPeriode(OppgittPeriode.forOpphold(
                                fødselsdato.plusWeeks(16),
                                fødselsdato.plusWeeks(20),
                                OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER,
                                null,
                                null)));

        var resultat = fastsettPerioder(grunnlag);

        // Skal ligge igjen opphold på slutten som ikke overlapper med annenpart, alt som overlapper
        // skal fjernes
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(resultat.get(1).uttakPeriode().getOppholdÅrsak())
                .isEqualTo(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER);
        assertThat(resultat.get(1).uttakPeriode().getFom())
                .isEqualTo(fødselsdato.plusWeeks(17).plusDays(1));
        assertThat(resultat.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(20));
    }

    @Test
    void oppholdsperioder_som_overlapper_med_annenpart_uten_trekkdager_skal_ikke_fjernes() {
        var annenpartAktivitet =
                new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, Trekkdager.ZERO, Utbetalingsgrad.ZERO);
        var annenpartPeriodeUtenTrekkdager = AnnenpartUttakPeriode.Builder.uttak(
                        fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16))
                .innvilget(false)
                .uttakPeriodeAktivitet(annenpartAktivitet)
                .build();
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartsPeriode(
                                FORELDREPENGER_FØR_FØDSEL,
                                fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartsPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(15).minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartPeriodeUtenTrekkdager))
                .behandling(farBehandling().kreverSammenhengendeUttak(true))
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fødselsdato.plusWeeks(14),
                                fødselsdato.plusWeeks(15).minusDays(1)))
                        .oppgittPeriode(OppgittPeriode.forOpphold(
                                annenpartPeriodeUtenTrekkdager.getFom(),
                                annenpartPeriodeUtenTrekkdager.getTom(),
                                OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER,
                                null,
                                null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(resultat.get(1).uttakPeriode().getOppholdÅrsak())
                .isEqualTo(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER);
    }

    @Test
    void oppholdsperioder_som_overlapper_med_annenpart_innvilget_utsettelse_skal_fjernes() {
        var annenpartAktivitet =
                new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, Trekkdager.ZERO, Utbetalingsgrad.ZERO);
        var annenpartPeriodeUtsettelse = AnnenpartUttakPeriode.Builder.utsettelse(
                        fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16))
                .innvilget(true)
                .uttakPeriodeAktivitet(annenpartAktivitet)
                .build();
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartsPeriode(
                                FORELDREPENGER_FØR_FØDSEL,
                                fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartsPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(15).minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartPeriodeUtsettelse))
                .behandling(farBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fødselsdato.plusWeeks(14),
                                fødselsdato.plusWeeks(15).minusDays(1)))
                        .oppgittPeriode(OppgittPeriode.forOpphold(
                                annenpartPeriodeUtsettelse.getFom(),
                                annenpartPeriodeUtsettelse.getTom(),
                                OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER,
                                null,
                                null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FEDREKVOTE);
    }

    // FAGSYSTEM-85385
    @Test
    void berørt_behandling_som_har_annenpart_med_overlappende_oppholdsperiode_skal_ikke_tape_perioden() {
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartsPeriode(
                                MØDREKVOTE,
                                fødselsdato,
                                fødselsdato.plusWeeks(15).minusDays(1),
                                forFrilans(),
                                true))
                        .uttaksperiode(annenpartPeriodeOpphold(
                                fødselsdato.plusWeeks(15),
                                fødselsdato.plusWeeks(30).minusDays(1),
                                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)))
                .behandling(farBehandling().berørtBehandling(true))
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder()
                        .type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fødselsdato.plusWeeks(15),
                                fødselsdato.plusWeeks(30).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_lik_100_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(40)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(60))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(new Utbetalingsgrad(60));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_under_100_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(30)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(30))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(new Utbetalingsgrad(30));
    }

    @Test
    void samtidig_uttak_til_manuell_i_berørt_hvis_samlet_over_100_prosent_uttak_ene_over_80_prosent() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(85)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).uttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    @Test
    void samtidig_uttak_automatisk_reduksjon_i_berørt_hvis_samlet_over_100_prosent_uttak() {
        var forventetRedusertTil = 40;
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(60)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getSamtidigUttaksprosent())
                .isEqualTo(new SamtidigUttaksprosent(forventetRedusertTil));
        assertThat(resultat.get(1).uttakPeriode().getAktiviteter().stream()
                        .findFirst()
                        .orElseThrow()
                        .getUtbetalingsgrad())
                .isEqualTo(new Utbetalingsgrad(forventetRedusertTil));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_lik_150_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(50)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(100))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(new Utbetalingsgrad(100));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_lik_150_prosent_uttak_byttom() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(100)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(50))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(new Utbetalingsgrad(50));
    }

    @Test
    void samtidig_uttak_automatiseres_i_berørt_hvis_samlet_under_150_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(30)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(30))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(new Utbetalingsgrad(30));
    }

    @Test
    void samtidig_uttak_til_manuell_i_berørt_hvis_samlet_over_150_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FELLESPERIODE, new Trekkdager(5), new Utbetalingsgrad(60)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void samtidig_uttak_til_manuell_i_berørt_hvis_samlet_over_150_prosent_uttak_byttom() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(60)))
                                .innvilget(true)
                                .samtidigUttak(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(
                                FELLESPERIODE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                false,
                                new SamtidigUttaksprosent(70))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void skal_gi_riktig_saldo_for_flerbarnsdager() {
        var fh = LocalDate.of(2022, 4, 1);
        var annenpartUttakPeriode1 = AnnenpartUttakPeriode.Builder.uttak(
                        fh, fh.plusWeeks(7).minusDays(1))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), MØDREKVOTE, new Trekkdager(30), Utbetalingsgrad.HUNDRED))
                .innvilget(true)
                .build();
        var annenpartUttakPeriode2 = AnnenpartUttakPeriode.Builder.uttak(
                        fh.plusWeeks(9), fh.plusWeeks(10).minusDays(1))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), MØDREKVOTE, new Trekkdager(5), Utbetalingsgrad.HUNDRED))
                .innvilget(true)
                .flerbarnsdager(true)
                .samtidigUttak(true)
                .build();
        var annenpartUttakPeriode3 = AnnenpartUttakPeriode.Builder.uttak(
                        fh.plusWeeks(10), fh.plusWeeks(11).minusDays(1))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), MØDREKVOTE, new Trekkdager(5), Utbetalingsgrad.HUNDRED))
                .innvilget(true)
                .flerbarnsdager(true)
                .samtidigUttak(false)
                .build();
        var annenpartUttakPeriode4 = AnnenpartUttakPeriode.Builder.uttak(
                        fh.plusWeeks(11), fh.plusWeeks(12).minusDays(1))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), MØDREKVOTE, new Trekkdager(5), Utbetalingsgrad.HUNDRED))
                .innvilget(true)
                .flerbarnsdager(false)
                .samtidigUttak(false)
                .build();
        var grunnlag = basicGrunnlagFar(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartUttakPeriode1)
                        .uttaksperiode(annenpartUttakPeriode2)
                        .uttaksperiode(annenpartUttakPeriode3)
                        .uttaksperiode(annenpartUttakPeriode4))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fh.plusWeeks(8),
                                fh.plusWeeks(17).minusDays(1),
                                true,
                                new SamtidigUttaksprosent(100))))
                .kontoer(new Kontoer.Builder()
                        .flerbarnsdager(40)
                        .konto(konto(MØDREKVOTE, 100))
                        .konto(konto(FELLESPERIODE, 100))
                        .konto(konto(FEDREKVOTE, 100)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(6);
        // Siste periode knekkes og siste 5 dagene går til manuell pga tom for flerbarnsdager
        assertThat(resultat.get(5).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(5).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(resultat.get(5).uttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        // 5 dager over flerbarns-kvoten. Dette er pga vi ikke skal dobbelt trekke flerbarnsdager i
        // perioder der begge har oppgitt flerbarnsdager
        assertThat(resultat.get(5).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
    }

    @Test
    void skal_ikke_trekke_dobbelt_flerbarnsdager_når_begge_foreldre_oppgir_flerbarnsdager() {
        var fh = LocalDate.of(2022, 4, 1);
        var annenpartUttakPeriode1 = AnnenpartUttakPeriode.Builder.uttak(
                        fh, fh.plusWeeks(7).minusDays(1))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), MØDREKVOTE, new Trekkdager(30), Utbetalingsgrad.HUNDRED))
                .innvilget(true)
                .build();
        var annenpartUttakPeriode2 = AnnenpartUttakPeriode.Builder.uttak(
                        fh.plusWeeks(9), fh.plusWeeks(10).minusDays(1))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), MØDREKVOTE, new Trekkdager(5), Utbetalingsgrad.HUNDRED))
                .innvilget(true)
                .flerbarnsdager(true)
                .samtidigUttak(false)
                .build();
        var grunnlag = basicGrunnlagFar(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartUttakPeriode1)
                        .uttaksperiode(annenpartUttakPeriode2))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(
                                FEDREKVOTE,
                                fh.plusWeeks(8),
                                fh.plusWeeks(15).minusDays(1),
                                true,
                                new SamtidigUttaksprosent(100))))
                .kontoer(new Kontoer.Builder()
                        .flerbarnsdager(34)
                        .konto(konto(MØDREKVOTE, 100))
                        .konto(konto(FELLESPERIODE, 100))
                        .konto(konto(FEDREKVOTE, 100)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        // Siste periode knekkes og siste 1 dagene går til manuell pga tom for flerbarnsdager
        assertThat(resultat.get(3).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(3).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(resultat.get(3).uttakPeriode().getManuellbehandlingårsak())
                .isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(resultat.get(3).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));
    }

    @Test
    void overlappende_gradering_avslås_i_berørt_selv_om_samlet_under_100_prosent_uttak() {
        var fh = LocalDate.of(2022, 4, 1);
        var grunnlag = basicGrunnlagMor(fh)
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(
                                        fh.plusWeeks(6), fh.plusWeeks(7).minusDays(1))
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                                        forFrilans(), FEDREKVOTE, new Trekkdager(5), new Utbetalingsgrad(30)))
                                .innvilget(true)
                                .build()))
                .behandling(morBehandling().berørtBehandling(true))
                .søknad(søknad(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, fh, fh.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(gradertoppgittPeriode(
                                MØDREKVOTE,
                                fh.plusWeeks(6),
                                fh.plusWeeks(7).minusDays(1),
                                BigDecimal.valueOf(30),
                                Set.of(ARBEIDSFORHOLD))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    private AnnenpartUttakPeriode annenpartPeriodeOpphold(LocalDate fom, LocalDate tom, OppholdÅrsak oppholdÅrsak) {
        return AnnenpartUttakPeriode.Builder.opphold(fom, tom, oppholdÅrsak)
                .innvilget(true)
                .build();
    }

    private AnnenpartUttakPeriode annenpartPeriodeInnvilget(
            LocalDate fom, LocalDate tom, Stønadskontotype stønadskontotype, Trekkdager trekkdager) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .innvilget(true)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        forFrilans(), stønadskontotype, trekkdager, Utbetalingsgrad.HUNDRED))
                .build();
    }

    private AnnenpartUttakPeriode lagPeriodeForFar(
            Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, boolean samtidigUttak) {
        return lagPeriode(
                stønadskontotype,
                fom,
                tom,
                FAR_ARBEIDSFORHOLD,
                new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)),
                samtidigUttak);
    }

    private AnnenpartUttakPeriode lagPeriode(
            Stønadskontotype stønadskontotype,
            LocalDate fom,
            LocalDate tom,
            AktivitetIdentifikator aktivitet,
            Trekkdager trekkdager,
            boolean samtidigUttak) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .samtidigUttak(samtidigUttak)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(
                        aktivitet, stønadskontotype, trekkdager, Utbetalingsgrad.TEN))
                .build();
    }
}
