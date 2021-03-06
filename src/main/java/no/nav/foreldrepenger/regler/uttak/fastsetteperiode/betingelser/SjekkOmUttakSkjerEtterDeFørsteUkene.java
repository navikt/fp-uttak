package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttakSkjerEtterDeFørsteUkene.ID)
public class SjekkOmUttakSkjerEtterDeFørsteUkene extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 19.2";
    public static final String BESKRIVELSE = "Er perioden etter uke 6 etter termin/fødsel?";

    private final Konfigurasjon konfigurasjon;

    public SjekkOmUttakSkjerEtterDeFørsteUkene(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var minsteKravTilMødrekvoteEtterFødsel = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER,
                grunnlag.getFamiliehendelse());
        var tidligsteStartDatoForFedrekvote = grunnlag.getFamiliehendelse().plusWeeks(minsteKravTilMødrekvoteEtterFødsel);
        if (!aktuellPeriode.getFom().isBefore(tidligsteStartDatoForFedrekvote)) {
            return ja();
        }
        return nei();
    }
}
