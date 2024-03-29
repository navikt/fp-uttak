package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.util.Collection;
import java.util.List;

public record Pleiepenger(Collection<PleiepengerPeriode> perioder) {

    public List<PleiepengerPeriode> innleggelser() {
        return perioder.stream().filter(p -> p.isBarnInnlagt()).toList();
    }
}
