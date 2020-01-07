package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAlleBarnErDøde.ID)
public class SjekkOmAlleBarnErDøde extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 17.1.1.2";

    public SjekkOmAlleBarnErDøde() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return fastsettePeriodeGrunnlag.erAlleBarnDøde() ? ja() : nei();
    }
}
