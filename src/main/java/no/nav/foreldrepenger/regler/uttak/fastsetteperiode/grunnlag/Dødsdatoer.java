package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

public final class Dødsdatoer {

    private LocalDate søkersDødsdato;
    private LocalDate barnsDødsdato;

    private Optional<Integer> gjenlevendeBarn;

    private Dødsdatoer() {
    }

    public LocalDate getSøkersDødsdato(){
        return søkersDødsdato;
    }

    public LocalDate getBarnsDødsdato(){
        return barnsDødsdato;
    }

    public Optional<Integer> getGjenlevendeBarn() {
        return gjenlevendeBarn;
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

        public Builder medGjenlevedeBarn(Optional<Integer> gjenlevendeBarn) {
            kladd.gjenlevendeBarn = gjenlevendeBarn;
            return this;
        }
        public Dødsdatoer build() {
            return kladd;
        }
    }

}
