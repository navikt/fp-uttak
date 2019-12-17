package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningTjeneste;

public class RegelResultatBehandlerImplTest {

    @Test
    public void skal_knekke_på_riktig_datoer_ved_avslag() {
        var arbeidsforhold = new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FELLESPERIODE)));
        var fom = LocalDate.of(2018, 10, 10);
        var tom = LocalDate.of(2018, 11, 11);
        var uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                fom, tom, null, false);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(uttakPeriode))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(false))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .build();
        var knekkpunkt = new TomKontoKnekkpunkt(LocalDate.of(2018, 10, 15));
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(),
                List.of(), grunnlag.getArbeid().getArbeidsforhold(), uttakPeriode.getFom());
        var behandler = new RegelResultatBehandlerImpl(SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        uttakPeriode.setSluttpunktTrekkerDager(arbeidsforhold.getIdentifikator(), true);
        var resultat = behandler.avslåAktuellPeriode(uttakPeriode, Optional.of(knekkpunkt), null, true, false);

        assertThat(resultat.getPeriode().getFom()).isEqualTo(fom);
        assertThat(resultat.getPeriode().getTom()).isEqualTo(knekkpunkt.getDato().minusDays(1));
        assertThat(resultat.getEtterKnekkPeriode().getFom()).isEqualTo(knekkpunkt.getDato());
        assertThat(resultat.getEtterKnekkPeriode().getTom()).isEqualTo(tom);
    }
}
