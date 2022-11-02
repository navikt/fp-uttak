package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.ER_ALENEOMSORG;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.ALENEOMSORG;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmGyldigOverføringPgaAleneomsorg.ID)
public class SjekkOmGyldigOverføringPgaAleneomsorg extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9.3";

    public SjekkOmGyldigOverføringPgaAleneomsorg() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        return ALENEOMSORG.equals(oppgittPeriode.getOverføringÅrsak()) && ER_ALENEOMSORG.equals(oppgittPeriode.getDokumentasjonVurdering()) ? ja() : nei();
    }
}
