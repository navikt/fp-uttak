package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent.ID)
public class SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27.3.2";

    public SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();

        var familiehendelse = grunnlag.getFamiliehendelse();

        var ukerFørFødsel = Konfigurasjon.STANDARD.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelse);

        var førsteLovligeDag = familiehendelse.minusWeeks(ukerFørFødsel);
        var sisteLovligeDag = familiehendelse.minusDays(1);

        if (aktuellPeriode.getFom().isBefore(førsteLovligeDag) || aktuellPeriode.getTom().isAfter(sisteLovligeDag)) {
            return ja();
        }

        return nei();

    }
}
