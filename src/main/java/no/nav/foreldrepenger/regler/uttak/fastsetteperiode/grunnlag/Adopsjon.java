package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Adopsjon {

    private LocalDate ankomstNorge;
    private boolean stebarnsadopsjon;

    private Adopsjon() {
    }

    public LocalDate getAnkomstNorgeDato() {
        return ankomstNorge;
    }

    public boolean erStebarnsadopsjon() {
        return stebarnsadopsjon;
    }

    public static class Builder {
        private final Adopsjon kladd = new Adopsjon();

        public Builder medAnkomstNorge(LocalDate ankomstNorge) {
            kladd.ankomstNorge = ankomstNorge;
            return this;
        }

        public Builder medStebarnsadopsjon(boolean stebarnsadopsjon) {
            kladd.stebarnsadopsjon = stebarnsadopsjon;
            return this;
        }

        public Adopsjon build() {
            return kladd;
        }
    }
}
