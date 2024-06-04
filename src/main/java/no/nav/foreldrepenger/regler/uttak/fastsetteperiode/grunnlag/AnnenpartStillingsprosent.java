package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class AnnenpartStillingsprosent implements Comparable<AnnenpartStillingsprosent> {

    public static final AnnenpartStillingsprosent ZERO = new AnnenpartStillingsprosent(0);
    public static final AnnenpartStillingsprosent TEN = new AnnenpartStillingsprosent(10);


    private final BigDecimal verdi;

    public AnnenpartStillingsprosent(BigDecimal verdi) {
        this.verdi = scale(verdi);
    }

    public AnnenpartStillingsprosent(int verdi) {
        this(BigDecimal.valueOf(verdi));
    }

    public BigDecimal decimalValue() {
        return scale(verdi);
    }

    private BigDecimal scale(BigDecimal verdi) {
        return verdi.setScale(2, RoundingMode.DOWN);
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
        var that = (AnnenpartStillingsprosent) o;
        return Objects.equals(decimalValue(), that.decimalValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(decimalValue());
    }

    @Override
    public int compareTo(AnnenpartStillingsprosent annenpartStillingsprosent) {
        return decimalValue().compareTo(annenpartStillingsprosent.decimalValue());
    }
}
