package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse.ID)
public class SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.4";

    public SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(app -> app.isUtsettelse() && app.isInnvilget())
            .isPresent() ? ja() : nei();
    }
}
