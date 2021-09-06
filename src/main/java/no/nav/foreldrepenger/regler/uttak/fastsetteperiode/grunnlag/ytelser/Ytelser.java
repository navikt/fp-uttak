package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.util.Optional;

public class Ytelser {

    private final Pleiepenger pleiepenger;

    public Ytelser(Pleiepenger pleiepenger) {
        this.pleiepenger = pleiepenger;
    }

    public Optional<Pleiepenger> pleiepenger() {
        return Optional.ofNullable(pleiepenger);
    }
}
