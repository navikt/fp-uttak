package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenGjelderFlerbarnsdager.ID)
public class SjekkOmPeriodenGjelderFlerbarnsdager
        extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.4";
    public static final String BESKRIVELSE = "Gjelder perioden flerbarnsdager?";

    public SjekkOmPeriodenGjelderFlerbarnsdager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        if (oppgittPeriode.isFlerbarnsdager()) {
            return ja();
        }
        return nei();
    }
}
