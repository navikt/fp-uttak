package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

public enum Parametertype {

    // Uttaksperidoer
    FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
    TIDLIGST_UTTAK_FØR_TERMIN_UKER,
    SENEST_UTTAK_FØR_TERMIN_UKER,
    UTTAK_ETTER_BARN_DØDT_UKER,
    FAR_UTTAK_FØR_TERMIN_UKER,
    FAR_UTTAK_ETTER_FØDSEL_UKER,

    // Grenser
    PREMATURUKER_ANTALL_DAGER_FØR_TERMIN,
    TETTE_SAKER_MELLOMROM_UKER,
    GRENSE_ETTER_FØDSELSDATO_ÅR
}
