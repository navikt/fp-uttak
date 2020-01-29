package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUtsettelsePgaSøkerInnleggelse.ID)
public class SjekkOmUtsettelsePgaSøkerInnleggelse extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.3";

    public SjekkOmUtsettelsePgaSøkerInnleggelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        return oppgittPeriode.isUtsettelsePga(UtsettelseÅrsak.INNLAGT_SØKER) ? ja() : nei();
    }
}
