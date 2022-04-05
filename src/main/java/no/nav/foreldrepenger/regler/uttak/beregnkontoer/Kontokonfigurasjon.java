package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class Kontokonfigurasjon {

    private final StønadskontoBeregningStønadskontotype stønadskontotype;
    private final Parametertype parametertype;

    Kontokonfigurasjon(StønadskontoBeregningStønadskontotype stønadskontotype, Parametertype parametertype) {
        this.stønadskontotype = stønadskontotype;
        this.parametertype = parametertype;
    }

    public StønadskontoBeregningStønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public Parametertype getParametertype() {
        return parametertype;
    }
}
