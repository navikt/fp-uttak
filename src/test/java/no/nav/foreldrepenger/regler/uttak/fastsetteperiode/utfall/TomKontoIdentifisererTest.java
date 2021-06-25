package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

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

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, int arbeidsprosent, int virkedagerVarighet) {
        verifiserKnekkpunktVedGradering(søktOmDag, saldo, BigDecimal.valueOf(arbeidsprosent), virkedagerVarighet);
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, BigDecimal arbeidsprosent, int virkedagerVarighet) {
        var idag = LocalDate.of(2019, 3, 14);

        var oppgittPeriode = OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, idag, idag.plusDays(søktOmDag - 1),
                arbeidsprosent, null, false, Set.of(ARBEIDSFORHOLD_1), PeriodeVurderingType.IKKE_VURDERT, null, null, null);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(saldo));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
                .behandling(new Behandling.Builder().søkerErMor(true))
                .kontoer(kontoer)
                .build();

        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), grunnlag.getKontoer(),
                oppgittPeriode.getFom(), grunnlag.getArbeid().getAktiviteter(), grunnlag.getSøknad().getMottattTidspunkt(),
                grunnlag.getAnnenPart() == null ? null : grunnlag.getAnnenPart().getSisteSøknadMottattTidspunkt());
        var saldoUtregning = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);
        var tomKontoKnekkpunkt = TomKontoIdentifiserer.identifiser(oppgittPeriode, List.of(ARBEIDSFORHOLD_1), saldoUtregning,
                Stønadskontotype.MØDREKVOTE, true);
        assertThat(tomKontoKnekkpunkt.orElseThrow().getDato()).isEqualTo(Virkedager.plusVirkedager(idag, virkedagerVarighet));
    }
}
