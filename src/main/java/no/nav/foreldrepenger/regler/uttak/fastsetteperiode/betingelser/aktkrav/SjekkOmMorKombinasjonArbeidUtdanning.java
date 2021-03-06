package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorArbeid.sjekk;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorKombinasjonArbeidUtdanning.ID)
public class SjekkOmMorKombinasjonArbeidUtdanning extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_4";
    public static final String BESKRIVELSE = "Har søker oppgitt at mor er i arbeid kombinert med utdanning?";

    public SjekkOmMorKombinasjonArbeidUtdanning() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return sjekk(fastsettePeriodeGrunnlag, MorsAktivitet.ARBEID_OG_UTDANNING) ? ja() : nei();
    }
}
