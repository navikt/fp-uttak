package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmEnPartsPeriodeErFlerbarnsdager.ID)
public class SjekkOmEnPartsPeriodeErFlerbarnsdager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.8";

    public SjekkOmEnPartsPeriodeErFlerbarnsdager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        if (oppgittPeriode.isFlerbarnsdager()) {
            return ja();
        }
        for (var periodeAnnenPart : grunnlag.getAnnenPartUttaksperioder()) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(oppgittPeriode, periodeAnnenPart)
                    && periodeAnnenPart.isFlerbarnsdager()) {
                return ja();
            }
        }
        return nei();
    }
}
