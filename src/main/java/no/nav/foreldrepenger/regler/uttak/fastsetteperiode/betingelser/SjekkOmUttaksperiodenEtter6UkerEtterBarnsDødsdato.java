package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato.ID)
public class SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 17.1.1.1";

    private final Konfigurasjon konfigurasjon;

    public SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var antallUkerUttakEtterBarnDødt = konfigurasjon.getParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER,
                fastsettePeriodeGrunnlag.getFamiliehendelse());
        if (fastsettePeriodeGrunnlag.getDødsdatoForBarn() != null) {
            var dødsdatoForBarn = fastsettePeriodeGrunnlag.getDødsdatoForBarn();
            var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
            var ukerEtterBarnDødt = dødsdatoForBarn.plusWeeks(antallUkerUttakEtterBarnDødt);
            if (oppgittPeriode.getFom().isAfter(ukerEtterBarnDødt) || oppgittPeriode.getFom().equals(ukerEtterBarnDødt)) {
                return ja();
            }
        }
        return nei();
    }
}
