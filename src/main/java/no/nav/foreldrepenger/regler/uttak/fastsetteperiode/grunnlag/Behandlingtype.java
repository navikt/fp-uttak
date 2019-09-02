package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Behandlingtype {
    FØRSTEGANGSSØKNAD, REVURDERING, REVURDERING_BERØRT_SAK;

    public static final List<Behandlingtype> REVURDERING_TYPER = Collections.unmodifiableList(
            Arrays.asList(REVURDERING, REVURDERING_BERØRT_SAK)
    );
}
