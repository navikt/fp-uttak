package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSykdomSkade.ID)
public class SjekkOmSykdomSkade extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.2";

    public SjekkOmSykdomSkade() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        for (var periodeMedSykdomEllerSkade : grunnlag.getPerioderMedSykdomEllerSkade()) {
            if (oppgittPeriode.erOmsluttetAv(periodeMedSykdomEllerSkade)) {
                return ja();
            }
        }
        return nei();
    }
}
