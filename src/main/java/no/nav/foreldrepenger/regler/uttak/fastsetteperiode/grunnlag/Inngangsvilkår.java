package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public final class Inngangsvilkår {

    private boolean adopsjonOppfylt;
    private boolean foreldreansvarOppfylt;
    private boolean fødselOppfylt;
    private boolean opptjeningOppfylt;
    private Boolean medlemskapOppfylt;

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

    public boolean erMedlemskapOppfylt() {
        // Kompatibilitet ved deserialisering av eldre grunnlag
        return !Objects.equals(medlemskapOppfylt, Boolean.FALSE);
    }

    public static class Builder {

        private final Inngangsvilkår kladd = new Inngangsvilkår();

        public Builder adopsjonOppfylt(boolean oppfylt) {
            kladd.adopsjonOppfylt = oppfylt;
            return this;
        }

        public Builder foreldreansvarnOppfylt(boolean oppfylt) {
            kladd.foreldreansvarOppfylt = oppfylt;
            return this;
        }

        public Builder fødselOppfylt(boolean oppfylt) {
            kladd.fødselOppfylt = oppfylt;
            return this;
        }

        public Builder opptjeningOppfylt(boolean oppfylt) {
            kladd.opptjeningOppfylt = oppfylt;
            return this;
        }

        public Builder medlemskapOppfylt(boolean oppfylt) {
            kladd.medlemskapOppfylt = oppfylt;
            return this;
        }

        public Inngangsvilkår build() {
            return kladd;
        }
    }
}
