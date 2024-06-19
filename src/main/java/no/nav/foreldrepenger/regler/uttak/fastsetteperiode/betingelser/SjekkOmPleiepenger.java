package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPleiepenger.ID)
public class SjekkOmPleiepenger extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK PP1";
    public static final String BESKRIVELSE = "Er bruker innvilget pleiepenger i perioden?";

    public SjekkOmPleiepenger() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var periode = grunnlag.getAktuellPeriode();
        var perioderMedPleiepenger = grunnlag.perioderMedPleiepenger();
        // Regner med at søknadsperiode er knekt etter perioder med pleiepenger
        return perioderMedPleiepenger.stream().anyMatch(pp -> pp.overlapper(periode))
                ? ja()
                : nei();
    }
}
