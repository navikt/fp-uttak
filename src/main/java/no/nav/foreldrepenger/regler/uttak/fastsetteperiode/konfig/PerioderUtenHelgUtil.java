package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import static java.time.temporal.TemporalAdjusters.next;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;

public final class PerioderUtenHelgUtil {

    private static final Set<DayOfWeek> WEEKEND = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private PerioderUtenHelgUtil() {
        //Privat constructor for å hindre instanser.
    }

    public static boolean periodeUtenHelgOmslutter(LukketPeriode omsluttendePeriode, LukketPeriode omsluttetPeriode) {
        var fom1 = justerFom(omsluttendePeriode.getFom());
        var tom1 = justerTom(omsluttendePeriode.getTom());
        var fom2 = justerFom(omsluttetPeriode.getFom());
        var tom2 = justerTom(omsluttetPeriode.getTom());

        return !fom2.isBefore(fom1) && !tom2.isAfter(tom1);
    }

    private static boolean periodeErTom(LocalDate fom1, LocalDate tom1) {
        return tom1.isBefore(fom1);
    }

    public static boolean perioderUtenHelgOverlapper(LukketPeriode periode1, LukketPeriode periode2) {
        return perioderUtenHelgOverlapper(periode1.getFom(), periode1.getTom(), periode2.getFom(), periode2.getTom());
    }

    public static boolean perioderUtenHelgOverlapper(LocalDate fom1, LocalDate tom1, LocalDate fom2, LocalDate tom2) {
        var justertFom1 = justerFom(fom1);
        var justertTom1 = justerTom(tom1);
        if (periodeErTom(justertFom1, justertTom1)) {
            return false;
        }
        var justertFom2 = justerFom(fom2);
        var justertTom2 = justerTom(tom2);
        if (periodeErTom(justertFom2, justertTom2)) {
            return false;
        }
        return !justertFom2.isAfter(justertTom1) && !justertTom2.isBefore(justertFom1);
    }

    public static boolean likNårHelgIgnoreres(LocalDate fom1, LocalDate tom1, LocalDate fom2, LocalDate tom2) {
        return justerFom(fom1).equals(justerFom(fom2)) && justerTom(tom1).equals(justerTom(tom2));
    }

    private static LocalDate justerFom(LocalDate dato) {
        return helgBlirMandag(dato);
    }

    private static LocalDate justerTom(LocalDate dato) {
        return helgBlirFredag(dato);
    }

    public static LocalDate helgBlirMandag(LocalDate dato) {
        return WEEKEND.contains(dato.getDayOfWeek()) ? dato.with(next(DayOfWeek.MONDAY)) : dato;
    }

    public static LocalDate helgBlirFredag(LocalDate dato) {
        return WEEKEND.contains(dato.getDayOfWeek()) ? dato.with(DayOfWeek.FRIDAY) : dato;
    }

    public static LocalDate fredagLørdagBlirSøndag(LocalDate dato) {
        return WEEKEND.contains(dato.plusDays(1).getDayOfWeek()) ? dato.with(next(DayOfWeek.MONDAY)) : dato;
    }

}
