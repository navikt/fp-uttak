package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenErEtterNyStønadsperiode.ID)
public class SjekkOmPeriodenErEtterNyStønadsperiode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 15.7";

    public SjekkOmPeriodenErEtterNyStønadsperiode() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        var grense = grunnlag.getStartNyStønadsperiode().orElse(null);
        if (grense != null && (oppgittPeriode.getFom().isAfter(grense) || oppgittPeriode.getFom().equals(grense))) {
            return ja();
        }
        return nei();
    }
}
