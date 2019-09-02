package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Revurdering {

    private LocalDate endringssøknadMottattdato;
    private LocalDate endringsdato;

    private Revurdering() {
    }

    public LocalDate getEndringssøknadMottattdato() {
        return endringssøknadMottattdato;
    }

    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public boolean getErEndringssøknad() {
        return endringssøknadMottattdato != null;
    }

    public static class Builder {

        private final Revurdering kladd = new Revurdering();

        public Builder medEndringssøknadMottattdato(LocalDate endringssøknadMottattdato) {
            this.kladd.endringssøknadMottattdato = endringssøknadMottattdato;
            return this;
        }

        public Builder medEndringsdato(LocalDate endringsdato) {
            this.kladd.endringsdato = endringsdato;
            return this;
        }

        public Revurdering build() {
            return kladd;
        }
    }
}
