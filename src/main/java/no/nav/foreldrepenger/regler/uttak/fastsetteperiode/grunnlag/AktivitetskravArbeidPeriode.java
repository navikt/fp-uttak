package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class AktivitetskravArbeidPeriode extends LukketPeriode {
    private final BigDecimal stillingsprosent;

    public AktivitetskravArbeidPeriode(LocalDate fom, LocalDate tom, BigDecimal stillingsprosent) {
        super(fom, tom);
        this.stillingsprosent = stillingsprosent;
    }

    public AktivitetskravArbeidPeriode(LocalDate fom, LocalDate tom, int stillingsprosent) {
        this(fom, tom, new BigDecimal(stillingsprosent));
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    @Override
    public String toString() {
        return "AktivitetskravArbeidPeriode{" + "stillingsprosent=" + stillingsprosent + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AktivitetskravArbeidPeriode that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(stillingsprosent, that.stillingsprosent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stillingsprosent);
    }
}
