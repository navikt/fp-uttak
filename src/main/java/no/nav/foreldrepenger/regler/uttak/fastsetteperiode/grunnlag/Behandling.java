package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class Behandling {

    private boolean søkerMor;
    private boolean berørtBehandling;

    private Behandling() {
    }

    public boolean isSøkerMor() {
        return søkerMor;
    }

    public boolean isBerørtBehandling() {
        return berørtBehandling;
    }

    public static class Builder {

        private final Behandling kladd = new Behandling();


        public Builder medSøkerErMor(boolean søkerErMor) {
            kladd.søkerMor = søkerErMor;
            return this;
        }

        public Builder medErBerørtBehandling(boolean berørtBehandling) {
            kladd.berørtBehandling = berørtBehandling;
            return this;
        }

        public Behandling build() {
            return kladd;
        }
    }
}
