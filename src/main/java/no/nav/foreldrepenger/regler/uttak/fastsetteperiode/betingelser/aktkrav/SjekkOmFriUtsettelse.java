package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFriUtsettelse.ID)
public class SjekkOmFriUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "AVSLAG_AKT_14";
    public static final String BESKRIVELSE = "Er perioden utsettelse med årsak Fri?";

    public SjekkOmFriUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return Objects.equals(UtsettelseÅrsak.FRI, fastsettePeriodeGrunnlag.getAktuellPeriode().getUtsettelseÅrsak()) ? ja() : nei();
    }
}
