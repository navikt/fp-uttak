package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareMorHarRett.ID)
public class SjekkOmBareMorHarRett extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 36.2.3";
    public static final String BESKRIVELSE = "Er det bare mor som har rett?";

    public SjekkOmBareMorHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isSøkerMor() && grunnlag.rettighetsType().bareSøkerRett()) {
            return ja();
        }
        return nei();
    }
}
