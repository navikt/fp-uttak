package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_BARN_GODKJENT;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBarnInnlagt.ID)
public class SjekkOmBarnInnlagt extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.4.1";
    public static final String BESKRIVELSE = "Var barnet innlagt på helseinstitusjon i perioden?";

    public SjekkOmBarnInnlagt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        for (var periodeMedBarnInnlagt : grunnlag.getPleiepengerInnleggelse()) {
            if (oppgittPeriode.erOmsluttetAv(periodeMedBarnInnlagt)) {
                return ja();
            }
        }
        if (INNLEGGELSE_BARN_GODKJENT.equals(oppgittPeriode.getDokumentasjonVurdering())) {
            return ja();
        }
        return nei();
    }
}
