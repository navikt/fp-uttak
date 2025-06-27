package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;

public record AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype, Trekkdager trekkdager,
                                             Utbetalingsgrad utbetalingsgrad) {
    public AnnenpartUttakPeriodeAktivitet {
        Objects.requireNonNull(aktivitetIdentifikator);
        Objects.requireNonNull(utbetalingsgrad);
    }

}
