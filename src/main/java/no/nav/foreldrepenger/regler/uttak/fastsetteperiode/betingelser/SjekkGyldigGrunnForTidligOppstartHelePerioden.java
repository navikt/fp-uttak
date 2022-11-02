package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID)
public class SjekkGyldigGrunnForTidligOppstartHelePerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.5.1";

    public SjekkGyldigGrunnForTidligOppstartHelePerioden() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        return aktuellPeriode.getDokumentasjonVurdering() != null && aktuellPeriode.getDokumentasjonVurdering().erGyldigGrunnForTidligOppstart() ? ja() : nei();
    }
}
