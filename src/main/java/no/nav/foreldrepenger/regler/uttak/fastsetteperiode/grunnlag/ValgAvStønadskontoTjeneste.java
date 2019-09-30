package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;

/**
 * Brukes ved når det skal trekkes dager fra periode uten stønadskontotype
 */
public final class ValgAvStønadskontoTjeneste {

    private ValgAvStønadskontoTjeneste() {
    }

    public static Optional<Stønadskontotype> velgStønadskonto(UttakPeriode periode,
                                                              RegelGrunnlag regelGrunnlag,
                                                              Trekkdagertilstand trekkdagertilstand,
                                                              Konfigurasjon konfigurasjon) {
        if (!Stønadskontotype.UKJENT.equals(periode.getStønadskontotype())) {
            throw new IllegalArgumentException("Forventet periode uten stønadskontotype");
        }
        if (erUtsettelse(periode)) {
            return velgStønadskontoForUtsettelse((UtsettelsePeriode) periode, regelGrunnlag, trekkdagertilstand, konfigurasjon);
        }
        return velgStønadskontoVanligPeriode(regelGrunnlag, trekkdagertilstand);
    }

    private static Optional<Stønadskontotype> velgStønadskontoVanligPeriode(RegelGrunnlag regelGrunnlag,
                                                                            Trekkdagertilstand trekkdagertilstand) {
        return velgStønadskonto(regelGrunnlag, trekkdagertilstand);
    }

    private static boolean erUtsettelse(UttakPeriode periode) {
        return periode instanceof UtsettelsePeriode;
    }

    private static Optional<Stønadskontotype> velgStønadskontoForUtsettelse(UtsettelsePeriode periode,
                                                                            RegelGrunnlag regelGrunnlag,
                                                                            Trekkdagertilstand trekkdagertilstand,
                                                                            Konfigurasjon konfigurasjon) {
        if (periodeErPleiepenger(periode, regelGrunnlag, konfigurasjon)) {
            return stønadskontoVedPleiepenger(regelGrunnlag);
        }
        return velgStønadskonto(regelGrunnlag, trekkdagertilstand);
    }

    private static Optional<Stønadskontotype> velgStønadskonto(RegelGrunnlag regelGrunnlag,
                                                               Trekkdagertilstand trekkdagertilstand) {
        for (Stønadskontotype stønadskontotype : hentSøkerSineKontoer(regelGrunnlag)) {
            if (!erTomForKonto(stønadskontotype, regelGrunnlag, trekkdagertilstand)) {
                return Optional.of(stønadskontotype);
            }
        }
        return Optional.empty();
    }

    private static boolean periodeErPleiepenger(UtsettelsePeriode periode, RegelGrunnlag regelGrunnlag, Konfigurasjon konfigurasjon) {
        return PrematurukerUtil.oppfyllerKravTilPrematuruker(regelGrunnlag.getDatoer().getFødsel(), regelGrunnlag.getDatoer().getTermin(), konfigurasjon)
                && periodeErFørTermin(periode, regelGrunnlag)
                && periode.getUtsettelseårsaktype().equals(Utsettelseårsaktype.INNLAGT_BARN);
    }

    private static boolean periodeErFørTermin(UtsettelsePeriode periode, RegelGrunnlag regelGrunnlag) {
        return regelGrunnlag.getDatoer().getTermin() != null && periode.getTom().isBefore(regelGrunnlag.getDatoer().getTermin());
    }

    private static Optional<Stønadskontotype> stønadskontoVedPleiepenger(RegelGrunnlag regelGrunnlag) {
        //Trenger ikke å sjekke om tom på konto, ettersom bruker ikke kan gå tom før termin
        if (regelGrunnlag.getGyldigeStønadskontotyper().contains(Stønadskontotype.FELLESPERIODE)) {
            return Optional.of(Stønadskontotype.FELLESPERIODE);
        }
        if (regelGrunnlag.getGyldigeStønadskontotyper().contains(Stønadskontotype.FORELDREPENGER)) {
            return Optional.of(Stønadskontotype.FORELDREPENGER);
        }
        throw new IllegalStateException("Trenger enten fellesperiode eller foreldrepenger konto. Kontotyper: " + regelGrunnlag.getGyldigeStønadskontotyper());
    }

    private static List<Stønadskontotype> hentSøkerSineKontoer(RegelGrunnlag regelGrunnlag) {
        final List<Stønadskontotype> søkerSineKonto;
        Set<Stønadskontotype> gyldige = regelGrunnlag.getGyldigeStønadskontotyper();
        if (regelGrunnlag.getBehandling().getSøkerErMor() && gyldige.contains(Stønadskontotype.MØDREKVOTE)) {
            søkerSineKonto = Arrays.asList(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
        } else if (gyldige.contains(Stønadskontotype.FEDREKVOTE)) {
            søkerSineKonto = Arrays.asList(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
        } else {
            søkerSineKonto = Collections.singletonList(Stønadskontotype.FORELDREPENGER);
        }
        return søkerSineKonto;
    }

    private static boolean erTomForKonto(Stønadskontotype stønadskontotype, RegelGrunnlag regelGrunnlag, Trekkdagertilstand trekkdagertilstand) {
        boolean tomForKonto = false;
        for (AktivitetIdentifikator aktivitet : regelGrunnlag.getKontoer().keySet()) {
            Trekkdager saldo = trekkdagertilstand.saldo(aktivitet, stønadskontotype);
            if (!saldo.merEnn0()) {
                tomForKonto = true;
                break;
            }
        }
        return tomForKonto;
    }
}
