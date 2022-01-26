package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMinsterettUtenAktivitetskravHarDisponibleDager.ID)
public class SjekkOmMinsterettUtenAktivitetskravHarDisponibleDager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.4";
    public static final String BESKRIVELSE = "Er det disponible dager uten aktivitetskrav?";

    public SjekkOmMinsterettUtenAktivitetskravHarDisponibleDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        // TODO TFP-4842 - tilpass når flere tilfelle kan passere denne sjekken. Nå er den konservativ
        if (grunnlag.isSakMedDagerUtenAktivitetskrav() && grunnlag.getAktuellPeriode().gjelderPeriodeMinsterett()) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().restSaldoDagerUtenAktivitetskrav(grunnlag.getAktuellPeriode().getStønadskontotype(), aktivitet);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }
        // TODO WLB - tilpass når flere tilfelle kan passere denne sjekken. Nå er den konservativ
        if (grunnlag.isSakMedMinsterett() && grunnlag.getAktuellPeriode().gjelderPeriodeMinsterett()) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().restSaldoMinsterett(grunnlag.getAktuellPeriode().getStønadskontotype(), aktivitet);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
