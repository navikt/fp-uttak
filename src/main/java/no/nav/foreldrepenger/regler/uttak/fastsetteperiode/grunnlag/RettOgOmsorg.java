package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class RettOgOmsorg {

    private boolean farHarRett;
    private boolean morHarRett;
    private boolean samtykke;
    private boolean aleneomsorg;

    private RettOgOmsorg() {
    }

    public boolean getFarHarRett() {
        return farHarRett;
    }

    public boolean getMorHarRett() {
        return morHarRett;
    }

    public boolean getSamtykke() {
        return samtykke;
    }

    public boolean getAleneomsorg() {
        return aleneomsorg;
    }

    public static class Builder {
        private final RettOgOmsorg kladd = new RettOgOmsorg();

        public Builder medFarHarRett(boolean medFarRett) {
            kladd.farHarRett = medFarRett;
            return this;
        }
        public Builder medMorHarRett(boolean medMorRett) {
            kladd.morHarRett = medMorRett;
            return this;
        }
        public Builder medSamtykke(boolean medSamtykke) {
            kladd.samtykke = medSamtykke;
            return this;
        }
        public Builder medAleneomsorg(boolean medAleneomsorg) {
            kladd.aleneomsorg = medAleneomsorg;
            return this;
        }

        public RettOgOmsorg build() {
            return kladd;
        }
    }
}
