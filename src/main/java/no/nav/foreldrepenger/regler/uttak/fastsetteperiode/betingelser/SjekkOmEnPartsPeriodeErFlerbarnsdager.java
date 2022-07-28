package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmEnPartsPeriodeErFlerbarnsdager.ID)
public class SjekkOmEnPartsPeriodeErFlerbarnsdager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.8";

    public SjekkOmEnPartsPeriodeErFlerbarnsdager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        if (oppgittPeriode.isFlerbarnsdager()) {
            return ja();
        }
        return grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(AnnenpartUttakPeriode::isFlerbarnsdager)
            .isPresent() ? ja() : nei();
    }
}
