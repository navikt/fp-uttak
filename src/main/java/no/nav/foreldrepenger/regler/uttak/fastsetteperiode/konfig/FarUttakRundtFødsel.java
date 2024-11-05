package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;

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

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(RegelGrunnlag grunnlag) {
        return utledFarsPeriodeRundtFødsel(grunnlag.getDatoer(), grunnlag.getKontoer(), grunnlag.getSøknad().getType());
    }

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(Datoer datoer, Kontoer kontoer, Søknadstype søknadstype) {
        if (!kontoer.harSpesialkonto(Spesialkontotype.FAR_RUNDT_FØDSEL) || kontoer.getSpesialkontoTrekkdager(Spesialkontotype.FAR_RUNDT_FØDSEL) == 0
            || !søknadstype.gjelderTerminFødsel()) {
            return Optional.empty();
        }
        return utledFarsPeriodeRundtFødsel(false, søknadstype.gjelderTerminFødsel(), datoer.getFamiliehendelse(), datoer.getTermin());

    }

    public static Optional<LukketPeriode> utledFarsPeriodeRundtFødsel(boolean utenFarUttakRundtFødsel,
                                                                      boolean gjelderFødsel,
                                                                      LocalDate familieHendelseDato,
                                                                      LocalDate terminDato) {
        if (utenFarUttakRundtFødsel || !gjelderFødsel || familieHendelseDato == null) {
            return Optional.empty();
        }
        var farFørTerminFødsel = Konfigurasjon.STANDARD.getParameterHvisAktivVed(Parametertype.FAR_UTTAK_FØR_FØDSEL_TERMIN_UKER, familieHendelseDato)
            .map(Period::ofWeeks)
            .orElse(Period.ZERO);
        var farEtterFødsel = Konfigurasjon.STANDARD.getParameterHvisAktivVed(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, familieHendelseDato)
            .map(Period::ofWeeks)
            .orElse(Period.ZERO);
        if (farFørTerminFødsel.equals(Period.ZERO) && farEtterFødsel.equals(Period.ZERO)) {
            return Optional.empty();
        }
        // Bruker min(Termin, Fødsel) - 2uker
        var farUttakFom = Optional.ofNullable(terminDato)
            .filter(t -> t.isBefore(familieHendelseDato))
            .orElse(familieHendelseDato)
            .minus(farFørTerminFødsel);

        // Bruker fødsel + 6uker
        var farUttakTom = familieHendelseDato.plus(farEtterFødsel).minusDays(1);
        return Optional.of(new LukketPeriode(farUttakFom, farUttakTom));
    }
}
