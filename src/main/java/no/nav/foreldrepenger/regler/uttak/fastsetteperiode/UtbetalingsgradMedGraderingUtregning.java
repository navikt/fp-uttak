package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

class UtbetalingsgradMedGraderingUtregning implements UtbetalingsgradUtregning {

    private final OppgittPeriode uttakPeriode;
    private final AktivitetIdentifikator aktivitet;

    UtbetalingsgradMedGraderingUtregning(OppgittPeriode uttakPeriode,
                                         AktivitetIdentifikator aktivitet) {
        this.uttakPeriode = uttakPeriode;
        this.aktivitet = aktivitet;
    }

    @Override
    public Utbetalingsgrad resultat() {
        if (uttakPeriode.erSøktGradering(aktivitet)) {
            if (uttakPeriode.getArbeidsprosent() == null) {
                throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
            }
            return Utbetalingsgrad.HUNDRED.subtract(uttakPeriode.getArbeidsprosent());
        }
        return Utbetalingsgrad.HUNDRED;
    }
}
