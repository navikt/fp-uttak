package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmGradertPeriode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 26.1.6";
    public static final String BESKRIVELSE = "Er det gradering i perioden?";

    public SjekkOmGradertPeriode() {
        super(SjekkOmGradertPeriode.ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        if (uttakPeriode.isGradering()) {
            return ja();
        }
        return nei();
    }
}
