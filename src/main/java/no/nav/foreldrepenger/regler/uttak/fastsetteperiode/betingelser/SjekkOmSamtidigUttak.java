package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSamtidigUttak.ID)
public class SjekkOmSamtidigUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.6";

    public SjekkOmSamtidigUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        if (oppgittPeriode.erSøktSamtidigUttak() || harAnnenForelderHuketAvForSamtidigUttak(grunnlag)) {
            return ja();
        }
        return nei();
    }

    private boolean harAnnenForelderHuketAvForSamtidigUttak(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(AnnenpartUttakPeriode::isSamtidigUttak).isPresent();
    }
}
