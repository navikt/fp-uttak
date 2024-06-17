package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;

public class PleiepengerPeriode extends LukketPeriode {

    private final boolean barnInnlagt;

    public PleiepengerPeriode(LocalDate fom, LocalDate tom, boolean barnInnlagt) {
        super(fom, tom);
        this.barnInnlagt = barnInnlagt;
    }

    public boolean isBarnInnlagt() {
        return barnInnlagt;
    }

    @Override
    public String toString() {
        return "PleiepengerPeriode{" + "periode=" + super.toString() + ", barnInnlagt=" + barnInnlagt + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var that = (PleiepengerPeriode) o;
        return barnInnlagt == that.barnInnlagt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), barnInnlagt);
    }
}
