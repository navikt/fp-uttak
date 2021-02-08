package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

;

public class SaldoUtregningTjenesteTest {

    @Test
    public void skal_knekke_annenparts_perioder() {
        var fomAnnenpart = LocalDate.of(2019, 12, 2);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(50), Utbetalingsgrad.FULL))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FELLESPERIODE, fomAnnenpart.plusWeeks(5), tomAnnenpart);
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder())
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FELLESPERIODE)).isEqualTo(75);
    }

    private OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, PeriodeVurderingType.IKKE_VURDERT, null, null);
    }

    @Test
    public void skal_ikke_ta_med_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER).medInnvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FEDREKVOTE, fomAnnenpartOpphold, tomAnnenpartOpphold);
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(
                new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(100);
    }

    @Test
    public void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling_tidlig_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER).medInnvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FEDREKVOTE, fomAnnenpartOpphold, tomAnnenpartOpphold.minusWeeks(5));
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(
                new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
    }

    @Test
    public void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling_sen_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER).medInnvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FEDREKVOTE, fomAnnenpartOpphold.plusWeeks(5), tomAnnenpartOpphold);
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(
                new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
    }

    @Test
    public void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_tapende_behandling_midt_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER).medInnvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                fomAnnenpartOpphold.minusWeeks(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FEDREKVOTE, fomAnnenpartOpphold.plusWeeks(2),
                tomAnnenpartOpphold.minusWeeks(3));
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(
                new AnnenPart.Builder().leggTilUttaksperiode(annenpartOpphold).leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
    }

    private SaldoUtregningGrunnlag lagGrunnlag(OppgittPeriode aktuellPeriode, RegelGrunnlag grunnlag) {
        if (grunnlag.getBehandling().isTapende()) {
            return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttakTapendeBehandling(List.of(),
                    grunnlag.getAnnenPart().getUttaksperioder(), grunnlag.getKontoer(), aktuellPeriode.getFom(),
                    new ArrayList<>(grunnlag.getSøknad().getOppgittePerioder()), grunnlag.getArbeid().getAktiviteter());
        }
        return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), grunnlag.getAnnenPart().getUttaksperioder(),
                grunnlag.getKontoer(), aktuellPeriode.getFom(), grunnlag.getArbeid().getAktiviteter());
    }

    @Test
    public void skal_knekke_annenparts_perioder_overlapp_annenpart_starter_en_dag_før() {
        var fomAnnenpart = LocalDate.of(2019, 12, 3);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FELLESPERIODE, fomAnnenpart.plusDays(1), tomAnnenpart.plusWeeks(10));
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder())
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var resultat = SaldoUtregningTjeneste.lagUtregning(lagGrunnlag(aktuellPeriode, grunnlag));

        assertThat(resultat.saldo(Stønadskontotype.FELLESPERIODE)).isEqualTo(99);
    }

    @Test
    public void skal_ikke_knekke_annenparts_perioder_ved_tapende_behandling() {
        var fomAnnenpart = LocalDate.of(2019, 12, 3);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.FELLESPERIODE,
                                new Trekkdager(70), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 100));
        var aktuellPeriode = oppgittPeriode(Stønadskontotype.FELLESPERIODE, fomAnnenpart.plusWeeks(5).minusDays(1), tomAnnenpart);
        var grunnlag = new RegelGrunnlag.Builder().medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartUttaksperiode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.annenAktivitet())))
                .medBehandling(new Behandling.Builder().medErTapende(true))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(aktuellPeriode))
                .build();

        var resultat = SaldoUtregningTjeneste.lagUtregning(lagGrunnlag(aktuellPeriode, grunnlag));

        assertThat(resultat.saldo(Stønadskontotype.FELLESPERIODE)).isEqualTo(100 - 70);
    }

    @Test
    public void skal_ikke_avrunde_før_saldo_arves_til_nytt_arbeidsforhold() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 100));

        var utregningsdato = LocalDate.of(2019, 12, 5);
        var identifikator = AktivitetIdentifikator.annenAktivitet();
        var identifikatorNyttArbeidsforhold = AktivitetIdentifikator.forFrilans();
        var fastsattPeriode = new FastsattUttakPeriode.Builder().medTidsperiode(utregningsdato.minusWeeks(1),
                utregningsdato.minusDays(1))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(
                        List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2.5), Stønadskontotype.FELLESPERIODE, identifikator)))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(fastsattPeriode), List.of(),
                kontoer.build(), utregningsdato, Set.of(identifikator, identifikatorNyttArbeidsforhold));
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikator)).isEqualTo(new Trekkdager(97.5));
        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikatorNyttArbeidsforhold)).isEqualTo(
                new Trekkdager(97.5));
    }

    @Test
    public void skal_ikke_dobbelt_trekke_fra_annenpart_når_arbeidsforhold_starter_ila_uttaket() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 100))
                .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 100));

        var utregningsdato = LocalDate.MAX;
        var identifikator = AktivitetIdentifikator.forArbeid("123", "456");
        var identifikatorNyttArbeidsforhold = AktivitetIdentifikator.forArbeid("123", "789");
        var fastsattPeriode = new FastsattUttakPeriode.Builder().medTidsperiode(LocalDate.of(2019, 12, 18), LocalDate.of(2019, 12, 19))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(
                        List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE, identifikator)))
                .build();
        var annenpartsPeriode = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 12, 11), LocalDate.of(2019, 12, 17))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forSelvstendigNæringsdrivende(),
                        Stønadskontotype.MØDREKVOTE, new Trekkdager(100), Utbetalingsgrad.HUNDRED))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(fastsattPeriode),
                List.of(annenpartsPeriode), kontoer.build(), utregningsdato, Set.of(identifikator, identifikatorNyttArbeidsforhold));
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(Stønadskontotype.MØDREKVOTE, identifikator)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.saldoITrekkdager(Stønadskontotype.MØDREKVOTE, identifikatorNyttArbeidsforhold)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void skal_arve_saldo_flere_ganger() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 155));

        var utregningsdato = LocalDate.MAX;
        var identifikator = AktivitetIdentifikator.forArbeid("123", "456");
        var identifikatorNyttArbeidsforhold1 = AktivitetIdentifikator.forArbeid("123", "789");
        var identifikatorNyttArbeidsforhold2 = AktivitetIdentifikator.forArbeid("345", null);
        var fastsattPeriode1 = new FastsattUttakPeriode.Builder().medTidsperiode(LocalDate.of(2019, 12, 18),
                LocalDate.of(2019, 12, 18))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(
                        List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE, identifikator)))
                .build();
        var fastsattPeriode2 = new FastsattUttakPeriode.Builder().medTidsperiode(LocalDate.of(2019, 12, 19),
                LocalDate.of(2019, 12, 19))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(
                        List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE, identifikator),
                                new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE,
                                        identifikatorNyttArbeidsforhold1)))
                .build();
        var fastsattPeriode3 = new FastsattUttakPeriode.Builder().medTidsperiode(LocalDate.of(2019, 12, 20),
                LocalDate.of(2019, 12, 20))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(
                        List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE, identifikator),
                                new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE,
                                        identifikatorNyttArbeidsforhold1),
                                new FastsattUttakPeriodeAktivitet(new Trekkdager(50), Stønadskontotype.FELLESPERIODE,
                                        identifikatorNyttArbeidsforhold2)))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(
                List.of(fastsattPeriode1, fastsattPeriode2, fastsattPeriode3), List.of(), kontoer.build(), utregningsdato,
                Set.of(identifikator, identifikatorNyttArbeidsforhold1, identifikatorNyttArbeidsforhold2));
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikator)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikatorNyttArbeidsforhold1)).isEqualTo(
                new Trekkdager(5));
        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, identifikatorNyttArbeidsforhold2)).isEqualTo(
                new Trekkdager(5));
    }

    @Test
    public void skal_regne_riktig_flerbarnsdager_hvis_annenpart_har_nyoppstartet_arbeidsforhold() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 100))
                .leggTilKonto(konto(Stønadskontotype.FLERBARNSDAGER, 50));

        var søkersArbeidsforhold = AktivitetIdentifikator.forArbeid("123", "456");
        var fastsattPeriode = new FastsattUttakPeriode.Builder().medTidsperiode(LocalDate.of(2019, 12, 18), LocalDate.of(2019, 12, 18))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), Stønadskontotype.FELLESPERIODE,
                        søkersArbeidsforhold)))
                .build();
        var annenpartsArbeidsforhold1 = AktivitetIdentifikator.forArbeid("123", "789");
        var annenpartsArbeidsforhold2 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var annenpartPeriode1 = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 12, 17), LocalDate.of(2019, 12, 17))
                .medFlerbarnsdager(true)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(annenpartsArbeidsforhold1, Stønadskontotype.FELLESPERIODE,
                        new Trekkdager(1), Utbetalingsgrad.HUNDRED))
                .build();
        var annenpartPeriode2 = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 12, 19), LocalDate.of(2019, 12, 19))
                .medFlerbarnsdager(false)
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(annenpartsArbeidsforhold1, Stønadskontotype.FELLESPERIODE,
                        new Trekkdager(1), Utbetalingsgrad.HUNDRED))
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(annenpartsArbeidsforhold2, Stønadskontotype.FELLESPERIODE,
                        new Trekkdager(1), Utbetalingsgrad.HUNDRED))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvHeleUttaket(List.of(fastsattPeriode), false,
                List.of(annenpartPeriode1, annenpartPeriode2), kontoer.build());
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FELLESPERIODE, søkersArbeidsforhold)).isEqualTo(new Trekkdager(97));
        assertThat(resultat.saldoITrekkdager(Stønadskontotype.FLERBARNSDAGER, søkersArbeidsforhold)).isEqualTo(new Trekkdager(49));
    }

    private Konto.Builder konto(Stønadskontotype type, int dager) {
        return new Konto.Builder().medTrekkdager(dager).medType(type);
    }
}
