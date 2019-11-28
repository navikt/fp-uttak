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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;

public class FastsettePerioderRegelOrkestreringTapendeSakTest extends FastsettePerioderRegelOrkestreringTestBase {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();
    private static final AktivitetIdentifikator MOR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;


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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16), MOR_ARBEIDSFORHOLD, true)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPerioderUtenOmsorg(periodeUtenOmsorg)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

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
                        .medFørsteLovligeUttaksdag(førsteLovligeDato))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenpartsPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), MOR_ARBEIDSFORHOLD, true))
                        .leggTilUttaksperiode(annenpartsPeriode(FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16), MOR_ARBEIDSFORHOLD, false)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(periodeUtenOmsorg)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        UttakPeriode resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isZero();
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
    }

    private UttakPeriode uttakPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    static AnnenpartUttaksperiode annenpartsPeriode(Stønadskontotype stønadskontotype,
                                                       LocalDate fom,
                                                       LocalDate tom,
                                                       AktivitetIdentifikator aktivitet,
                                                       boolean innvilget) {
        return AnnenpartUttaksperiode.Builder.uttak(fom, tom)
                .medSamtidigUttak(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitet, stønadskontotype,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), BigDecimal.TEN))
                .medInnvilget(innvilget)
                .build();
    }

}
