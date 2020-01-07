package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID)
public class SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.5";

    public SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        for (AnnenpartUttaksperiode periodeAnnenPart : grunnlag.getAnnenPartUttaksperioder()) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(uttakPeriode, periodeAnnenPart)) {
                if(finnesDetEnAktivitetMedUtbetalingsgradHøyereEnnNull(periodeAnnenPart)) {
                    return ja();
                }
            }
        }
        return nei();
    }

    private boolean finnesDetEnAktivitetMedUtbetalingsgradHøyereEnnNull(AnnenpartUttaksperiode periodeAnnenPart) {
        for (UttakPeriodeAktivitet periodeAktivitet : periodeAnnenPart.getAktiviteter()) {
            if (periodeAktivitet.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
        }
        return false;
    }
}
