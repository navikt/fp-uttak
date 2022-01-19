package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMinsterettMedDisponibleDager.ID)
public class SjekkOmMinsterettMedDisponibleDager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.4";
    public static final String BESKRIVELSE = "Gjelder perioden dager der mor bekreftet ufør?";

    public SjekkOmMinsterettMedDisponibleDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        // TODO WLB - tilpass når flere tilfelle kan passere denne sjekken. Nå er den konservativ
        if (grunnlag.isSakMedMinsterett() && grunnlag.getAktuellPeriode().gjelderPeriodeMinsterett()) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().restSaldoMinsterett(grunnlag.getAktuellPeriode().getStønadskontotype(), aktivitet);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
