package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

class UtbetalingsprosentMedGraderingUtregning implements UtbetalingsprosentUtregning {

    private final UttakPeriode uttakPeriode;
    private final AktivitetIdentifikator aktivitet;

    UtbetalingsprosentMedGraderingUtregning(UttakPeriode uttakPeriode,
                                            AktivitetIdentifikator aktivitet) {
        this.uttakPeriode = uttakPeriode;
        this.aktivitet = aktivitet;
    }

    @Override
    public BigDecimal resultat() {
        if (uttakPeriode.søktGradering(aktivitet)) {
            if (uttakPeriode.getGradertArbeidsprosent() == null) {
                throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
            }
            return new BigDecimal("100.00").subtract(uttakPeriode.getGradertArbeidsprosent());
        }
        return new BigDecimal("100.00");
    }
}
