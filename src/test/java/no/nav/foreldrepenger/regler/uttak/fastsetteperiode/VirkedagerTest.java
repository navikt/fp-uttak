package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VirkedagerTest {
    private Map<DayOfWeek, LocalDate> uke;

    @BeforeEach
    void setUp() {
        var iDag = LocalDate.now();
        var mandag = iDag.minusDays(iDag.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        uke = Stream.of(DayOfWeek.values()).collect(Collectors.toMap(day -> day, day -> mandag.plusDays(day.ordinal())));
    }

    @Test
    void skalBeregneAntallVirkedager() {
        var mandag = getDayOfWeek(DayOfWeek.MONDAY);
        var søndag = getDayOfWeek(DayOfWeek.SUNDAY);

        Assertions.assertThat(Virkedager.beregnAntallVirkedager(mandag, søndag)).isEqualTo(5);
        assertThat(Virkedager.beregnAntallVirkedager(mandag, søndag.plusDays(1))).isEqualTo(6);
        assertThat(Virkedager.beregnAntallVirkedager(mandag, søndag.plusDays(10))).isEqualTo(13);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.plusDays(1), søndag)).isEqualTo(4);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.plusDays(1), søndag.plusDays(1))).isEqualTo(5);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.plusDays(4), søndag)).isEqualTo(1);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.plusDays(5), søndag)).isEqualTo(0);

        assertThat(Virkedager.beregnAntallVirkedager(mandag.minusDays(1), søndag)).isEqualTo(5);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.minusDays(2), søndag)).isEqualTo(5);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.minusDays(3), søndag)).isEqualTo(6);
        assertThat(Virkedager.beregnAntallVirkedager(mandag.minusDays(3), søndag.plusDays(1))).isEqualTo(7);
    }

    @Test
    void skalLeggeTilVirkedager() {
        var mandag = getDayOfWeek(DayOfWeek.MONDAY);
        var tirsdag = getDayOfWeek(DayOfWeek.TUESDAY);
        var onsdag = getDayOfWeek(DayOfWeek.WEDNESDAY);
        var fredag = getDayOfWeek(DayOfWeek.FRIDAY);
        var lørdag = getDayOfWeek(DayOfWeek.SATURDAY);
        var søndag = getDayOfWeek(DayOfWeek.SUNDAY);
        var nesteMandag = mandag.plusWeeks(1);
        var nesteTirsdag = tirsdag.plusWeeks(1);

        assertThat(Virkedager.plusVirkedager(mandag, 1)).isEqualTo(tirsdag);
        assertThat(Virkedager.plusVirkedager(mandag, 4)).isEqualTo(fredag);
        assertThat(Virkedager.plusVirkedager(mandag, 5)).isEqualTo(nesteMandag);

        assertThat(Virkedager.plusVirkedager(tirsdag, 1)).isEqualTo(onsdag);
        assertThat(Virkedager.plusVirkedager(tirsdag, 3)).isEqualTo(fredag);
        assertThat(Virkedager.plusVirkedager(tirsdag, 4)).isEqualTo(nesteMandag);

        assertThat(Virkedager.plusVirkedager(lørdag, 1)).isEqualTo(nesteMandag);
        assertThat(Virkedager.plusVirkedager(fredag, 2)).isEqualTo(nesteTirsdag);
        assertThat(Virkedager.plusVirkedager(lørdag, 2)).isEqualTo(nesteTirsdag);

        assertThat(Virkedager.plusVirkedager(søndag, 5)).isEqualTo(fredag.plusWeeks(1));
    }

    @Test
    void søndagPlusEnVirkedagerSkalBliMandag() {
        var nyDato = Virkedager.plusVirkedager(LocalDate.of(2020, 8, 2), 1);
        assertThat(nyDato).isEqualTo(LocalDate.of(2020, 8, 3));
    }

    @Test
    void søndagPlusNullVirkedagerSkalBliSøndag() {
        var nyDato = Virkedager.plusVirkedager(LocalDate.of(2020, 8, 2), 0);
        assertThat(nyDato).isEqualTo(LocalDate.of(2020, 8, 2));
    }

    private LocalDate getDayOfWeek(DayOfWeek dayOfWeek) {
        var date = uke.get(dayOfWeek);
        assertThat(date.getDayOfWeek()).isEqualTo(dayOfWeek);
        return date;
    }
}
