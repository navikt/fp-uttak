package no.nav.foreldrepenger.regler.uttak;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

public class UttakParametre {
    private UttakParametre() {
    }

    /**
     * Grenser for mors uttak og periode forbeholdt mor
     */
    public static int ukerFørTerminTidligstUttak(LocalDate dato) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.TIDLIGST_UTTAK_FØR_TERMIN_UKER, dato);
    }

    public static int ukerFørTerminSenestUttak(LocalDate dato) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.SENEST_UTTAK_FØR_TERMIN_UKER, dato);
    }

    public static int ukerReservertMorEtterFødsel(LocalDate dato) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, dato);
    }

    /**
     * Dødsfall
     */
    public static int ukerTilgjengeligEtterDødsfall(LocalDate dato) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER, dato);
    }

    /**
     * Stønadsperiode
     */
    public static int ukerMellomTetteFødsler(LocalDate dato) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, dato);
    }

    public static int årMaksimalStønadsperiode(LocalDate dato) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO_ÅR, dato);
    }


}
