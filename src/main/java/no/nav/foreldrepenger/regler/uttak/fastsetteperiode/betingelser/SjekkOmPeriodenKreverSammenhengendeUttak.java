package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenKreverSammenhengendeUttak.ID)
public class SjekkOmPeriodenKreverSammenhengendeUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "SHU.1";
    public static final String BESKRIVELSE = "Krever perioden sammenhengende uttak?";

    public SjekkOmPeriodenKreverSammenhengendeUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAktuellPeriode().kreverSammenhengendeUttak(grunnlag.getSammenhengendeUttakTomDato()) ? ja() : nei();
    }
}



