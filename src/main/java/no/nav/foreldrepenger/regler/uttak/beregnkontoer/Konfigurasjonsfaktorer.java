package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

public class Konfigurasjonsfaktorer {

    enum Berettiget{
        MOR,
        FAR,
        FAR_ALENE,
        BEGGE
    }
    /*
    static final Map<Berettiget, List<Kontokonfigurasjon>> KONFIGURASJONER_100_PROSENT = Map.ofEntries(
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.MOR,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER))
                            .collect(Collectors.toList())),
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.FAR,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER))
                            .collect(Collectors.toList())),
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.FAR_ALENE,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER))
                            .collect(Collectors.toList())),
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.BEGGE,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                            new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                            new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT))
                            .collect(Collectors.toList()))
    );

    static final Map<Konfigurasjonsfaktorer.Berettiget, List<Kontokonfigurasjon>> KONFIGURASJONER_80_PROSENT = Map.ofEntries(
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.MOR,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER))
                            .collect(Collectors.toList())),
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.FAR,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER))
                            .collect(Collectors.toList())),
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.FAR_ALENE,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER))
                            .collect(Collectors.toList())),
            new AbstractMap.SimpleEntry<Berettiget, List<Kontokonfigurasjon>>(
                    Berettiget.BEGGE,
                    Stream.of(new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                            new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                            new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT))
                            .collect(Collectors.toList()))
    );
*/
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

        public Konfigurasjonsfaktorer build() {
            return this.kladd;
        }
    }
}
