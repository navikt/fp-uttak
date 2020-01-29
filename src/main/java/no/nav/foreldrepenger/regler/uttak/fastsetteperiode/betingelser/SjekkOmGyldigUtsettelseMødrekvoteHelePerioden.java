package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
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
        List<GyldigGrunnPeriode> gyldigePerioder = fastsettePeriodeGrunnlag.getAktuelleGyldigeGrunnPerioder();

        OppgittPeriode aktuellPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        LocalDate dato = aktuellPeriode.getFom();
        for (GyldigGrunnPeriode periode : gyldigePerioder) {
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
