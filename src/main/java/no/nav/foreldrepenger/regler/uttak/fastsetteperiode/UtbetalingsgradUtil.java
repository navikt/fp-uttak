package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

import java.math.BigDecimal;

public final class UtbetalingsgradUtil {

    private static final BigDecimal UTBETALINGSGRAD_100 = BigDecimal.valueOf(100);

    private UtbetalingsgradUtil() {}


    public static Utbetalingsgrad beregnUtbetalingsgradFor(OppgittPeriode oppgittPeriode, AktivitetIdentifikator aktivitet, SamtidigUttaksprosent øvreGrenseUtbetalingsgrad) {
        var beregnetUtbetalingsgrad = UTBETALINGSGRAD_100;
        if (oppgittPeriode.erSøktGradering(aktivitet) || (oppgittPeriode.erSøktGradering() && oppgittPeriode.erSøktSamtidigUttak())) {
            beregnetUtbetalingsgrad = UTBETALINGSGRAD_100.subtract(oppgittPeriode.getArbeidsprosent());
        } else if (oppgittPeriode.erSøktSamtidigUttak()) {
            beregnetUtbetalingsgrad = oppgittPeriode.getSamtidigUttaksprosent().decimalValue();
        }


        return øvreGrenseUtbetalingsgrad != null
            ? new Utbetalingsgrad(beregnetUtbetalingsgrad.min(øvreGrenseUtbetalingsgrad.decimalValue())) //
            : new Utbetalingsgrad(beregnetUtbetalingsgrad);
    }
}
