package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.SøknadsfristUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenErFørGyldigDato.ID)
public class SjekkOmPeriodenErFørGyldigDato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 15.3.1";

    public SjekkOmPeriodenErFørGyldigDato() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        var tidligstMottattDato = oppgittPeriode.getTidligstMottattDato();
        if (tidligstMottattDato.isEmpty()) {
            return nei();
        }
        if (oppgittPeriode.getTom().isBefore(SøknadsfristUtil.finnFørsteLoveligeUttaksdag(tidligstMottattDato.get()))) {
            return ja();
        }
        return nei();
    }
}
