package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

class RegelResultatBehandlerTest {

    @Test
    void skal_knekke_på_riktig_datoer_ved_avslag() {
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FELLESPERIODE));
        var arbeidsforhold = new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet());
        var fom = LocalDate.of(2018, 10, 10);
        var tom = LocalDate.of(2018, 11, 11);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, fom, tom, null, false,
                PeriodeVurderingType.IKKE_VURDERT, null, null, null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(false))
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold))
                .kontoer(kontoer)
                .build();
        var knekkpunkt = new TomKontoKnekkpunkt(LocalDate.of(2018, 10, 15));
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), grunnlag.getKontoer(),
                oppgittPeriode.getFom(), grunnlag.getArbeid().getAktiviteter(), grunnlag.getSøknad().getMottattTidspunkt(),
                grunnlag.getAnnenPart() == null ? null : grunnlag.getAnnenPart().getSisteSøknadMottattTidspunkt(),
                FarUttakRundtFødsel.utledFarsPeriodeRundtFødsel(grunnlag, StandardKonfigurasjon.KONFIGURASJON));
        oppgittPeriode.setAktiviteter(Set.of(arbeidsforhold.getIdentifikator()));
        var behandler = new RegelResultatBehandler(SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), grunnlag,
                StandardKonfigurasjon.KONFIGURASJON);

        var regelresultat = new FastsettePerioderRegelresultat(null, false, true, null, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, null,
                UtfallType.AVSLÅTT);
        var resultat = behandler.avslåAktuellPeriode(oppgittPeriode, regelresultat, Optional.of(knekkpunkt), false);

        assertThat(resultat.getPeriode().getFom()).isEqualTo(fom);
        assertThat(resultat.getPeriode().getTom()).isEqualTo(knekkpunkt.getDato().minusDays(1));
        assertThat(resultat.getEtterKnekkPeriode().getFom()).isEqualTo(knekkpunkt.getDato());
        assertThat(resultat.getEtterKnekkPeriode().getTom()).isEqualTo(tom);
    }
}
