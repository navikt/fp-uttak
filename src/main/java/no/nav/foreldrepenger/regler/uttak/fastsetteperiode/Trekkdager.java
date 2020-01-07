package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Trekkdager {
    public static final Trekkdager ZERO = new Trekkdager(0);

    private final BigDecimal value;

    public Trekkdager(BigDecimal value) {
        this.value = value.setScale(1, RoundingMode.DOWN);
    }

    public Trekkdager(int value) {
        this(BigDecimal.valueOf(value));
    }

    public BigDecimal decimalValue() {
        return value;
    }

    public Trekkdager add(Trekkdager trekkdager) {
        return new Trekkdager(decimalValue().add(trekkdager.decimalValue()));
    }

    public Trekkdager subtract(Trekkdager trekkdager) {
        return new Trekkdager(decimalValue().subtract(trekkdager.decimalValue()));
    }

    public boolean mindreEnn0() {
        return compareTo(ZERO) < 0;
    }

    public boolean merEnn0() {
        return compareTo(ZERO) > 0;
    }

    public int compareTo(Trekkdager trekkdager) {
        return decimalValue().compareTo(trekkdager.decimalValue());
    }

    public int rundOpp() {
        return decimalValue().setScale(0, RoundingMode.UP).intValue();
    }

    public boolean gårAkkuratOppIHeleVirkedager(BigDecimal uttaksprosent) {
        return decimalValue().remainder(uttaksprosent).setScale(0, RoundingMode.UP).compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trekkdager that = (Trekkdager) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Trekkdager{" +
                "value=" + value +
                '}';
    }
}
