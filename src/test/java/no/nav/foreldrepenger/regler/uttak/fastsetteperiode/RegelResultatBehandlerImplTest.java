package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class RegelResultatBehandlerImplTest {

    @Test
    public void skal_knekke_på_riktig_datoer_ved_avslag() {
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FELLESPERIODE));
        var arbeidsforhold = new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet());
        var fom = LocalDate.of(2018, 10, 10);
        var tom = LocalDate.of(2018, 11, 11);
        var uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                fom, tom, null, false);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(uttakPeriode))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(false))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .medKontoer(kontoer)
                .build();
        var knekkpunkt = new TomKontoKnekkpunkt(LocalDate.of(2018, 10, 15));
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(),
                List.of(), grunnlag.getArbeid().getArbeidsforhold(), grunnlag.getKontoer(), uttakPeriode.getFom());
        var behandler = new RegelResultatBehandlerImpl(SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        uttakPeriode.setSluttpunktTrekkerDager(arbeidsforhold.getIdentifikator(), true);
        var resultat = behandler.avslåAktuellPeriode(uttakPeriode, Optional.of(knekkpunkt), null, true, false);

        assertThat(resultat.getPeriode().getFom()).isEqualTo(fom);
        assertThat(resultat.getPeriode().getTom()).isEqualTo(knekkpunkt.getDato().minusDays(1));
        assertThat(resultat.getEtterKnekkPeriode().getFom()).isEqualTo(knekkpunkt.getDato());
        assertThat(resultat.getEtterKnekkPeriode().getTom()).isEqualTo(tom);
    }
}
