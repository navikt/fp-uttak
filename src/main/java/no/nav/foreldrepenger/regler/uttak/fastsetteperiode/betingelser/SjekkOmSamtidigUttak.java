package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
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
        OppgittPeriode oppgittPeriode = grunnlag.getAktuellPeriode();
        if (oppgittPeriode.erSøktSamtidigUttak() || harAnnenForelderHuketAvForSamtidigUttak(oppgittPeriode, grunnlag.getAnnenPartUttaksperioder())) {
            return ja();
        }
        return nei();
    }

    private boolean harAnnenForelderHuketAvForSamtidigUttak(OppgittPeriode oppgittPeriode, List<AnnenpartUttakPeriode> perioderAnnenPart) {
        for (AnnenpartUttakPeriode periodeAnnenPart : perioderAnnenPart) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(oppgittPeriode, periodeAnnenPart) && periodeAnnenPart.isSamtidigUttak()) {
                return true;
            }
        }
        return false;
    }
}
