package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;

/**
 * Brukes ved når det skal trekkes dager fra periode uten stønadskontotype
 */
final class ValgAvStønadskontoTjeneste {

    private ValgAvStønadskontoTjeneste() {
    }

    static Optional<Stønadskontotype> velgStønadskonto(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        if (fastsettePeriodeGrunnlag.getAktuellPeriode().getStønadskontotype() != null) {
            throw new IllegalArgumentException("Forventet periode uten stønadskontotype");
        }
        if (fastsettePeriodeGrunnlag.getAktuellPeriode().isUtsettelse()) {
            return velgStønadskontoForUtsettelse(fastsettePeriodeGrunnlag);
        }
        return velgStønadskontoVanligPeriode(fastsettePeriodeGrunnlag);
    }

    private static Optional<Stønadskontotype> velgStønadskontoVanligPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return velg(fastsettePeriodeGrunnlag);
    }

    private static Optional<Stønadskontotype> velgStønadskontoForUtsettelse(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        if (periodeErPleiepenger(fastsettePeriodeGrunnlag)) {
            return stønadskontoVedPleiepenger(fastsettePeriodeGrunnlag);
        }
        return velg(fastsettePeriodeGrunnlag);
    }

    private static Optional<Stønadskontotype> velg(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        for (var stønadskontotype : hentSøkerSineKontoer(fastsettePeriodeGrunnlag)) {
            if (!erTomForKonto(fastsettePeriodeGrunnlag.getAktuellPeriode(), stønadskontotype, fastsettePeriodeGrunnlag.getSaldoUtregning())) {
                return Optional.of(stønadskontotype);
            }
        }
        return Optional.empty();
    }

    private static boolean periodeErPleiepenger(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return PrematurukerUtil.oppfyllerKravTilPrematuruker(fastsettePeriodeGrunnlag.getFødselsdato(), fastsettePeriodeGrunnlag.getTermindato())
            && periodeErFørTermin(fastsettePeriodeGrunnlag) && fastsettePeriodeGrunnlag.getAktuellPeriode().isUtsettelsePga(UtsettelseÅrsak.INNLAGT_BARN);
    }

    private static boolean periodeErFørTermin(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return fastsettePeriodeGrunnlag.getTermindato() != null &&
            fastsettePeriodeGrunnlag.getAktuellPeriode().getTom().isBefore(fastsettePeriodeGrunnlag.getTermindato());
    }

    private static Optional<Stønadskontotype> stønadskontoVedPleiepenger(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        //Trenger ikke å sjekke om tom på konto, ettersom bruker ikke kan gå tom før termin
        if (fastsettePeriodeGrunnlag.getGyldigeStønadskontotyper().contains(Stønadskontotype.FELLESPERIODE)) {
            return Optional.of(Stønadskontotype.FELLESPERIODE);
        }
        if (fastsettePeriodeGrunnlag.getGyldigeStønadskontotyper().contains(Stønadskontotype.FORELDREPENGER)) {
            return Optional.of(Stønadskontotype.FORELDREPENGER);
        }
        throw new IllegalStateException(
            "Trenger enten fellesperiode eller foreldrepenger konto. Kontotyper: " + fastsettePeriodeGrunnlag.getGyldigeStønadskontotyper());
    }

    private static List<Stønadskontotype> hentSøkerSineKontoer(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        final List<Stønadskontotype> søkerSineKonto;
        var gyldige = fastsettePeriodeGrunnlag.getGyldigeStønadskontotyper();
        if (fastsettePeriodeGrunnlag.isSøkerMor() && gyldige.contains(Stønadskontotype.MØDREKVOTE)) {
            søkerSineKonto = Arrays.asList(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
        } else if (gyldige.contains(Stønadskontotype.FEDREKVOTE)) {
            søkerSineKonto = Arrays.asList(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
        } else {
            søkerSineKonto = List.of(Stønadskontotype.FORELDREPENGER);
        }
        return søkerSineKonto;
    }

    private static boolean erTomForKonto(OppgittPeriode periode, Stønadskontotype stønadskontotype, SaldoUtregning saldoUtregning) {
        var tomForKonto = true;
        for (var arbeidsforhold : periode.getAktiviteter()) {
            var saldo = saldoUtregning.nettoSaldoJustertForMinsterett(stønadskontotype, arbeidsforhold, periode.kanTrekkeAvMinsterett());
            if (saldo.merEnn0()) {
                tomForKonto = false;
            } else {
                tomForKonto = true;
                break;
            }
        }
        return tomForKonto;
    }

}
