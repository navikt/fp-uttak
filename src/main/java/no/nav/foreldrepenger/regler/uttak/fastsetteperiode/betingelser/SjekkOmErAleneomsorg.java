package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Rettighetstype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmErAleneomsorg.ID)
public class SjekkOmErAleneomsorg extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 36.2.1";
    public static final String BESKRIVELSE = "Er det aleneomsorg?";

    public SjekkOmErAleneomsorg() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.rettighetsType().equals(Rettighetstype.ALENEOMSORG) ? ja() : nei();
    }
}
