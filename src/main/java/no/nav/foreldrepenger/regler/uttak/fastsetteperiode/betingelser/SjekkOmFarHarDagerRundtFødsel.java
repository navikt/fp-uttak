package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFarHarDagerRundtFødsel.ID)
public class SjekkOmFarHarDagerRundtFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.9.1";
    public static final String BESKRIVELSE = "Kan far ta ut uttak rundt fødsel?";

    public SjekkOmFarHarDagerRundtFødsel() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.periodeFarRundtFødsel().isPresent() ? ja() : nei();
    }
}
