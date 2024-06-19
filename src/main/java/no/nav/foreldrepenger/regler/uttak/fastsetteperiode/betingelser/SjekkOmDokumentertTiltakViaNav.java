package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.NAV_TILTAK_GODKJENT;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDokumentertTiltakViaNav.ID)
public class SjekkOmDokumentertTiltakViaNav extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.7";
    public static final String BESKRIVELSE =
            "Er det dokumentert at søker er i tiltak i regi av nav i perioden?";

    public SjekkOmDokumentertTiltakViaNav() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return NAV_TILTAK_GODKJENT.equals(grunnlag.getAktuellPeriode().getDokumentasjonVurdering())
                ? ja()
                : nei();
    }
}
