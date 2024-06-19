package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFarsUttakRundtFødselTilgjengeligeDager.ID)
public class SjekkOmFarsUttakRundtFødselTilgjengeligeDager
        extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.9";

    public SjekkOmFarsUttakRundtFødselTilgjengeligeDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isSøkerMor()
                || grunnlag.getSaldoUtregning()
                        .getFarUttakRundtFødselDager()
                        .equals(Trekkdager.ZERO)
                || grunnlag.periodeFarRundtFødsel().isEmpty()) {
            return nei();
        }
        var periodeForUttakRundtFødsel = grunnlag.periodeFarRundtFødsel().orElseThrow();
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        if (aktuellPeriode.erOmsluttetAv(periodeForUttakRundtFødsel)) {
            for (var aktivitet : aktuellPeriode.getAktiviteter()) {
                var saldo =
                        grunnlag.getSaldoUtregning()
                                .restSaldoFarUttakRundtFødsel(
                                        aktivitet, periodeForUttakRundtFødsel);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
