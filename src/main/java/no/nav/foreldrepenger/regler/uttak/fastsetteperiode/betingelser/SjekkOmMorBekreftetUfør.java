package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorBekreftetUfør.ID)
public class SjekkOmMorBekreftetUfør extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 36.3.1";
    public static final String BESKRIVELSE = "Gjelder perioden dager der mor bekreftet ufør?";

    public SjekkOmMorBekreftetUfør() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isBareFarHarRettMorUføretrygd()) {
            return ja();
        }
        return nei();
    }
}
