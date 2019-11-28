package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


public class EndringAvStilling {

    private final LocalDate dato;
    private final BigDecimal stillingsprosent;

    public EndringAvStilling(LocalDate dato, BigDecimal stillingsprosent) {
        this.dato = dato;
        this.stillingsprosent = stillingsprosent;
    }

    public LocalDate getDato() {
        return dato;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndringAvStilling that = (EndringAvStilling) o;
        return Objects.equals(dato, that.dato) &&
                Objects.equals(stillingsprosent, that.stillingsprosent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dato, stillingsprosent);
    }
}
