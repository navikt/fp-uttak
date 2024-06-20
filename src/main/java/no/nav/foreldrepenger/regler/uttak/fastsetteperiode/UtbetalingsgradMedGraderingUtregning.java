package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Optional;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradMedGraderingUtregning implements UtbetalingsgradUtregning {

    private final OppgittPeriode uttakPeriode;
    private final AktivitetIdentifikator aktivitet;
    private final SamtidigUttaksprosent annenpartSamtidigUttaksprosent;

    UtbetalingsgradMedGraderingUtregning(
            OppgittPeriode uttakPeriode,
            AktivitetIdentifikator aktivitet,
            SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        this.uttakPeriode = uttakPeriode;
        this.aktivitet = aktivitet;
        this.annenpartSamtidigUttaksprosent = annenpartSamtidigUttaksprosent;
    }

    @Override
    public Utbetalingsgrad resultat() {
        // Samtidiguttaksprosent med mindre gradering på noen aktiviteter i perioden
        var lokalSamtidigUttaksprosent = uttakPeriode.erSøktGradering(aktivitet)
                ? Optional.ofNullable(uttakPeriode.getArbeidsprosent())
                        .map(SamtidigUttaksprosent.HUNDRED::subtract)
                        .orElseThrow(() -> new IllegalArgumentException("arbeidstidsprosent kan ikke være null"))
                : SamtidigUttaksprosent.HUNDRED;
        var maksSamtidigUttakUtFraAnnenpart = SamtidigUttaksprosent.HUNDRED.subtract(annenpartSamtidigUttaksprosent);
        // Reduser utbetaling dersom annenpart > 0 og det ligger an til mer enn 100 prosent
        if (lokalSamtidigUttaksprosent.subtract(maksSamtidigUttakUtFraAnnenpart).merEnn0()) {
            return new Utbetalingsgrad(maksSamtidigUttakUtFraAnnenpart.decimalValue());
        }
        return new Utbetalingsgrad(lokalSamtidigUttaksprosent.decimalValue());
    }
}
