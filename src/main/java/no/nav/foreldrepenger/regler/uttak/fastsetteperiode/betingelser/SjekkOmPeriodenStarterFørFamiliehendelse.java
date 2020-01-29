package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenStarterFørFamiliehendelse.ID)
public class SjekkOmPeriodenStarterFørFamiliehendelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.2";

    public SjekkOmPeriodenStarterFørFamiliehendelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode aktuellPeriode = grunnlag.getAktuellPeriode();

        if (aktuellPeriode.getFom().isBefore(grunnlag.getFamiliehendelse())) {
            return ja();
        }
        return nei();
    }

}
