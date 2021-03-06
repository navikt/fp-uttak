package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttaksperiodenEtterSøkersDødsdato.ID)
public class SjekkOmUttaksperiodenEtterSøkersDødsdato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 17.0";

    public SjekkOmUttaksperiodenEtterSøkersDødsdato() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        if (fastsettePeriodeGrunnlag.getDødsdatoForSøker() != null) {
            var dødsdatoForSøker = fastsettePeriodeGrunnlag.getDødsdatoForSøker();
            var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
            if (oppgittPeriode.getFom().isAfter(dødsdatoForSøker)) {
                return ja();
            }
        }
        return nei();
    }
}
