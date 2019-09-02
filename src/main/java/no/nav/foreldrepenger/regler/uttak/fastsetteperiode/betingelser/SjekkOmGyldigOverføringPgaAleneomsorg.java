package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmGyldigOverføringPgaAleneomsorg.ID)
public class SjekkOmGyldigOverføringPgaAleneomsorg extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9.3";

    public SjekkOmGyldigOverføringPgaAleneomsorg() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        for (UttakPeriode perioderMedAleneomsorg : grunnlag.getPerioderMedAleneomsorg()) {
            if (uttakPeriode.erOmsluttetAv(perioderMedAleneomsorg) && harGyldigGrunn(uttakPeriode, grunnlag.getGyldigGrunnPerioder())) {
                return ja();
            }
        }
        return nei();
    }

    private boolean harGyldigGrunn(UttakPeriode uttakPeriode, List<GyldigGrunnPeriode> gyldigGrunnPerioder) {
        for (GyldigGrunnPeriode gyldigGrunnPeriode : gyldigGrunnPerioder) {
            if (uttakPeriode.erOmsluttetAv(gyldigGrunnPeriode)) {
                return true;
            }
        }
        return false;
    }
}
