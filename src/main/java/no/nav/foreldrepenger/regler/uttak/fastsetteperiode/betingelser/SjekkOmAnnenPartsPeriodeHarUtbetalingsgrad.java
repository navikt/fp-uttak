package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.SamtidigUttakUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID)
public class SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.5";

    public SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return SamtidigUttakUtil.annenpartHarSamtidigPeriodeMedUtbetaling(grunnlag) ? ja() : nei();
    }
}
