package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodeErForeldrepengerFørFødsel.ID)
public class SjekkOmPeriodeErForeldrepengerFørFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27";

    public SjekkOmPeriodeErForeldrepengerFørFødsel() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var stønadskontotype = grunnlag.getAktuellPeriode().getStønadskontotype();
        if (stønadskontotype == Stønadskontotype.FORELDREPENGER_FØR_FØDSEL) {
            return ja();
        }
        return nei();
    }
}
