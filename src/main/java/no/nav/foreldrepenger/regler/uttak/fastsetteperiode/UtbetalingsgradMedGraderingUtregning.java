package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradMedGraderingUtregning implements UtbetalingsgradUtregning {

    private final OppgittPeriode uttakPeriode;
    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;

    UtbetalingsgradMedGraderingUtregning(OppgittPeriode uttakPeriode, SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        this.uttakPeriode = uttakPeriode;
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        if (uttakPeriode.getArbeidsprosent() == null) {
            throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
        }
        var søktUttaksprosent = SamtidigUttaksprosent.HUNDRED.subtract(uttakPeriode.getArbeidsprosent());

        // Hvis far søker gradering. Mor kan ha mindre enn 75% arbeids
        var morsStillingsprosent = uttakPeriode.getMorsStillingsprosent();
        // TODO: Far graderer, mor i arbeid og samtidig uttak. Hva skjer hera?
        var gjenståendeUttakForBruker = SamtidigUttaksprosent.HUNDRED.subtract(annenpartSamtidigUttaksprosent);

        if (morsStillingsprosent != null) {
            // Reduser utbetaling dersom annenpart > 0 og det ligger an til mer enn 100 prosent
            return new Utbetalingsgrad(minstAv(søktUttaksprosent, morsStillingsprosent));
        }
        return new Utbetalingsgrad(søktUttaksprosent.decimalValue());
    }

    private static BigDecimal minstAv(SamtidigUttaksprosent søktUttaksprosent, MorsStillingsprosent morsStillingsprosent) {
        return søktUttaksprosent.decimalValue().compareTo(morsStillingsprosent.decimalValue())
            > 0 ? morsStillingsprosent.decimalValue() : søktUttaksprosent.decimalValue();
    }
}
