package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

public final class UtbetalingsgradUtil {

    private static final BigDecimal UTBETALINGSGRAD_100 = BigDecimal.valueOf(100);

    private UtbetalingsgradUtil() {}


    public static Utbetalingsgrad beregnUtbetalingsgradFor(OppgittPeriode oppgittPeriode, AktivitetIdentifikator aktivitet, SamtidigUttaksprosent avgrensetUttaksprosentForÅOppnåSamtidigUttak100) {
        var beregnetUtbetalingsgrad = UTBETALINGSGRAD_100;

        if (oppgittPeriode.erSøktGradering(aktivitet)) {
            beregnetUtbetalingsgrad = UTBETALINGSGRAD_100.subtract(oppgittPeriode.getArbeidsprosent());
        } else if (oppgittPeriode.erSøktSamtidigUttak()) { // TODO: erSøktGradering() + erSøktSamtidigUttak() gir samtidig uttaksprosenten?
            beregnetUtbetalingsgrad = oppgittPeriode.getSamtidigUttaksprosent().decimalValue();
        } else if (oppgittPeriode.getMorsStillingsprosent() != null) {
            beregnetUtbetalingsgrad = oppgittPeriode.getMorsStillingsprosent().decimalValue();
        }

        return avgrensetUttaksprosentForÅOppnåSamtidigUttak100 != null
            ? new Utbetalingsgrad(beregnetUtbetalingsgrad.min(avgrensetUttaksprosentForÅOppnåSamtidigUttak100.decimalValue()))
            : new Utbetalingsgrad(beregnetUtbetalingsgrad);
    }
}
