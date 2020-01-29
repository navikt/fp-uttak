package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class UtbetalingsprosentMedGraderingUtregningTest {

    @Test
    public void toArbeidsforholdMedEnGradertGirRedusertUtbetalingsprosent() {
        var aktivitet1 = AktivitetIdentifikator.annenAktivitet();
        var aktivitet2 = AktivitetIdentifikator.forFrilans();
        var arbeidstidsprosent = BigDecimal.valueOf(20);

        var periode = OppgittPeriode.forGradering(Stønadskontotype.FEDREKVOTE, LocalDate.now(), LocalDate.now().plusWeeks(1),
                PeriodeKilde.SØKNAD, arbeidstidsprosent, null, false, Set.of(aktivitet1), PeriodeVurderingType.IKKE_VURDERT);

        var utregningForAktivitet1 = utregning(aktivitet1, periode);
        var utregningForAktivitet2 = utregning(aktivitet2, periode);

        assertThat(utregningForAktivitet1.resultat()).isEqualByComparingTo(BigDecimal.valueOf(100).subtract(arbeidstidsprosent));
        assertThat(utregningForAktivitet2.resultat()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    private UtbetalingsprosentMedGraderingUtregning utregning(AktivitetIdentifikator aktivitetIdentifikator,
                                                              OppgittPeriode periode) {
        return new UtbetalingsprosentMedGraderingUtregning(periode, aktivitetIdentifikator);
    }
}
