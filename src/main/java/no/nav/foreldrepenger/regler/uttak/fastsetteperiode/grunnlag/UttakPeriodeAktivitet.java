package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;

public class UttakPeriodeAktivitet {

    private final AktivitetIdentifikator identifikator;
    private final BigDecimal utbetalingsprosent;
    private final Trekkdager trekkdager;
    private final boolean søktGradering;

    public UttakPeriodeAktivitet(AktivitetIdentifikator identifikator,
                                 BigDecimal utbetalingsprosent,
                                 Trekkdager trekkdager,
                                 boolean søktGradering) {
        this.identifikator = identifikator;
        this.utbetalingsprosent = utbetalingsprosent;
        this.trekkdager = trekkdager;
        this.søktGradering = søktGradering;
    }

    public AktivitetIdentifikator getIdentifikator() {
        return identifikator;
    }

    public BigDecimal getUtbetalingsprosent() {
        return utbetalingsprosent;
    }

    public Trekkdager getTrekkdager() {
        return trekkdager;
    }

    public boolean isSøktGradering() {
        return søktGradering;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UttakPeriodeAktivitet that = (UttakPeriodeAktivitet) o;
        return søktGradering == that.søktGradering &&
                Objects.equals(identifikator, that.identifikator) &&
                Objects.equals(utbetalingsprosent, that.utbetalingsprosent) &&
                Objects.equals(trekkdager, that.trekkdager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifikator, utbetalingsprosent, trekkdager, søktGradering);
    }

    @Override
    public String toString() {
        return "UttakPeriodeAktivitet{" +
                "identifikator=" + identifikator +
                ", utbetalingsprosent=" + utbetalingsprosent +
                ", trekkdager=" + trekkdager +
                ", gradering=" + søktGradering +
                '}';
    }
}
