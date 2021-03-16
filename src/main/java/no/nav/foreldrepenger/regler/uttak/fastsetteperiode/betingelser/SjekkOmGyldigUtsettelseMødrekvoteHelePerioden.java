package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmGyldigUtsettelseMødrekvoteHelePerioden.ID)
public class SjekkOmGyldigUtsettelseMødrekvoteHelePerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 18.3.1";

    public SjekkOmGyldigUtsettelseMødrekvoteHelePerioden() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var gyldigePerioder = fastsettePeriodeGrunnlag.getAktuelleGyldigeGrunnPerioder();

        var aktuellPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        var dato = aktuellPeriode.getFom();
        for (var periode : gyldigePerioder) {
            if (periode.overlapper(dato)) {
                dato = periode.getTom().plusDays(1);
            }
            if (aktuellPeriode.getTom().isBefore(dato)) {
                return ja();
            }
        }
        return nei();
    }

}
