package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class SamtidigUttaksprosent implements Comparable<SamtidigUttaksprosent> {

    public static final SamtidigUttaksprosent ZERO = new SamtidigUttaksprosent(0);
    public static final SamtidigUttaksprosent TEN = new SamtidigUttaksprosent(10);
    public static final SamtidigUttaksprosent TWENTY = new SamtidigUttaksprosent(20);
    public static final SamtidigUttaksprosent FIFTY = new SamtidigUttaksprosent(50);
    public static final SamtidigUttaksprosent HUNDRED = new SamtidigUttaksprosent(100);

    private final BigDecimal verdi;

    public SamtidigUttaksprosent(BigDecimal verdi) {
        this.verdi = scale(verdi);
    }

    public SamtidigUttaksprosent(double verdi) {
        this(BigDecimal.valueOf(verdi));
    }

    public SamtidigUttaksprosent(int verdi) {
        this(BigDecimal.valueOf(verdi));
    }

    public BigDecimal decimalValue() {
        return scale(verdi);
    }

    private BigDecimal scale(BigDecimal verdi) {
        return verdi.setScale(2, RoundingMode.DOWN);
    }

    public SamtidigUttaksprosent subtract(SamtidigUttaksprosent samtidigUttaksprosent) {
        return subtract(samtidigUttaksprosent.decimalValue());
    }

    public SamtidigUttaksprosent subtract(BigDecimal verdi) {
        return new SamtidigUttaksprosent(decimalValue().subtract(verdi));
    }

    public SamtidigUttaksprosent add(SamtidigUttaksprosent samtidigUttaksprosent) {
        return new SamtidigUttaksprosent(decimalValue().add(samtidigUttaksprosent.decimalValue()));
    }

    public boolean merEnn100() {
        return this.compareTo(SamtidigUttaksprosent.HUNDRED) > 0;
    }

    public boolean merEnn50() {
        return this.compareTo(SamtidigUttaksprosent.FIFTY) > 0;
    }

    public boolean merEnn0() {
        return this.compareTo(SamtidigUttaksprosent.ZERO) > 0;
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
        var that = (SamtidigUttaksprosent) o;
        return Objects.equals(decimalValue(), that.decimalValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(decimalValue());
    }

    @Override
    public int compareTo(SamtidigUttaksprosent samtidigUttaksprosent) {
        return decimalValue().compareTo(samtidigUttaksprosent.decimalValue());
    }
}
