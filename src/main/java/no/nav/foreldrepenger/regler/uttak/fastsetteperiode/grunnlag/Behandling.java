package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Behandling {

    private boolean søkerMor;
    private boolean berørtBehandling;
    private LocalDate sammenhengendeUttakTomDato = LocalDate.MIN;

    private Behandling() {
    }

    public boolean isSøkerMor() {
        return søkerMor;
    }

    public boolean isSøkerFarMedMor() {
        return !isSøkerMor();
    }

    public boolean isBerørtBehandling() {
        return berørtBehandling;
    }

    public LocalDate getSammenhengendeUttakTomDato() {
        return sammenhengendeUttakTomDato;
    }

    public static class Builder {

        private final Behandling kladd = new Behandling();


        public Builder søkerErMor(boolean søkerErMor) {
            kladd.søkerMor = søkerErMor;
            return this;
        }

        public Builder berørtBehandling(boolean berørtBehandling) {
            kladd.berørtBehandling = berørtBehandling;
            return this;
        }

        public Builder sammenhengendeUttakTomDato(LocalDate sammenhengendeUttakTomDato) {
            kladd.sammenhengendeUttakTomDato = sammenhengendeUttakTomDato;
            return this;
        }

        public Behandling build() {
            return kladd;
        }
    }
}
