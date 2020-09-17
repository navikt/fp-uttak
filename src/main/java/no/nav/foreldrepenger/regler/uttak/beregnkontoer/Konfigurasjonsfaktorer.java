package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Optional;

public class Konfigurasjonsfaktorer {

    enum Berettiget{
        MOR,
        FAR,
        BEGGE
    }

    private Boolean er100Prosent;
    private Integer antallLevendeBarn;
    private Boolean erFødsel;
    private Berettiget berettiget;
    private Boolean erAleneomsorg;

    public Konfigurasjonsfaktorer() {
    }

    public Optional<Boolean> er100Prosent() {
        return Optional.ofNullable(er100Prosent);
    }

    public Optional<Integer> antallLevendeBarn() {
        return Optional.ofNullable(antallLevendeBarn);
    }

    public Optional<Boolean> erFødsel() {
        return Optional.ofNullable(erFødsel);
    }

    public Optional<Berettiget> berettiget() {
        return Optional.ofNullable(berettiget);
    }

    public Optional<Boolean> erAleneomsorg() {
        return Optional.ofNullable(erAleneomsorg);
    }

    public static class Builder {
        private final Konfigurasjonsfaktorer kladd;

        public Builder() {
            this.kladd = new Konfigurasjonsfaktorer();

        }

        public Konfigurasjonsfaktorer.Builder medEr100Prosent(Boolean er100Prosent) {
            this.kladd.er100Prosent = er100Prosent;
            return this;
        }

        public Konfigurasjonsfaktorer.Builder medAntallLevendeBarn(Integer antallLevendeBarn) {
            this.kladd.antallLevendeBarn = antallLevendeBarn;
            return this;
        }

        public Konfigurasjonsfaktorer.Builder medErFødsel(Boolean erFødsel) {
            this.kladd.erFødsel = erFødsel;
            return this;
        }

        public Konfigurasjonsfaktorer.Builder medBerettiget(Berettiget berettiget) {
            this.kladd.berettiget = berettiget;
            return this;
        }
        public Konfigurasjonsfaktorer.Builder medErAleneomsorg(Boolean erAleneomsorg) {
            this.kladd.erAleneomsorg = erAleneomsorg;
            return this;
        }
        public Konfigurasjonsfaktorer build() {
            return this.kladd;
        }
    }
}
