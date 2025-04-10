package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSkalTrekkeDagerFraKonto.ID)
public class SjekkOmSkalTrekkeDagerFraKonto extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "AVSLAG_AKT_TREKK";
    public static final String BESKRIVELSE = "Skal avslag trekke dager?";

    public SjekkOmSkalTrekkeDagerFraKonto() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var bareFarRett = fastsettePeriodeGrunnlag.isSøkerFarMedmor() && fastsettePeriodeGrunnlag.rettighetsType().bareSøkerRett();
        return fastsettePeriodeGrunnlag.getAktuellPeriode().kreverSammenhengendeUttak(fastsettePeriodeGrunnlag.getSammenhengendeUttakTomDato())
            || bareFarRett ? ja() : nei();
    }
}
