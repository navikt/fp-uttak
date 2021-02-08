package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class AnnenpartUttakPeriodeAktivitet {
    private final Stønadskontotype stønadskontotype;
    private final AktivitetIdentifikator aktivitetIdentifikator;
    private final Trekkdager trekkdager;
    private final Utbetalingsgrad utbetalingsgrad;

    public AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator aktivitetIdentifikator,
                                          Stønadskontotype stønadskontotype,
                                          Trekkdager trekkdager,
                                          Utbetalingsgrad utbetalingsgrad) {
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

    public Utbetalingsgrad getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

}
