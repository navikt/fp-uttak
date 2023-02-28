package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenInnenforUkerReservertMor.ID)
public class SjekkOmPeriodenInnenforUkerReservertMor extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.3";

    public SjekkOmPeriodenInnenforUkerReservertMor() {
        super(SjekkOmPeriodenInnenforUkerReservertMor.class.getSimpleName());
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();

        var familiehendelse = grunnlag.getFamiliehendelse();

        var antallUkerEtterFødsel = Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, familiehendelse);

        var periodeEtterFødselNormaltReservertMor = new LukketPeriode(familiehendelse,
                familiehendelse.plusWeeks(antallUkerEtterFødsel).minusDays(1));
        if (periodeEtterFødselNormaltReservertMor.overlapper(aktuellPeriode)) {
            if (aktuellPeriode.erOmsluttetAv(periodeEtterFødselNormaltReservertMor)) {
                return ja();
            }
            throw new IllegalArgumentException(
                    "Utvikler-feil: periode er ikke knekt riktig fom=" + aktuellPeriode.getFom() + " tom="
                            + aktuellPeriode.getTom());
        }
        return nei();
    }
}
