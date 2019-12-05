package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
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
        UttakPeriode aktuellPeriode = grunnlag.getAktuellPeriode();
        Stønadskontotype stønadskontotype = aktuellPeriode.getStønadskontotype();

        for (AktivitetIdentifikator aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
            Trekkdager saldo = grunnlag.getSaldoUtregning().saldoITrekkdager(aktivitet, stønadskontotype);
            if (saldo.compareTo(Trekkdager.ZERO) > 0) {
                return ja();
            }
            if (aktuellPeriode.isFlerbarnsdager()) {
                Trekkdager saldoFlerbarnsdager = grunnlag.getSaldoUtregning().saldoITrekkdager(aktivitet, Stønadskontotype.FLERBARNSDAGER);
                if (saldoFlerbarnsdager.compareTo(Trekkdager.ZERO) > 0) {
                    return ja();
                }
            }
        }
        return nei();
    }

}
