package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID)
public class SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto
        extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5";
    public static final String BESKRIVELSE = "Har noen aktiviteter disponible stønadsdager på kvoten?";

    public SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var stønadskontotype = aktuellPeriode.getStønadskontotype();

        for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
            var saldo = grunnlag.getSaldoUtregning().saldoITrekkdager(stønadskontotype, aktivitet);
            if (saldo.merEnn0()) {
                if (aktuellPeriode.isFlerbarnsdager()) {
                    var saldoFlerbarnsdager = grunnlag.getSaldoUtregning().restSaldoFlerbarnsdager(aktivitet);
                    if (saldoFlerbarnsdager.merEnn0()) {
                        return ja();
                    }
                } else {
                    return ja();
                }
            }
        }
        return nei();
    }
}
