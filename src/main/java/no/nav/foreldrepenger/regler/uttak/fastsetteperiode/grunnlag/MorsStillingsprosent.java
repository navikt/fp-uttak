package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class MorsStillingsprosent implements Comparable<MorsStillingsprosent> {

    public static final MorsStillingsprosent ZERO = new MorsStillingsprosent(0);
    public static final MorsStillingsprosent TEN = new MorsStillingsprosent(10);


    private final BigDecimal verdi;

    public MorsStillingsprosent(BigDecimal verdi) {
        this.verdi = scale(verdi);
    }

    public MorsStillingsprosent(int verdi) {
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
