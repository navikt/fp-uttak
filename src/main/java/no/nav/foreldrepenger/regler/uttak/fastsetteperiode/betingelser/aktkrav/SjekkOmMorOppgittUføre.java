package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorArbeid.sjekk;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorOppgittUføre.ID)
public class SjekkOmMorOppgittUføre extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_12";
    public static final String BESKRIVELSE = "Har søker oppgitt at mor mottar uføretrygd?";

    public SjekkOmMorOppgittUføre() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        if (!fastsettePeriodeGrunnlag.rettighetsType().bareFarRett()) {
            return nei();
        }
        if (fastsettePeriodeGrunnlag.isMorOppgittUføretrygd()) {
            return ja();
        }
        return sjekk(fastsettePeriodeGrunnlag, MorsAktivitet.UFØRE) ? ja() : nei();
    }
}
