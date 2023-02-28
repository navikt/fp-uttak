package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.PrematurukerUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFødselErFørUke33.ID)
public class SjekkOmFødselErFørUke33 extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK prematur1";
    public static final String BESKRIVELSE = "Er det fødsel før uke 33?";

    public SjekkOmFødselErFørUke33() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var fødselsdato = grunnlag.getFødselsdato();
        var termindato = grunnlag.getTermindato();
        if (PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
            return ja();
        }
        return nei();
    }
}
