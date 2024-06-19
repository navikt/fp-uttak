package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMinsterettHarDisponibleDager.ID)
public class SjekkOmMinsterettHarDisponibleDager
        extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.4";
    public static final String BESKRIVELSE = "Er det disponible minsterettdager?";

    public SjekkOmMinsterettHarDisponibleDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isSakMedMinsterett()
                && grunnlag.getAktuellPeriode().gjelderPeriodeMinsterett()
                && !grunnlag.erAktuellPeriodeEtterStartNesteStønadsperiode()) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldoUtregning = grunnlag.getSaldoUtregning();
                if (saldoUtregning
                        .saldoITrekkdager(
                                grunnlag.getAktuellPeriode().getStønadskontotype(), aktivitet)
                        .merEnn0()) {
                    var saldo = saldoUtregning.restSaldoMinsterett(aktivitet);
                    if (saldo.merEnn0()) {
                        return ja();
                    }
                }
            }
        }
        return nei();
    }
}
