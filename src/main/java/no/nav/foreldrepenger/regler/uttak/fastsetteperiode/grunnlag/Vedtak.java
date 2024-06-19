package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.List;

public final class Vedtak {

    private final List<FastsattUttakPeriode> perioder = new ArrayList<>();

    private Vedtak() {}

    public List<FastsattUttakPeriode> getPerioder() {
        return perioder;
    }

    public static class Builder {
        private final Vedtak kladd = new Vedtak();

        public Vedtak.Builder leggTilPeriode(FastsattUttakPeriode periode) {
            kladd.perioder.add(periode);
            return this;
        }

        public Vedtak.Builder leggTilPeriode(FastsattUttakPeriode.Builder periode) {
            kladd.perioder.add(periode.build());
            return this;
        }

        public Vedtak build() {
            return kladd;
        }
    }
}
