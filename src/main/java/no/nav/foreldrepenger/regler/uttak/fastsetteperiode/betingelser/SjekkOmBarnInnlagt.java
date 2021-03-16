package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBarnInnlagt.ID)
public class SjekkOmBarnInnlagt extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.4.1";

    public SjekkOmBarnInnlagt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        for (var periodeMedBarnInnlagt : grunnlag.getPerioderMedBarnInnlagt()) {
            if (oppgittPeriode.erOmsluttetAv(periodeMedBarnInnlagt)) {
                return ja();
            }
        }
        return nei();
    }
}
