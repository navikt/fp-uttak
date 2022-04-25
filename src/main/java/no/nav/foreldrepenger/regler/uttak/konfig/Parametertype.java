package no.nav.foreldrepenger.regler.uttak.konfig;

public enum Parametertype {

    //Stønadskontoer
    EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100,
    EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80,
    EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100,
    EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80,
    FEDREKVOTE_DAGER_100_PROSENT,
    MØDREKVOTE_DAGER_100_PROSENT,
    FEDREKVOTE_DAGER_80_PROSENT,
    MØDREKVOTE_DAGER_80_PROSENT,
    FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER,
    FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER,
    FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER,
    FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER,
    FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER,
    FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER,
    FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER,
    FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER,
    FORELDREPENGER_FØR_FØDSEL,

    //Rettigheter utenom konti
    BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_100_PROSENT,
    BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_80_PROSENT,
    BARE_FAR_DAGER_MINSTERETT,
    BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_100_PROSENT,
    BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_80_PROSENT,
    MOR_TO_TETTE_DAGER_MINSTERETT,
    FAR_TO_TETTE_DAGER_MINSTERETT,

    //Uttaksperidoer
    UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER,
    LOVLIG_UTTAK_FØR_FØDSEL_UKER,
    UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER,
    UTTAK_ETTER_BARN_DØDT_UKER,
    FAR_UTTAK_FØR_TERMIN_UKER,
    FAR_UTTAK_ETTER_FØDSEL_UKER,

    //Grenser
    PREMATURUKER_ANTALL_DAGER_FØR_TERMIN,
    GRENSE_ETTER_FØDSELSDATO_ÅR;

}
