package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;

public class SamtidigUttak {

    private final BigDecimal prosent;

    public SamtidigUttak(BigDecimal prosent) {
        this.prosent = prosent;
    }

    public BigDecimal getProsent() {
        return prosent;
    }
}
