package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import java.time.Period;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID)
public class SjekkOmPeriodenErEtterMaksgrenseForUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 15.6";
    private Konfigurasjon konfigurasjon;

    public SjekkOmPeriodenErEtterMaksgrenseForUttak(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        LocalDate grense = regnUtMaksgrenseForLovligeUttaksdag(grunnlag.getFamiliehendelse(), konfigurasjon);
        if (uttakPeriode.getFom().isAfter(grense) || uttakPeriode.getFom().equals(grense)) {
            return ja();
        }
        return nei();
    }

    public static LocalDate regnUtMaksgrenseForLovligeUttaksdag(LocalDate familiehendelse, Konfigurasjon konfigurasjon) {
        Period maksGrenseRelativTilFamiliehendelse = konfigurasjon.getParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO, Period.class, familiehendelse);
        return familiehendelse.plus(maksGrenseRelativTilFamiliehendelse);
    }
}
