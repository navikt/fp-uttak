package no.nav.foreldrepenger.regler.uttak.felles;

import static java.lang.Math.toIntExact;
import static java.time.temporal.TemporalAdjusters.next;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public class Virkedager {

    private static final Set<DayOfWeek> WEEKEND = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private static final int DAGER_PR_UKE = 7;
    private static final int VIRKEDAGER_PR_UKE = 5;
    private static final int HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;

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
        return WEEKEND.contains(dato.getDayOfWeek()) ? dato.with(next(DayOfWeek.MONDAY)) : dato;
    }

    private static LocalDate plusVirkedag(LocalDate dato) {
        var plussen = dato.plusDays(1);
        return WEEKEND.contains(plussen.getDayOfWeek()) ? dato.with(next(DayOfWeek.MONDAY)) : plussen;
    }

}
