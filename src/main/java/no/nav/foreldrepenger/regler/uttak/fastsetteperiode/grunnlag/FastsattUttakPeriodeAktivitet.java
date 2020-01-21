package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsattUttakPeriodeAktivitet {
    private final Trekkdager trekkdager;
    private final Stønadskontotype trekkonto;
    private final AktivitetIdentifikator aktivitetIdentifikator;

    public FastsattUttakPeriodeAktivitet(Trekkdager trekkdager, Stønadskontotype trekkonto, AktivitetIdentifikator aktivitetIdentifikator) {
        this.trekkdager = trekkdager;
        this.trekkonto = trekkonto;
        this.aktivitetIdentifikator = aktivitetIdentifikator;
    }

    public Trekkdager getTrekkdager() {
        return trekkdager;
    }

    public Stønadskontotype getTrekkonto() {
        return trekkonto;
    }

    public AktivitetIdentifikator getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }
}
