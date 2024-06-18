package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

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

        // Far søker gradering. Mor kan ha mindre enn 75% arbeids
        var morsStillingsprosent = uttakPeriode.getMorsStillingsprosent();
        var maksSamtidigUttakUtFraAnnenpart = SamtidigUttaksprosent.HUNDRED.subtract(annenpartSamtidigUttaksprosent);
        // Reduser utbetaling dersom annenpart > 0 og det ligger an til mer enn 100 prosent


        if (morsStillingsprosent != null && maksSamtidigUttakUtFraAnnenpart.subtract(morsStillingsprosent.decimalValue()).merEnn0()) {
            // morsStillingsprosent er minst og er taket
            // TODO: Far graderer, mor i arbeid og samtidig uttak. Hva skjer hera?
            return new Utbetalingsgrad(morsStillingsprosent.decimalValue());
        } else {
            // maksSamtidigUttakUtFraAnnenpart er minst og er taket
            if (søktUttaksprosent.subtract(maksSamtidigUttakUtFraAnnenpart).merEnn0()) {
                return new Utbetalingsgrad(maksSamtidigUttakUtFraAnnenpart.decimalValue());
            }

            return new Utbetalingsgrad(søktUttaksprosent.decimalValue());
        }




    }
}
