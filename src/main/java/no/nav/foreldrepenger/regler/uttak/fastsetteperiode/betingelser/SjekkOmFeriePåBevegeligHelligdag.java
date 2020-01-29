package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.BevegeligeHelligdagerUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmFeriePåBevegeligHelligdag extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 14.1.4.1";

    public SjekkOmFeriePåBevegeligHelligdag() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode oppgittPeriode = grunnlag.getAktuellPeriode();
        for (LocalDate bevegeligHelligdag : BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(oppgittPeriode)) {
            if (oppgittPeriode.overlapper(bevegeligHelligdag)) {
                return ja();
            }
        }
        return nei();
    }

}
