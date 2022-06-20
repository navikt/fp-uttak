package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmEnePeriodenErFarsUttakRundtFødsel.ID)
public class SjekkOmEnePeriodenErFarsUttakRundtFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.10";

    public SjekkOmEnePeriodenErFarsUttakRundtFødsel() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var farRundtFødselPeriode = grunnlag.periodeFarRundtFødsel();
        // Far og periode innenfor intervall rundt fødsel
        if (!grunnlag.isSøkerMor() && farRundtFødselPeriode.filter(aktuellPeriode::erOmsluttetAv).isPresent()) {
            return ja();
        }

        // Mor og annanpart har periode innenfor intervall rundt fødsel
        if (grunnlag.isSøkerMor() && farRundtFødselPeriode.isPresent()) {
            for (var periodeAnnenPart : grunnlag.getAnnenPartUttaksperioder()) {
                if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(aktuellPeriode, periodeAnnenPart)
                        && periodeAnnenPart.erOmsluttetAv(farRundtFødselPeriode.get())) {
                    return ja();
                }
            }
        }
        return nei();

    }
}
