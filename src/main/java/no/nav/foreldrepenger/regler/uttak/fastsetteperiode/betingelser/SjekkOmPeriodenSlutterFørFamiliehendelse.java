package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenSlutterFørFamiliehendelse.ID)
public class SjekkOmPeriodenSlutterFørFamiliehendelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 11.5";

    public SjekkOmPeriodenSlutterFørFamiliehendelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var familiehendelse = grunnlag.getFamiliehendelse();
        if (aktuellPeriode.getTom().isBefore(familiehendelse)) {
            return ja();
        }
        return nei();
    }


}
