package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public class AktørId implements ArbeidsgiverIdentifikator {

    private final String value;

    public AktørId(String value) {
        this.value = Objects.requireNonNull(value, "aktørId må ha en verdi");
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var aktørId = (AktørId) o;
        return value.equals(aktørId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
