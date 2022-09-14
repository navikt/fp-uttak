package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID)
public class SjekkGyldigGrunnForTidligOppstartHelePerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.5.1";

    public SjekkGyldigGrunnForTidligOppstartHelePerioden() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        if (harGyldigGrunn(aktuellPeriode, grunnlag.getGyldigGrunnPerioder())) {
            return ja();
        }
        //Brukes bare for tidlig oppstart for far
        if (!grunnlag.isSøkerMor() && Set.of(PeriodeVurderingType.PERIODE_OK, PeriodeVurderingType.ENDRE_PERIODE).contains(aktuellPeriode.getPeriodeVurderingType())) {
            return ja();
        }
        return nei();
    }

    private boolean harGyldigGrunn(OppgittPeriode oppgittPeriode, List<GyldigGrunnPeriode> gyldigGrunnPerioder) {
        for (var gyldigGrunnPeriode : gyldigGrunnPerioder) {
            if (oppgittPeriode.erOmsluttetAv(gyldigGrunnPeriode)) {
                return true;
            }
        }
        return false;
    }
}
