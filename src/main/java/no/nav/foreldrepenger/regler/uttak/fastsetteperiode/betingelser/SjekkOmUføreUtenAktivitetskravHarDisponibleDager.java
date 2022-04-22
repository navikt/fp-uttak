package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUføreUtenAktivitetskravHarDisponibleDager.ID)
public class SjekkOmUføreUtenAktivitetskravHarDisponibleDager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.5";
    public static final String BESKRIVELSE = "Er det disponible dager uten aktivitetskrav?";

    public SjekkOmUføreUtenAktivitetskravHarDisponibleDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isSakMedDagerUtenAktivitetskrav() && grunnlag.getAktuellPeriode().gjelderPeriodeMinsterett()) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var saldo = grunnlag.getSaldoUtregning().restSaldoDagerUtenAktivitetskrav(aktivitet);
                if (saldo.merEnn0()) {
                    return ja();
                }
            }
        }

        return nei();
    }
}
