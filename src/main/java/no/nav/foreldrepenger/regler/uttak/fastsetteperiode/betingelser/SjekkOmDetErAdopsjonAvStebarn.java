package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDetErAdopsjonAvStebarn.ID)
public class SjekkOmDetErAdopsjonAvStebarn extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 16.1.1";

    public SjekkOmDetErAdopsjonAvStebarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var adopsjon = fastsettePeriodeGrunnlag.getAdopsjon();
        if (adopsjon != null) {
            return adopsjon.erStebarnsadopsjon() ? ja() : nei();
        }
        return nei();
    }
}
