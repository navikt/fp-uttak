package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.HV_OVELSE_GODKJENT;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDokumentertHV.ID)
public class SjekkOmDokumentertHV extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.6";
    public static final String BESKRIVELSE = "Er det dokumentert at søker er i HV i perioden?";

    public SjekkOmDokumentertHV() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return HV_OVELSE_GODKJENT.equals(grunnlag.getAktuellPeriode().getDokumentasjonVurdering()) ? ja() : nei();
    }
}
