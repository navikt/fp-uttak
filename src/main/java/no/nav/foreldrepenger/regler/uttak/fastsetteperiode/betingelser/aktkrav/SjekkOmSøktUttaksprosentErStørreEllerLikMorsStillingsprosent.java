package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent.ID)
public class SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AKT_ARBEID_2";
    public static final String BESKRIVELSE = "Er gradert uttaksprosenten større eller lik mors stillingsprosent?";

    public SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        if (gradertUttaksprosentStørreEllerLikMorsStillingsprosent(aktuellPeriode)) {
            return ja();
        }
        return nei();
    }

    private static boolean gradertUttaksprosentStørreEllerLikMorsStillingsprosent(OppgittPeriode aktuellPeriode) {
        var gradertUttaksprosent = BigDecimal.valueOf(100).subtract(aktuellPeriode.getArbeidsprosent());
        return gradertUttaksprosent.compareTo(aktuellPeriode.getMorsStillingsprosent().decimalValue()) >= 0;
    }
}
