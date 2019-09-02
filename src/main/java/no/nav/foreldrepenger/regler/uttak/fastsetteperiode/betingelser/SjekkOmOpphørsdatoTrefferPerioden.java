package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmOpphørsdatoTrefferPerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 2.0.1";

    public SjekkOmOpphørsdatoTrefferPerioden() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.getAktuellPeriode();
        LocalDate opphørsdatoForMedlemskap = grunnlag.getOpphørsdatoForMedlemskap();

        if (opphørsdatoForMedlemskap == null) {
            return nei();
        }

        return erOpphørsdatoIEllerFørPerioden(uttakPeriode, opphørsdatoForMedlemskap) ? ja() : nei();
    }


    private boolean erOpphørsdatoIEllerFørPerioden(UttakPeriode uttakPeriode, LocalDate opphørsdato) {
        return uttakPeriode.overlapper(opphørsdato) || uttakPeriode.getFom().isAfter(opphørsdato);
    }
}
