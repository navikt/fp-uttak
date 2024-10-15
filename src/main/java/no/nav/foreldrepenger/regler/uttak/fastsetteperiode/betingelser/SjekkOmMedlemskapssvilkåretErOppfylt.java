package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmMedlemskapssvilkåretErOppfylt extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 23";

    public SjekkOmMedlemskapssvilkåretErOppfylt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getInngangsvilkår().erMedlemskapOppfylt() ? ja() : nei();
    }
}
