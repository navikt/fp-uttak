package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public final class Konto {

    private Stønadskontotype type;
    private int trekkdager;

    private Konto() {
    }

    public Stønadskontotype getType() {
        return type;
    }

    public int getTrekkdager() {
        return trekkdager;
    }

    public static class Builder {
        private final Konto kladd = new Konto();

        public Builder type(Stønadskontotype type) {
            kladd.type = type;
            return this;
        }

        public Builder trekkdager(int trekkdager) {
            kladd.trekkdager = trekkdager;
            return this;
        }

        public Konto build() {
            return kladd;
        }
    }
}
