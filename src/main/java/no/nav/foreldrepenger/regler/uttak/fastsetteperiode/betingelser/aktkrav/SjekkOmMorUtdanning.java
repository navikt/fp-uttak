package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorArbeid.sjekk;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorUtdanning.ID)
public class SjekkOmMorUtdanning extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_7";
    public static final String BESKRIVELSE = "Har søker oppgitt at mor er i utdanning?";

    public SjekkOmMorUtdanning() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return sjekk(fastsettePeriodeGrunnlag, MorsAktivitet.UTDANNING) ? ja() : nei();
    }
}
