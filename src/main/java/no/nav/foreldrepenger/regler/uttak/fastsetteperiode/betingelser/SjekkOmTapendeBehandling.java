package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
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
        var gammeltResultat = gammeltResultat(grunnlag);
        var nyttResultat = nyttResultat(grunnlag);

        if (gammeltResultat != nyttResultat) {
            var aktuellPeriode = grunnlag.getAktuellPeriode();
            var annenpartMottattDato = grunnlag.getAnnenPartUttaksperioder()
                    .stream()
                    .filter(aup -> aktuellPeriode.erOmsluttetAv(aup))
                    .map(aup -> aup.getMottattDato())
                    .findFirst()
                    .orElse(null);
            LOG.info("Endring i sjekk om hvilken forelder som taper for periode {} - {}. Gammelt resultat: {}, N"
                            + "ytt resultat: {}. Mottatt dato søker {}, annenpart {}", aktuellPeriode.getFom(), aktuellPeriode.getTom(),
                    gammeltResultat, nyttResultat, aktuellPeriode.getMottattDato(), annenpartMottattDato);
        }
        return grunnlag.isTapendeBehandling() ? ja() : nei();
    }

    private boolean gammeltResultat(FastsettePeriodeGrunnlag grunnlag) {
        if (!grunnlag.isTapendeBehandling()) {
            return false;
        }
        return grunnlag.getAnnenPartUttaksperioder()
                .stream()
                .anyMatch(aup -> grunnlag.getAktuellPeriode().erOmsluttetAv(aup) && ((aup.isInnvilget() && aup.isUtsettelse())
                        || aup.harUtbetaling()) && !aup.isSamtidigUttak());
    }

    private boolean nyttResultat(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.getAnnenPartUttaksperioder().isEmpty()) {
            return false;
        }
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var mottattDato = aktuellPeriode.getMottattDato();
        return mottattDato.map(md -> {
            var overlappende = grunnlag.getAnnenPartUttaksperioder()
                    .stream()
                    .filter(aup -> aktuellPeriode.erOmsluttetAv(aup))
                    .filter(aup -> (aup.isInnvilget() && aup.isUtsettelse()) || aup.harUtbetaling())
                    .filter(aup -> !aup.isSamtidigUttak())
                    .findFirst();
            if (overlappende.isEmpty()) {
                return false;
            }
            return overlappende.get().getMottattDato().map(aupMottattDato -> aupMottattDato.isAfter(md)).orElse(false);
        }).orElse(true);
    }

}
