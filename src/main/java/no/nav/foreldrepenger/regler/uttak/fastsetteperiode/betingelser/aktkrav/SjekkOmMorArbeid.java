package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorArbeid.ID)
public class SjekkOmMorArbeid extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_1";
    public static final String BESKRIVELSE = "Har søker oppgitt at mor er i arbeid?";

    public SjekkOmMorArbeid() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return sjekk(fastsettePeriodeGrunnlag, MorsAktivitet.ARBEID) ? ja() : nei();
    }

    static boolean sjekk(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag, MorsAktivitet morsAktivitet) {
        var morsAktivitetIPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode().getMorsAktivitet();
        if (morsAktivitetIPeriode == null) {
            throw new IllegalStateException("Forventer mors aktivitet");
        }
        return morsAktivitetIPeriode.equals(morsAktivitet);
    }
}
