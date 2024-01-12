package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.PerioderUtenHelgUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAnnenPartsPeriodeErFratrekkPleiepenger.ID)
public class SjekkOmAnnenPartsPeriodeErFratrekkPleiepenger extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.4.3";
    public static final String BESKRIVELSE = "Har annen part fratrekk for pleiepenger i perioden?";

    public SjekkOmAnnenPartsPeriodeErFratrekkPleiepenger() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        return grunnlag.getAnnenPartUttaksperioder().stream()
            .filter(app -> PerioderUtenHelgUtil.perioderUtenHelgOverlapper(aktuellPeriode, app))
            .anyMatch(AnnenpartUttakPeriode::isFratrekkPleiepenger) ? ja() : nei();

    }
}
