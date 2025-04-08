package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public final class RettOgOmsorg {

    private Rettighetstype rettighetstype;

    private boolean samtykke;
    private boolean morOppgittUføretrygd;
    private boolean harOmsorg = true;

    private RettOgOmsorg() {
    }

    public boolean getSamtykke() {
        return samtykke;
    }

    public boolean getMorOppgittUføretrygd() {
        return morOppgittUføretrygd;
    }

    public boolean getHarOmsorg() {
        return harOmsorg;
    }

    public Rettighetstype rettighetsType() {
        return rettighetstype;
    }

    public static class Builder {
        private final RettOgOmsorg kladd = new RettOgOmsorg();

        public Builder samtykke(boolean samtykke) {
            kladd.samtykke = samtykke;
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

        public Builder rettighetstype(Rettighetstype rettighetstype) {
            kladd.rettighetstype = rettighetstype;
            return this;
        }

        public RettOgOmsorg build() {
            Objects.requireNonNull(kladd.rettighetstype, "rettighetstype");
            return kladd;
        }
    }
}
