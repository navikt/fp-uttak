package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIAktivitet.periodeMedAktivitet;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUkjentErFellesEllerDokumentert.ID)
public class SjekkOmUkjentErFellesEllerDokumentert extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "AVSLAG_AKT_13";
    public static final String BESKRIVELSE = "Er ikke angitt aktivitet vurdert som aktiv eller dokumentert?";

    public SjekkOmUkjentErFellesEllerDokumentert() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var periodeMedAktivitet = periodeMedAktivitet(fastsettePeriodeGrunnlag);
        return Objects.equals(fastsettePeriodeGrunnlag.getAktuellPeriode().getStønadskontotype(), Stønadskontotype.FELLESPERIODE) ||
                periodeMedAktivitet.filter(PeriodeMedAvklartMorsAktivitet::erDokumentert).isPresent() ? ja() : nei();
    }
}
