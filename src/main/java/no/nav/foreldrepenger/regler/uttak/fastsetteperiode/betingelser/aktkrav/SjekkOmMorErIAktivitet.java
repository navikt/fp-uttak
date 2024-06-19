package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorErIAktivitet.ID)
public class SjekkOmMorErIAktivitet extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_9";
    public static final String BESKRIVELSE = "Er det avklart at mor er i aktivitet?";

    public SjekkOmMorErIAktivitet() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return MORS_AKTIVITET_GODKJENT.equals(
                        grunnlag.getAktuellPeriode().getDokumentasjonVurdering())
                ? ja()
                : nei();
    }
}
