package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID)
public class SjekkOmHvisOverlapperSåSamtykkeMellomParter extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.2";

    public SjekkOmHvisOverlapperSåSamtykkeMellomParter() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        boolean ikkeOverlapperEllerOverlapperOgSamtykke = true;
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        for (AnnenpartUttaksperiode periodeAnnenPart : grunnlag.getAnnenPartUttaksperioder()) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(uttakPeriode, periodeAnnenPart)) {
                if (!grunnlag.isSamtykke()) {
                    ikkeOverlapperEllerOverlapperOgSamtykke = false;
                }
            }
        }
        return ikkeOverlapperEllerOverlapperOgSamtykke ? ja() : nei();
    }
}
