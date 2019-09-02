package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import static no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon.PREMATURUKER_REGELENDRING_START_DATO;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

public final class PrematurukerUtil {

    private PrematurukerUtil() {
    }

    public static boolean oppfyllerKravTilPrematuruker(LocalDate fødselsdato,
                                                       LocalDate termindato,
                                                       Konfigurasjon konfigurasjon) {
        if (fødselsdato == null || termindato == null) {
            return false;
        }
        if (erEtterRegelendringStartdato(fødselsdato)) {
            int antallDagerFørTermin = konfigurasjon.getParameter(Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, fødselsdato);
            return fødselsdato.plusDays(antallDagerFørTermin).isBefore(termindato);
        }
        return false;
    }

    private static boolean erEtterRegelendringStartdato(LocalDate fødselsdato) {
        return !fødselsdato.isBefore(PREMATURUKER_REGELENDRING_START_DATO);
    }
}
