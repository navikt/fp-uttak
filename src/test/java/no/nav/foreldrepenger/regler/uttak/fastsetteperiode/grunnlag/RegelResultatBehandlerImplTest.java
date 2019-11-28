package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class RegelResultatBehandlerImplTest {

    @Test
    public void skal_knekke_på_riktig_datoer_ved_avslag() {
        var arbeidsforhold = new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FELLESPERIODE)));
        RegelGrunnlag regelGrunnlag = RegelGrunnlagTestBuilder.create()
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(false))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .build();
        Trekkdagertilstand trekkdagertilstand = Trekkdagertilstand.ny(regelGrunnlag, Collections.emptyList());
        RegelResultatBehandlerImpl behandler = new RegelResultatBehandlerImpl(trekkdagertilstand, regelGrunnlag, StandardKonfigurasjon.KONFIGURASJON);

        LocalDate fom = LocalDate.of(2018, 10, 10);
        LocalDate tom = LocalDate.of(2018, 11, 11);
        TomKontoKnekkpunkt knekkpunkt = new TomKontoKnekkpunkt(LocalDate.of(2018, 10, 15));
        UttakPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                fom, tom, null, false);
        uttakPeriode.setSluttpunktTrekkerDager(arbeidsforhold.getIdentifikator(), true);
        RegelResultatBehandlerResultat resultat = behandler.avslåAktuellPeriode(uttakPeriode, Optional.of(knekkpunkt), null, true, false);

        assertThat(resultat.getPeriode().getFom()).isEqualTo(fom);
        assertThat(resultat.getPeriode().getTom()).isEqualTo(knekkpunkt.getDato().minusDays(1));
        assertThat(resultat.getEtterKnekkPeriode().getFom()).isEqualTo(knekkpunkt.getDato());
        assertThat(resultat.getEtterKnekkPeriode().getTom()).isEqualTo(tom);
    }

}
