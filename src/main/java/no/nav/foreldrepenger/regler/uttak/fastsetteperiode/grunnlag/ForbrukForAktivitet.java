package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class ForbrukForAktivitet {
    private Map<Stønadskontotype, Trekkdager> saldoer = new EnumMap<>(Stønadskontotype.class);

    public static ForbrukForAktivitet zero() {
        return new ForbrukForAktivitet();
    }

    void registrerForbruk(Stønadskontotype konto, Trekkdager forbruk) {
        Trekkdager forrige = saldoer.getOrDefault(konto, Trekkdager.ZERO);
        saldoer.put(konto, forrige.add(forbruk));
    }

    Trekkdager getForbruk(Stønadskontotype konto) {
        return saldoer.getOrDefault(konto, Trekkdager.ZERO);
    }

}
