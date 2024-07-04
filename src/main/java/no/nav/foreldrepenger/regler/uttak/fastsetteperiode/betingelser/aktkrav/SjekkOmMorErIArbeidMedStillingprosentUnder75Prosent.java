package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.SamtidigUttakUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.ID)
public class SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AKT_ARBEID_1";
    public static final String BESKRIVELSE = "Er mor i arbeid og har en stillingsprosent under 75 prosent?";

    public SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        if (erMorIArbeidOgHarEnStillingsprosentMindreEnn75Prosent(aktuellPeriode) && (SamtidigUttakUtil.søktSamtidigUttakForPeriode(grunnlag) || grunnlag.getAktuellPeriode().erSøktGradering())) {
            return ja();
        }
        return nei();
    }


    private static boolean erMorIArbeidOgHarEnStillingsprosentMindreEnn75Prosent(OppgittPeriode aktuellPeriode) {
        return MorsAktivitet.ARBEID.equals(aktuellPeriode.getMorsAktivitet()) && aktuellPeriode.getMorsStillingsprosent() != null;
    }
}
