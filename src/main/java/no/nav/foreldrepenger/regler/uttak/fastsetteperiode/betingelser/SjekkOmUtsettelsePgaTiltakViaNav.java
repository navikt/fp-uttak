package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUtsettelsePgaTiltakViaNav.ID)
public class SjekkOmUtsettelsePgaTiltakViaNav extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.8";
    public static final String BESKRIVELSE = "Er det utsettelse pga tiltak i regi av nav?";

    public SjekkOmUtsettelsePgaTiltakViaNav() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        return oppgittPeriode.isUtsettelsePga(UtsettelseÅrsak.TILTAK_NAV) ? ja() : nei();
    }
}
