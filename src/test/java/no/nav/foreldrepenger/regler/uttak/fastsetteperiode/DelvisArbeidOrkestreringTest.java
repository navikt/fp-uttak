package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

public class DelvisArbeidOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {
    public static final LocalDate FØDSELSDATO = LocalDate.of(2024, 6, 17);

    @Nested
    class BareFarHarRett {
        @Test
        void far_fullt_uttak_mor_over_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(100));

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(100));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_fullt_uttak_mor_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(75));

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(100));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_fullt_uttak_mor_under_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(40));

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_graderer_20_prosent_mor_er_over_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(100), 20);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(80));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(24)); //  80% av 30
        }

        @Test
        void far_graderer_40_prosent_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(40), 40);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_graderer_60_prosent_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(40), 60);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_graderer_80_prosent_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagOppgittPeriode(new MorsStillingsprosent(40), 80);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(20));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(15)); // 50% av 30
        }
    }

    @Nested
    class BeggeHarRett {
        @Test
        void far_graderer_80_prosent_mor_arbeider_40_prosent_fellesperiode() {
            var farsArbeidsprosent = 80;
            var morsStillingsprosent = new MorsStillingsprosent(40);
            var oppgittPeriode = OppgittPeriode.forGradering(
                FELLESPERIODE,
                FØDSELSDATO.plusWeeks(6), // 30 dager
                FØDSELSDATO.plusWeeks(12).minusDays(1),
                BigDecimal.valueOf(farsArbeidsprosent),
                null,
                false,
                Set.of(ARBEIDSFORHOLD),
                FØDSELSDATO,
                FØDSELSDATO,
                ARBEID,
                morsStillingsprosent,
                DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);

            var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(FØDSELSDATO))
                .datoer(new Datoer.Builder().fødsel(FØDSELSDATO))
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(new Kontoer.Builder().konto(FELLESPERIODE, 100));

            var fastsattePerioder = fastsettPerioder(grunnlag);

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(20));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(15)); // 50% av 30
        }

    }


    private static OppgittPeriode lagOppgittPeriode(MorsStillingsprosent morsStillingsprosent) {
        return OppgittPeriode.forVanligPeriode(
            FORELDREPENGER,
            FØDSELSDATO.plusWeeks(6), // 30 dager
            FØDSELSDATO.plusWeeks(12).minusDays(1),
            null,
            false,
            FØDSELSDATO,
            FØDSELSDATO,
            ARBEID,
            morsStillingsprosent,
            DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);
    }

    private static OppgittPeriode lagOppgittPeriode(MorsStillingsprosent morsStillingsprosent, Integer farsArbeidsprosent) {
        return OppgittPeriode.forGradering(
            FORELDREPENGER,
            FØDSELSDATO.plusWeeks(6), // 30 dager
            FØDSELSDATO.plusWeeks(12).minusDays(1),
            BigDecimal.valueOf(farsArbeidsprosent),
            null,
            false,
            Set.of(ARBEIDSFORHOLD),
            FØDSELSDATO,
            FØDSELSDATO,
            ARBEID,
            morsStillingsprosent,
            DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);
    }

    private RegelGrunnlag.Builder lagGrunnlag(OppgittPeriode oppgittPeriode) {
        return new RegelGrunnlag.Builder().behandling(farBehandling())
            .opptjening(new Opptjening.Builder().skjæringstidspunkt(FØDSELSDATO))
            .datoer(new Datoer.Builder().fødsel(FØDSELSDATO))
            .rettOgOmsorg(bareFarRett())
            .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(new Kontoer.Builder().konto(FORELDREPENGER, 100));
    }

}
