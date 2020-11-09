package no.nav.foreldrepenger.regler.uttak.felles.grunnlag;

import java.time.LocalDate;
import java.util.Objects;

public class Periode {

    private final LocalDate fom;
    private final LocalDate tom;

    public Periode(LocalDate fom, LocalDate tom) {
        if (fom != null && tom != null && tom.isBefore(fom)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato: " + fom + ">" + tom);
        }
        this.fom = fom == null ? LocalDate.MIN : fom;
        this.tom = tom == null ? LocalDate.MAX : tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public boolean overlapper(LocalDate dato) {
        return !(dato.isBefore(fom) || dato.isAfter(tom));
    }

    public boolean overlapper(LukketPeriode periode) {
        return overlapper(periode.getFom()) || overlapper(periode.getTom()) || erOmsluttetAv(periode);
    }

    public boolean erOmsluttetAv(LukketPeriode periode) {
        return !periode.getFom().isAfter(fom) && !periode.getTom().isBefore(tom);
    }

    public boolean erLik(LukketPeriode periode) {
        return Objects.equals(getFom(), periode.getFom()) && Objects.equals(getTom(), periode.getTom());
    }

    @Override
    public String toString() {
        return "Periode{" +
                "fom=" + fom +
                ", tom=" + tom +
                '}';
    }
}
