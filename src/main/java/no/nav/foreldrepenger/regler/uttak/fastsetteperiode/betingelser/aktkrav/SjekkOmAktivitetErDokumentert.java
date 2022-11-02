package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_DOKUMENTERT_IKKE_AKTIVITET;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAktivitetErDokumentert.ID)
public class SjekkOmAktivitetErDokumentert extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "AVSLAG_AKT_8";
    public static final String BESKRIVELSE = "Er mors aktivitet er dokumentert?";

    public SjekkOmAktivitetErDokumentert() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return MORS_AKTIVITET_DOKUMENTERT_IKKE_AKTIVITET.equals(fastsettePeriodeGrunnlag.getAktuellPeriode().getDokumentasjonVurdering()) ? ja() : nei();
    }
}
