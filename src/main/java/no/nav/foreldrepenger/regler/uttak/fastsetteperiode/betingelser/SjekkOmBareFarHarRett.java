package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareFarHarRett.ID)
public class SjekkOmBareFarHarRett extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.2";
    public static final String BESKRIVELSE = "Er det bare far som har rett?";


    public SjekkOmBareFarHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isFarRett() && !grunnlag.isMorRett()) {
            return ja();
        }
        return nei();
    }
}
