package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;

public class TomKontoIdentifisererTest {

    @Test
    public void knekkpunkt_ved_50_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 50, 2);
    }


    @Test
    public void knekkpunkt_ved_80_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 80, 5);
    }

    @Test
    public void knekkpunkt_ved_0_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 0, 1);
    }

    @Test
    public void knekkpunkt_ved_10_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 10, 2);
    }

    @Test
    public void eksempel_1_fra_krav_9() {
        verifiserKnekkpunktVedGradering(17, 4, 60, 10);
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, int arbeidsprosent, int virkedagerVarighet) {
        verifiserKnekkpunktVedGradering(søktOmDag, saldo, BigDecimal.valueOf(arbeidsprosent), virkedagerVarighet);
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, BigDecimal arbeidsprosent, int virkedagerVarighet) {
        LocalDate idag = LocalDate.of(2019, 3, 14);

        StønadsPeriode uttakPeriode = StønadsPeriode.medGradering(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, idag, idag.plusDays(søktOmDag - 1),
                Collections.singletonList(ARBEIDSFORHOLD_1), arbeidsprosent, PeriodeVurderingType.PERIODE_OK);
        uttakPeriode.setSluttpunktTrekkerDager(ARBEIDSFORHOLD_1, true);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .medTrekkdager(saldo));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(uttakPeriode))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Trekkdagertilstand trekkdagertilstand = Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode));
        Optional<TomKontoKnekkpunkt> tomKontoKnekkpunkt = TomKontoIdentifiserer.identifiser(uttakPeriode, Collections.singletonList(ARBEIDSFORHOLD_1), trekkdagertilstand, Stønadskontotype.MØDREKVOTE);
        Assertions.assertThat(tomKontoKnekkpunkt.get().getDato()).isEqualTo(Virkedager.plusVirkedager(idag, virkedagerVarighet));
    }
}
