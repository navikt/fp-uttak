package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class MorsStillingsprosent implements Comparable<MorsStillingsprosent> {

    private final BigDecimal verdi;

    public MorsStillingsprosent(BigDecimal verdi) {
        Objects.requireNonNull(verdi, "Morsstillingssprosent må ha verdi hvis satt!");
        this.verdi = scale(verdi);
        if (this.verdi.compareTo(BigDecimal.valueOf(75)) >= 0 || this.verdi.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Mors stillingsprosent har en ugyldig verdi: " + this.verdi);
        }
    }

    public MorsStillingsprosent(int verdi) {
        this(BigDecimal.valueOf(verdi));
    }

    public BigDecimal decimalValue() {
        return scale(verdi);
    }

    private BigDecimal scale(BigDecimal verdi) {
        return verdi.setScale(2, RoundingMode.UP);
    }

    @Override
    public String toString() {
        return verdi.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (MorsStillingsprosent) o;
        return Objects.equals(decimalValue(), that.decimalValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(decimalValue());
    }

    @Override
    public int compareTo(MorsStillingsprosent morsStillingsprosent) {
        return decimalValue().compareTo(morsStillingsprosent.decimalValue());
    }
}
