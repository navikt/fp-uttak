package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBehandlingKreverSammenhengendeUttak.ID)
public class SjekkOmBehandlingKreverSammenhengendeUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK TODO fritt uttak";
    public static final String BESKRIVELSE = "Krever behandlingen sammenhengende uttak?";

    public SjekkOmBehandlingKreverSammenhengendeUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.kreverBehandlingSammenhengendeUttak() ? ja() : nei();
    }
}



