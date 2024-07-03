package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradUtilTest {

    private static final AktivitetIdentifikator AKTIVITET_1 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
    private static final AktivitetIdentifikator AKTIVITET_2 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000002"), null);

    @Test
    void utbetaling_skal_være_100_prosent_når_en_ikke_graderer_eller_samtidig_uttak_eller_grense() {
        var periode = vanligPeriode(null);

        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_1, null);

        assertThat(resultat).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void toArbeidsforholdMedEnGradertGirRedusertUtbetalingsgrad() {
        var arbeidstidsprosent = BigDecimal.valueOf(20);
        var periode = graderingsPeriode(arbeidstidsprosent, AKTIVITET_1, null);

        var utregningForAktivitetGradert = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_1, null);
        var utregningForAktivitetUgradert = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_2, null);

        assertThat(utregningForAktivitetGradert).isEqualTo(Utbetalingsgrad.FULL.subtract(arbeidstidsprosent));
        assertThat(utregningForAktivitetUgradert).isEqualTo(Utbetalingsgrad.FULL);
    }

    @Test
    void hvis_ugradert_periode_skal_utbetalingsgrad_være_lik_samtidig_uttaksprosent() {
        var samtidigUttaksprosent = SamtidigUttaksprosent.TEN;
        var periode = vanligPeriode(samtidigUttaksprosent);

        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_1, null);

        assertThat(resultat.decimalValue()).isEqualTo(samtidigUttaksprosent.decimalValue());
    }

    @Test
    void hvis_gradert_periode_skal_utbetalingsgrad_være_gradering_arbeidstidsprosent() {
        var graderingArbeidstidsprosent = BigDecimal.ONE;
        var samtidigUttaksgrad = 10;
        var periode = graderingsPeriode(graderingArbeidstidsprosent, AKTIVITET_1, new SamtidigUttaksprosent(samtidigUttaksgrad));

        var resultat1 = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_1, null);
        var resultat2 = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_2, null);

        assertThat(resultat1).isEqualTo(Utbetalingsgrad.HUNDRED.subtract(graderingArbeidstidsprosent));
        assertThat(resultat2).isEqualTo(new Utbetalingsgrad(samtidigUttaksgrad));
    }

    @Test
    void utbetalingsgraden_skal_redusers_hvis_redusertUttaksprosent_er_satt_hvis_utbetalingsgrad_er_over_denne_verdien() {
        var redusertUttaksprosent = SamtidigUttaksprosent.FIFTY;
        var periode = vanligPeriode(null);

        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_1, redusertUttaksprosent);

        assertThat(resultat).isEqualTo(new Utbetalingsgrad(redusertUttaksprosent.decimalValue()));
    }

    @Test
    void utbetalingsgraden_skal_redusers_hvis_redusertUttaksprosent_er_satt_hvis_utbetalingsgrad_er_over_denne_verdien_også_for_gradering() {
        var redusertUttaksprosent = SamtidigUttaksprosent.FIFTY;
        var periode = graderingsPeriode(BigDecimal.valueOf(20), AKTIVITET_1, null);

        var resultat = UtbetalingsgradUtil.beregnUtbetalingsgradFor(periode, AKTIVITET_1, redusertUttaksprosent);

        assertThat(resultat).isEqualTo(new Utbetalingsgrad(redusertUttaksprosent.decimalValue()));
    }


    private static OppgittPeriode vanligPeriode(SamtidigUttaksprosent samtidigUttaksprosent) {
        return OppgittPeriode.forVanligPeriode(
            Stønadskontotype.FELLESPERIODE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            samtidigUttaksprosent,
            false,
            null,
            null,
            null,
            null,
            null
        );
    }


    private static OppgittPeriode graderingsPeriode(BigDecimal arbeidsprosent, AktivitetIdentifikator gradertAktivitet, SamtidigUttaksprosent samtidigUttaksprosent) {
        return OppgittPeriode.forGradering(
            Stønadskontotype.FELLESPERIODE,
            LocalDate.now(),
            LocalDate.now().plusWeeks(1),
            arbeidsprosent,
            samtidigUttaksprosent,
            false,
            Set.of(gradertAktivitet),
            null,
            null,
            null,
            null,
            null
        );
    }
}
