package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID)
public class SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5";
    public static final String BESKRIVELSE = "Har noen aktiviteter disponible stønadsdager på kvoten?";

    public SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var stønadskontotype = aktuellPeriode.getStønadskontotype();

        for (AktivitetIdentifikator aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
            Trekkdager saldo = grunnlag.getSaldoUtregning().saldoITrekkdager(stønadskontotype, aktivitet);
            if (saldo.merEnn0()) {
                if (aktuellPeriode.isFlerbarnsdager()) {
                    Trekkdager saldoFlerbarnsdager = grunnlag.getSaldoUtregning().saldoITrekkdager(Stønadskontotype.FLERBARNSDAGER, aktivitet);
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
