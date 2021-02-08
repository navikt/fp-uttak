package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent.ID)
public class SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent extends LeafSpecification<FastsettePeriodeGrunnlag> {

    private final Konfigurasjon konfigurasjon;
    public static final String ID = "FP_VK 27.3.2";

    public SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        OppgittPeriode aktuellPeriode = grunnlag.getAktuellPeriode();

        LocalDate familiehendelse = grunnlag.getFamiliehendelse();

        int ukerFørFødsel = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelse);

        LocalDate førsteLovligeDag = familiehendelse.minusWeeks(ukerFørFødsel);
        LocalDate sisteLovligeDag = familiehendelse.minusDays(1);

        if (aktuellPeriode.getFom().isBefore(førsteLovligeDag) || aktuellPeriode.getTom().isAfter(sisteLovligeDag)) {
            return ja();
        }

        return nei();

    }
}
