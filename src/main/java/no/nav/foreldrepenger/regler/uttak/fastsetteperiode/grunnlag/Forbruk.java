package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class Forbruk {
    private Map<AktivitetIdentifikator, ForbrukForAktivitet> saldoer;

    private Forbruk(Map<AktivitetIdentifikator, ForbrukForAktivitet> saldoer) {
        this.saldoer = saldoer;
    }

    static Forbruk zero(List<AktivitetIdentifikator> aktivitetIdentifikatorer) {
        Map<AktivitetIdentifikator, ForbrukForAktivitet> saldoer = new HashMap<>();
        for (AktivitetIdentifikator aktivitet : aktivitetIdentifikatorer) {
            saldoer.put(aktivitet, ForbrukForAktivitet.zero());
        }
        return new Forbruk(saldoer);
    }

    void registrerForbruk(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype konto, Trekkdager forbruk) {
        ForbrukForAktivitet aktuelleSaldoer = this.saldoer.get(aktivitetIdentifikator);
        aktuelleSaldoer.registrerForbruk(konto, forbruk);
    }

    Trekkdager getForbruk(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype konto) {
        ForbrukForAktivitet aktuelleSaldoer = this.saldoer.get(aktivitetIdentifikator);
        return aktuelleSaldoer.getForbruk(konto);
    }

    Trekkdager getMinsteForbruk(Stønadskontotype stønadskontotype) {
        return saldoer.values().stream()
                .map(s -> s.getForbruk(stønadskontotype))
                .min(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }
}
