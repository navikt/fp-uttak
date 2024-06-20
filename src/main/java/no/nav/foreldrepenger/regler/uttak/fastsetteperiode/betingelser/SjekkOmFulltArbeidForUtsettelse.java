package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.math.BigDecimal;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmFulltArbeidForUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.2.7";

    public SjekkOmFulltArbeidForUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        var stillingsprosent = grunnlag.getArbeid().getStillingsprosent(oppgittPeriode.getFom());
        if (stillingsprosent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return ja();
        }
        return nei();
    }
}
