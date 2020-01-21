package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
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
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        if (uttakPeriode.isSamtidigUttak() || harAnnenForelderHuketAvForSamtidigUttak(uttakPeriode, grunnlag.getAnnenPartUttaksperioder())) {
            return ja();
        }
        return nei();
    }

    private boolean harAnnenForelderHuketAvForSamtidigUttak(UttakPeriode uttakPeriode, List<AnnenpartUttaksperiode> perioderAnnenPart) {
        for (AnnenpartUttaksperiode periodeAnnenPart : perioderAnnenPart) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(uttakPeriode, periodeAnnenPart) && periodeAnnenPart.isSamtidigUttak()) {
                return true;
            }
        }
        return false;
    }
}
