package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public class Medlemskap {

    private LocalDate opphørsdato;

    private Medlemskap() {

    }

    public LocalDate getOpphørsdato() {
        return opphørsdato;
    }

    public static class Builder {

        private final Medlemskap kladd = new Medlemskap();

        public Builder opphørsdato(LocalDate opphørsdato) {
            kladd.opphørsdato = opphørsdato;
            return this;
        }

        public Medlemskap build() {
            return kladd;
        }
    }
}
