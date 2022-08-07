package no.nav.foreldrepenger.regler.uttak.felles;

import java.time.LocalDate;

/*
 * Brukes kun internt
 */
public final class SøknadsfristUtil {

    private static final long SØKNADSFRIST_MÅNEDER = 3;

    private SøknadsfristUtil() {
    }

    public static LocalDate finnFørsteLoveligeUttaksdag(LocalDate søknadMottattDato) {
        return søknadMottattDato.withDayOfMonth(1).minusMonths(SØKNADSFRIST_MÅNEDER);
    }
}
