package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Revurdering {

    private LocalDate endringsdato;

    private Revurdering() {
    }

    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public static class Builder {

        private final Revurdering kladd = new Revurdering();

        public Builder medEndringsdato(LocalDate endringsdato) {
            this.kladd.endringsdato = endringsdato;
            return this;
        }

        public Revurdering build() {
            return kladd;
        }
    }
}
