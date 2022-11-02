package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmGyldigOverføringPgaSykdomSkade.ID)
public class SjekkOmGyldigOverføringPgaSykdomSkade extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9.3";

    public SjekkOmGyldigOverføringPgaSykdomSkade() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        return OverføringÅrsak.SYKDOM_ELLER_SKADE.equals(oppgittPeriode.getOverføringÅrsak())
            && DokumentasjonVurdering.SYKDOM_ANNEN_FORELDER_DOKUMENTERT.equals(oppgittPeriode.getDokumentasjonVurdering()) ? ja() : nei();
    }
}
