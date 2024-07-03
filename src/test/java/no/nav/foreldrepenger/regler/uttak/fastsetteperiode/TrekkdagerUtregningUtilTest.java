package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

class TrekkdagerUtregningUtilTest {
    private static final LocalDate FOM = LocalDate.now();
    private static final LocalDate TOM = LocalDate.now().plusWeeks(6).minusDays(1);
    private static final int FULLE_TREKKDAGER = 30;


    @Test
    void reduksjon_av_trekkdager_skal_rundes_ned_ved_desimaltall() {
        var periode = vanligPeriode(null);
        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_1,
            new Utbetalingsgrad(33.33),
            true,
            null);

        // 33.33% av 30 = 9,999
        assertThat(trekkdager).isEqualTo(new Trekkdager(9.9));
    }

    @Test
    void skal_redusere_trekkdager_basert_på_utbetalingsgraden_ved_samtidig_uttak() {
        var samtidigUttaksprosent = new SamtidigUttaksprosent(40);
        var periode = vanligPeriode(samtidigUttaksprosent);

        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_1,
            new Utbetalingsgrad(60),
            true,
            null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(18));
    }

    @Test
    void fulle_trekkdager_ved_avslag_hvor_det_ikke_graderes() {
        var periode = vanligPeriode(null);

        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_1,
            Utbetalingsgrad.ZERO,
            false,
            null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(FULLE_TREKKDAGER));
    }

    @Test
    void skal_basere_seg_på_utbetalingsgrader_der_hvor_det_er_gradering_med_utbetaling() {
        var arbeidstidsprosent = BigDecimal.valueOf(44);// Skal ikke basere seg på denne, men arbeisdprosenten derfor "feil" verdi
        var periode = graderingsperiode(Stønadskontotype.FORELDREPENGER, arbeidstidsprosent, Set.of());

        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_1,
            new Utbetalingsgrad(90),
            true,
            null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(27)); // basert på utbetalingsgraden
    }

    @Test
    void fulle_trekkdager_ved_innvilget_uttak_men_avslått_gradering_før_uke_6() {
        var periode = graderingsperiode(Stønadskontotype.MØDREKVOTE, BigDecimal.TEN, Set.of(ARBEIDSFORHOLD_1));

        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_1,
            Utbetalingsgrad.FULL,
            true,
            GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING
        );

        assertThat(trekkdager.decimalValue().intValue()).isEqualTo(FULLE_TREKKDAGER);
    }

    @Test
    void trekkdager_baseres_på_arbeidsprosenten_og_ikke_utbetalingsgraden_ved_avslått_gradering() {
        var arbeidstidsprosent = BigDecimal.valueOf(50);
        var periode = graderingsperiode(Stønadskontotype.MØDREKVOTE, arbeidstidsprosent, Set.of(ARBEIDSFORHOLD_1));

        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_1,
            Utbetalingsgrad.ZERO,
            false,
            null);

        assertThat(trekkdager.decimalValue().intValue()).isEqualTo(FULLE_TREKKDAGER/2); // basert på utbetalingsgraden
    }

    @Test
    void trekkdager_baseres_på_arbeidsprosenten_og_ikke_utbetalingsgraden_ved_avslått_gradering_bare_for_aktivteten_som_er_gradert() {
        var arbeidstidsprosent = BigDecimal.valueOf(50);
        var periode = graderingsperiode(Stønadskontotype.MØDREKVOTE, arbeidstidsprosent, Set.of(ARBEIDSFORHOLD_1));

        var trekkdager = TrekkdagerUtregningUtil.beregnTrekkdagerFor(
            periode,
            ARBEIDSFORHOLD_2,
            Utbetalingsgrad.ZERO,
            false,
            null);

        assertThat(trekkdager.decimalValue().intValue()).isEqualTo(FULLE_TREKKDAGER);
    }

    private static OppgittPeriode vanligPeriode(SamtidigUttaksprosent samtidigUttaksprosent) {
        return OppgittPeriode.forVanligPeriode(
            Stønadskontotype.FORELDREPENGER,
            FOM,
            TOM,
            samtidigUttaksprosent,
            false,
            null,
            null,
            null,
            null,
            null
        );
    }

    private static OppgittPeriode graderingsperiode(Stønadskontotype stønadskontotype, BigDecimal arbeidstidsprosent, Set<AktivitetIdentifikator> gradertAktiveter) {
        return OppgittPeriode.forGradering(stønadskontotype,
            FOM,
            TOM,
            arbeidstidsprosent,
            null,
            false,
            gradertAktiveter,
            null,
            null,
            null,
            null,
            null);
    }
}
