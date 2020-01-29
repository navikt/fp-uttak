package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;

class UtbetalingsprosentMedGraderingUtregning implements UtbetalingsprosentUtregning {

    private final OppgittPeriode uttakPeriode;
    private final AktivitetIdentifikator aktivitet;

    UtbetalingsprosentMedGraderingUtregning(OppgittPeriode uttakPeriode,
                                            AktivitetIdentifikator aktivitet) {
        this.uttakPeriode = uttakPeriode;
        this.aktivitet = aktivitet;
    }

    @Override
    public BigDecimal resultat() {
        if (uttakPeriode.erSøktGradering(aktivitet)) {
            if (uttakPeriode.getArbeidsprosent() == null) {
                throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
            }
            return new BigDecimal("100.00").subtract(uttakPeriode.getArbeidsprosent());
        }
        return new BigDecimal("100.00");
    }
}
