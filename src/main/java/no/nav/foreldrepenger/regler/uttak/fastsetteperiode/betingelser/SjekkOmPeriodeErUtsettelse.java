package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodeErUtsettelse.ID)
public class SjekkOmPeriodeErUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18";
    public static final String BESKRIVELSE = "Er det utsettelse?";

    public SjekkOmPeriodeErUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode oppgittPeriode = grunnlag.getAktuellPeriode();
        return oppgittPeriode.isUtsettelse() ? ja() : nei();
    }

}



