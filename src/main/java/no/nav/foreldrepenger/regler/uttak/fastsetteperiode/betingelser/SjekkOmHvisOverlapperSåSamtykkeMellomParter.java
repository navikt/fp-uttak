package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID)
public class SjekkOmHvisOverlapperSåSamtykkeMellomParter extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.2";

    public SjekkOmHvisOverlapperSåSamtykkeMellomParter() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(app -> true).isEmpty() || grunnlag.isSamtykke() ? ja() : nei();
    }
}
