package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFarsUttakRundtFødselTilgjengeligeDager.ID)
public class SjekkOmFarsUttakRundtFødselTilgjengeligeDager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.9";

    private Konfigurasjon konfigurasjon;

    public SjekkOmFarsUttakRundtFødselTilgjengeligeDager(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isSøkerMor() || grunnlag.periodeFarRundtFødsel(konfigurasjon).isEmpty()) {
            return nei();
        }
        var periodeForUttakRundtFødsel = grunnlag.periodeFarRundtFødsel(konfigurasjon).orElseThrow();
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        if (aktuellPeriode.erOmsluttetAv(periodeForUttakRundtFødsel)) {
            for (var aktivitet : aktuellPeriode.getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().restSaldoFarUttakRundtFødsel(aktivitet);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
