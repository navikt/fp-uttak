package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak.MOR_OPPFYLLER_IKKE_AKTIVITETSKRAV;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

public class DelvisArbeidOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {
    public static final LocalDate FØDSELSDATO = LocalDate.of(2024, 6, 17);
    public static final AktivitetIdentifikator FRILANS = AktivitetIdentifikator.forFrilans();

    @Nested
    class BareFarHarRett {
        @Test
        void far_fullt_uttak_mor_over_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(FORELDREPENGER, null, null);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(100));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_fullt_uttak_mor_akkurat_ikke_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(FORELDREPENGER, new MorsStillingsprosent(BigDecimal.valueOf(74.99)), null);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(74.99));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_fullt_uttak_mor_under_75_prosent_i_arbeid() {
            var oppgittPeriode = lagOppgittPeriode(FORELDREPENGER, new MorsStillingsprosent(40), null);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_søker_80_prosent_uttak_mor_er_over_75_prosent_i_arbeid() {
            var oppgittPeriode = lagGraderingsperiode(FORELDREPENGER, null, 20, ARBEIDSFORHOLD);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(80));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(24)); //  80% av 30
        }

        @Test
        void far_søker_60_prosent_uttak_mor_arbeider_40_prosent_to_aktiviteter() {
            var oppgittPeriode = lagGraderingsperiode(FORELDREPENGER, new MorsStillingsprosent(40), 40, ARBEIDSFORHOLD);

            var grunnlag = lagGrunnlag(oppgittPeriode, FORELDREPENGER)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)).arbeidsforhold(new Arbeidsforhold(FRILANS)));

            var fastsattePerioder = fastsettPerioder(grunnlag);

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isEqualTo(MOR_OPPFYLLER_IKKE_AKTIVITETSKRAV);

            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(FRILANS)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(FRILANS)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_søker_40_prosent_uttak_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagGraderingsperiode(FORELDREPENGER, new MorsStillingsprosent(40), 60, ARBEIDSFORHOLD);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_søker_30_prosent_uttak_mor_arbeider_30_prosent_tom_for_dager() {
            var oppgittPeriode = OppgittPeriode.forGradering(FORELDREPENGER, FØDSELSDATO.plusWeeks(6),
                FØDSELSDATO.plusWeeks(100).minusDays(1), BigDecimal.valueOf(70), null, false, Set.of(ARBEIDSFORHOLD), FØDSELSDATO,
                FØDSELSDATO, ARBEID, new MorsStillingsprosent(30), DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(2);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(30));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(100));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isEqualTo(MOR_OPPFYLLER_IKKE_AKTIVITETSKRAV);

            assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
            assertThat(fastsattePerioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
            assertThat(fastsattePerioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        }

        @Test
        void far_søker_20_prosent_uttak_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagGraderingsperiode(FORELDREPENGER, new MorsStillingsprosent(40), 80, ARBEIDSFORHOLD);

            var fastsattePerioder = fastsettPerioder(lagGrunnlag(oppgittPeriode, FORELDREPENGER));

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID);
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(20));
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(15)); // 50% av 30
        }
    }

    @Nested
    class BeggeHarRett {
        @Test
        void far_søker_20_prosent_uttak_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagGraderingsperiode(FELLESPERIODE, new MorsStillingsprosent(40), 80, ARBEIDSFORHOLD);
            var grunnlag = lagGrunnlag(oppgittPeriode, FELLESPERIODE);

            var fastsattePerioder = fastsettPerioder(grunnlag);

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID);
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(20));
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(15)); // 50% av 30
        }

        @Test
        void far_søker_60_prosent_uttak_mor_arbeider_40_prosent_to_aktiviteter() {
            var oppgittPeriode = lagGraderingsperiode(FELLESPERIODE, new MorsStillingsprosent(40), 40, ARBEIDSFORHOLD);

            var grunnlag = lagGrunnlag(oppgittPeriode, FELLESPERIODE)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)).arbeidsforhold(new Arbeidsforhold(FRILANS)));

            var fastsattePerioder = fastsettPerioder(grunnlag);

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getGraderingIkkeInnvilgetÅrsak()).isEqualTo(MOR_OPPFYLLER_IKKE_AKTIVITETSKRAV);

            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(FRILANS)).isEqualTo(new Utbetalingsgrad(40));
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(FRILANS)).isEqualTo(new Trekkdager(30));
        }

        @Test
        void far_søker_50_prosent_uttak_mor_arbeider_40_prosent() {
            var oppgittPeriode = lagOppgittPeriode(FELLESPERIODE, new MorsStillingsprosent(40), SamtidigUttaksprosent.FIFTY);
            var grunnlag = lagGrunnlag(oppgittPeriode, FELLESPERIODE);

            var fastsattePerioder = fastsettPerioder(grunnlag);

            assertThat(fastsattePerioder).hasSize(1);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_FELLESPERIODE_TIL_FAR);
            assertThat(fastsattePerioder.getFirst().uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID);
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
//            assertThat(fastsattePerioder.getFirst().uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        }

    }


    private static OppgittPeriode lagOppgittPeriode(Stønadskontotype stønadskontotype, MorsStillingsprosent morsStillingsprosent, SamtidigUttaksprosent samtidigUttaksprosent) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, FØDSELSDATO.plusWeeks(6), // 30 dager
            FØDSELSDATO.plusWeeks(12).minusDays(1), samtidigUttaksprosent, false, FØDSELSDATO, FØDSELSDATO, ARBEID, morsStillingsprosent,
            DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);
    }

    private static OppgittPeriode lagGraderingsperiode(Stønadskontotype stønadskontotype, MorsStillingsprosent morsStillingsprosent, Integer farsArbeidsprosent, AktivitetIdentifikator gradertArbeidsforhold) {
        return OppgittPeriode.forGradering(stønadskontotype, FØDSELSDATO.plusWeeks(6), // 30 dager
            FØDSELSDATO.plusWeeks(12).minusDays(1), BigDecimal.valueOf(farsArbeidsprosent), null, false, Set.of(gradertArbeidsforhold), FØDSELSDATO,
            FØDSELSDATO, ARBEID, morsStillingsprosent, DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);
    }

    private RegelGrunnlag.Builder lagGrunnlag(OppgittPeriode oppgittPeriode, Stønadskontotype stønadskontotype) {
        return new RegelGrunnlag.Builder().behandling(farBehandling())
            .opptjening(new Opptjening.Builder().skjæringstidspunkt(FØDSELSDATO))
            .datoer(new Datoer.Builder().fødsel(FØDSELSDATO))
            .rettOgOmsorg(stønadskontotype.equals(FORELDREPENGER) ? bareSøkerRett() : beggeRett())
            .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(new Kontoer.Builder().konto(stønadskontotype, 100));
    }

}
