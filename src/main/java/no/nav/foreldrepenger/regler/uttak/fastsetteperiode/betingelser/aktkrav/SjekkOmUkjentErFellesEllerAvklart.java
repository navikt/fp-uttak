package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIAktivitet.periodeMedAktivitet;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUkjentErFellesEllerAvklart.ID)
public class SjekkOmUkjentErFellesEllerAvklart extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "AVSLAG_AKT_13";
    public static final String BESKRIVELSE = "Er ikke angitt aktivitet vurdert mhp aktivitet?";

    public SjekkOmUkjentErFellesEllerAvklart() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var periodeMedAktivitet = periodeMedAktivitet(fastsettePeriodeGrunnlag);
        return Objects.equals(fastsettePeriodeGrunnlag.getAktuellPeriode().getStønadskontotype(), Stønadskontotype.FELLESPERIODE) ||
                periodeMedAktivitet.isPresent() ? ja() : nei();
    }
}
