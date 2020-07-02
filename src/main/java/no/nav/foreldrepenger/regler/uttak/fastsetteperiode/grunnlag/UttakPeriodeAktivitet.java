package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;

public class UttakPeriodeAktivitet {

    private final AktivitetIdentifikator identifikator;
    private final Utbetalingsgrad utbetalingsgrad;
    private final Trekkdager trekkdager;
    private final boolean søktGradering;

    public UttakPeriodeAktivitet(AktivitetIdentifikator identifikator,
                                 Utbetalingsgrad utbetalingsgrad,
                                 Trekkdager trekkdager,
                                 boolean søktGradering) {
        this.identifikator = identifikator;
        this.utbetalingsgrad = utbetalingsgrad;
        this.trekkdager = trekkdager;
        this.søktGradering = søktGradering;
    }

    public AktivitetIdentifikator getIdentifikator() {
        return identifikator;
    }

    public Utbetalingsgrad getUtbetalingsgrad() {
        return utbetalingsgrad;
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
                Objects.equals(utbetalingsgrad, that.utbetalingsgrad) &&
                Objects.equals(trekkdager, that.trekkdager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifikator, utbetalingsgrad, trekkdager, søktGradering);
    }

    @Override
    public String toString() {
        return "UttakPeriodeAktivitet{" +
                "identifikator=" + identifikator +
                ", utbetalingsgrad=" + utbetalingsgrad +
                ", trekkdager=" + trekkdager +
                ", gradering=" + søktGradering +
                '}';
    }
}
