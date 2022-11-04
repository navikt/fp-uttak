package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ValgAvStønadskontoTjeneste.velgStønadskonto;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;

class RegelResultatBehandler {

    private final SaldoUtregning saldoUtregning;
    private final RegelGrunnlag regelGrunnlag;

    RegelResultatBehandler(SaldoUtregning saldoUtregning, RegelGrunnlag regelGrunnlag) {
        this.saldoUtregning = saldoUtregning;
        this.regelGrunnlag = regelGrunnlag;
    }

    RegelResultatBehandlerResultat innvilgAktuellPeriode(OppgittPeriode oppgittPeriode,
                                                         Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                         FastsettePerioderRegelresultat regelresultat,
                                                         SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        var innvilgPeriode = knekkpunktOpt.map(TomKontoKnekkpunkt::dato)
            .map(k -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), k.minusDays(1)))
            .orElse(oppgittPeriode);

        var innvilget = new UttakPeriode(innvilgPeriode, Perioderesultattype.INNVILGET, null,
            regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(),
            lagAktiviteter(innvilgPeriode, regelresultat, false, annenpartSamtidigUttaksprosent),
            regnSamtidigUttaksprosentMotGradering(innvilgPeriode, annenpartSamtidigUttaksprosent),
            innvilgPeriode.getStønadskontotype());

        if (knekkpunktOpt.isEmpty()) {
            return RegelResultatBehandlerResultat.utenKnekk(innvilget);
        } else {
            validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
            var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().dato(), oppgittPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(innvilget, etterKnekk);
        }
    }

    RegelResultatBehandlerResultat avslåAktuellPeriode(OppgittPeriode oppgittPeriode,
                                                       FastsettePerioderRegelresultat regelresultat,
                                                       Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                       boolean overlapperInnvilgetAnnenpartsPeriode) {
        var avslåPeriode = knekkpunktOpt.map(TomKontoKnekkpunkt::dato)
            .filter(d -> !overlapperInnvilgetAnnenpartsPeriode)
            .map(knekkdato -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), knekkdato.minusDays(1)))
            .orElse(oppgittPeriode);

        var avslått = new UttakPeriode(avslåPeriode, Perioderesultattype.AVSLÅTT, null,
            regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(),
            lagAktiviteter(avslåPeriode, regelresultat, overlapperInnvilgetAnnenpartsPeriode, SamtidigUttaksprosent.ZERO),
            regnSamtidigUttaksprosentMotGradering(avslåPeriode, SamtidigUttaksprosent.ZERO),
            konto(avslåPeriode).orElse(null));

        if (!overlapperInnvilgetAnnenpartsPeriode && knekkpunktOpt.isPresent()) {
            validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
            var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().dato(), oppgittPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(avslått, etterKnekk);
        } else {
            return RegelResultatBehandlerResultat.utenKnekk(avslått);
        }
    }

    private static SamtidigUttaksprosent regnSamtidigUttaksprosentMotGradering(OppgittPeriode oppgittPeriode, SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        if (!oppgittPeriode.erSøktSamtidigUttak() && !annenpartSamtidigUttaksprosent.merEnn0()) {
            return null;
        }
        if (annenpartSamtidigUttaksprosent.merEnn0()) {
            return SamtidigUttaksprosent.HUNDRED.subtract(annenpartSamtidigUttaksprosent);
        }
        return oppgittPeriode.erSøktGradering() ? SamtidigUttaksprosent.HUNDRED.subtract(oppgittPeriode.getArbeidsprosent()) : oppgittPeriode.getSamtidigUttaksprosent();
    }

    private Optional<Stønadskontotype> konto(OppgittPeriode oppgittPeriode) {
        return oppgittPeriode.getStønadskontotype() != null ? Optional.of(oppgittPeriode.getStønadskontotype()) : utledKonto(
                oppgittPeriode);
    }

    RegelResultatBehandlerResultat manuellBehandling(OppgittPeriode oppgittPeriode, FastsettePerioderRegelresultat regelresultat) {
        var stønadskontotype = konto(oppgittPeriode);
        var resultat = new UttakPeriode(oppgittPeriode, Perioderesultattype.MANUELL_BEHANDLING,
                regelresultat.getManuellbehandlingårsak(), regelresultat.getAvklaringÅrsak(),
                regelresultat.getGraderingIkkeInnvilgetÅrsak(),
                lagAktiviteter(oppgittPeriode, regelresultat, false, SamtidigUttaksprosent.ZERO),
                regnSamtidigUttaksprosentMotGradering(oppgittPeriode, SamtidigUttaksprosent.ZERO),
                stønadskontotype.orElse(null));
        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private Optional<Stønadskontotype> utledKonto(OppgittPeriode oppgittPeriode) {
        return velgStønadskonto(oppgittPeriode, regelGrunnlag, saldoUtregning);
    }

    private Set<UttakPeriodeAktivitet> lagAktiviteter(OppgittPeriode oppgittPeriode,
                                                      FastsettePerioderRegelresultat regelresultat,
                                                      boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                      SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        return oppgittPeriode.getAktiviteter()
                .stream()
                .map(a -> lagAktivitet(a, regelresultat, overlapperMedInnvilgetPeriodeHosAnnenpart, oppgittPeriode, annenpartSamtidigUttaksprosent))
                .collect(Collectors.toSet());
    }

    private UttakPeriodeAktivitet lagAktivitet(AktivitetIdentifikator identifikator,
                                               FastsettePerioderRegelresultat regelresultat,
                                               boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                               OppgittPeriode oppgittPeriode,
                                               SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        var søktGradering = oppgittPeriode.erSøktGradering(identifikator);
        var periodeAktivitetResultat = finnPeriodeAktivitetResultat(oppgittPeriode, overlapperMedInnvilgetPeriodeHosAnnenpart,
                identifikator, regelresultat, annenpartSamtidigUttaksprosent);
        return new UttakPeriodeAktivitet(identifikator, periodeAktivitetResultat.utbetalingsgrad(),
                periodeAktivitetResultat.trekkdager(), søktGradering);
    }

    private PeriodeAktivitetResultat finnPeriodeAktivitetResultat(OppgittPeriode oppgittPeriode,
                                                                  boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                                  AktivitetIdentifikator identifikator,
                                                                  FastsettePerioderRegelresultat regelresultat,
                                                                  SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        //Må sjekke saldo her, ved flere arbeidsforhold kan det reglene ha gått til sluttpunkt som trekkes dager selv om ett av arbeidsforholdene er tom
        //På arbeidsforholdet som er tom på konto skal det settes 0 trekkdager
        var stønadskonto = konto(oppgittPeriode);
        var nettosaldo = saldoUtregning.nettoSaldoJustertForMinsterett(stønadskonto.orElse(null), identifikator, oppgittPeriode.kanTrekkeAvMinsterett());
        var harIgjenTrekkdager = nettosaldo.merEnn0();

        var manuellBehandling = manuellBehandling(regelresultat);
        if (overlapperMedInnvilgetPeriodeHosAnnenpart || (!manuellBehandling && !harIgjenTrekkdager)) {
            return new PeriodeAktivitetResultat(Utbetalingsgrad.ZERO, Trekkdager.ZERO);
        }

        var utbetalingsgrad = Utbetalingsgrad.ZERO;
        if (regelresultat.skalUtbetale()) {
            var utbetalingsgradUtregning = bestemUtbetalingsgradUtregning(oppgittPeriode, identifikator, annenpartSamtidigUttaksprosent);
            utbetalingsgrad = utbetalingsgradUtregning.resultat();
        }
        var trekkdager = Trekkdager.ZERO;
        if (regelresultat.trekkDagerFraSaldo()) {
            if (manuellBehandling && stønadskonto.isEmpty()) {
                trekkdager = Trekkdager.ZERO;
            } else {
                var graderingInnvilget =
                        regelresultat.getGraderingIkkeInnvilgetÅrsak() == null && oppgittPeriode.erSøktGradering(identifikator);
                trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(oppgittPeriode, graderingInnvilget,
                        oppgittPeriode.getArbeidsprosent(), regnSamtidigUttaksprosentMotGradering(oppgittPeriode, annenpartSamtidigUttaksprosent));
            }
        }
        return new PeriodeAktivitetResultat(utbetalingsgrad, trekkdager);
    }

    private boolean manuellBehandling(FastsettePerioderRegelresultat regelresultat) {
        return regelresultat.getUtfallType().equals(UtfallType.MANUELL_BEHANDLING);
    }

    private void validerKnekkpunkt(OppgittPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        if (!uttakPeriode.overlapper(knekkpunkt.dato())) {
            throw new IllegalArgumentException("Knekkpunkt må være i periode. " + knekkpunkt.dato() + " - " + uttakPeriode);
        }
    }

    private UtbetalingsgradUtregning bestemUtbetalingsgradUtregning(OppgittPeriode oppgittPeriode, AktivitetIdentifikator aktivitet, SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        if (oppgittPeriode.erSøktGradering(aktivitet)) {
            return new UtbetalingsgradMedGraderingUtregning(oppgittPeriode, aktivitet, annenpartSamtidigUttaksprosent);
        }
        var samtidigUttaksprosent = regnSamtidigUttaksprosentMotGradering(oppgittPeriode, annenpartSamtidigUttaksprosent);
        if (samtidigUttaksprosent != null) {
            return new UtbetalingsgradSamtidigUttakUtregning(samtidigUttaksprosent, oppgittPeriode.getArbeidsprosent(), annenpartSamtidigUttaksprosent);
        }
        return new UtbetalingsgradUtenGraderingUtregning(annenpartSamtidigUttaksprosent);
    }

    private record PeriodeAktivitetResultat(Utbetalingsgrad utbetalingsgrad, Trekkdager trekkdager) {
    }
}
