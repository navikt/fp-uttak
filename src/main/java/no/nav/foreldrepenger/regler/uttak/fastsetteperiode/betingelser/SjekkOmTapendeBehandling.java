package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTapendeBehandling.ID)
public class SjekkOmTapendeBehandling extends LeafSpecification<FastsettePeriodeGrunnlag> {

    private static final Logger LOG = LoggerFactory.getLogger(SjekkOmTapendeBehandling.class);

    public static final String ID = "FP_VK 30.0.3";
    public static final String BESKRIVELSE = "Er behandlingen en tapende behandling?";

    public SjekkOmTapendeBehandling() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var gammeltResultat = grunnlag.isTapendeBehandling() ? ja() : nei();
        var nyttResultat = nyttResultat(grunnlag);

        if (gammeltResultat.result() != nyttResultat.result()) {
            var aktuellPeriode = grunnlag.getAktuellPeriode();
            var annenpartMottattDato = grunnlag.getAnnenPartUttaksperioder()
                    .stream()
                    .filter(aup -> aktuellPeriode.erOmsluttetAv(aup))
                    .map(aup -> aup.getMottattDato())
                    .findFirst()
                    .orElse(null);
            LOG.info("Endring i sjekk om hvilken forelder som taper for periode {} - {}. Gammelt resultat: {}, N"
                            + "ytt resultat: {}. Mottatt dato søker {}, annenpart {}", aktuellPeriode.getFom(), aktuellPeriode.getTom(),
                    gammeltResultat.result(), nyttResultat.result(), aktuellPeriode.getMottattDato(), annenpartMottattDato);
        }
        return gammeltResultat;
    }

    private SingleEvaluation nyttResultat(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var mottattDato = aktuellPeriode.getMottattDato();
        return mottattDato.map(md -> {
            var overlappendeSomStjeler = grunnlag.getAnnenPartUttaksperioder()
                    .stream()
                    .anyMatch(aup -> stjelerAnnenpart(aktuellPeriode, md, aup));

            return overlappendeSomStjeler ? ja() : nei();
        }).orElse(ja());
    }

    private boolean stjelerAnnenpart(OppgittPeriode aktuellPeriode, LocalDate mottattDato, AnnenpartUttakPeriode aup) {
        return aktuellPeriode.erOmsluttetAv(aup) && aup.getMottattDato().map(md ->  md.isAfter(mottattDato)).orElse(false);
    }
}
