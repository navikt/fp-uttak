package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmFødselsvilkåretErOppfylt extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 1/FP_VK 11";

    public SjekkOmFødselsvilkåretErOppfylt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getInngangsvilkår().erFødselsvilkåretOppfylt() ? ja() : nei();
    }
}
