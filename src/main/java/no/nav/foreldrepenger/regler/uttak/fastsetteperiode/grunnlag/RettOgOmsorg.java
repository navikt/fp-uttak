package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public final class RettOgOmsorg {

    private boolean farHarRett;
    private boolean morHarRett;
    private boolean farHarRettEØS;
    private boolean morHarRettEØS;
    private boolean samtykke;
    private boolean aleneomsorg;
    private boolean morUføretrygd;
    private boolean morOppgittUføretrygd;
    private boolean harOmsorg = true;

    private RettOgOmsorg() {}

    public boolean getFarHarRett() {
        return farHarRett || farHarRettEØS;
    }

    public boolean getMorHarRett() {
        return morHarRett || morHarRettEØS;
    }

    public boolean bareFarHarRett() {
        return getFarHarRett() && !getMorHarRett();
    }

    public boolean getSamtykke() {
        return samtykke;
    }

    public boolean getAleneomsorg() {
        return aleneomsorg;
    }

    public boolean getMorUføretrygd() {
        return morUføretrygd;
    }

    public boolean getMorOppgittUføretrygd() {
        return morOppgittUføretrygd;
    }

    public boolean getHarOmsorg() {
        return harOmsorg;
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

        public Builder farHarRettEØS(boolean farHarRettEØS) {
            kladd.farHarRettEØS = farHarRettEØS;
            return this;
        }

        public Builder morHarRettEØS(boolean morHarRettEØS) {
            kladd.morHarRettEØS = morHarRettEØS;
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

        public Builder morUføretrygd(boolean morUføretrygd) {
            kladd.morUføretrygd = morUføretrygd;
            return this;
        }

        public Builder morOppgittUføretrygd(boolean morOppgittUføretrygd) {
            kladd.morOppgittUføretrygd = morOppgittUføretrygd;
            return this;
        }

        public Builder harOmsorg(boolean harOmsorg) {
            kladd.harOmsorg = harOmsorg;
            return this;
        }

        public RettOgOmsorg build() {
            return kladd;
        }
    }
}
