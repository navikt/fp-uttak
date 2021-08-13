package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.util.Optional;

public class Ytelser {

    private final PleiepengerMedInnleggelse pleiepengerMedInnleggelse;

    public Ytelser(PleiepengerMedInnleggelse pleiepengerMedInnleggelse) {
        this.pleiepengerMedInnleggelse = pleiepengerMedInnleggelse;
    }

    public Optional<PleiepengerMedInnleggelse> pleiepenger() {
        return Optional.ofNullable(pleiepengerMedInnleggelse);
    }
}
