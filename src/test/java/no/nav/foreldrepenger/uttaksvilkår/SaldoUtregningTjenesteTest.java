package no.nav.foreldrepenger.uttaksvilkår;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class SaldoUtregningTjenesteTest {

    @Test
    public void skal_knekke_annenparts_perioder() {
        var fomAnnenpart = LocalDate.of(2019, 12, 2);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE, new Trekkdager(50), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FELLESPERIODE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fomAnnenpart.plusWeeks(5), tomAnnenpart, null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder())
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FELLESPERIODE)).isEqualTo(75);
    }

    private SaldoUtregningGrunnlag lagGrunnlag(StønadsPeriode aktuellPeriode, RegelGrunnlag grunnlag) {
        return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), grunnlag.getBehandling().isTapende(),
                grunnlag.getAnnenPart().getUttaksperioder(), grunnlag.getArbeid().getArbeidsforhold(), aktuellPeriode.getFom());
    }

    @Test
    public void skal_knekke_annenparts_perioder_overlapp_annenpart_starter_en_dag_før() {
        var fomAnnenpart = LocalDate.of(2019, 12, 3);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE, new Trekkdager(50), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FELLESPERIODE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fomAnnenpart.plusDays(1), tomAnnenpart.plusWeeks(10), null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder())
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var resultat = SaldoUtregningTjeneste.lagUtregning(lagGrunnlag(aktuellPeriode, grunnlag));

        assertThat(resultat.saldo(Stønadskontotype.FELLESPERIODE)).isEqualTo(99);
    }

    @Test
    public void skal_ikke_knekke_annenparts_perioder_ved_tapende_behandling() {
        var fomAnnenpart = LocalDate.of(2019, 12, 3);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE, new Trekkdager(70), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FELLESPERIODE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fomAnnenpart.plusWeeks(5).minusDays(1), tomAnnenpart, null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var resultat = SaldoUtregningTjeneste.lagUtregning(lagGrunnlag(aktuellPeriode, grunnlag));

        assertThat(resultat.saldo(Stønadskontotype.FELLESPERIODE)).isEqualTo(100 - 70);
    }
}
