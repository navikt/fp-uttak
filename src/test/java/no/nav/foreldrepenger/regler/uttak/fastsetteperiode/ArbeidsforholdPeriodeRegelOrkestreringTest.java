package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;

class ArbeidsforholdPeriodeRegelOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_starte_arbeidsforhold_fra_startdato() {
        var omsorgsovertakelse = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = ARBEIDSFORHOLD;
        var arbeidsforhold2 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold3 = AktivitetIdentifikator.forFrilans();
        var arbeidsforhold4 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(arbeidsforhold1, omsorgsovertakelse.minusYears(1)))
                //Ingen startdato på arbeidsforholdet skal tolkes som at arbeidsforholdet varer hele uttaket
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold2))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold3, omsorgsovertakelse.plusWeeks(8)))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold4, omsorgsovertakelse.plusWeeks(35)));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, omsorgsovertakelse, omsorgsovertakelse.plusWeeks(40));
        var grunnlag = basicGrunnlag()
                .arbeid(arbeid)
                .søknad(søknad(Søknadstype.ADOPSJON, mødrekvote))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(omsorgsovertakelse))
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(aktiviteterIPeriode(resultat.get(0).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(aktiviteterIPeriode(resultat.get(1).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2,
                arbeidsforhold3);
        assertThat(aktiviteterIPeriode(resultat.get(2).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2,
                arbeidsforhold3);
        assertThat(aktiviteterIPeriode(resultat.get(3).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2,
                arbeidsforhold3, arbeidsforhold4);
    }

    @Test
    void nytt_arbeidsforhold_skal_hente_saldo_fra_eksisterende_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold2 = AktivitetIdentifikator.forFrilans();
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(arbeidsforhold1))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold2, fødselsdato.plusWeeks(8)));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote1 = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var gradertMødrekvote = gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                BigDecimal.valueOf(50), Set.of(arbeidsforhold1));
        var mødrekvote2 = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(11).minusDays(1));
        var grunnlag = basicGrunnlag()
                .arbeid(arbeid)
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote1, gradertMødrekvote, mødrekvote2))
                .datoer(new Datoer.Builder().fødsel(fødselsdato));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(aktiviteterIPeriode(resultat.get(0).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1);
        assertThat(aktiviteterIPeriode(resultat.get(1).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1);
        assertThat(aktiviteterIPeriode(resultat.get(2).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1);
        assertThat(aktiviteterIPeriode(resultat.get(3).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold1)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold2)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void nytt_arbeidsforhold_skal_hente_saldo_fra_arbeidsforholdet_som_har_høyest_saldo_på_startdato() {
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = ARBEIDSFORHOLD;
        var arbeidsforhold2 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold3 = AktivitetIdentifikator.forFrilans();
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(arbeidsforhold1))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold2))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold3, fødselsdato.plusWeeks(10)));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote1 = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(8).minusDays(1));
        var gradertMødrekvote = gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(12).minusDays(1),
                BigDecimal.valueOf(50), Set.of(arbeidsforhold1));
        var grunnlag = basicGrunnlag()
                .arbeid(arbeid)
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote1, gradertMødrekvote))
                .datoer(new Datoer.Builder().fødsel(fødselsdato));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(6);
        assertThat(aktiviteterIPeriode(resultat.get(0).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(aktiviteterIPeriode(resultat.get(1).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(aktiviteterIPeriode(resultat.get(2).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        assertThat(aktiviteterIPeriode(resultat.get(3).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2);
        //NYtt arbeidsforhold arver saldo fra arbeidsforhold 1 pga arbeidsforhold 1 har gradert i perioden før
        assertThat(aktiviteterIPeriode(resultat.get(4).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2,
                arbeidsforhold3);
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold1)).isEqualTo(new Utbetalingsgrad(50));
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold2)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold3)).isEqualTo(Utbetalingsgrad.HUNDRED);

        //Nytt arbeidsforhold går tomt for dager, arbeidsforhold holder 1 uke til pga gradering
        assertThat(aktiviteterIPeriode(resultat.get(5).getUttakPeriode())).containsExactlyInAnyOrder(arbeidsforhold1, arbeidsforhold2,
                arbeidsforhold3);
        assertThat(resultat.get(5).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold1)).isEqualTo(new Utbetalingsgrad(50));
        assertThat(resultat.get(5).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold2)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(5).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold3)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    void nytt_arbeidsforhold_skal_hente_saldo_fra_arbeidsforholdet_som_har_høyest_saldo_på_startdato_2() {
        //Testen sjekker om det nye arbeidsforholdet vinner dager på at begge eksisterende arbeidsforhold graderer to uker hver
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold1 = ARBEIDSFORHOLD;
        var arbeidsforhold2 = AktivitetIdentifikator.annenAktivitet();
        var arbeidsforhold3 = AktivitetIdentifikator.forFrilans();
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(arbeidsforhold1))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold2))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold3, fødselsdato.plusWeeks(10)));
        var mødrekvote1 = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var gradertMødrekvote1 = gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                BigDecimal.valueOf(50), Set.of(arbeidsforhold1));
        var gradertMødrekvote2 = gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(10).minusDays(1),
                BigDecimal.valueOf(50), Set.of(arbeidsforhold2));
        var mødrekvote2 = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1));
        var grunnlag = basicGrunnlag()
                .arbeid(arbeid)
                .søknad(søknad(Søknadstype.FØDSEL, mødrekvote1, gradertMødrekvote1, gradertMødrekvote2, mødrekvote2))
                .datoer(new Datoer.Builder().fødsel(fødselsdato));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(5);

        //Arver 45 dager, har igjen 50. Nok til en uke. Siste uken skal avslås for tom for dager
        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(arbeidsforhold3)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    void skal_ikke_ta_hensyn_til_startdato_hvis_bare_ett_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 11, 27);
        var arbeidsforhold = AktivitetIdentifikator.annenAktivitet();
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(arbeidsforhold, fødselsdato.plusWeeks(4)));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        var grunnlag = basicGrunnlag()
                .arbeid(arbeid)
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote))
                .datoer(new Datoer.Builder().fødsel(fødselsdato));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(aktiviteterIPeriode(resultat.get(0).getUttakPeriode())).containsExactly(arbeidsforhold);
        assertThat(aktiviteterIPeriode(resultat.get(1).getUttakPeriode())).containsExactly(arbeidsforhold);
        assertThat(aktiviteterIPeriode(resultat.get(2).getUttakPeriode())).containsExactly(arbeidsforhold);
    }

    @Test
    void skal_kunne_lage_manglende_søkt_periode_i_periode_uten_at_søker_har_aktive_arbeidsforhold() {
        var fødsel = LocalDate.of(2018, 10, 26);
        var tilkommetArbeidsforhold1 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var tilkommetArbeidsforhold2 = AktivitetIdentifikator.forFrilans();
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(tilkommetArbeidsforhold1, LocalDate.of(2019, 5, 1)))
                .arbeidsforhold(new Arbeidsforhold(tilkommetArbeidsforhold2, LocalDate.of(2019, 6, 10)));
        var utsettelseArbeid = utsettelsePeriode(LocalDate.of(2019, 11, 4), LocalDate.of(2019, 12, 6), UtsettelseÅrsak.ARBEID);
        var fpPeriode = oppgittPeriode(FORELDREPENGER, LocalDate.of(2019, 12, 16), LocalDate.of(2020, 1, 3));
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(utsettelseArbeid.getFom(), fpPeriode.getTom(), I_AKTIVITET));
        var søknad = søknad(Søknadstype.FØDSEL, utsettelseArbeid, fpPeriode).dokumentasjon(dokumentasjon);
        var grunnlag = basicGrunnlag()
                .arbeid(arbeid)
                .søknad(søknad)
                .rettOgOmsorg(bareFarRett())
                .behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødsel));

        var resultat = fastsettPerioder(grunnlag);
        //Skal legge inn aktivitet med tidligst startdato hvis alle arbeidsforhold starter etter manglende søkt
        assertThat(resultat.get(0).getUttakPeriode().getAktiviteter().stream().findFirst().orElseThrow().getIdentifikator()).isEqualTo(
                tilkommetArbeidsforhold1);
    }
}
