package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmErUtsettelseFørSøknadMottattdato.ID)
public class SjekkOmErUtsettelseFørSøknadMottattdato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 18.1.8";
    public static final String BESKRIVELSE = "Er utsettelsen før mottattdato?";

    public SjekkOmErUtsettelseFørSøknadMottattdato() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode periode = grunnlag.getAktuellPeriode();
        if (periode.isUtsettelse() && mottattFørSisteDag(periode)) {
            return ja();
        }
        return nei();
    }

    static boolean mottattFørSisteDag(OppgittPeriode periode) {
        return !periode.getTom().isAfter(periode.getMottattDato().orElse(LocalDate.MIN));
    }
}
