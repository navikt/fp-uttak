package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmAdopsjonsvilkåretErOppfylt extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 16";

    public SjekkOmAdopsjonsvilkåretErOppfylt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getInngangsvilkår().erAdopsjonOppfylt() ? ja() : nei();
    }
}
