package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmErGradertFørSøknadMottattdato.ID)
public class SjekkOmErGradertFørSøknadMottattdato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 26.1.15";

    public SjekkOmErGradertFørSøknadMottattdato() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode periode = grunnlag.getAktuellPeriode();
        if (periode.isGradering() && førEllerLik(grunnlag, periode)) {
            return ja();
        }
        return nei();
    }

    private boolean førEllerLik(FastsettePeriodeGrunnlag grunnlag, UttakPeriode periode) {
        return !periode.getTom().isAfter(grunnlag.getSøknadMottattdato());
    }
}
