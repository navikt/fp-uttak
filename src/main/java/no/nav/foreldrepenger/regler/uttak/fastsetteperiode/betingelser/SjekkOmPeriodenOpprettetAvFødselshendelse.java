package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil.helgBlirFredag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenOpprettetAvFødselshendelse.ID)
public class SjekkOmPeriodenOpprettetAvFødselshendelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.4";
    public static final String BESKRIVELSE = "Er perioden opprettet pga justering ved fødselshendelse?";

    public SjekkOmPeriodenOpprettetAvFødselshendelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (!grunnlag.isSøkerMor()) {
            return nei();
        }

        var fødselsdato = grunnlag.getFødselsdato();
        var termindato = grunnlag.getTermindato();
        if (fødselsdato == null || termindato == null) {
            return nei();
        }

        //Ved fødsel etter termin vil det ligge en periode fellesperiode som første periode i uttaket. Sjekker dette for
        //å ikke innvilge msp hvis det ikke har vært noe justering. Best effort
        if (!forbruktFellesperiode(grunnlag)) {
            return nei();
        }

        var antallUkerEtterFødsel = Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, fødselsdato);
        var aktuellPeriode = grunnlag.getAktuellPeriode();

        //Noen caser vil vi innvilge msp selv om bruker bestemt har søkt om å ikke ha uttak i perioden. Burde være veldig få saker
        if (erPeriodeISisteDelAvFørsteUkeneForbeholdtMor(fødselsdato, antallUkerEtterFødsel, aktuellPeriode)) {
            return ja();
        }
        return nei();
    }

    private boolean forbruktFellesperiode(FastsettePeriodeGrunnlag grunnlag) {
        var saldoUtregning = grunnlag.getSaldoUtregning();
        return saldoUtregning.getMaxDager(FELLESPERIODE) > saldoUtregning.saldo(FELLESPERIODE);
    }

    private static boolean erPeriodeISisteDelAvFørsteUkeneForbeholdtMor(LocalDate fødselsdato,
                                                                        Integer antallUkerEtterFødsel,
                                                                        OppgittPeriode aktuellPeriode) {
        return aktuellPeriode.getTom().isEqual(helgBlirFredag(fødselsdato.plusWeeks(antallUkerEtterFødsel).minusDays(1)));
    }
}
