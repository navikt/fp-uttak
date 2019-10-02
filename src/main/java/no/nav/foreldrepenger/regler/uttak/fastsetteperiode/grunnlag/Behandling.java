package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class Behandling {

    private boolean søkerErMor;
    private boolean tapende;

    private Behandling() {
    }

    public boolean isSøkerMor() {
        return søkerErMor;
    }

    public boolean isTapende() {
        return tapende;
    }

    public static class Builder {

        private final Behandling kladd = new Behandling();


        public Builder medSøkerErMor(boolean søkerErMor) {
            kladd.søkerErMor = søkerErMor;
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
