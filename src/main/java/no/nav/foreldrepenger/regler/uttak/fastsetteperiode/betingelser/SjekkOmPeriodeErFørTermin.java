package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodeErFørTermin.ID)
public class SjekkOmPeriodeErFørTermin extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK prematur2";
    public static final String BESKRIVELSE = "Er perioden før termin?";

    public SjekkOmPeriodeErFørTermin() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var fom = grunnlag.getAktuellPeriode().getFom();
        var termindato = grunnlag.getTermindato();
        if (termindato == null) {
            return nei();
        }
        return fom.isBefore(termindato) ? ja() : nei();
    }
}
