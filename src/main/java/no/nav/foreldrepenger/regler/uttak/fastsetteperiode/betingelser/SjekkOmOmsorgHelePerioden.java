package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmOmsorgHelePerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.7";
    public static final String BESKRIVELSE = "Har søker omsorg for barnet?";

    public SjekkOmOmsorgHelePerioden() {
        super(SjekkOmOmsorgHelePerioden.class.getSimpleName());
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.harOmsorg() ? ja() : nei();
    }
}
