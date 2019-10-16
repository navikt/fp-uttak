package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmErUtsettelseFørSøknadMottattdato.ID)
public class SjekkOmErUtsettelseFørSøknadMottattdato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 18.1.8";

    public SjekkOmErUtsettelseFørSøknadMottattdato() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode periode = grunnlag.getAktuellPeriode();
        if (periode instanceof UtsettelsePeriode && førEllerLik(grunnlag, periode)) {
            return ja();
        }
        return nei();
    }

    private boolean førEllerLik(FastsettePeriodeGrunnlag grunnlag, UttakPeriode periode) {
        return !periode.getTom().isAfter(grunnlag.getSøknadMottattdato());
    }
}
