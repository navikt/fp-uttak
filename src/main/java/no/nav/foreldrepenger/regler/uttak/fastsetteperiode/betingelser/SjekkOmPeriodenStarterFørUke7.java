package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenStarterFørUke7.ID)
public class SjekkOmPeriodenStarterFørUke7 extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.2";

    public SjekkOmPeriodenStarterFørUke7() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var ukerReservertForMor = Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
                grunnlag.getFamiliehendelse());
        if (grunnlag.getAktuellPeriode().getFom().isBefore(grunnlag.getFamiliehendelse().plusWeeks(ukerReservertForMor))) {
            return ja();
        }
        return nei();
    }
}
