package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public record Pleiepenger(Collection<PleiepengerPeriode> perioder) {

    public Pleiepenger(LocalDate fom, LocalDate tom, boolean barnInnlagt) {
        this(List.of(new PleiepengerPeriode(fom, tom, barnInnlagt)));
    }

    public List<PleiepengerPeriode> innleggelser() {
        return perioder.stream().filter(PleiepengerPeriode::isBarnInnlagt).toList();
    }
}
