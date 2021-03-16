package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFørsteDelAvPeriodenHarGyldigGrunn.ID)
public class SjekkOmFørsteDelAvPeriodenHarGyldigGrunn extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.5.3";

    public SjekkOmFørsteDelAvPeriodenHarGyldigGrunn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var aktuelleGyldigGrunnPeriode = grunnlag.getAktuelleGyldigeGrunnPerioder();

        if (aktuelleGyldigGrunnPeriode.isEmpty()) {
            return nei();
        }

        var periode = aktuelleGyldigGrunnPeriode.get(0);

        if (starterGyldigGrunnEtterAktuellPeriode(periode, aktuellPeriode)) {
            return nei();
        }
        return ja();

    }


    private boolean starterGyldigGrunnEtterAktuellPeriode(Periode periode1, Periode periode2) {
        return periode1.getFom().isAfter(periode2.getFom());
    }
}
