package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Dødsdatoer {

    private LocalDate søkersDødsdato;
    private LocalDate barnsDødsdato;
    private boolean alleBarnDøde;

    private Dødsdatoer() {}

    public LocalDate getSøkersDødsdato() {
        return søkersDødsdato;
    }

    public LocalDate getBarnsDødsdato() {
        return barnsDødsdato;
    }

    public boolean erAlleBarnDøde() {
        return alleBarnDøde;
    }

    public static class Builder {
        private final Dødsdatoer kladd = new Dødsdatoer();

        public Builder søkersDødsdato(LocalDate søkersDødsdato) {
            kladd.søkersDødsdato = søkersDødsdato;
            return this;
        }

        public Builder barnsDødsdato(LocalDate barnsDødsdato) {
            kladd.barnsDødsdato = barnsDødsdato;
            return this;
        }

        public Builder alleBarnDøde(boolean alleBarnDøde) {
            kladd.alleBarnDøde = alleBarnDøde;
            return this;
        }

        public Dødsdatoer build() {
            return kladd;
        }
    }
}
