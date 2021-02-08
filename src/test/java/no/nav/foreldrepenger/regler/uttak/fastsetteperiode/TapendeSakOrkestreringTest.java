package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

;

public class TapendeSakOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    private static final AktivitetIdentifikator MOR_ARBEIDSFORHOLD = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;

    private final LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    @Test
    public void skal_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_innvilget_samme_tidsrom() {
        /*
           Far søker fedrekvote samtidig som mor tar fellesperiode. Far har ikke omsorg i denne perioden og skal få avslag og egentlig trukket dager.
           Skal ikke trekke dager siden mor har innvilget i samme tidsrom
         */
        PeriodeUtenOmsorg periodeUtenOmsorg = new PeriodeUtenOmsorg(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
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

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isZero();
    }

    @Test
    public void skal_ikke_sette_0_trekkdager_når_perioden_avslås_men_annen_forelder_har_avslått_samme_tidsrom() {
        PeriodeUtenOmsorg periodeUtenOmsorg = new PeriodeUtenOmsorg(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
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

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var resultatPeriode = resultat.get(0).getUttakPeriode();
        assertThat(resultatPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultatPeriode.getUtbetalingsgrad(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultatPeriode.getTrekkdager(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
    }

    static AnnenpartUttakPeriode annenpartsPeriode(Stønadskontotype stønadskontotype,
                                                   LocalDate fom,
                                                   LocalDate tom,
                                                   AktivitetIdentifikator aktivitet,
                                                   boolean innvilget) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitet, stønadskontotype,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), Utbetalingsgrad.TEN))
                .medInnvilget(innvilget)
                .build();
    }

}
