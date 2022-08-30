package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.SamtidigUttakUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTapendePeriode.ID)
public class SjekkOmTapendePeriode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.7";
    public static final String BESKRIVELSE = "Har annen part senere søkt om uttak/utsettelse i samme tidsperiode?";

    public SjekkOmTapendePeriode() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return harAnnenpartSenereMottattDato(grunnlag) ? ja() : nei();
    }

    private boolean harAnnenpartSenereMottattDato(FastsettePeriodeGrunnlag grunnlag) {
        return SamtidigUttakUtil.erTapendePeriodeRegel(grunnlag);
    }

}
