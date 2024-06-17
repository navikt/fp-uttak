package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradMedGraderingUtregningTest {

    @Test
    void toArbeidsforholdMedEnGradertGirRedusertUtbetalingsgrad() {
        var aktivitet1 = AktivitetIdentifikator.annenAktivitet();
        var aktivitet2 = AktivitetIdentifikator.forFrilans();
        var arbeidstidsprosent = BigDecimal.valueOf(20);

        var periode = OppgittPeriode.forGradering(Stønadskontotype.FEDREKVOTE, LocalDate.now(), LocalDate.now().plusWeeks(1), arbeidstidsprosent,
            null, false, Set.of(aktivitet1), null, null, null, null);

        var utregningForAktivitet1 = utregning(aktivitet1, periode);
        var utregningForAktivitet2 = utregning(aktivitet2, periode);

        assertThat(utregningForAktivitet1.resultat()).isEqualTo(Utbetalingsgrad.FULL.subtract(arbeidstidsprosent));
        assertThat(utregningForAktivitet2.resultat()).isEqualTo(Utbetalingsgrad.FULL);
    }

    private UtbetalingsgradMedGraderingUtregning utregning(AktivitetIdentifikator aktivitetIdentifikator, OppgittPeriode periode) {
        return new UtbetalingsgradMedGraderingUtregning(periode, aktivitetIdentifikator, SamtidigUttaksprosent.ZERO);
    }
}
