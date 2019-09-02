package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmFulltArbeidForUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.2.4";

    public SjekkOmFulltArbeidForUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        BigDecimal stillingsprosent = grunnlag.getArbeidsprosenter().getStillingsprosent(uttakPeriode);
        if (stillingsprosent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return ja();
        }
        return nei();
    }
}
