package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmOppholdKvoteAnnenForelder extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9.0.0";

    public SjekkOmOppholdKvoteAnnenForelder() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        var erOppholdKvote =
                oppgittPeriode.isOppholdPga(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                        || oppgittPeriode.isOppholdPga(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                        || oppgittPeriode.isOppholdPga(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER);
        return erOppholdKvote ? ja() : nei();
    }
}
