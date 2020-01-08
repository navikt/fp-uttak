package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;

public class StartdatoArbeidRegelOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void skal_starte_arbeidsforhold_fra_startdato() {
        var omsorgsovertakelse = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = ARBEIDSFORHOLD;
        var arbeidsforhold2 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold3 = AktivitetIdentifikator.forFrilans();
        var arbeidsforhold4 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var arbeid = new Arbeid.Builder()
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold1, defaultKontoer(), omsorgsovertakelse.minusYears(1)))
                //Ingen startdato på arbeidsforholdet skal tolkes som at arbeidsforholdet varer hele uttaket
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold2, defaultKontoer()))
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold3, defaultKontoer(), omsorgsovertakelse.plusWeeks(8)))
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold4, defaultKontoer(), omsorgsovertakelse.plusWeeks(35)));
        var mødrekvote = søknadsperiode(MØDREKVOTE, omsorgsovertakelse, omsorgsovertakelse.plusWeeks(40));
        grunnlag.medArbeid(arbeid)
                .medSøknad(søknad(Søknadstype.ADOPSJON, mødrekvote))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(omsorgsovertakelse))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .medFørsteLovligeUttaksdag(omsorgsovertakelse.minusYears(3)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(resultat.get(1).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2, arbeidsforhold3);
        assertThat(resultat.get(2).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2, arbeidsforhold3);
        assertThat(resultat.get(3).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2, arbeidsforhold3, arbeidsforhold4);
    }

    @Test
    public void nytt_arbeidsforhold_skal_hente_saldo_fra_eksisterende_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold2 = AktivitetIdentifikator.forFrilans();
        var arbeid = new Arbeid.Builder()
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold1, defaultKontoer()))
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold2, defaultKontoer(), fødselsdato.plusWeeks(8)));
        var fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote1 = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var gradertMødrekvote = StønadsPeriode.medGradering(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                List.of(arbeidsforhold1), BigDecimal.valueOf(50), PeriodeVurderingType.IKKE_VURDERT);
        var mødrekvote2 = søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(11).minusDays(1));
        grunnlag.medArbeid(arbeid)
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote1, gradertMødrekvote, mødrekvote2))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(fødselsdato.minusYears(3)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1);
        assertThat(resultat.get(1).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1);
        assertThat(resultat.get(2).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1);
        assertThat(resultat.get(3).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold1)).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold2)).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    public void nytt_arbeidsforhold_skal_hente_saldo_fra_arbeidsforholdet_som_har_høyest_saldo_på_startdato() {
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = ARBEIDSFORHOLD;
        var arbeidsforhold2 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold3 = AktivitetIdentifikator.forFrilans();
        var arbeid = new Arbeid.Builder()
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold1, defaultKontoer()))
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold2, defaultKontoer()))
                .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold3, defaultKontoer(), fødselsdato.plusWeeks(10)));
        var fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote1 = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(8).minusDays(1));
        var gradertMødrekvote = StønadsPeriode.medGradering(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(12).minusDays(1),
                List.of(arbeidsforhold1), BigDecimal.valueOf(50), PeriodeVurderingType.IKKE_VURDERT);
        grunnlag.medArbeid(arbeid)
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote1, gradertMødrekvote))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(fødselsdato.minusYears(3)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(6);
        assertThat(resultat.get(0).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(resultat.get(1).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(resultat.get(2).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(resultat.get(3).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        //NYtt arbeidsforhold arver saldo fra arbeidsforhold 1 pga arbeidsforhold 1 har gradert i perioden før
        assertThat(resultat.get(4).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2, arbeidsforhold3);
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold1)).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold2)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold3)).isEqualByComparingTo(BigDecimal.valueOf(100));

        //Nytt arbeidsforhold går tomt for dager, arbeidsforhold holder 1 uke til pga gradering
        assertThat(resultat.get(5).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2, arbeidsforhold3);
        assertThat(resultat.get(5).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold1)).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(resultat.get(5).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold2)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultat.get(5).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold3)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_ikke_ta_hensyn_til_startdato_hvis_bare_ett_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold = AktivitetIdentifikator.annenAktivitet();
        var arbeid = new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold, defaultKontoer(), fødselsdato.plusWeeks(4)));
        var fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        grunnlag.medArbeid(arbeid)
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(fødselsdato.minusYears(3)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold);
        assertThat(resultat.get(1).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold);
        assertThat(resultat.get(2).getUttakPeriode().getAktiviteter()).containsExactlyInAnyOrder(arbeidsforhold);
    }

}
