package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum IkkeOppfyltÅrsak implements Årsak {

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
    MOR_TAR_IKKE_ALLE_UKENE(4095, "Mor tar ikke alle ukene"),
    SØKER_DØD(4071, "Søker er død"),
    BARN_DØD(4072, "Barnet er dødt"),
    MOR_IKKE_RETT_FK(4073, "Ikke rett til kvote fordi mor ikke har rett til foreldrepenger"),
    MOR_IKKE_RETT_FP(4075, "Ikke rett til fellesperiode fordi mor ikke har rett til foreldrepenger"),

    // Adopsjon
    FØR_OMSORGSOVERTAKELSE(4100, "Uttak før omsorgsovertakelse"),

    // Overføring årsaker
    DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT(4007, "Den andre part syk/skadet ikke oppfylt"),
    DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT(4008, "Den andre part innleggelse ikke oppfylt"),
    ALENEOMSORG_IKKE_OPPFYLT(4092, "Aleneomsorg ikke oppfylt"),
    DEN_ANDRE_PART_IKKE_RETT_IKKE_OPPFYLT(4076, "Den andre part ikke rett ikke oppfylt"),

    // Utsettelse årsaker
    UTSETTELSE_FØR_TERMIN_FØDSEL(4030, "Avslag utsettelse før termin/fødsel"),
    FERIE_INNENFOR_DE_FØRSTE_6_UKENE(4031, "Ferie/arbeid innenfor de første 6 ukene"),
    FERIE_SELVSTENDIG_NÆRINGSDRIVENDSE_FRILANSER(4032, "Ferie - selvstendig næringsdrivende/frilanser"),
    IKKE_HELTIDSARBEID(4037, "Ikke heltidsarbeid"),
    SØKERS_SYKDOM_SKADE_IKKE_OPPFYLT(4038, "Søkers sykdom/skade ikke oppfylt"),
    SØKERS_INNLEGGELSE_IKKE_OPPFYLT(4039, "Søkers innleggelse ikke oppfylt"),
    BARNETS_INNLEGGELSE_IKKE_OPPFYLT(4040, "Barnets innleggelse ikke oppfylt"),
    UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG(4041, "Avslag utsettelse ferie på bevegelig helligdag"),

    SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT(4080 , "Søker har søkt om gradert uttak etter at perioden med delvis arbeid er påbegynt"),
    SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT(4081, "Søker har søkt om utsettelse pga ferie etter ferien er begynt"),
    SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT(4082, "Søker har søkt om utsettelse pga arbeid etter ferien er begynt"),

    //Medlem
    SØKER_IKKE_MEDLEM(4087, "Søker ikke medlem"),

    //Vilkår
    FØDSELSVILKÅRET_IKKE_OPPFYLT(4096, "Fødselsvilkåret er ikke oppfylt"),
    ADOPSJONSVILKÅRET_IKKE_OPPFYLT(4097, "Adopsjonsvilkåret er ikke oppfylt"),
    FORELDREANSVARSVILKÅRET_IKKE_OPPFYLT(4098, "Foreldreansvarsvilkåret er ikke oppfylt"),
    OPPTJENINGSVILKÅRET_IKKE_OPPFYLT(4099, "Opptjeningsvilkåret er ikke oppfylt"),

    //Prematur
    FRATREKK_PLEIEPENGER(4077, "Avslag utsettelse, fratrekk pleiepenger");

    private int id;
    private String beskrivelse;

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
