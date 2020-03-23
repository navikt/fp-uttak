package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDokumentertTiltakViaNav.ID)
public class SjekkOmDokumentertTiltakViaNav extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.7";
    public static final String BESKRIVELSE = "Er det dokumentert at søker er i tiltak i regi av nav i perioden?";

    public SjekkOmDokumentertTiltakViaNav() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        for (var periodeMedTiltakIRegiAvNav : grunnlag.getPerioderMedTiltakIRegiAvNav()) {
            if (oppgittPeriode.erOmsluttetAv(periodeMedTiltakIRegiAvNav)) {
                return ja();
            }
        }
        return nei();
    }
}
