package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer.hentSøkerSineKonto;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
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
        for (Stønadskontotype stønadskontotype : hentSøkerSineKonto(grunnlag)) {
            if (dagerIgjenPåKonto(grunnlag, stønadskontotype)) {
                return ja();
            }
        }
        return nei();
    }

    private boolean dagerIgjenPåKonto(FastsettePeriodeGrunnlag grunnlag, Stønadskontotype stønadskontotype) {
        for (AktivitetIdentifikator aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
            Trekkdager saldo = grunnlag.getSaldoUtregning().saldoITrekkdager(aktivitet, stønadskontotype);
            if (!saldo.merEnn0()) {
                return false;
            }
        }
        return true;
    }
}
