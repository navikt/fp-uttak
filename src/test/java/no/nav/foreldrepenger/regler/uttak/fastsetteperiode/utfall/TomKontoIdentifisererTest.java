package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;

class TomKontoIdentifisererTest {

    @Test
    void knekkpunkt_ved_50_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 50, 2);
    }


    @Test
    void knekkpunkt_ved_80_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 80, 5);
    }

    @Test
    void knekkpunkt_ved_0_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 0, 1);
    }

    @Test
    void knekkpunkt_ved_10_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 10, 2);
    }

    @Test
    void eksempel_1_fra_krav_9() {
        verifiserKnekkpunktVedGradering(17, 4, 60, 10);
    }

    @Test
    void knekkpunkt_ved_redusert_samtidig_uttaksprosent_uten_søkt_samtidig_uttak() {
        var idag = LocalDate.of(2019, 3, 18);

        var knekkpunkt = identifiserKnekkpunkt(idag, 10, 3, BigDecimal.valueOf(100), false, false, new SamtidigUttaksprosent(50));

        assertThat(knekkpunkt.orElseThrow().dato()).isEqualTo(Virkedager.plusVirkedager(idag, 6));
    }

    @Test
    void ingen_knekkpunkt_når_redusert_samtidig_uttak_bruker_hele_periodens_saldo() {
        var idag = LocalDate.of(2019, 3, 18);

        var knekkpunkt = identifiserKnekkpunkt(idag, 10, 5, BigDecimal.valueOf(100), false, false, new SamtidigUttaksprosent(50));

        assertThat(knekkpunkt).isEmpty();
    }

    @Test
    void samtidig_uttaksprosent_zero_kaster_ikke_arithmetic_exception() {
        var idag = LocalDate.of(2019, 3, 18);

        var knekkpunkt = identifiserKnekkpunkt(idag, 10, 3, BigDecimal.valueOf(100), false, false, SamtidigUttaksprosent.ZERO);

        assertThat(knekkpunkt).isPresent();
    }

    @Test
    void knekkpunkt_ved_ikke_heltallig_redusert_samtidig_uttak() {
        var idag = LocalDate.of(2019, 3, 18);

        var knekkpunkt = identifiserKnekkpunkt(idag, 10, 2, BigDecimal.valueOf(100), false, false, new SamtidigUttaksprosent(60));

        // saldoTilVirkedagerVedRedusertUttak runder (2 + 1) / 0.6 ned til 5, og trekker én virkedag.
        assertThat(knekkpunkt.orElseThrow().dato()).isEqualTo(Virkedager.plusVirkedager(idag, 4));
    }

    @Test
    void knekkpunkt_ved_avslått_gradering_bruker_samtidig_uttaksprosent() {
        var idag = LocalDate.of(2019, 3, 18);

        var knekkpunkt = identifiserKnekkpunkt(idag, 10, 3, BigDecimal.valueOf(20), true, true, new SamtidigUttaksprosent(50));

        assertThat(knekkpunkt.orElseThrow().dato()).isEqualTo(Virkedager.plusVirkedager(idag, 6));
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, int arbeidsprosent, int virkedagerVarighet) {
        verifiserKnekkpunktVedGradering(søktOmDag, saldo, BigDecimal.valueOf(arbeidsprosent), virkedagerVarighet);
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, BigDecimal arbeidsprosent, int virkedagerVarighet) {
        var idag = LocalDate.of(2019, 3, 14);

        var oppgittPeriode = OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, idag, idag.plusDays(søktOmDag - 1), arbeidsprosent, null, false,
            Set.of(ARBEIDSFORHOLD_1), null, null, null, null, null);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(saldo));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
            .behandling(new Behandling.Builder().søkerErMor(true))
            .kontoer(kontoer)
            .build();

        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), grunnlag, oppgittPeriode.getFom());
        var saldoUtregning = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);
        var tomKontoKnekkpunkt = TomKontoIdentifiserer.identifiser(oppgittPeriode, List.of(ARBEIDSFORHOLD_1), saldoUtregning,
            Stønadskontotype.MØDREKVOTE, null, false, true, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, UtfallType.INNVILGET, false, null);
        assertThat(tomKontoKnekkpunkt.orElseThrow().dato()).isEqualTo(Virkedager.plusVirkedager(idag, virkedagerVarighet));
    }

    private Optional<TomKontoKnekkpunkt> identifiserKnekkpunkt(LocalDate fom,
                                                               int virkedager,
                                                               int saldo,
                                                               BigDecimal arbeidsprosent,
                                                               boolean graderer,
                                                               boolean graderingAvslag,
                                                               SamtidigUttaksprosent samtidigUttaksprosent) {
        var tom = Virkedager.plusVirkedager(fom, virkedager).minusDays(1);
        var aktiviteter = graderer ? Set.of(ARBEIDSFORHOLD_1) : Set.<AktivitetIdentifikator>of();
        var oppgittPeriode = graderer ? OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, fom, tom, arbeidsprosent, null, false, aktiviteter,
            null, null, null, null, null) : OppgittPeriode.forVanligPeriode(Stønadskontotype.MØDREKVOTE, fom, tom, null, false, null, null, null, null,
            null);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(saldo));
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
            .behandling(new Behandling.Builder().søkerErMor(true))
            .kontoer(kontoer)
            .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), grunnlag, oppgittPeriode.getFom());
        var saldoUtregning = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);
        return TomKontoIdentifiserer.identifiser(oppgittPeriode, List.of(ARBEIDSFORHOLD_1), saldoUtregning, Stønadskontotype.MØDREKVOTE, null,
            false, true, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, UtfallType.INNVILGET, graderingAvslag, samtidigUttaksprosent);
    }
}
