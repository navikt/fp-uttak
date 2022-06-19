package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

public class FarUttakRundtFødsel {

    private FarUttakRundtFødsel() {
        //hindrer instansiering
    }

    public static boolean erKontoRelevant(Stønadskontotype konto) {
        return Objects.equals(Stønadskontotype.FEDREKVOTE, konto);
    }

    public static boolean erPeriodeRelevant(LukketPeriode farRundtFødselIntervall, LukketPeriode periode) {
        return farRundtFødselIntervall != null && periode.erOmsluttetAv(farRundtFødselIntervall);
    }

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        return utledFarsPeriodeRundtFødsel(grunnlag.getDatoer(), grunnlag.getKontoer(), grunnlag.getSøknad().getType(), konfigurasjon);
    }

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(Datoer datoer, Kontoer kontoer, Søknadstype søknadstype, Konfigurasjon konfigurasjon) {
        if (!kontoer.harSpesialkonto(Spesialkontotype.FAR_RUNDT_FØDSEL) || kontoer.getSpesialkontoTrekkdager(Spesialkontotype.FAR_RUNDT_FØDSEL) == 0 || !søknadstype.gjelderTerminFødsel()) {
            return Optional.empty();
        }
        return utledFarsPeriodeRundtFødsel(false, søknadstype.gjelderTerminFødsel(), datoer.getFamiliehendelse(), datoer.getTermin(), konfigurasjon);

    }

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(boolean utenFarUttakRundtFødsel, boolean gjelderFødsel,
                                                                      LocalDate familieHendelseDato, LocalDate terminDato, Konfigurasjon konfigurasjon) {
        if (utenFarUttakRundtFødsel || !gjelderFødsel || familieHendelseDato == null) {
            return Optional.empty();
        }
        var farFørTermin = konfigurasjon.getParameterHvisAktivVed(Parametertype.FAR_UTTAK_FØR_TERMIN_UKER, familieHendelseDato)
                .map(Period::ofWeeks).orElse(Period.ZERO);
        var farEtterFødsel =  konfigurasjon.getParameterHvisAktivVed(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, familieHendelseDato)
                .map(Period::ofWeeks).orElse(Period.ZERO);
        if (farFørTermin.equals(Period.ZERO) && farEtterFødsel.equals(Period.ZERO)) {
            return Optional.empty();
        }
        // Bruker min(Termin-2uker, Fødsel)
        var farUttakFom = Optional.ofNullable(terminDato)
                .filter(d -> d.minus(farFørTermin).isBefore(familieHendelseDato))
                .map(d -> d.minus(farFørTermin))
                .orElse(familieHendelseDato);
        // Bruker fødsel + 6uker
        var farUttakTom = familieHendelseDato.plus(farEtterFødsel);
        return Optional.of(new LukketPeriode(farUttakFom, farUttakTom));
    }
}
