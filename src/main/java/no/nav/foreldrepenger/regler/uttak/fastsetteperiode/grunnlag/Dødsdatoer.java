package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Dødsdatoer {

    private LocalDate søkersDødsdato;
    private LocalDate barnsDødsdato;
    private boolean alleBarnDøde;

    private Dødsdatoer() {
    }

    public LocalDate getSøkersDødsdato(){
        return søkersDødsdato;
    }

    public LocalDate getBarnsDødsdato(){
        return barnsDødsdato;
    }

    public boolean erAlleBarnDøde(){
        return alleBarnDøde;
    }

    public static class Builder {
        private final Dødsdatoer kladd = new Dødsdatoer();

        public Builder medSøkersDødsdato(LocalDate søkersDødsdato) {
            kladd.søkersDødsdato = søkersDødsdato;
            return this;
        }

        public Builder medBarnsDødsdato(LocalDate barnsDødsdato) {
            kladd.barnsDødsdato = barnsDødsdato;
            return this;
        }

        public Builder medErAlleBarnDøde(boolean alleBarnDøde) {
            kladd.alleBarnDøde = alleBarnDøde;
            return this;
        }

        public Dødsdatoer build() {
            return kladd;
        }
    }

}
