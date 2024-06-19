package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttakOgUtenAktivitetskrav.ID)
public class SjekkOmUttakOgUtenAktivitetskrav extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "AVSLAG_AKT_13";
    public static final String BESKRIVELSE =
            "Er perioden uttak og finnes dager uten aktivitetskrav?";

    public SjekkOmUttakOgUtenAktivitetskrav() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var erUttak =
                !(fastsettePeriodeGrunnlag.getAktuellPeriode().isUtsettelse()
                        || fastsettePeriodeGrunnlag.getAktuellPeriode().isOpphold());
        return erUttak
                        && (fastsettePeriodeGrunnlag.isSakMedDagerUtenAktivitetskrav()
                                || fastsettePeriodeGrunnlag.isSakMedMinsterett())
                ? ja()
                : nei();
    }
}
