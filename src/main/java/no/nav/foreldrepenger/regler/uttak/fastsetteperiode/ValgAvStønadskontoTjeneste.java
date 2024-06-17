package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
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

    static Optional<Stønadskontotype> velgStønadskonto(OppgittPeriode periode, RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning) {
        if (periode.getStønadskontotype() != null) {
            throw new IllegalArgumentException("Forventet periode uten stønadskontotype");
        }
        if (periode.isUtsettelse()) {
            return velgStønadskontoForUtsettelse(periode, regelGrunnlag, saldoUtregning);
        }
        return velgStønadskontoVanligPeriode(periode, regelGrunnlag, saldoUtregning);
    }

    private static Optional<Stønadskontotype> velgStønadskontoVanligPeriode(OppgittPeriode periode,
                                                                            RegelGrunnlag regelGrunnlag,
                                                                            SaldoUtregning saldoUtregning) {
        return velg(periode, regelGrunnlag, saldoUtregning);
    }

    private static Optional<Stønadskontotype> velgStønadskontoForUtsettelse(OppgittPeriode periode,
                                                                            RegelGrunnlag regelGrunnlag,
                                                                            SaldoUtregning saldoUtregning) {
        if (periodeErPleiepenger(periode, regelGrunnlag)) {
            return stønadskontoVedPleiepenger(regelGrunnlag);
        }
        return velg(periode, regelGrunnlag, saldoUtregning);
    }

    private static Optional<Stønadskontotype> velg(OppgittPeriode periode, RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning) {
        for (var stønadskontotype : hentSøkerSineKontoer(regelGrunnlag)) {
            if (!erTomForKonto(periode, stønadskontotype, saldoUtregning)) {
                return Optional.of(stønadskontotype);
            }
        }
        return Optional.empty();
    }

    private static boolean periodeErPleiepenger(OppgittPeriode periode, RegelGrunnlag regelGrunnlag) {
        return PrematurukerUtil.oppfyllerKravTilPrematuruker(regelGrunnlag.getDatoer().getFødsel(), regelGrunnlag.getDatoer().getTermin())
            && periodeErFørTermin(periode, regelGrunnlag) && periode.isUtsettelsePga(UtsettelseÅrsak.INNLAGT_BARN);
    }

    private static boolean periodeErFørTermin(OppgittPeriode periode, RegelGrunnlag regelGrunnlag) {
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
        throw new IllegalStateException(
            "Trenger enten fellesperiode eller foreldrepenger konto. Kontotyper: " + regelGrunnlag.getGyldigeStønadskontotyper());
    }

    private static List<Stønadskontotype> hentSøkerSineKontoer(RegelGrunnlag regelGrunnlag) {
        final List<Stønadskontotype> søkerSineKonto;
        var gyldige = regelGrunnlag.getGyldigeStønadskontotyper();
        if (regelGrunnlag.getBehandling().isSøkerMor() && gyldige.contains(Stønadskontotype.MØDREKVOTE)) {
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
