package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmOverføringPgaAleneomsorg.ID)
public class SjekkOmOverføringPgaAleneomsorg extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9";

    public SjekkOmOverføringPgaAleneomsorg() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        if (OverføringÅrsak.ALENEOMSORG.equals(uttakPeriode.getOverføringÅrsak())) {
            return ja();
        }
        return nei();
    }
}
