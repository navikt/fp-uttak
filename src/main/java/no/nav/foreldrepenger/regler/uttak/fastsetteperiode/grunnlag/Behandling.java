package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype.REVURDERING_TYPER;

public final class Behandling {

    private Behandlingtype type;
    private boolean søkerErMor;

    private Behandling() {
    }

    public Behandlingtype getType() {
        return type;
    }

    public boolean getSøkerErMor() {
        return søkerErMor;
    }

    boolean erRevurdering() {
        return REVURDERING_TYPER.contains(type);
    }

    public static class Builder {

        private final Behandling kladd = new Behandling();


        public Builder medSøkerErMor(boolean søkerErMor) {
            kladd.søkerErMor = søkerErMor;
            return this;
        }

        public Builder medType(Behandlingtype type) {
            kladd.type = type;
            return this;
        }

        public Behandling build() {
            return kladd;
        }
    }
}
