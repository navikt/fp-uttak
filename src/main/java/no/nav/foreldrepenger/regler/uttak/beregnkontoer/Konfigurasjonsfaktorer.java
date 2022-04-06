package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

public class Konfigurasjonsfaktorer {

    enum Berettiget {
        MOR,
        FAR,
        FAR_ALENE,
        BEGGE
    }

    static final Map<Berettiget, List<Kontokonfigurasjon>> KONFIGURASJONER_100_PROSENT = Map.ofEntries(Map.entry(Berettiget.MOR,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER,
                    Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER))), Map.entry(Berettiget.FAR,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER,
                    Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER))), Map.entry(Berettiget.FAR_ALENE,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER,
                    Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER))), Map.entry(Berettiget.BEGGE,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                    new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                    new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT))));

    static final Map<Konfigurasjonsfaktorer.Berettiget, List<Kontokonfigurasjon>> KONFIGURASJONER_80_PROSENT = Map.ofEntries(
            Map.entry(Berettiget.MOR, List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER,
                    Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER))), Map.entry(Berettiget.FAR,
                    List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER,
                            Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER))), Map.entry(Berettiget.FAR_ALENE,
                    List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER,
                            Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER))), Map.entry(Berettiget.BEGGE,
                    List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FELLESPERIODE,
                                    Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                            new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                            new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT))));

    private Boolean er100Prosent;
    private Integer antallLevendeBarn;
    private Boolean erFødsel;
    private Berettiget berettiget;

    public Konfigurasjonsfaktorer() {
    }

    public Boolean er100Prosent() {
        return er100Prosent;
    }

    public Integer getAntallLevendeBarn() {
        return antallLevendeBarn;
    }

    public Boolean erFødsel() {
        return erFødsel;
    }

    public Berettiget getBerettiget() {
        return berettiget;
    }

    public static class Builder {
        private final Konfigurasjonsfaktorer kladd;

        public Builder() {
            this.kladd = new Konfigurasjonsfaktorer();

        }

        public Konfigurasjonsfaktorer.Builder er100Prosent(Boolean er100Prosent) {
            this.kladd.er100Prosent = er100Prosent;
            return this;
        }

        public Konfigurasjonsfaktorer.Builder antallLevendeBarn(Integer antallLevendeBarn) {
            this.kladd.antallLevendeBarn = antallLevendeBarn;
            return this;
        }

        public Konfigurasjonsfaktorer.Builder erFødsel(Boolean erFødsel) {
            this.kladd.erFødsel = erFødsel;
            return this;
        }

        public Konfigurasjonsfaktorer.Builder berettiget(Berettiget berettiget) {
            this.kladd.berettiget = berettiget;
            return this;
        }

        public Konfigurasjonsfaktorer build() {
            return this.kladd;
        }
    }
}
