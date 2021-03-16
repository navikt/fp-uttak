package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmSøkerErArbeidstaker extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 18.2.4";

    public SjekkOmSøkerErArbeidstaker() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        for (var aktiviteter : grunnlag.getAktuellPeriode().getAktiviteter()) {
            if (AktivitetType.ARBEID.equals(aktiviteter.getAktivitetType())) {
                return ja();
            }
        }
        return nei();
    }
}
