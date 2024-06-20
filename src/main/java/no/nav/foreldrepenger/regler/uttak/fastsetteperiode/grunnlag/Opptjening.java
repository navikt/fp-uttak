package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.Objects;

public final class Opptjening {

    private LocalDate skjæringstidspunkt;

    private Opptjening() {}

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public static class Builder {
        private final Opptjening kladd = new Opptjening();

        public Builder skjæringstidspunkt(LocalDate skjæringstidspunkt) {
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Opptjening build() {
            Objects.requireNonNull(kladd.skjæringstidspunkt, "skjæringstidspunkt");
            return kladd;
        }
    }
}
