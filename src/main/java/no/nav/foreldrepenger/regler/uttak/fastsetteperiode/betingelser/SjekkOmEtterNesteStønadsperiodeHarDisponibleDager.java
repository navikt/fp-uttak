package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmEtterNesteStønadsperiodeHarDisponibleDager.ID)
public class SjekkOmEtterNesteStønadsperiodeHarDisponibleDager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.7";
    public static final String BESKRIVELSE = "Er det disponible minsterettdager etter start av neste stønadsperiode?";

    public SjekkOmEtterNesteStønadsperiodeHarDisponibleDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isSakMedRettEtterStartNesteStønadsperiode() && grunnlag.getAktuellPeriode().gjelderPeriodeMinsterett() && grunnlag.etterStartNesteStønadsperiode()) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().restSaldoEtterNesteStønadsperiode(aktivitet);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
