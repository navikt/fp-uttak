package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
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
        int antallUkerUttakEtterBarnDødt = konfigurasjon.getParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER,
                fastsettePeriodeGrunnlag.getFamiliehendelse());
        if (fastsettePeriodeGrunnlag.getDødsdatoForBarn() != null) {
            LocalDate dødsdatoForBarn = fastsettePeriodeGrunnlag.getDødsdatoForBarn();
            OppgittPeriode oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
            LocalDate ukerEtterBarnDødt = dødsdatoForBarn.plusWeeks(antallUkerUttakEtterBarnDødt);
            if (oppgittPeriode.getFom().isAfter(ukerEtterBarnDødt) || oppgittPeriode.getFom().equals(ukerEtterBarnDødt)) {
                return ja();
            }
        }
        return nei();
    }
}
