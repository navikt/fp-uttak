package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
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
            return grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(app -> app.erOmsluttetAv(farRundtFødselPeriode.get()))
                .isPresent() ? ja() : nei();
        }
        return nei();

    }
}
