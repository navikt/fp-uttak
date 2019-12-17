package no.nav.foreldrepenger.uttaksvilkår;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.FastsattUttakPeriodeAktivitet;
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

    @Test
    public void skal_ikke_ta_med_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttaksperiode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold, Oppholdårsaktype.FEDREKVOTE_ANNEN_FORELDER)
                .medInnvilget(true)
                .build();
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1), fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                        Stønadskontotype.FELLESPERIODE, new Trekkdager(50), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FEDREKVOTE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fomAnnenpartOpphold, tomAnnenpartOpphold, null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(100);
    }

    @Test
    public void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling_tidlig_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttaksperiode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold, Oppholdårsaktype.FEDREKVOTE_ANNEN_FORELDER)
                .medInnvilget(true)
                .build();
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1), fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                        Stønadskontotype.FELLESPERIODE, new Trekkdager(50), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FEDREKVOTE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fomAnnenpartOpphold,
                tomAnnenpartOpphold.minusWeeks(5), null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
    }

    @Test
    public void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling_sen_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttaksperiode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold, Oppholdårsaktype.FEDREKVOTE_ANNEN_FORELDER)
                .medInnvilget(true)
                .build();
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1), fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                        Stønadskontotype.FELLESPERIODE, new Trekkdager(50), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FEDREKVOTE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fomAnnenpartOpphold.plusWeeks(5),
                tomAnnenpartOpphold, null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
    }

    @Test
    public void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling_midt_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttaksperiode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold, Oppholdårsaktype.FEDREKVOTE_ANNEN_FORELDER)
                .medInnvilget(true)
                .build();
        var annenpartUttaksperiode = AnnenpartUttaksperiode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1), fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                        Stønadskontotype.FELLESPERIODE, new Trekkdager(50), BigDecimal.valueOf(100)))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FEDREKVOTE));
        var aktuellPeriode = new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fomAnnenpartOpphold.plusWeeks(2),
                tomAnnenpartOpphold.minusWeeks(3), null, false);
        var grunnlag = new RegelGrunnlag.Builder()
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet(), kontoer)))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
    }

    private SaldoUtregningGrunnlag lagGrunnlag(StønadsPeriode aktuellPeriode, RegelGrunnlag grunnlag) {
        if (grunnlag.getBehandling().isTapende()) {
            return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttakTapendeBehandling(List.of(), grunnlag.getAnnenPart().getUttaksperioder(),
                    grunnlag.getArbeid().getArbeidsforhold(), aktuellPeriode.getFom(), new ArrayList<>(grunnlag.getSøknad().getUttaksperioder()));
        }
        return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(),
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

    @Test
    public void skal_ikke_avrunde_før_saldo_arves_til_nytt_arbeidsforhold() {
        var kontoerArbeidsforhold1 = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FELLESPERIODE));
        var kontoerNyttArbeidsforhold = new Kontoer.Builder().leggTilKonto(new Konto.Builder()
                .medTrekkdager(100)
                .medType(Stønadskontotype.FELLESPERIODE));

        var utregningsdato = LocalDate.of(2019, 12, 5);
        var identifikator1 = AktivitetIdentifikator.annenAktivitet();
        var identifikatorNyttArbeidsforhold = AktivitetIdentifikator.forFrilans();
        var fastsattPeriode = new FastsattUttakPeriode.Builder()
                .medTidsperiode(utregningsdato.minusWeeks(1), utregningsdato.minusDays(1))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(BigDecimal.valueOf(2.5)), Stønadskontotype.FELLESPERIODE, identifikator1)))
                .build();
        var arbeidsforhold1 = new Arbeidsforhold(identifikator1, kontoerArbeidsforhold1);
        var nyttArbeidsforhold = new Arbeidsforhold(identifikatorNyttArbeidsforhold, kontoerNyttArbeidsforhold, utregningsdato);
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(fastsattPeriode), List.of(),
                Set.of(arbeidsforhold1, nyttArbeidsforhold), utregningsdato);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikator1)).isEqualTo(new Trekkdager(BigDecimal.valueOf(97.5)));
        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikatorNyttArbeidsforhold)).isEqualTo(new Trekkdager(BigDecimal.valueOf(97.5)));
    }
}
