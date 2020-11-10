package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFødselErFørUke33.ID)
public class SjekkOmFødselErFørUke33 extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK prematur1";
    public static final String BESKRIVELSE = "Er det fødsel før uke 33?";

    private final Konfigurasjon konfigurasjon;

    public SjekkOmFødselErFørUke33(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        LocalDate fødselsdato = grunnlag.getFødselsdato();
        LocalDate termindato = grunnlag.getTermindato();
        if (PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, termindato, konfigurasjon)) {
            return ja();
        }
        return nei();
    }
}
