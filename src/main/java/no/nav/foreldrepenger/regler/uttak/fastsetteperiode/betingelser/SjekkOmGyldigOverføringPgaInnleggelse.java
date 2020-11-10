package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmGyldigOverføringPgaInnleggelse.ID)
public class SjekkOmGyldigOverføringPgaInnleggelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9.1";

    public SjekkOmGyldigOverføringPgaInnleggelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode oppgittPeriode = grunnlag.getAktuellPeriode();
        for (OppgittPeriode periodeMedAnnenForelderInnlagt : grunnlag.getPerioderMedAnnenForelderInnlagt()) {
            if (oppgittPeriode.erOmsluttetAv(periodeMedAnnenForelderInnlagt) && harGyldigGrunn(oppgittPeriode,
                    grunnlag.getGyldigGrunnPerioder())) {
                return ja();
            }
        }
        return nei();
    }

    private boolean harGyldigGrunn(OppgittPeriode oppgittPeriode, List<GyldigGrunnPeriode> gyldigGrunnPerioder) {
        for (GyldigGrunnPeriode gyldigGrunnPeriode : gyldigGrunnPerioder) {
            if (oppgittPeriode.erOmsluttetAv(gyldigGrunnPeriode)) {
                return true;
            }
        }
        return false;
    }
}
