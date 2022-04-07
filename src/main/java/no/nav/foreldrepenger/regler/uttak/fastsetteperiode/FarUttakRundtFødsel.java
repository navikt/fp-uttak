package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

public class FarUttakRundtFødsel {

    private FarUttakRundtFødsel() {
        //hindrer instansiering
    }

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        if (grunnlag.getKontoer().getFarUttakRundtFødselDager() == 0 || !grunnlag.getSøknad().gjelderTerminFødsel()) {
            return Optional.empty();
        }
        // TODO: finne ut hvor strengt dette skal være i fall f < t ller f > t
        var familieHendelseDato = grunnlag.getDatoer().getFamiliehendelse(); // Presedens: omsorg, fødsel, eller termin
        var farFørTermin = Period.ofWeeks(konfigurasjon.getParameter(Parametertype.FAR_UTTAK_FØR_TERMIN_UKER, familieHendelseDato));
        var farEtterFødsel =  Period.ofWeeks(konfigurasjon.getParameter(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, familieHendelseDato));
        if (farFørTermin.equals(Period.ZERO) && farEtterFødsel.equals(Period.ZERO)) {
            return Optional.empty();
        }
        // Bruker min(Termin-2uker, Fødsel)
        var farUttakFom = Optional.ofNullable(grunnlag.getDatoer().getTermin())
                .filter(d -> d.minus(farFørTermin).isBefore(familieHendelseDato))
                .map(d -> d.minus(farFørTermin))
                .orElse(familieHendelseDato);
        // Bruker max(Termin, Fødsel) + 2uker - det er denne som trengs vurdering der F < T eller F < T-2u
        var farUttakTom = Optional.ofNullable(grunnlag.getDatoer().getTermin())
                .filter(d -> d.isAfter(familieHendelseDato))
                .orElse(familieHendelseDato)
                .plus(farEtterFødsel);
        return Optional.of(new LukketPeriode(farUttakFom, farUttakTom));
    }

    static Set<LocalDate> finnKnekkpunkterFarsPeriodeRundtFødsel(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        return utledFarsPeriodeRundtFødsel(grunnlag, konfigurasjon)
                .map(p -> Set.of(p.getFom(), p.getTom()))
                .orElse(Set.of());
    }
}
