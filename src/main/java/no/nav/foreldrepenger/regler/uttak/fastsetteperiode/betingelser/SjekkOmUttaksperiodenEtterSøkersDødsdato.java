package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
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
            LocalDate dødsdatoForSøker = fastsettePeriodeGrunnlag.getDødsdatoForSøker();
            UttakPeriode uttakPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
            if (uttakPeriode.getFom().isAfter(dødsdatoForSøker)) {
                return ja();
            }
        }
        return nei();
    }
}
