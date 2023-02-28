package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periode;

class Parameter extends Periode {

    private final Integer verdi;

    Parameter(LocalDate fom, LocalDate tom, Integer verdi) {
        super(fom, tom);
        this.verdi = verdi;
    }

    public Integer getVerdi() {
        return verdi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Parameter))
            return false;
        if (!super.equals(o))
            return false;
        Parameter parameter = (Parameter) o;
        return Objects.equals(verdi, parameter.verdi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), verdi);
    }
}
