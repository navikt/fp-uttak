package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErUtsettelseFørSøknadMottattdato.mottattFørSisteDag;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmErGradertFørSøknadMottattdato.ID)
public class SjekkOmErGradertFørSøknadMottattdato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 26.1.15";

    public SjekkOmErGradertFørSøknadMottattdato() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode periode = grunnlag.getAktuellPeriode();
        if (periode.erSøktGradering() && mottattFørSisteDag(periode)) {
            return ja();
        }
        return nei();
    }
}
