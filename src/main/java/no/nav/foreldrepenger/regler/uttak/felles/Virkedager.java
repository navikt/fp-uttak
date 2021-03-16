package no.nav.foreldrepenger.regler.uttak.felles;

import static java.lang.Math.toIntExact;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public class Virkedager {

    private static final int DAGER_PR_UKE = 7;
    private static final int VIRKEDAGER_PR_UKE = 5;
    private static final int HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;
    private static final TemporalAdjuster NESTE_VIRKEDAG_ADJUSTER = TemporalAdjusters.ofDateAdjuster(
            dato -> dato.plusDays(finnDagerÅLeggeTil(dato)));
    private static final TemporalAdjuster HELG_TIL_MANDAG_ADJUSTER = TemporalAdjusters.ofDateAdjuster(
            dato -> erHelg(dato) ? dato.with(NESTE_VIRKEDAG_ADJUSTER) : dato);

    private Virkedager() {
        // For å unngå instanser
    }

    public static int beregnAntallVirkedager(Periode periode) {
        Objects.requireNonNull(periode);
        return beregnAntallVirkedager(periode.getFom(), periode.getTom());
    }

    public static int beregnAntallVirkedager(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
        if (fom.isAfter(tom)) {
            throw new IllegalArgumentException("Utviklerfeil: fom " + fom + " kan ikke være før tom " + tom);
        }

        try {
            // Utvid til nærmeste mandag tilbake i tid fra og med begynnelse (fom) (0-6 dager)
            var padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            // Utvid til nærmeste søndag fram i tid fra og med slutt (tom) (0-6 dager)
            var padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            // Antall virkedager i perioden utvidet til hele uker
            var virkedagerPadded = toIntExact(
                    ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1)) * VIRKEDAGER_PR_UKE);
            // Antall virkedager i utvidelse
            var virkedagerPadding = Math.min(padBefore, VIRKEDAGER_PR_UKE) + Math.max(padAfter - HELGEDAGER_PR_UKE, 0);
            // Virkedager i perioden uten virkedagene fra utvidelse
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", e);
        }
    }

    public static LocalDate plusVirkedager(LocalDate dato, int virkedager) {
        while (virkedager > 0) {
            dato = plusVirkedag(dato);
            virkedager--;
        }
        return dato;
    }

    public static LocalDate justerHelgTilMandag(LocalDate dato) {
        return dato.with(HELG_TIL_MANDAG_ADJUSTER);
    }

    private static LocalDate plusVirkedag(LocalDate dato) {
        return dato.with(NESTE_VIRKEDAG_ADJUSTER);
    }

    private static int finnDagerÅLeggeTil(LocalDate dato) {
        if (erFredag(dato)) {
            return 3;
        }
        if (erLørdag(dato)) {
            return 2;
        }
        return 1;
    }

    private static boolean erHelg(LocalDate dato) {
        return erLørdag(dato) || erSøndag(dato);
    }

    private static boolean erFredag(LocalDate dato) {
        return dato.getDayOfWeek().equals(DayOfWeek.FRIDAY);
    }

    private static boolean erSøndag(LocalDate dato) {
        return dato.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

    private static boolean erLørdag(LocalDate dato) {
        return dato.getDayOfWeek().equals(DayOfWeek.SATURDAY);
    }
}
