package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

public enum IkkeOppfyltÅrsak implements PeriodeResultatÅrsak {

    // Uttak årsaker
    IKKE_STØNADSDAGER_IGJEN(4002, "Ikke Stønadsdager igjen"),
    MOR_HAR_IKKE_OMSORG(4003, "Mor har ikke omsorg"),
    HULL_MELLOM_FORELDRENES_PERIODER(4005, "Hull mellom foreldrenes perioder"),
    FAR_HAR_IKKE_OMSORG(4012, "Far har ikke omsorg"),
    MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL(4013, "Mor søker fellesperiode før 12 uker før termin/fødsel"),
    SØKNADSFRIST(4020, "Brudd på søknadsfrist"),
    UTTAK_ETTER_3_ÅRSGRENSE(4022, "Uttak etter 3 årsgrense"),
    ARBEID_HUNDRE_PROSENT_ELLER_MER(4025, "Arbeider 100 prosent eller mer"),
    OPPHOLD_IKKE_SAMTIDIG_UTTAK(4084, "Opphold på grunn av den andre forelderens vedtak"),
    IKKE_SAMTYKKE(4085, "Ikke samtykke mellom foreldrene"),
    OPPHOLD_UTSETTELSE(4086, "Opphold på grunn av den andre forelderens vedtak"),
    MOR_TAR_IKKE_UKENE_FØR_FØDSEL(4095, "Mor tar ikke alle ukene før fødsel"),
    MOR_TAR_IKKE_UKENE_ETTER_FØDSEL(4103, "Mor tar ikke alle ukene etter fødsel"),
    BARE_FAR_RETT_IKKE_SØKT(4102, "Bare far rett ikke-søkt periode"),
    SØKER_DØD(4071, "Søker er død"),
    BARN_DØD(4072, "Barnet er dødt"),
    MOR_IKKE_RETT_FK(4073, "Ikke rett til kvote fordi mor ikke har rett til foreldrepenger"),
    MOR_IKKE_RETT_FP(4075, "Ikke rett til fellesperiode fordi mor ikke har rett til foreldrepenger"),
    FAR_PERIODE_FØR_FØDSEL(4105, "Far/medmor søker uttak før fødsel/omsorg"),

    // Adopsjon
    FØR_OMSORGSOVERTAKELSE(4100, "Uttak før omsorgsovertakelse"),

    // Overføring årsaker
    DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT(4007, "Den andre part syk/skadet ikke oppfylt"),
    DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT(4008, "Den andre part innleggelse ikke oppfylt"),
    ALENEOMSORG_IKKE_OPPFYLT(4092, "Aleneomsorg ikke oppfylt"),
    DEN_ANDRE_PART_IKKE_RETT_IKKE_OPPFYLT(4076, "Den andre part ikke rett ikke oppfylt"),

    // Utsettelse årsaker
    UTSETTELSE_FØR_TERMIN_FØDSEL(4030, "Avslag utsettelse før termin/fødsel"),
    UTSETTELSE_INNENFOR_DE_FØRSTE_6_UKENE(4031, "Utsettelse innenfor de første 6 ukene"),
    FERIE_SELVSTENDIG_NÆRINGSDRIVENDSE_FRILANSER(4032, "Ferie - selvstendig næringsdrivende/frilanser"),
    INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE(4034, "Avslag utsettelse - ingen stønadsdager igjen"),
    IKKE_HELTIDSARBEID(4037, "Ikke heltidsarbeid"),
    SØKERS_SYKDOM_SKADE_IKKE_OPPFYLT(4038, "Søkers sykdom/skade ikke oppfylt"),
    SØKERS_INNLEGGELSE_IKKE_OPPFYLT(4039, "Søkers innleggelse ikke oppfylt"),
    BARNETS_INNLEGGELSE_IKKE_OPPFYLT(4040, "Barnets innleggelse ikke oppfylt"),
    UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG(4041, "Avslag utsettelse ferie på bevegelig helligdag"),

    SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT(4080, "Søker har søkt om gradert uttak etter at perioden med delvis arbeid er påbegynt"),
    SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT(4081, "Søker har søkt om utsettelse pga ferie etter ferien er begynt"),
    SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT(4082, "Søker har søkt om utsettelse pga arbeid etter arbeid er begynt"),

    SØKERS_SYKDOM_SKADE_SEKS_UKER_IKKE_OPPFYLT(4110, "Søkers sykdom/skade første 6 uker ikke oppfylt"),
    SØKERS_INNLEGGELSE_SEKS_UKER_IKKE_OPPFYLT(4111, "Søkers innleggelse første 6 uker ikke oppfylt"),
    BARNETS_INNLEGGELSE_SEKS_UKER_IKKE_OPPFYLT(4112, "Barnets innleggelse første 6 uker ikke oppfylt"),

    //Medlem
    SØKER_IKKE_MEDLEM(4087, "Søker ikke medlem"),

    //Vilkår
    FØDSELSVILKÅRET_IKKE_OPPFYLT(4096, "Fødselsvilkåret er ikke oppfylt"),
    ADOPSJONSVILKÅRET_IKKE_OPPFYLT(4097, "Adopsjonsvilkåret er ikke oppfylt"),
    FORELDREANSVARSVILKÅRET_IKKE_OPPFYLT(4098, "Foreldreansvarsvilkåret er ikke oppfylt"),
    OPPTJENINGSVILKÅRET_IKKE_OPPFYLT(4099, "Opptjeningsvilkåret er ikke oppfylt"),

    //Prematur
    FRATREKK_PLEIEPENGER(4077, "Avslag utsettelse, fratrekk pleiepenger"),

    //aktivitetskrav
    AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT(4050,"Aktivitetskravet arbeid ikke oppfylt"),
    AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT(4066,"Aktivitetskravet arbeid ikke dokumentert"),

    AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT(4051,"Aktivitetskravet utdanning ikke oppfylt"),
    AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT(4067,"Aktivitetskravet utdanning ikke dokumentert"),

    AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_OPPFYLT(4052,"Aktivitetskravet arbeid+utdanning ikke oppfylt"),
    AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_DOKUMENTERT(4068,"Aktivitetskravet arbeid+utdanning ikke dokumentert"),

    AKTIVITETSKRAVET_SYKDOM_IKKE_OPPFYLT(4053,"Aktivitetskravet sykdom ikke oppfylt"),
    AKTIVITETSKRAVET_SYKDOM_IKKE_DOKUMENTERT(4069,"Aktivitetskravet sykdom ikke dokumentert"),

    AKTIVITETSKRAVET_INNLEGGELSE_IKKE_OPPFYLT(4054,"Aktivitetskravet innleggelse ikke oppfylt"),
    AKTIVITETSKRAVET_INNLEGGELSE_IKKE_DOKUMENTERT(4070,"Aktivitetskravet innleggelse ikke dokumentert"),

    AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT(4055,"Aktivitetskravet introduksjonsprogrammet ikke oppfylt"),
    AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT(4088,"Aktivitetskravet introduksjonsprogrammet ikke dokumentert"),

    AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT(4056,"Aktivitetskravet kvalifiseringsprogrammet ikke oppfylt"),
    AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_DOKUMENTERT(4089,"Aktivitetskravet kvalifiseringsprogrammet ikke dokumentert"),


    FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR(4057, "Foreldrepenger, kun far har rett mor er ikke ufør");

    private final int id;
    private final String beskrivelse;

    IkkeOppfyltÅrsak(int id, String beskrivelse) {
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
}
