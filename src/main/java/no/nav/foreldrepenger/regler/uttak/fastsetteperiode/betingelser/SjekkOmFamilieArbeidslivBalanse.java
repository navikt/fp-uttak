package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFamilieArbeidslivBalanse.ID)
public class SjekkOmFamilieArbeidslivBalanse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.10";

    public SjekkOmFamilieArbeidslivBalanse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.isSakMedMinsterett() ? ja() : nei();
    }
}
