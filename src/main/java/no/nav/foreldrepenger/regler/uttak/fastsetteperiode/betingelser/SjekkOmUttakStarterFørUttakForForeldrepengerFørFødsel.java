package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel.ID)
public class SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27.5";

    private final Konfigurasjon konfigurasjon;

    public SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var startDatoUttak = aktuellPeriode.getFom();
        var ukerFørFødselUttaksgrenseForeldrepenger = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER,
                grunnlag.getFamiliehendelse());
        if (startDatoUttak.isBefore(grunnlag.getFamiliehendelse().minusWeeks(ukerFørFødselUttaksgrenseForeldrepenger))) {
            return ja();
        }
        return nei();
    }

}
