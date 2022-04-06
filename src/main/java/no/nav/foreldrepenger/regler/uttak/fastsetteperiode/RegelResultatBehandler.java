package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ValgAvStønadskontoTjeneste.velgStønadskonto;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;

class RegelResultatBehandler {

    private final SaldoUtregning saldoUtregning;
    private final RegelGrunnlag regelGrunnlag;
    private final Konfigurasjon konfigurasjon;

    RegelResultatBehandler(SaldoUtregning saldoUtregning, RegelGrunnlag regelGrunnlag, Konfigurasjon konfigurasjon) {
        this.saldoUtregning = saldoUtregning;
        this.regelGrunnlag = regelGrunnlag;
        this.konfigurasjon = konfigurasjon;
    }

    RegelResultatBehandlerResultat innvilgAktuellPeriode(OppgittPeriode oppgittPeriode,
                                                         Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                         FastsettePerioderRegelresultat regelresultat) {
        if (knekkpunktOpt.isEmpty()) {
            var resultat = new UttakPeriode(oppgittPeriode, Perioderesultattype.INNVILGET, null,
                    regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(),
                    lagAktiviteter(oppgittPeriode, regelresultat, false),
                    regnSamtidigUttaksprosentMotGradering(oppgittPeriode),
                    oppgittPeriode.getStønadskontotype());

            return RegelResultatBehandlerResultat.utenKnekk(resultat);
        }
        validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
        var oppgittPeriodeFørKnekk = oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(),
                knekkpunktOpt.get().getDato().minusDays(1));
        var førKnekk = new UttakPeriode(oppgittPeriodeFørKnekk,
                Perioderesultattype.INNVILGET, null, regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(),
                lagAktiviteter(oppgittPeriodeFørKnekk, regelresultat, false),
                regnSamtidigUttaksprosentMotGradering(oppgittPeriodeFørKnekk),
                oppgittPeriodeFørKnekk.getStønadskontotype());
        var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().getDato(), oppgittPeriode.getTom());
        return RegelResultatBehandlerResultat.medKnekk(førKnekk, etterKnekk);
    }

    RegelResultatBehandlerResultat avslåAktuellPeriode(OppgittPeriode oppgittPeriode,
                                                       FastsettePerioderRegelresultat regelresultat,
                                                       Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                       boolean overlapperInnvilgetAnnenpartsPeriode) {
        if (!overlapperInnvilgetAnnenpartsPeriode && knekkpunktOpt.isPresent()) {
            validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
            var oppgittPeriodeFørKnekk = oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(),
                    knekkpunktOpt.get().getDato().minusDays(1));
            var resultat = new UttakPeriode(oppgittPeriodeFørKnekk,
                    Perioderesultattype.AVSLÅTT, null, regelresultat.getAvklaringÅrsak(),
                    regelresultat.getGraderingIkkeInnvilgetÅrsak(),
                    lagAktiviteter(oppgittPeriodeFørKnekk, regelresultat, overlapperInnvilgetAnnenpartsPeriode),
                    regnSamtidigUttaksprosentMotGradering(oppgittPeriodeFørKnekk),
                    konto(oppgittPeriodeFørKnekk).orElse(null));
            var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().getDato(), oppgittPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(resultat, etterKnekk);
        }
        var resultat = new UttakPeriode(oppgittPeriode, Perioderesultattype.AVSLÅTT, null,
                regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(),
                lagAktiviteter(oppgittPeriode, regelresultat, overlapperInnvilgetAnnenpartsPeriode),
                regnSamtidigUttaksprosentMotGradering(oppgittPeriode),
                konto(oppgittPeriode).orElse(null));
        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private static SamtidigUttaksprosent regnSamtidigUttaksprosentMotGradering(OppgittPeriode oppgittPeriode) {
        if (!oppgittPeriode.erSøktSamtidigUttak()) {
            return null;
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
                regelresultat.getGraderingIkkeInnvilgetÅrsak(), lagAktiviteter(oppgittPeriode, regelresultat, false),
                regnSamtidigUttaksprosentMotGradering(oppgittPeriode),
                stønadskontotype.orElse(null));
        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private Optional<Stønadskontotype> utledKonto(OppgittPeriode oppgittPeriode) {
        return velgStønadskonto(oppgittPeriode, regelGrunnlag, saldoUtregning, konfigurasjon);
    }

    private Set<UttakPeriodeAktivitet> lagAktiviteter(OppgittPeriode oppgittPeriode,
                                                      FastsettePerioderRegelresultat regelresultat,
                                                      boolean overlapperMedInnvilgetPeriodeHosAnnenpart) {
        return oppgittPeriode.getAktiviteter()
                .stream()
                .map(a -> lagAktivitet(a, regelresultat, overlapperMedInnvilgetPeriodeHosAnnenpart, oppgittPeriode))
                .collect(Collectors.toSet());
    }

    private UttakPeriodeAktivitet lagAktivitet(AktivitetIdentifikator identifikator,
                                               FastsettePerioderRegelresultat regelresultat,
                                               boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                               OppgittPeriode oppgittPeriode) {
        var søktGradering = oppgittPeriode.erSøktGradering(identifikator);
        var periodeAktivitetResultat = finnPeriodeAktivitetResultat(oppgittPeriode, overlapperMedInnvilgetPeriodeHosAnnenpart,
                identifikator, regelresultat);
        return new UttakPeriodeAktivitet(identifikator, periodeAktivitetResultat.utbetalingsgrad(),
                periodeAktivitetResultat.trekkdager(), søktGradering);
    }

    private PeriodeAktivitetResultat finnPeriodeAktivitetResultat(OppgittPeriode oppgittPeriode,
                                                                  boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                                  AktivitetIdentifikator identifikator,
                                                                  FastsettePerioderRegelresultat regelresultat) {
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
            var utbetalingsgradUtregning = bestemUtbetalingsgradUtregning(oppgittPeriode, identifikator);
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
                        oppgittPeriode.getArbeidsprosent(), regnSamtidigUttaksprosentMotGradering(oppgittPeriode));
            }
        }
        return new PeriodeAktivitetResultat(utbetalingsgrad, trekkdager);
    }

    private boolean manuellBehandling(FastsettePerioderRegelresultat regelresultat) {
        return regelresultat.getUtfallType().equals(UtfallType.MANUELL_BEHANDLING);
    }

    private void validerKnekkpunkt(OppgittPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        if (!uttakPeriode.overlapper(knekkpunkt.getDato())) {
            throw new IllegalArgumentException("Knekkpunkt må være i periode. " + knekkpunkt.getDato() + " - " + uttakPeriode);
        }
    }

    private UtbetalingsgradUtregning bestemUtbetalingsgradUtregning(OppgittPeriode oppgittPeriode, AktivitetIdentifikator aktivitet) {
        if (oppgittPeriode.erSøktGradering(aktivitet)) {
            return new UtbetalingsgradMedGraderingUtregning(oppgittPeriode, aktivitet);
        }
        var samtidigUttaksprosent = regnSamtidigUttaksprosentMotGradering(oppgittPeriode);
        if (samtidigUttaksprosent != null) {
            return new UtbetalingsgradSamtidigUttakUtregning(samtidigUttaksprosent, oppgittPeriode.getArbeidsprosent());
        }
        return new UtbetalingsgradUtenGraderingUtregning();
    }

    private static record PeriodeAktivitetResultat(Utbetalingsgrad utbetalingsgrad, Trekkdager trekkdager) {
    }
}
