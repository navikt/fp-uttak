package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;


import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIAktivitet.erIAktivitet;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorErIAktivitetBfhrUtsettelse.ID)
public class SjekkOmMorErIAktivitetBfhrUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "BFHR_UTSETTELSE_1";
    public static final String BESKRIVELSE = "Er det avklart at mor er i aktivitet?";

    public SjekkOmMorErIAktivitetBfhrUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return erIAktivitet(grunnlag, 1) ? ja() : nei();
    }
}
