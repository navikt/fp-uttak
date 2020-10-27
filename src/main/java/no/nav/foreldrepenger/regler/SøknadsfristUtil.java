package no.nav.foreldrepenger.regler;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class SøknadsfristUtil {

    private static final long SØKNADSFRIST_MÅNEDER = 3;

    private SøknadsfristUtil() {
    }

    public static LocalDate finnFørsteLoveligeUttaksdag(LocalDate søknadMottattDato) {
        return søknadMottattDato.withDayOfMonth(1).minusMonths(SØKNADSFRIST_MÅNEDER);
    }

    public static LocalDate finnSøknadsfrist(LocalDate periodeStart) {
        return periodeStart.plusMonths(SØKNADSFRIST_MÅNEDER).with(TemporalAdjusters.lastDayOfMonth());
    }
}
