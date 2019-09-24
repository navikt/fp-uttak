package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
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

public class FastsettePerioderRegelOrkestreringBerørtSakTest {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();
    private static final int UKER_FPFF = 3;
    private static final int UKER_MK = 15;
    private static final int UKER_FK = 15;
    private static final int UKER_FP = 16;
    private static final AktivitetIdentifikator MOR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
    private static final AktivitetIdentifikator FAR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;


    private LocalDate førsteLovligeDato = LocalDate.of(2017, 10, 1);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);


    @Test
    public void skal_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_innvilget_samme_tidsrom() {
        /*
           Far søker fedrekvote samtidig som mor tar fellesperiode. Far har ikke omsorg i denne perioden og skal få avslag og egentlig trukket dager.
           Skal ikke trekke dager siden mor har innvilget i samme tidsrom
         */
        PeriodeUtenOmsorg periodeUtenOmsorg = new PeriodeUtenOmsorg(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16), MOR_ARBEIDSFORHOLD, true))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(periodeUtenOmsorg)
                                .build())
                        .build());

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        UttakPeriode resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isZero();
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isZero();
    }

    @Test
    public void skal_ikke_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_avslått_samme_tidsrom() {
        PeriodeUtenOmsorg periodeUtenOmsorg = new PeriodeUtenOmsorg(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeDato)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16), MOR_ARBEIDSFORHOLD, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(periodeUtenOmsorg)
                                .build())
                        .build());

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        UttakPeriode resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isZero();
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
    }

    private UttakPeriode uttakPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    private FastsattPeriodeAnnenPart annenpartsPeriode(Stønadskontotype stønadskontotype,
                                                       LocalDate fom,
                                                       LocalDate tom,
                                                       AktivitetIdentifikator aktivitet,
                                                       boolean innvilget) {
        return new FastsattPeriodeAnnenPart.Builder(fom, tom)
                .medSamtidigUttak(true)
                .medInnvilgetUtsettelse(false)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitet, stønadskontotype,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), BigDecimal.TEN))
                .medInnvilget(innvilget)
                .build();
    }

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

}
