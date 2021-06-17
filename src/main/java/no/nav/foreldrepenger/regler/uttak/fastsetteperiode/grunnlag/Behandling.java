package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class Behandling {

    private boolean søkerMor;
    private boolean berørtBehandling;
    private boolean kreverSammenhengendeUttak;

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

    public boolean isKreverSammenhengendeUttak() {
        return kreverSammenhengendeUttak;
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

        public Builder medKreverSammenhengendeUttak(boolean kreverSammenhengendeUttak) {
            kladd.kreverSammenhengendeUttak = kreverSammenhengendeUttak;
            return this;
        }

        public Behandling build() {
            return kladd;
        }
    }
}
