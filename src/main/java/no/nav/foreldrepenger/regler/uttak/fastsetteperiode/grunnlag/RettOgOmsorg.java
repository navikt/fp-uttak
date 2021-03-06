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

        public Builder farHarRett(boolean farHarRett) {
            kladd.farHarRett = farHarRett;
            return this;
        }

        public Builder morHarRett(boolean morHarRett) {
            kladd.morHarRett = morHarRett;
            return this;
        }

        public Builder samtykke(boolean samtykke) {
            kladd.samtykke = samtykke;
            return this;
        }

        public Builder aleneomsorg(boolean aleneomsorg) {
            kladd.aleneomsorg = aleneomsorg;
            return this;
        }

        public RettOgOmsorg build() {
            return kladd;
        }
    }
}
