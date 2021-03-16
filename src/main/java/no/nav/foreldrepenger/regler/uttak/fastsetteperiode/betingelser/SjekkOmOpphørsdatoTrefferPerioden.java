package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmOpphørsdatoTrefferPerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 2.0.1";

    public SjekkOmOpphørsdatoTrefferPerioden() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        var opphørsdatoForMedlemskap = grunnlag.getOpphørsdatoForMedlemskap();

        if (opphørsdatoForMedlemskap == null) {
            return nei();
        }

        return erOpphørsdatoIEllerFørPerioden(oppgittPeriode, opphørsdatoForMedlemskap) ? ja() : nei();
    }


    private boolean erOpphørsdatoIEllerFørPerioden(OppgittPeriode oppgittPeriode, LocalDate opphørsdato) {
        return oppgittPeriode.overlapper(opphørsdato) || oppgittPeriode.getFom().isAfter(opphørsdato);
    }
}
