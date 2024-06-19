package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import org.junit.jupiter.api.Test;

class ManglendeSøktSammenhengendeUttakOrkestreringTest
        extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_avslå_og_trekke_mødrekvote_for_mor_hvis_dager_igjen() {

        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag =
                basicGrunnlagMorSammenhengendeUttak(fødselsdato)
                        .søknad(
                                søknad(
                                        Søknadstype.FØDSEL,
                                        oppgittPeriode(
                                                Stønadskontotype.FORELDREPENGER_FØR_FØDSEL,
                                                fødselsdato.minusWeeks(3),
                                                fødselsdato.minusDays(1)),
                                        oppgittPeriode(
                                                Stønadskontotype.MØDREKVOTE,
                                                fødselsdato,
                                                fødselsdato.plusWeeks(6).minusDays(1)),
                                        // manglende søkt i mellom
                                        oppgittPeriode(
                                                Stønadskontotype.MØDREKVOTE,
                                                fødselsdato.plusWeeks(8),
                                                fødselsdato.plusWeeks(9))))
                        .kontoer(
                                kontoer(
                                        konto(Stønadskontotype.MØDREKVOTE, 1000),
                                        konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                        .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue())
                .isNotEqualByComparingTo(BigDecimal.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.MØDREKVOTE);
    }

    private Kontoer.Builder kontoer(Konto.Builder... konto) {
        var kontoer = new Kontoer.Builder();
        for (var k : konto) {
            kontoer.konto(k);
        }
        return kontoer;
    }

    @Test
    void skal_avslå_og_trekke_foreldrepenger_for_far_med_enerett_hvis_dager_igjen() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var oppgittPeriode =
                oppgittPeriode(
                        Stønadskontotype.FORELDREPENGER,
                        fødselsdato.plusWeeks(10),
                        fødselsdato.plusWeeks(12),
                        MORS_AKTIVITET_GODKJENT);
        var søknad =
                søknad(
                        Søknadstype.FØDSEL,
                        // manglende søkt blir opprettet før foreldrepenger-perioder
                        oppgittPeriode);
        var grunnlag =
                basicGrunnlagFarSammenhengendeUttak(fødselsdato)
                        .rettOgOmsorg(bareFarRett())
                        .søknad(søknad)
                        .kontoer(kontoer(konto(Stønadskontotype.FORELDREPENGER, 1000)))
                        .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue())
                .isNotZero();
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.FORELDREPENGER);
    }

    private RegelGrunnlag.Builder basicGrunnlagFarSammenhengendeUttak(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .behandling(farBehandling().kreverSammenhengendeUttak(true));
    }

    private RegelGrunnlag.Builder basicGrunnlagMorSammenhengendeUttak(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .behandling(morBehandling().kreverSammenhengendeUttak(true));
    }

    @Test
    void skal_avslå_og_ikke_trekke_dager_når_alle_kontoer_går_tom() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag =
                basicGrunnlagMorSammenhengendeUttak(fødselsdato)
                        .søknad(
                                søknad(
                                        Søknadstype.FØDSEL,
                                        oppgittPeriode(
                                                Stønadskontotype.FORELDREPENGER_FØR_FØDSEL,
                                                fødselsdato.minusWeeks(3),
                                                fødselsdato.minusDays(1)),
                                        oppgittPeriode(
                                                Stønadskontotype.MØDREKVOTE,
                                                fødselsdato,
                                                fødselsdato.plusWeeks(6).minusDays(1)),
                                        // manglende søkt i mellom
                                        oppgittPeriode(
                                                Stønadskontotype.MØDREKVOTE,
                                                fødselsdato.plusWeeks(8),
                                                fødselsdato.plusWeeks(9))))
                        .kontoer(
                                kontoer(
                                        konto(Stønadskontotype.MØDREKVOTE, 30),
                                        konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                        .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue())
                .isZero();
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isNull();
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void
            skal_avslå_og_ikke_trekke_dager_når_alle_kontoer_går_tom_midt_i_en_manglede_søkt_periode() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag =
                basicGrunnlagFarSammenhengendeUttak(fødselsdato)
                        .rettOgOmsorg(bareFarRett())
                        .kontoer(
                                new Kontoer.Builder()
                                        .konto(
                                                new Konto.Builder()
                                                        .trekkdager(30)
                                                        .type(FORELDREPENGER)))
                        .søknad(
                                søknad(
                                        Søknadstype.FØDSEL,
                                        oppgittPeriode(
                                                FORELDREPENGER,
                                                fødselsdato.plusWeeks(6),
                                                fødselsdato.plusWeeks(10)),
                                        // Bare far har rett, msp i mellom
                                        oppgittPeriode(
                                                FORELDREPENGER,
                                                fødselsdato.plusWeeks(15),
                                                fødselsdato.plusWeeks(20))))
                        .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue())
                .isNotZero();
        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isNull();
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue())
                .isZero();
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    void
            skal_kunne_håndtere_ulikt_antall_dager_gjenværende_på_arbeidsforhold_ved_manglende_søkt_periode() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var kontoer =
                new Kontoer.Builder()
                        .konto(konto(Stønadskontotype.MØDREKVOTE, 75))
                        .konto(konto(Stønadskontotype.FELLESPERIODE, 1));
        var arbeid =
                new Arbeid.Builder()
                        .arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                        .arbeidsforhold(
                                new Arbeidsforhold(
                                        AktivitetIdentifikator.forSelvstendigNæringsdrivende()));
        // En fastsatt periode for å få ulikt antall saldo
        var fastsattPeriode =
                new FastsattUttakPeriode.Builder()
                        .tidsperiode(fødselsdato.minusDays(1), fødselsdato.minusDays(1))
                        .aktiviteter(
                                List.of(
                                        new FastsattUttakPeriodeAktivitet(
                                                new Trekkdager(1),
                                                Stønadskontotype.FELLESPERIODE,
                                                AktivitetIdentifikator.forFrilans()),
                                        new FastsattUttakPeriodeAktivitet(
                                                new Trekkdager(0),
                                                Stønadskontotype.FELLESPERIODE,
                                                AktivitetIdentifikator
                                                        .forSelvstendigNæringsdrivende())))
                        .periodeResultatType(Perioderesultattype.INNVILGET);
        var grunnlag =
                basicGrunnlagMorSammenhengendeUttak(fødselsdato)
                        .søknad(
                                søknad(
                                        Søknadstype.FØDSEL,
                                        oppgittPeriode(
                                                Stønadskontotype.MØDREKVOTE,
                                                fødselsdato,
                                                fødselsdato.plusWeeks(15).minusDays(1)),
                                        // Har igjen 1 dag på fellesperiode på ett arbeidsforhold
                                        // når manglende søkt skal behandles
                                        oppgittPeriode(
                                                Stønadskontotype.MØDREKVOTE,
                                                fødselsdato.plusWeeks(16),
                                                fødselsdato.plusWeeks(17).minusDays(1))))
                        .kontoer(kontoer)
                        .arbeid(arbeid)
                        .revurdering(
                                new Revurdering.Builder()
                                        .endringsdato(fødselsdato)
                                        .gjeldendeVedtak(
                                                new Vedtak.Builder()
                                                        .leggTilPeriode(fastsattPeriode)))
                        .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        // UT1291
        assertThat(perioder.get(3).isManuellBehandling()).isTrue();
    }

    @Test
    void manglende_søkt_periode_før_nytt_arbeidsforhold_tilkommer() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var arbeidsforhold = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
        var arbeidsforholdMedId =
                AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), "1234");

        var grunnlag =
                RegelGrunnlagTestBuilder.create()
                        .arbeid(
                                new Arbeid.Builder()
                                        .arbeidsforhold(new Arbeidsforhold(arbeidsforhold))
                                        .arbeidsforhold(
                                                new Arbeidsforhold(
                                                        arbeidsforholdMedId,
                                                        fødselsdato.plusWeeks(12))))
                        .kontoer(defaultKontoer())
                        .inngangsvilkår(oppfyltAlleVilkår())
                        .datoer(new Datoer.Builder().fødsel(fødselsdato))
                        .behandling(morBehandling().kreverSammenhengendeUttak(true))
                        .rettOgOmsorg(beggeRett())
                        .søknad(
                                new Søknad.Builder()
                                        .type(Søknadstype.FØDSEL)
                                        .oppgittPeriode(
                                                oppgittPeriode(
                                                        Stønadskontotype.FORELDREPENGER_FØR_FØDSEL,
                                                        fødselsdato.minusWeeks(3),
                                                        fødselsdato.minusDays(1)))
                                        .oppgittPeriode(
                                                oppgittPeriode(
                                                        Stønadskontotype.MØDREKVOTE,
                                                        fødselsdato,
                                                        fødselsdato.plusWeeks(10).minusDays(1)))
                                        .oppgittPeriode(
                                                oppgittPeriode(
                                                        Stønadskontotype.FELLESPERIODE,
                                                        fødselsdato.plusWeeks(11),
                                                        fødselsdato.plusWeeks(15).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(6);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(3 * 5));
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(6 * 5));
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom())
                .isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(4 * 5));
        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).uttakPeriode().getTom())
                .isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(3).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(3).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(3).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).uttakPeriode().getTom())
                .isEqualTo(fødselsdato.plusWeeks(11).minusDays(3)); // tar ikke med helg

        assertThat(perioder.get(4).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(4).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(4).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(4).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(perioder.get(4).uttakPeriode().getTom())
                .isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));

        assertThat(perioder.get(5).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(5).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(5).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(3 * 5));
        assertThat(perioder.get(5).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(12));
        assertThat(perioder.get(5).uttakPeriode().getTom())
                .isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));
    }

    @Test
    void skal_avslå_og_trekke_foreldrepenger_for_bare_far_har_rett_hvis_dager_igjen_regresjon() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag =
                basicGrunnlagFarSammenhengendeUttak(fødselsdato)
                        .søknad(
                                søknad(
                                        FØDSEL,
                                        oppgittPeriode(
                                                FORELDREPENGER,
                                                fødselsdato.plusWeeks(50),
                                                fødselsdato.plusWeeks(52))))
                        .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                        .rettOgOmsorg(bareFarRett())
                        .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0())
                .isFalse();
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0())
                .isFalse();
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().fødsel(fødselsdato);
    }

    @Test
    void foreldrepengerFørFødsel_for_kort_fpff_starter_for_sent() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag =
                basicGrunnlagMorSammenhengendeUttak(fødselsdato)
                        .søknad(
                                søknad(
                                        FØDSEL,
                                        oppgittPeriode(
                                                FORELDREPENGER_FØR_FØDSEL,
                                                fødselsdato.minusWeeks(1),
                                                fødselsdato.minusDays(1))));
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(1));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(0).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).evalueringResultat()).isNotNull();

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype())
                .isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom())
                .isEqualTo(fødselsdato.plusWeeks(6).minusDays(3));
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(30));
        assertThat(perioder.get(1).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).evalueringResultat()).isNotNull();
    }
}
