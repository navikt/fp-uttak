package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmManglendeSøktPeriode.ID)
public class SjekkOmManglendeSøktPeriode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.2.1";

    public SjekkOmManglendeSøktPeriode() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        return aktuellPeriode.isManglendeSøktPeriode() ? ja() : nei();
    }
}
