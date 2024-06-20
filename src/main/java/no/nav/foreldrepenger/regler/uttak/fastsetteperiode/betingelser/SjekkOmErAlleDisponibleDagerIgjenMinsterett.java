package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer.hentSøkerSineKontoer;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmErAlleDisponibleDagerIgjenMinsterett.ID)
public class SjekkOmErAlleDisponibleDagerIgjenMinsterett extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.6";
    public static final String BESKRIVELSE = "Er alle disponible dager minsterett?";

    public SjekkOmErAlleDisponibleDagerIgjenMinsterett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (!hentSøkerSineKontoer(grunnlag).contains(Stønadskontotype.FORELDREPENGER)) {
            throw new IllegalStateException("Støtter bare stønadskonto " + Stønadskontotype.FORELDREPENGER);
        }
        var foreldrepenger = grunnlag.getSaldoUtregning().saldoITrekkdager(Stønadskontotype.FORELDREPENGER);
        if (!foreldrepenger.merEnn0()) {
            return nei();
        }
        var restSaldoMinsterett = grunnlag.getSaldoUtregning().restSaldoMinsterett();
        if (restSaldoMinsterett.compareTo(foreldrepenger) >= 0) {
            return ja();
        }
        return nei();
    }
}
