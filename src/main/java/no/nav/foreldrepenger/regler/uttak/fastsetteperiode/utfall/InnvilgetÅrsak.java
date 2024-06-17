package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

public enum InnvilgetÅrsak implements PeriodeResultatÅrsak {

    // Uttak årsaker
    FELLESPERIODE_ELLER_FORELDREPENGER(2002, "Fellesperiode/foreldrepenger"),
    KVOTE_ELLER_OVERFØRT_KVOTE(2003, "Kvote/overført kvote"),
    FORELDREPENGER_KUN_FAR_HAR_RETT(2004, "Foreldrepenger, kun far har rett"),
    FORELDREPENGER_ALENEOMSORG(2005, "Foreldrepenger ved aleneomsorg"),
    FORELDREPENGER_FØR_FØDSEL(2006, "Foreldrepenger før fødsel"),
    FORELDREPENGER_KUN_MOR_HAR_RETT(2007, "Foreldrepenger, kun mor har rett"),
    GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER(2030, "Gradering av fellesperiode/foreldrepenger"),
    GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE(2031, "Gradering av kvote/overført kvote"),
    GRADERING_ALENEOMSORG(2032, "Gradering ved aleneomsorg"),
    GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT(2033, "Gradering foreldrepenger, kun far har rett"),
    GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT(2034, "Gradering foreldrepenger, kun mor har rett"),
    GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV(2035, "Gradering foreldrepenger, kun far har rett - dager uten aktivitetskrav"),
    FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV(2036, "Innvilget foreldrepenger, kun far har rett - dager uten aktivitetskrav"),
    MSP_INNVILGET(2039, "Innvilger msp første 6 ukene"),

    // Overføring årsaker
    OVERFØRING_ANNEN_PART_IKKE_RETT(2020, "Overføring - annen part ikke rett"),
    OVERFØRING_ANNEN_PART_SYKDOM_SKADE(2021, "Overføring - annen part sykdom/skade"),
    OVERFØRING_ANNEN_PART_INNLAGT(2022, "Overføring - annen part innlagt"),
    OVERFØRING_ALENEOMSORG(2023, "Overføring - aleneomsorg"),

    //Utsettelse årsaker
    UTSETTELSE_GYLDIG_PGA_FERIE(2010, "Utsettelse pga ferie"),
    UTSETTELSE_GYLDIG_PGA_100_PROSENT_ARBEID(2011, "Utsettelse pga 100% arbeid"),
    UTSETTELSE_GYLDIG_PGA_INNLEGGELSE(2012, "Utsettelse pga innleggelse"),
    UTSETTELSE_GYLDIG_PGA_BARN_INNLAGT(2013, "Utsettelse pga innleggelse barn"),
    UTSETTELSE_GYLDIG_PGA_SYKDOM(2014, "Utsettelse pga sykdom"),


    UTSETTELSE_GYLDIG(2024, "Gyldig utsettelse"),
    UTSETTELSE_GYLDIG_SEKS_UKER_INNLEGGELSE(2025, "Gyldig utsettelse første 6 uker pga. innleggelse"),
    UTSETTELSE_GYLDIG_SEKS_UKER_FRI_BARN_INNLAGT(2026, "Gyldig utsettelse første 6 uker pga. barn innlagt"),
    UTSETTELSE_GYLDIG_SEKS_UKER_FRI_SYKDOM(2027, "Gyldig utsettelse første 6 uker pga. sykdom"),
    UTSETTELSE_GYLDIG_BFR_AKT_KRAV_OPPFYLT(2028, "Gyldig utsettelse aktivitetskrav oppfylt "),
    ;

    private final int id;
    private final String beskrivelse;

    InnvilgetÅrsak(int id, String beskrivelse) {
        this.id = id;
        this.beskrivelse = beskrivelse;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getBeskrivelse() {
        return beskrivelse;
    }

    @Override
    public boolean trekkerMinsterett() {
        return this.equals(FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV) || this.equals(
            GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);
    }
}
