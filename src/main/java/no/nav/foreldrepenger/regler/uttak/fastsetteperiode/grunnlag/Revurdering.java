package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public final class Revurdering {

    private LocalDate endringsdato;
    private Vedtak gjeldendeVedtak;

    private Revurdering() {
    }

    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public Vedtak getGjeldendeVedtak() {
        return gjeldendeVedtak;
    }

    public static class Builder {

        private final Revurdering kladd = new Revurdering();

        public Builder endringsdato(LocalDate endringsdato) {
            this.kladd.endringsdato = endringsdato;
            return this;
        }

        public Builder gjeldendeVedtak(Vedtak.Builder vedtak) {
            kladd.gjeldendeVedtak = vedtak == null ? null : vedtak.build();
            return this;
        }

        public Revurdering build() {
            return kladd;
        }
    }
}
