package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenErEtterNesteStønadsperiode.ID)
public class SjekkOmPeriodenErEtterNesteStønadsperiode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 15.7";
    public static final String BESKRIVELSE = "Er uttaksperioden etter start av neste stønadsperiode??";

    public SjekkOmPeriodenErEtterNesteStønadsperiode() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.erAktuellPeriodeEtterStartNesteStønadsperiode() ? ja() : nei();
    }
}
