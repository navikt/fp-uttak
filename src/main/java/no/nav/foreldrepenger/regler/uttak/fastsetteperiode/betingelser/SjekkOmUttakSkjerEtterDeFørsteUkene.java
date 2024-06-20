package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttakSkjerEtterDeFørsteUkene.ID)
public class SjekkOmUttakSkjerEtterDeFørsteUkene extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 19.2";
    public static final String BESKRIVELSE = "Er perioden etter uke 6 etter termin/fødsel?";

    public SjekkOmUttakSkjerEtterDeFørsteUkene() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var minsteKravTilMødrekvoteEtterFødsel = Konfigurasjon.STANDARD.getParameter(
                Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, grunnlag.getFamiliehendelse());
        var tidligsteStartDatoForFedrekvote =
                grunnlag.getFamiliehendelse().plusWeeks(minsteKravTilMødrekvoteEtterFødsel);
        if (!aktuellPeriode.getFom().isBefore(tidligsteStartDatoForFedrekvote)) {
            return ja();
        }
        return nei();
    }
}
