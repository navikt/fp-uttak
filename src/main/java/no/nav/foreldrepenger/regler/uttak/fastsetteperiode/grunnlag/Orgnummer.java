package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public class Orgnummer implements ArbeidsgiverIdentifikator {

    private final String value;

    public Orgnummer(String value) {
        this.value = Objects.requireNonNull(value, "orgnr må ha en verdi");
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value.replaceAll("^\\d{5}", "*****");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var orgnummer = (Orgnummer) o;
        return value.equals(orgnummer.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
