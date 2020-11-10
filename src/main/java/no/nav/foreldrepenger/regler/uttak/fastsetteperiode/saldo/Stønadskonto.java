package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class Stønadskonto {

    private final Stønadskontotype stønadskontoType;
    private final Trekkdager maksdager;

    public Stønadskonto(Stønadskontotype stønadskontoType, Trekkdager maksdager) {
        this.stønadskontoType = stønadskontoType;
        this.maksdager = maksdager;
    }

    Stønadskontotype getStønadskontotype() {
        return stønadskontoType;
    }

    Trekkdager getMaksdager() {
        return maksdager;
    }

    @Override
    public String toString() {
        return "Stønadskonto{" + "stønadskontoType=" + stønadskontoType + ", maksdager=" + maksdager + '}';
    }
}
