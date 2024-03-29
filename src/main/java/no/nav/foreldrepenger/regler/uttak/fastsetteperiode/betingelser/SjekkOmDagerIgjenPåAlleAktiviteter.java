package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer.hentSøkerSineKontoer;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDagerIgjenPåAlleAktiviteter.ID)
public class SjekkOmDagerIgjenPåAlleAktiviteter extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.3";
    public static final String BESKRIVELSE = "Er det dager igjen på alle aktiviteter for minst en av bruker sine stønadskonto?";

    public SjekkOmDagerIgjenPåAlleAktiviteter() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        for (var stønadskontotype : hentSøkerSineKontoer(grunnlag)) {
            if (dagerIgjenPåKonto(grunnlag, stønadskontotype)) {
                return ja();
            }
        }
        return nei();
    }

    private boolean dagerIgjenPåKonto(FastsettePeriodeGrunnlag grunnlag, Stønadskontotype stønadskontotype) {
        for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
            var saldo = grunnlag.getSaldoUtregning().saldoITrekkdager(stønadskontotype, aktivitet);
            if (!saldo.merEnn0()) {
                return false;
            }
        }
        return true;
    }
}
