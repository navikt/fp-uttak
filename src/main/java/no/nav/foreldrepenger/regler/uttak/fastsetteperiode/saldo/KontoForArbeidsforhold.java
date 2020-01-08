package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;

class KontoForArbeidsforhold {
    private final AktivitetIdentifikator aktivitetIdentifikator;
    private final Set<Stønadskonto> stønadskontoer;

    KontoForArbeidsforhold(AktivitetIdentifikator aktivitetIdentifikator, Set<Stønadskonto> stønadskontoer) {
        this.aktivitetIdentifikator = aktivitetIdentifikator;
        this.stønadskontoer = stønadskontoer;
    }

    AktivitetIdentifikator getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }

    Set<Stønadskonto> getStønadskontoer() {
        return stønadskontoer;
    }

    @Override
    public String toString() {
        return "KontoForArbeidsforhold{" +
                "aktivitetIdentifikator=" + aktivitetIdentifikator +
                ", stønadskontoer=" + stønadskontoer +
                '}';
    }
}
