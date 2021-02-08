package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class Inngangsvilkår {

    private boolean adopsjonOppfylt;
    private boolean foreldreansvarOppfylt;
    private boolean fødselOppfylt;
    private boolean opptjeningOppfylt;

    private Inngangsvilkår() {

    }

    public boolean erAdopsjonOppfylt() {
        return adopsjonOppfylt;
    }

    public boolean erForeldreansvarOppfylt() {
        return foreldreansvarOppfylt;
    }

    public boolean erFødselsvilkåretOppfylt() {
        return fødselOppfylt;
    }

    public boolean erOpptjeningOppfylt() {
        return opptjeningOppfylt;
    }

    public static class Builder {

        private final Inngangsvilkår kladd = new Inngangsvilkår();

        public Builder medAdopsjonOppfylt(boolean oppfylt) {
            kladd.adopsjonOppfylt = oppfylt;
            return this;
        }

        public Builder medForeldreansvarnOppfylt(boolean oppfylt) {
            kladd.foreldreansvarOppfylt = oppfylt;
            return this;
        }

        public Builder medFødselOppfylt(boolean oppfylt) {
            kladd.fødselOppfylt = oppfylt;
            return this;
        }

        public Builder medOpptjeningOppfylt(boolean oppfylt) {
            kladd.opptjeningOppfylt = oppfylt;
            return this;
        }

        public Inngangsvilkår build() {
            return kladd;
        }
    }
}
