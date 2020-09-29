package no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDødtBarn.ID)
public class SjekkOmDødtBarn extends LeafSpecification<BeregnKontoerGrunnlag> {
    public static final String ID = "FP_VK 17.1.1.3";

    public SjekkOmDødtBarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.getDødsdato().isPresent()) {
            return ja();
        }
        return nei();
    }
}
