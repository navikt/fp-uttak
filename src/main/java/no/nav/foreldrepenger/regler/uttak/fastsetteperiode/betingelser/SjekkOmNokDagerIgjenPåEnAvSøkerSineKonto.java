package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer.hentSøkerSineKontoer;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmNokDagerIgjenPåEnAvSøkerSineKonto.ID)
public class SjekkOmNokDagerIgjenPåEnAvSøkerSineKonto extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.2";
    public static final String BESKRIVELSE = "Er det nok dager igjen på en konto?";

    public SjekkOmNokDagerIgjenPåEnAvSøkerSineKonto() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        for (var stønadskontotype : hentSøkerSineKontoer(grunnlag)) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().saldoITrekkdager(stønadskontotype, aktivitet);
                var virkedager = Virkedager.beregnAntallVirkedager(aktuellPeriode.getFom(), aktuellPeriode.getTom());
                if (!saldo.subtract(new Trekkdager(virkedager)).mindreEnn0()) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
