package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtbetalingsgradUtilTest {

    @Test
    void utbetaling_skal_være_100_prosent_når_en_ikke_graderer_eller_samtidig_uttak_eller_grense() {
        var aktivitet = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
        var periode = OppgittPeriode.forVanligPeriode(
            Stønadskontotype.FELLESPERIODE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            null,
            false,
            null,
            null,
            null,
            null
        );
        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet, null);
        assertThat(resultat).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void toArbeidsforholdMedEnGradertGirRedusertUtbetalingsgrad() {
        var aktivitet1 = AktivitetIdentifikator.annenAktivitet();
        var aktivitet2 = AktivitetIdentifikator.forFrilans();
        var arbeidstidsprosent = BigDecimal.valueOf(20);

        var periode = OppgittPeriode.forGradering(
            Stønadskontotype.FEDREKVOTE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            arbeidstidsprosent,
            null,
            false,
            Set.of(aktivitet1),
            null,
            null,
            null,
            null
        );

        var utregningForAktivitet1 = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet1, null);
        var utregningForAktivitet2 = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet2, null);

        assertThat(utregningForAktivitet1).isEqualTo(Utbetalingsgrad.FULL.subtract(arbeidstidsprosent));
        assertThat(utregningForAktivitet2).isEqualTo(Utbetalingsgrad.FULL);
    }

    @Test
    void hvis_ugradert_periode_skal_utbetalingsgrad_være_lik_samtidig_uttaksprosent() {
        var aktivitet = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
        var periode = OppgittPeriode.forVanligPeriode(
            Stønadskontotype.FELLESPERIODE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            SamtidigUttaksprosent.TEN,
            false,
            null,
            null,
            null,
            null
        );
        var samtidigUttaksprosent = SamtidigUttaksprosent.TEN;
        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet, null);

        assertThat(resultat.decimalValue()).isEqualTo(samtidigUttaksprosent.decimalValue());
    }

    @Test
    void hvis_gradert_periode_skal_utbetalingsgrad_være_gradering_arbeidstidsprosent() {
        var aktivitet1 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
        var aktivitet2 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
        var samtidigUttaksprosent = SamtidigUttaksprosent.TEN;
        var graderingArbeidstidsprosent = BigDecimal.ONE;
        var periode = OppgittPeriode.forGradering(
            Stønadskontotype.FELLESPERIODE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            graderingArbeidstidsprosent,
            samtidigUttaksprosent,
            false,
            Set.of(aktivitet1),
            null,
            null,
            null,
            null
        );
        var resultat1 = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet1, null);
        var resultat2 = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet2, null);

        assertThat(resultat1).isEqualTo(resultat2).isEqualTo(Utbetalingsgrad.HUNDRED.subtract(graderingArbeidstidsprosent));
    }

    @Test
    void utbetalingsgraden_skal_redusers_hvis_redusertUttaksprosent_er_satt_hvis_utbetalingsgrad_er_over_denne_verdien() {
        var aktivitet = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
        var periode = OppgittPeriode.forVanligPeriode(
            Stønadskontotype.FELLESPERIODE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            null,
            false,
            null,
            null,
            null,
            null
        );
        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, aktivitet, SamtidigUttaksprosent.FIFTY);
        assertThat(resultat).isEqualTo(new Utbetalingsgrad(50));
    }
}
