package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorBekreftetUføre.ID)
public class SjekkOmMorBekreftetUføre extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_11";
    public static final String BESKRIVELSE = "Har søker oppgitt at mor mottar uføretrygd ?";

    public SjekkOmMorBekreftetUføre() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return fastsettePeriodeGrunnlag.isBareFarHarRettMorUføretrygd() ? ja() : nei();
    }
}
