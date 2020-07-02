package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Utbetalingsgrad implements Comparable<Utbetalingsgrad> {

    public static final Utbetalingsgrad ZERO = new Utbetalingsgrad(0);
    public static final Utbetalingsgrad TEN = new Utbetalingsgrad(10);
    public static final Utbetalingsgrad HUNDRED = new Utbetalingsgrad(100);
    public static final Utbetalingsgrad FULL = HUNDRED;

    private BigDecimal verdi;

    public Utbetalingsgrad(BigDecimal verdi) {
        this.verdi = scale(verdi);
    }

    public Utbetalingsgrad(double verdi) {
        this(BigDecimal.valueOf(verdi));
    }

    public Utbetalingsgrad(int verdi) {
        this(BigDecimal.valueOf(verdi));
    }

    public BigDecimal decimalValue() {
        return scale(verdi);
    }

    private BigDecimal scale(BigDecimal verdi) {
        return verdi.setScale(2, RoundingMode.UP);
    }

    public Utbetalingsgrad subtract(Utbetalingsgrad utbetalingsgrad) {
        return subtract(utbetalingsgrad.decimalValue());
    }

    public Utbetalingsgrad subtract(BigDecimal verdi) {
        return new Utbetalingsgrad(decimalValue().subtract(verdi));
    }

    @Override
    public String toString() {
        return verdi.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utbetalingsgrad that = (Utbetalingsgrad) o;
        return Objects.equals(decimalValue(), that.decimalValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(decimalValue());
    }

    @Override
    public int compareTo(Utbetalingsgrad utbetalingsgrad) {
        return decimalValue().compareTo(utbetalingsgrad.decimalValue());
    }
}
