package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class Behandling {

    private boolean søkerMor;
    private boolean tapende;

    private Behandling() {
    }

    public boolean isSøkerMor() {
        return søkerMor;
    }

    public boolean isTapende() {
        return tapende;
    }

    public static class Builder {

        private final Behandling kladd = new Behandling();


        public Builder medSøkerErMor(boolean søkerErMor) {
            kladd.søkerMor = søkerErMor;
            return this;
        }

        public Builder medErTapende(boolean erTapende) {
            kladd.tapende = erTapende;
            return this;
        }

        public Behandling build() {
            return kladd;
        }
    }
}
