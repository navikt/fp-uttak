package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;


import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorErIAktivitet.ID)
public class SjekkOmMorErIAktivitet extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_9";
    public static final String BESKRIVELSE = "Er det avklart at mor er i aktivitet?";

    public SjekkOmMorErIAktivitet() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var periodeMedAvklartMorsAktivitet = periodeMedAktivitet(grunnlag);
        if (periodeMedAvklartMorsAktivitet.isEmpty()) {
            return nei();
        }
        return periodeMedAvklartMorsAktivitet.get().erIAktivitet() ? ja() : nei();
    }

    static Optional<PeriodeMedAvklartMorsAktivitet> periodeMedAktivitet(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getPerioderMedAvklartMorsAktivitet()
                .stream()
                .filter(periodeMedAvklartAktivitet -> grunnlag.getAktuellPeriode().erOmsluttetAv(periodeMedAvklartAktivitet))
                .findFirst();
    }
}
