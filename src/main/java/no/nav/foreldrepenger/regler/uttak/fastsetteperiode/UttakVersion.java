package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.fpsak.nare.evaluation.summary.EvaluationVersion;
import no.nav.fpsak.nare.evaluation.summary.NareVersion;

public class UttakVersion {

    private UttakVersion() {}

    public static final EvaluationVersion UTTAK_VERSION =
            NareVersion.readVersionPropertyFor("fp-uttak", "nare/fp-uttak-version.properties");
}
