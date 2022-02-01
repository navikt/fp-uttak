package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.annenAktivitet;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forArbeid;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forFrilans;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forSelvstendigNæringsdrivende;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class SaldoUtregningTjenesteTest {

    @Test
    void skal_knekke_annenparts_perioder() {
        var fomAnnenpart = LocalDate.of(2019, 12, 2);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(50), Utbetalingsgrad.FULL))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));
        var aktuellPeriode = oppgittPeriode(FELLESPERIODE, fomAnnenpart.plusWeeks(5), tomAnnenpart);
        var grunnlag = new RegelGrunnlag.Builder().annenPart(new AnnenPart.Builder().uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder())
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(FELLESPERIODE)).isEqualTo(75);
    }

    private OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, PeriodeVurderingType.IKKE_VURDERT, null, null,
                null);
    }

    @Test
    void skal_ikke_ta_med_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_berørt_behandling() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                FEDREKVOTE_ANNEN_FORELDER).innvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                        fomAnnenpartOpphold.minusWeeks(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(FEDREKVOTE, fomAnnenpartOpphold, tomAnnenpartOpphold);
        var grunnlag = new RegelGrunnlag.Builder().annenPart(
                        new AnnenPart.Builder().uttaksperiode(annenpartOpphold).uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder().berørtBehandling(true))
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(FEDREKVOTE)).isEqualTo(100);
    }

    @Test
    void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_berørt_behandling_tidlig_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                FEDREKVOTE_ANNEN_FORELDER).innvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                        fomAnnenpartOpphold.minusWeeks(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(FEDREKVOTE, fomAnnenpartOpphold, tomAnnenpartOpphold.minusWeeks(5));
        var grunnlag = new RegelGrunnlag.Builder().annenPart(
                        new AnnenPart.Builder().uttaksperiode(annenpartOpphold).uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder().berørtBehandling(true))
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(FEDREKVOTE)).isEqualTo(75);
    }

    @Test
    void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_berørt_behandling_sen_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                FEDREKVOTE_ANNEN_FORELDER).innvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                        fomAnnenpartOpphold.minusWeeks(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(FEDREKVOTE, fomAnnenpartOpphold.plusWeeks(5), tomAnnenpartOpphold);
        var grunnlag = new RegelGrunnlag.Builder().annenPart(
                        new AnnenPart.Builder().uttaksperiode(annenpartOpphold).uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder().berørtBehandling(true))
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(FEDREKVOTE)).isEqualTo(75);
    }

    @Test
    void skal_ta_med_deler_av_opphold_på_annenpart_hvis_overlapp_med_søkers_periode_og_berørt_behandling_midt_overlapp() {
        var fomAnnenpartOpphold = LocalDate.of(2019, 12, 2);
        var tomAnnenpartOpphold = fomAnnenpartOpphold.plusWeeks(10).minusDays(1);
        var annenpartOpphold = AnnenpartUttakPeriode.Builder.opphold(fomAnnenpartOpphold, tomAnnenpartOpphold,
                FEDREKVOTE_ANNEN_FORELDER).innvilget(true).build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpartOpphold.minusWeeks(1),
                        fomAnnenpartOpphold.minusWeeks(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 100));
        var aktuellPeriode = oppgittPeriode(FEDREKVOTE, fomAnnenpartOpphold.plusWeeks(2), tomAnnenpartOpphold.minusWeeks(3));
        var grunnlag = new RegelGrunnlag.Builder().annenPart(
                        new AnnenPart.Builder().uttaksperiode(annenpartOpphold).uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder().berørtBehandling(true))
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var saldoUtregningGrunnlag = lagGrunnlag(aktuellPeriode, grunnlag);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldo(FEDREKVOTE)).isEqualTo(75);
    }

    private SaldoUtregningGrunnlag lagGrunnlag(OppgittPeriode aktuellPeriode, RegelGrunnlag grunnlag) {
        if (grunnlag.getBehandling().isBerørtBehandling()) {
            return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttakBerørtBehandling(List.of(),
                    grunnlag.getAnnenPart().getUttaksperioder(), grunnlag.getKontoer(), aktuellPeriode.getFom(),
                    new ArrayList<>(grunnlag.getSøknad().getOppgittePerioder()), grunnlag.getArbeid().getAktiviteter());
        }
        return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), grunnlag.getAnnenPart().getUttaksperioder(),
                grunnlag.getKontoer(), aktuellPeriode.getFom(), grunnlag.getArbeid().getAktiviteter(),
                grunnlag.getSøknad().getMottattTidspunkt(),
                grunnlag.getAnnenPart() == null ? null : grunnlag.getAnnenPart().getSisteSøknadMottattTidspunkt());
    }

    @Test
    void skal_knekke_annenparts_perioder_overlapp_annenpart_starter_en_dag_før() {
        var fomAnnenpart = LocalDate.of(2019, 12, 3);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(50), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));
        var aktuellPeriode = oppgittPeriode(FELLESPERIODE, fomAnnenpart.plusDays(1), tomAnnenpart.plusWeeks(10));
        var grunnlag = new RegelGrunnlag.Builder().annenPart(new AnnenPart.Builder().uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder())
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var resultat = SaldoUtregningTjeneste.lagUtregning(lagGrunnlag(aktuellPeriode, grunnlag));

        assertThat(resultat.saldo(FELLESPERIODE)).isEqualTo(99);
    }

    @Test
    void skal_ikke_knekke_annenparts_perioder_ved_berørt_behandling() {
        var fomAnnenpart = LocalDate.of(2019, 12, 3);
        var tomAnnenpart = fomAnnenpart.plusWeeks(10).minusDays(1);
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(fomAnnenpart, tomAnnenpart)
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(70), Utbetalingsgrad.HUNDRED))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));
        var aktuellPeriode = oppgittPeriode(FELLESPERIODE, fomAnnenpart.plusWeeks(5).minusDays(1), tomAnnenpart);
        var grunnlag = new RegelGrunnlag.Builder().annenPart(new AnnenPart.Builder().uttaksperiode(annenpartUttaksperiode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(annenAktivitet())))
                .behandling(new Behandling.Builder().berørtBehandling(true))
                .søknad(new Søknad.Builder().oppgittPeriode(aktuellPeriode))
                .build();

        var resultat = SaldoUtregningTjeneste.lagUtregning(lagGrunnlag(aktuellPeriode, grunnlag));

        assertThat(resultat.saldo(FELLESPERIODE)).isEqualTo(100 - 70);
    }

    @Test
    void skal_ikke_avrunde_før_saldo_arves_til_nytt_arbeidsforhold() {
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));

        var utregningsdato = LocalDate.of(2019, 12, 5);
        var identifikator = annenAktivitet();
        var identifikatorNyttArbeidsforhold = forFrilans();
        var fastsattPeriode = new FastsattUttakPeriode.Builder().tidsperiode(utregningsdato.minusWeeks(1), utregningsdato.minusDays(1))
                .periodeResultatType(INNVILGET)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2.5), FELLESPERIODE, identifikator)))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(fastsattPeriode), List.of(),
                kontoer.build(), utregningsdato, Set.of(identifikator, identifikatorNyttArbeidsforhold), null, null);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(FELLESPERIODE, identifikator)).isEqualTo(new Trekkdager(97.5));
        assertThat(resultat.saldoITrekkdager(FELLESPERIODE, identifikatorNyttArbeidsforhold)).isEqualTo(new Trekkdager(97.5));
    }

    @Test
    void skal_ikke_dobbelt_trekke_fra_annenpart_når_arbeidsforhold_starter_ila_uttaket() {
        var kontoer = new Kontoer.Builder().konto(konto(MØDREKVOTE, 100)).konto(konto(FELLESPERIODE, 100));

        var utregningsdato = LocalDate.MAX;
        var identifikator = forArbeid(new Orgnummer("123"), "456");
        var identifikatorNyttArbeidsforhold = forArbeid(new Orgnummer("123"), "789");
        var fastsattPeriode = new FastsattUttakPeriode.Builder().tidsperiode(LocalDate.of(2019, 12, 18), LocalDate.of(2019, 12, 19))
                .periodeResultatType(INNVILGET)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikator)))
                .build();
        var annenpartsPeriode = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 12, 11), LocalDate.of(2019, 12, 17))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forSelvstendigNæringsdrivende(), MØDREKVOTE, new Trekkdager(100),
                                Utbetalingsgrad.HUNDRED))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(fastsattPeriode),
                List.of(annenpartsPeriode), kontoer.build(), utregningsdato, Set.of(identifikator, identifikatorNyttArbeidsforhold),
                null, null);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(MØDREKVOTE, identifikator)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.saldoITrekkdager(MØDREKVOTE, identifikatorNyttArbeidsforhold)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void skal_arve_saldo_flere_ganger() {
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 155));

        var utregningsdato = LocalDate.MAX;
        var identifikator = forArbeid(new Orgnummer("123"), "456");
        var identifikatorNyttArbeidsforhold1 = forArbeid(new Orgnummer("123"), "789");
        var identifikatorNyttArbeidsforhold2 = forArbeid(new Orgnummer("345"), null);
        var fastsattPeriode1 = new FastsattUttakPeriode.Builder().tidsperiode(LocalDate.of(2019, 12, 18), LocalDate.of(2019, 12, 18))
                .periodeResultatType(INNVILGET)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikator)))
                .build();
        var fastsattPeriode2 = new FastsattUttakPeriode.Builder().tidsperiode(LocalDate.of(2019, 12, 19), LocalDate.of(2019, 12, 19))
                .periodeResultatType(INNVILGET)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikator),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikatorNyttArbeidsforhold1)))
                .build();
        var fastsattPeriode3 = new FastsattUttakPeriode.Builder().tidsperiode(LocalDate.of(2019, 12, 20), LocalDate.of(2019, 12, 20))
                .periodeResultatType(INNVILGET)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikator),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikatorNyttArbeidsforhold1),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(50), FELLESPERIODE, identifikatorNyttArbeidsforhold2)))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(
                List.of(fastsattPeriode1, fastsattPeriode2, fastsattPeriode3), List.of(), kontoer.build(), utregningsdato,
                Set.of(identifikator, identifikatorNyttArbeidsforhold1, identifikatorNyttArbeidsforhold2), null, null);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(FELLESPERIODE, identifikator)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.saldoITrekkdager(FELLESPERIODE, identifikatorNyttArbeidsforhold1)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.saldoITrekkdager(FELLESPERIODE, identifikatorNyttArbeidsforhold2)).isEqualTo(new Trekkdager(5));
    }

    @Test
    void skal_regne_riktig_flerbarnsdager_hvis_annenpart_har_nyoppstartet_arbeidsforhold() {
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100)).konto(konto(FLERBARNSDAGER, 50));

        var søkersArbeidsforhold = forArbeid(new Orgnummer("123"), "456");
        var fastsattPeriode = new FastsattUttakPeriode.Builder().tidsperiode(LocalDate.of(2019, 12, 18), LocalDate.of(2019, 12, 18))
                .periodeResultatType(INNVILGET)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, søkersArbeidsforhold)))
                .build();
        var annenpartsArbeidsforhold1 = forArbeid(new Orgnummer("123"), "789");
        var annenpartsArbeidsforhold2 = forSelvstendigNæringsdrivende();
        var annenpartPeriode1 = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 12, 17), LocalDate.of(2019, 12, 17))
                .flerbarnsdager(true)
                .innvilget(true)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(annenpartsArbeidsforhold1, FELLESPERIODE, new Trekkdager(1),
                        Utbetalingsgrad.HUNDRED))
                .build();
        var annenpartPeriode2 = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 12, 19), LocalDate.of(2019, 12, 19))
                .flerbarnsdager(false)
                .innvilget(true)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(annenpartsArbeidsforhold1, FELLESPERIODE, new Trekkdager(1),
                        Utbetalingsgrad.HUNDRED))
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(annenpartsArbeidsforhold2, FELLESPERIODE, new Trekkdager(1),
                        Utbetalingsgrad.HUNDRED))
                .build();
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvHeleUttaket(List.of(fastsattPeriode), false,
                List.of(annenpartPeriode1, annenpartPeriode2), kontoer.build(), null, null);
        var resultat = SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag);

        assertThat(resultat.saldoITrekkdager(FELLESPERIODE, søkersArbeidsforhold)).isEqualTo(new Trekkdager(97));
        assertThat(resultat.saldoITrekkdager(FLERBARNSDAGER, søkersArbeidsforhold)).isEqualTo(new Trekkdager(49));
    }

    @DisplayName("FAGSYSTEM-204667 Oppholdsperiode hos søker ligger før endringsdato, slik at den ikke knekkes mot annen parts perioder")
    @Test
    void oppholdsperiode_hos_søker_som_ikke_er_knekt() {
        var opphold = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .tidsperiode(LocalDate.of(2021, 11, 1), LocalDate.of(2021, 11, 28))
                .build();
        var uttakEtterOpphold = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
                .tidsperiode(opphold.getTom().plusDays(1), opphold.getTom().plusDays(1))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, forFrilans())))
                .build();
        var annenpartUttaksperiode1 = AnnenpartUttakPeriode.Builder.uttak(opphold.getFom(), opphold.getFom().plusWeeks(1).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(25), Utbetalingsgrad.FULL))
                .build();
        var annenpartUttaksperiode2 = AnnenpartUttakPeriode.Builder.uttak(annenpartUttaksperiode1.getTom().plusDays(1),
                        opphold.getTom().minusWeeks(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(25), Utbetalingsgrad.FULL))
                .build();
        var annenpartUttaksperiode3 = AnnenpartUttakPeriode.Builder.uttak(annenpartUttaksperiode2.getTom().plusDays(1),
                        opphold.getTom())
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(25), Utbetalingsgrad.FULL))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));

        var grunnlag = SaldoUtregningGrunnlag.forUtregningAvHeleUttaket(List.of(opphold, uttakEtterOpphold), false,
                List.of(annenpartUttaksperiode1, annenpartUttaksperiode2, annenpartUttaksperiode3), kontoer.build(),
                LocalDateTime.of(annenpartUttaksperiode1.getFom(), LocalTime.NOON),
                LocalDateTime.of(opphold.getFom(), LocalTime.NOON));
        var resultat = SaldoUtregningTjeneste.lagUtregning(grunnlag);

        //100 - 25 - 25 - 25 - 1
        assertThat(resultat.saldo(FELLESPERIODE)).isEqualTo(24);
    }

    @Test
    void oppholdsperiode_hos_søker_der_annenpart_ikke_har_fylt_hele_perioden() {
        var opphold = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .tidsperiode(LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 4))
                .build();
        var uttakEtterOpphold = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
                .tidsperiode(LocalDate.of(2022, 2, 7), LocalDate.of(2022, 2, 7))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, forFrilans())))
                .build();
        var annenpartUttaksperiode = AnnenpartUttakPeriode.Builder.uttak(opphold.getFom(), LocalDate.of(2022, 2, 2))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(3), Utbetalingsgrad.FULL))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));

        var grunnlag = SaldoUtregningGrunnlag.forUtregningAvHeleUttaket(List.of(opphold, uttakEtterOpphold), false,
                List.of(annenpartUttaksperiode), kontoer.build(),
                LocalDateTime.of(annenpartUttaksperiode.getFom(), LocalTime.NOON),
                LocalDateTime.of(opphold.getFom(), LocalTime.NOON));
        var resultat = SaldoUtregningTjeneste.lagUtregning(grunnlag);

        //100 - 3 - 2 - 1
        assertThat(resultat.saldo(FELLESPERIODE)).isEqualTo(94);
    }

    @Test
    void oppholdsperiode_hos_søker_der_annenpart_har_hull_i_oppholdsperioden() {
        var opphold = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .tidsperiode(LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 4))
                .build();
        var uttakEtterOpphold = new FastsattUttakPeriode.Builder().periodeResultatType(INNVILGET)
                .tidsperiode(LocalDate.of(2022, 2, 7), LocalDate.of(2022, 2, 7))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, forFrilans())))
                .build();
        var annenpartUttaksperiode1 = AnnenpartUttakPeriode.Builder.uttak(opphold.getFom(), LocalDate.of(2022, 2, 1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(2), Utbetalingsgrad.FULL))
                .build();
        var annenpartUttaksperiode2 = AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2022, 2, 4), LocalDate.of(2022, 2, 4))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(forFrilans(), FELLESPERIODE, new Trekkdager(1), Utbetalingsgrad.FULL))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FELLESPERIODE, 100));

        var grunnlag = SaldoUtregningGrunnlag.forUtregningAvHeleUttaket(List.of(opphold, uttakEtterOpphold), false,
                List.of(annenpartUttaksperiode1, annenpartUttaksperiode2), kontoer.build(),
                LocalDateTime.of(annenpartUttaksperiode1.getFom(), LocalTime.NOON),
                LocalDateTime.of(opphold.getFom(), LocalTime.NOON));
        var resultat = SaldoUtregningTjeneste.lagUtregning(grunnlag);

        //100 - 2 - 2 - 1 - 1
        assertThat(resultat.saldo(FELLESPERIODE)).isEqualTo(94);
    }

    private Konto.Builder konto(Stønadskontotype type, int dager) {
        return new Konto.Builder().trekkdager(dager).type(type);
    }
}
