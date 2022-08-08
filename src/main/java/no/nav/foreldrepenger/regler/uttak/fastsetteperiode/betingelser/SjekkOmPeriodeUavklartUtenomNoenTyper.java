package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;


public class SjekkOmPeriodeUavklartUtenomNoenTyper extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.5.1";

    public SjekkOmPeriodeUavklartUtenomNoenTyper() {
        super(ID);

    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var oppgittPeriode = grunnlag.getAktuellPeriode();
        // Filtrer ut perioder hvor det er søkt om gradering, overføring og far som søker fellesperiode&FK før uke 7 på termin&fødsel
        // fordi de håndteres i sine delregler.
        if (oppgittPeriode.erSøktGradering() || oppgittPeriode.harSøktOmOverføringAvKvote() || tidligOppstart(grunnlag,
                oppgittPeriode)) {
            return nei();
        }
        if (PeriodeVurderingType.UAVKLART_PERIODE.equals(oppgittPeriode.getPeriodeVurderingType())) {
            return ja();
        }
        return nei();
    }

    private boolean tidligOppstart(FastsettePeriodeGrunnlag grunnlag, OppgittPeriode oppgittPeriode) {
        var ukerReservertForMor = Konfigurasjon.STANDARD.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER,
                grunnlag.getFamiliehendelse());

        return !grunnlag.isSøkerMor() && (Stønadskontotype.FEDREKVOTE.equals(oppgittPeriode.getStønadskontotype())
                || Stønadskontotype.FELLESPERIODE.equals(oppgittPeriode.getStønadskontotype())
                || Stønadskontotype.FORELDREPENGER.equals(oppgittPeriode.getStønadskontotype())) && oppgittPeriode.getFom()
                .isBefore(grunnlag.getFamiliehendelse().plusWeeks(ukerReservertForMor));
    }
}
