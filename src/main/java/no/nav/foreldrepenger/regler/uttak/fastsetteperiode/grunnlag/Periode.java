package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

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

    public boolean overlapper(Periode periode) {
        return overlapper(periode.getFom()) || overlapper(periode.getTom()) || erOmsluttetAv(periode);
    }

    //Også true hvis perioden er lik
    public boolean erOmsluttetAv(Periode periode) {
        return !periode.getFom().isAfter(fom) && !periode.getTom().isBefore(tom);
    }

    public boolean erLik(Periode periode) {
        return Objects.equals(getFom(), periode.getFom()) && Objects.equals(getTom(), periode.getTom());
    }

    @Override
    public String toString() {
        return "Periode{" + "fom=" + fom + ", tom=" + tom + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var periode = (Periode) o;
        return Objects.equals(fom, periode.fom) && Objects.equals(tom, periode.tom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom);
    }
}
