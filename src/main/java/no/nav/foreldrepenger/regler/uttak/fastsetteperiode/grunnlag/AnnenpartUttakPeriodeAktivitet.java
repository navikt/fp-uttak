package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class AnnenpartUttakPeriodeAktivitet {
    private Stønadskontotype stønadskontotype;
    private AktivitetIdentifikator aktivitetIdentifikator;
    private Trekkdager trekkdager;
    private BigDecimal utbetalingsgrad;

    public AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator aktivitetIdentifikator,
                                          Stønadskontotype stønadskontotype,
                                          Trekkdager trekkdager,
                                          BigDecimal utbetalingsgrad) {
        Objects.requireNonNull(aktivitetIdentifikator);
        Objects.requireNonNull(utbetalingsgrad);
        this.aktivitetIdentifikator = aktivitetIdentifikator;
        this.stønadskontotype = stønadskontotype;
        this.trekkdager = trekkdager;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public AktivitetIdentifikator getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }

    public Trekkdager getTrekkdager() {
        return trekkdager;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

}
