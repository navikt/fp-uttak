package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorsAktivitetErKjent.ID)
public class SjekkOmMorsAktivitetErKjent extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_10";
    public static final String BESKRIVELSE = "Har søker oppgitt mors aktivitet?";

    public SjekkOmMorsAktivitetErKjent() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return fastsettePeriodeGrunnlag.getAktuellPeriode().getMorsAktivitet() == null
                ? nei()
                : ja();
    }
}
