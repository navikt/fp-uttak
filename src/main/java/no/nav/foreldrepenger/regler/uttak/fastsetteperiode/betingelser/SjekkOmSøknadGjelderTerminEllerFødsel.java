package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSøknadGjelderTerminEllerFødsel.ID)
public class SjekkOmSøknadGjelderTerminEllerFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.1";
    public static final String BESKRIVELSE = "Er søknaden en terminsøknad/fødselsøknad?";

    public SjekkOmSøknadGjelderTerminEllerFødsel() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getSøknadstype().gjelderTerminFødsel() ? ja() : nei();
    }
}
