package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;

public class FastsattUttakPeriodeAktivitet {
    private final Trekkdager trekkdager;
    private final Stønadskontotype stønadskontotype;
    private final AktivitetIdentifikator aktivitetIdentifikator;

    public FastsattUttakPeriodeAktivitet(
            Trekkdager trekkdager,
            Stønadskontotype stønadskontotype,
            AktivitetIdentifikator aktivitetIdentifikator) {
        this.trekkdager = trekkdager;
        this.stønadskontotype = stønadskontotype;
        this.aktivitetIdentifikator = aktivitetIdentifikator;
    }

    public Trekkdager getTrekkdager() {
        return trekkdager;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public AktivitetIdentifikator getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }
}
