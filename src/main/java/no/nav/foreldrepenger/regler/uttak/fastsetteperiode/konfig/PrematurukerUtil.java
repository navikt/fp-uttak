package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon.PREMATURUKER_REGELENDRING_START_DATO;

import java.time.LocalDate;

public final class PrematurukerUtil {

    private PrematurukerUtil() {}

    public static boolean oppfyllerKravTilPrematuruker(
            LocalDate fødselsdato, LocalDate termindato) {
        if (fødselsdato == null || termindato == null) {
            return false;
        }
        if (erEtterRegelendringStartdato(fødselsdato)) {
            var antallDagerFørTermin =
                    Konfigurasjon.STANDARD.getParameter(
                            Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, fødselsdato);
            return fødselsdato.plusDays(antallDagerFørTermin).isBefore(termindato);
        }
        return false;
    }

    private static boolean erEtterRegelendringStartdato(LocalDate fødselsdato) {
        return !fødselsdato.isBefore(PREMATURUKER_REGELENDRING_START_DATO);
    }
}
