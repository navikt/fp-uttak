package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmOverlapperMedAnnenPartEøs.ID)
public class SjekkOmOverlapperMedAnnenPartEøs extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "TODO";
    public static final String BESKRIVELSE = "Overlapper perioden med annen parts uttak i EØS?";

    public SjekkOmOverlapperMedAnnenPartEøs() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.annenPartEøs() && grunnlag.getAnnenPartUttaksperioder()
            .stream()
            .anyMatch(eøsPeriode -> grunnlag.getAktuellPeriode().erOmsluttetAv(eøsPeriode)) ? ja() : nei();
    }
}
