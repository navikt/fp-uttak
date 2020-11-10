package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.felles.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;

/**
 * Brukes ved når det skal trekkes dager fra periode uten stønadskontotype
 */
final class ValgAvStønadskontoTjeneste {

    private ValgAvStønadskontoTjeneste() {
    }

    static Optional<Stønadskontotype> velgStønadskonto(OppgittPeriode periode,
                                                       RegelGrunnlag regelGrunnlag,
                                                       SaldoUtregning saldoUtregning,
                                                       Konfigurasjon konfigurasjon) {
        if (periode.getStønadskontotype() != null) {
            throw new IllegalArgumentException("Forventet periode uten stønadskontotype");
        }
        if (periode.isUtsettelse()) {
            return velgStønadskontoForUtsettelse(periode, regelGrunnlag, saldoUtregning, konfigurasjon);
        }
        return velgStønadskontoVanligPeriode(periode, regelGrunnlag, saldoUtregning);
    }

    private static Optional<Stønadskontotype> velgStønadskontoVanligPeriode(OppgittPeriode periode,
                                                                            RegelGrunnlag regelGrunnlag,
                                                                            SaldoUtregning saldoUtregning) {
        return velgStønadskonto(periode, regelGrunnlag, saldoUtregning);
    }

    private static Optional<Stønadskontotype> velgStønadskontoForUtsettelse(OppgittPeriode periode,
                                                                            RegelGrunnlag regelGrunnlag,
                                                                            SaldoUtregning saldoUtregning,
                                                                            Konfigurasjon konfigurasjon) {
        if (periodeErPleiepenger(periode, regelGrunnlag, konfigurasjon)) {
            return stønadskontoVedPleiepenger(regelGrunnlag);
        }
        return velgStønadskonto(periode, regelGrunnlag, saldoUtregning);
    }

    private static Optional<Stønadskontotype> velgStønadskonto(OppgittPeriode periode,
                                                               RegelGrunnlag regelGrunnlag,
                                                               SaldoUtregning saldoUtregning) {
        for (Stønadskontotype stønadskontotype : hentSøkerSineKontoer(regelGrunnlag)) {
            if (!erTomForKonto(periode, stønadskontotype, saldoUtregning)) {
                return Optional.of(stønadskontotype);
            }
        }
        return Optional.empty();
    }

    private static boolean periodeErPleiepenger(OppgittPeriode periode, RegelGrunnlag regelGrunnlag, Konfigurasjon konfigurasjon) {
        return PrematurukerUtil.oppfyllerKravTilPrematuruker(regelGrunnlag.getDatoer().getFødsel(),
                regelGrunnlag.getDatoer().getTermin(), konfigurasjon) && periodeErFørTermin(periode, regelGrunnlag)
                && periode.isUtsettelsePga(UtsettelseÅrsak.INNLAGT_BARN);
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
        Set<Stønadskontotype> gyldige = regelGrunnlag.getGyldigeStønadskontotyper();
        if (regelGrunnlag.getBehandling().isSøkerMor() && gyldige.contains(Stønadskontotype.MØDREKVOTE)) {
            søkerSineKonto = Arrays.asList(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE,
                    Stønadskontotype.FORELDREPENGER);
        } else if (gyldige.contains(Stønadskontotype.FEDREKVOTE)) {
            søkerSineKonto = Arrays.asList(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE,
                    Stønadskontotype.FORELDREPENGER);
        } else {
            søkerSineKonto = List.of(Stønadskontotype.FORELDREPENGER);
        }
        return søkerSineKonto;
    }

    private static boolean erTomForKonto(OppgittPeriode periode, Stønadskontotype stønadskontotype, SaldoUtregning saldoUtregning) {
        boolean tomForKonto = true;
        for (var arbeidsforhold : periode.getAktiviteter()) {
            Trekkdager saldo = saldoUtregning.saldoITrekkdager(stønadskontotype, arbeidsforhold);
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
