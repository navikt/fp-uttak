package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;

class UtbetalingsprosentUtenGraderingUtregning implements UtbetalingsprosentUtregning {

    @Override
    public BigDecimal resultat() {
        return new BigDecimal("100.00");
    }
}
