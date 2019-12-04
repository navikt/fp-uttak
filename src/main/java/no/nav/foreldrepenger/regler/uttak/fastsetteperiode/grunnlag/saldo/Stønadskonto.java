package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class Stønadskonto {

    private final Stønadskontotype stønadskontoType;
    private final int maksdager;

    public Stønadskonto(Stønadskontotype stønadskontoType, int maksdager) {
        this.stønadskontoType = stønadskontoType;
        this.maksdager = maksdager;
    }

    Stønadskontotype getStønadskontotype() {
        return stønadskontoType;
    }

    int getMaksdager() {
        return maksdager;
    }

    @Override
    public String toString() {
        return "Stønadskonto{" +
                "stønadskontoType=" + stønadskontoType +
                ", maksdager=" + maksdager +
                '}';
    }
}
