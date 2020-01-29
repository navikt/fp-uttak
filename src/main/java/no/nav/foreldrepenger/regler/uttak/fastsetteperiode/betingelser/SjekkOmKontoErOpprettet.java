package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSamtidigUttak.ID)
public class SjekkOmKontoErOpprettet extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 17.1.1";

    public SjekkOmKontoErOpprettet() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        if (grunnlag.getGyldigeStønadskontotyper().contains(oppgittPeriode.getStønadskontotype())) {
            return ja();
        }
        return nei();
    }
}
