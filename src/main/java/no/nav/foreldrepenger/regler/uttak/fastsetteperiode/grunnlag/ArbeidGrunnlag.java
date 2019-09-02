package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.List;

public final class ArbeidGrunnlag {

    private Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();

    private ArbeidGrunnlag() {
    }

    public Arbeidsprosenter getArbeidsprosenter() {
        return arbeidsprosenter;
    }

    public List<AktivitetIdentifikator> getAktiviteter() {
        return getArbeidsprosenter().getAktiviteter();
    }

    public static class Builder {

        private final ArbeidGrunnlag kladd = new ArbeidGrunnlag();

        public Builder medArbeidsprosenter(Arbeidsprosenter arbeidsprosenter) {
            kladd.arbeidsprosenter = arbeidsprosenter;
            return this;
        }

        public ArbeidGrunnlag build() {
            return kladd;
        }
    }
}
