package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.math.BigDecimal;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmSøktGraderingHundreProsentEllerMer
        extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 26.1.8.3";

    public SjekkOmSøktGraderingHundreProsentEllerMer() {
        super(SjekkOmSøktGraderingHundreProsentEllerMer.ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        if (oppgittPeriode.getArbeidsprosent().compareTo(BigDecimal.valueOf(100)) >= 0) {
            return ja();
        }
        return nei();
    }
}
