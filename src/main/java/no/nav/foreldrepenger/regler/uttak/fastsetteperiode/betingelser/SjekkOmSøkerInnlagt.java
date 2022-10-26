package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_SØKER_DOKUMENTERT;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSøkerInnlagt.ID)
public class SjekkOmSøkerInnlagt extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.4";
    public static final String BESKRIVELSE = "Var søker innlagt på helseinstitusjon i perioden?";

    public SjekkOmSøkerInnlagt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return INNLEGGELSE_SØKER_DOKUMENTERT.equals(grunnlag.getAktuellPeriode().getDokumentasjonVurdering()) ? ja() : nei();
    }
}
