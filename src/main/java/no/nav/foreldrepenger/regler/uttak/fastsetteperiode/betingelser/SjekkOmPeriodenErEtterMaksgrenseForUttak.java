package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import java.time.Period;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID)
public class SjekkOmPeriodenErEtterMaksgrenseForUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 15.6";
    public static final String BESKRIVELSE = "Er hele perioden innenfor maksimalgrense for foreldrepenger?";

    public SjekkOmPeriodenErEtterMaksgrenseForUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        var grense = regnUtMaksgrenseForLovligeUttaksdag(grunnlag.getFamiliehendelse());
        if (oppgittPeriode.getFom().isAfter(grense) || oppgittPeriode.getFom().equals(grense)) {
            return ja();
        }
        return nei();
    }

    public static LocalDate regnUtMaksgrenseForLovligeUttaksdag(LocalDate familiehendelse) {
        var maksGrenseRelativTilFamiliehendelse = Period.ofYears(Konfigurasjon.STANDARD.getParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO_ÅR, familiehendelse));
        return familiehendelse.plus(maksGrenseRelativTilFamiliehendelse);
    }
}
