package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAnnenPartHarPleiepengerFratrekk.ID)
public class SjekkOmAnnenPartHarPleiepengerFratrekk extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK PP2";
    public static final String BESKRIVELSE = "Har annen part allerede fått fratrekk pleiepenger i overlappende periode?";

    public SjekkOmAnnenPartHarPleiepengerFratrekk() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var periode = grunnlag.getAktuellPeriode();
        var annenPartHarFratrekk = grunnlag.getAnnenPartUttaksperioder().stream()
            .filter(app -> app.overlapper(periode))
            .anyMatch(app -> !app.isInnvilget() && app.harTrekkdager());
        return annenPartHarFratrekk ? ja() : nei();
    }
}
