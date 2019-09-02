package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.util.Objects;

public class Arbeid {

    private static final String ARBEIDSPROSENT_NON_NULL_MESSAGE = "arbeidsprosent";
    private final BigDecimal arbeidsprosent;
    private final BigDecimal stillingsprosent;
    private final boolean gradert;

    Arbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent, boolean gradert) {
        this.arbeidsprosent = arbeidsprosent;
        this.stillingsprosent = stillingsprosent;
        this.gradert = gradert;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public boolean isGradert() {
        return gradert;
    }

    public static Arbeid forFrilans(BigDecimal arbeidsprosent) {
        Objects.requireNonNull(arbeidsprosent, ARBEIDSPROSENT_NON_NULL_MESSAGE);
        boolean gradert = erGradert(arbeidsprosent);
        return new Arbeid(arbeidsprosent, null, gradert);
    }

    public static Arbeid forSelvstendigNæringsdrivende(BigDecimal arbeidsprosent) {
        Objects.requireNonNull(arbeidsprosent, ARBEIDSPROSENT_NON_NULL_MESSAGE);
        boolean gradert = erGradert(arbeidsprosent);
        return new Arbeid(arbeidsprosent,null, gradert);
    }

    public static Arbeid forOrdinærtArbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent) {
        return forOrdinærtArbeid(arbeidsprosent, stillingsprosent, false);
    }

    public static Arbeid forGradertOrdinærtArbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent) {
        return forOrdinærtArbeid(arbeidsprosent, stillingsprosent, true);
    }

    private static Arbeid forOrdinærtArbeid(BigDecimal arbeidsprosent, BigDecimal stillingsprosent, boolean gradert) {
        Objects.requireNonNull(stillingsprosent, "stillingsprosent");
        Objects.requireNonNull(arbeidsprosent, ARBEIDSPROSENT_NON_NULL_MESSAGE);
        return new Arbeid(arbeidsprosent, stillingsprosent, gradert);
    }

    public static Arbeid forAnnet() {
        return new Arbeid(BigDecimal.ZERO, null, false);
    }

    private static boolean erGradert(BigDecimal arbeidsprosent) {
        return arbeidsprosent.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbeid arbeid = (Arbeid) o;
        return gradert == arbeid.gradert &&
                Objects.equals(arbeidsprosent, arbeid.arbeidsprosent) &&
                Objects.equals(stillingsprosent, arbeid.stillingsprosent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(arbeidsprosent, stillingsprosent, gradert);
    }
}
