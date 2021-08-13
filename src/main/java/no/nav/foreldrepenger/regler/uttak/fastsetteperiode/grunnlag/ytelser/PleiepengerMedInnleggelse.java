package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.util.Collection;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;

public record PleiepengerMedInnleggelse(Collection<PeriodeMedBarnInnlagt> perioder) {
}
