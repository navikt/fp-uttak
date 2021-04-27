package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTapendePeriode.ID)
public class SjekkOmTapendePeriode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.7";
    public static final String BESKRIVELSE = "Har annen part senere søkt om uttak/utsettelse i samme tidsperiode?";

    public SjekkOmTapendePeriode() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return harAnnenpartSenereMottattDato(grunnlag) ? ja() : nei();
    }

    private boolean harAnnenpartSenereMottattDato(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var overlappende = grunnlag.getAnnenPartUttaksperioder()
                .stream()
                .filter(aup -> aktuellPeriode.erOmsluttetAv(aup))
                .filter(aup -> (aup.isInnvilget() && aup.isUtsettelse()) || aup.harUtbetaling())
                .filter(aup -> !aup.isSamtidigUttak())
                .findFirst();
        if (overlappende.isEmpty()) {
            return false;
        }
        var mottattDato = aktuellPeriode.getSenestMottattDato();
        return mottattDato.map(
                md -> {
                    var annenpartsPeriode = overlappende.get();
                    if (annenpartsPeriode.getSenestMottattDato().isEmpty()) {
                        return false;
                    }
                    if (annenpartsPeriode.getSenestMottattDato().get().isEqual(md)) {
                        //Foreldrene har søkt samme dag, bruker siste søknadtidspunkt
                        return grunnlag.getAnnenPartSisteSøknadMottattTidspunkt().isAfter(grunnlag.getSisteSøknadMottattTidspunkt());
                    }

                    return annenpartsPeriode.getSenestMottattDato().get().isAfter(md);
                })
                .orElse(false);
    }

}
