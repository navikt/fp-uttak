package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ValgAvStønadskontoTjeneste.velgStønadskonto;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.SamtidigUttakUtil;

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
                                                         FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var innvilgPeriode = knekkpunktOpt.map(TomKontoKnekkpunkt::dato)
            .map(k -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), k.minusDays(1)))
            .orElse(oppgittPeriode);

        // Vi skal redusere søker i forhold til annenparts uttaksprosent slik at de til sammen har 100% uttaksprosent
        var redusertUttaksprosentPgaSamtidigUttak = SamtidigUttakUtil.kanRedusereUtbetalingsgradForTapende(fastsettePeriodeGrunnlag, regelGrunnlag)
            ? SamtidigUttaksprosent.HUNDRED.subtract(SamtidigUttakUtil.uttaksprosentAnnenpart(fastsettePeriodeGrunnlag))
            : null;

        var aktiviteter = lagAktiviteter(innvilgPeriode, regelresultat, false, redusertUttaksprosentPgaSamtidigUttak);
        var samtidigUttaksprosent = regnSamtidigUttaksprosentMotGradering(innvilgPeriode, redusertUttaksprosentPgaSamtidigUttak);
        var innvilget = new UttakPeriode(innvilgPeriode, Perioderesultattype.INNVILGET, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosent, innvilgPeriode.getStønadskontotype());

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

        var avslått = new UttakPeriode(avslåPeriode, Perioderesultattype.AVSLÅTT, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(),
            lagAktiviteter(avslåPeriode, regelresultat, overlapperInnvilgetAnnenpartsPeriode, null),
            regnSamtidigUttaksprosentMotGradering(avslåPeriode), konto(avslåPeriode).orElse(null));

        if (!overlapperInnvilgetAnnenpartsPeriode && knekkpunktOpt.isPresent()) {
            validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
            var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().dato(), oppgittPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(avslått, etterKnekk);
        } else {
            return RegelResultatBehandlerResultat.utenKnekk(avslått);
        }
    }

    private static SamtidigUttaksprosent regnSamtidigUttaksprosentMotGradering(OppgittPeriode oppgittPeriode) {
        return regnSamtidigUttaksprosentMotGradering(oppgittPeriode, null);
    }

    private static SamtidigUttaksprosent regnSamtidigUttaksprosentMotGradering(OppgittPeriode oppgittPeriode,
                                                                               SamtidigUttaksprosent redusertSamtidigUttaksprosent) {
        if (!oppgittPeriode.erSøktSamtidigUttak() && redusertSamtidigUttaksprosent == null) {
            return null;
        }
        if (redusertSamtidigUttaksprosent != null) {
            return redusertSamtidigUttaksprosent;
        }
        return oppgittPeriode.erSøktGradering()
            ? SamtidigUttaksprosent.HUNDRED.subtract(oppgittPeriode.getArbeidsprosent())
            : oppgittPeriode.getSamtidigUttaksprosent();
    }

    private Optional<Stønadskontotype> konto(OppgittPeriode oppgittPeriode) {
        return oppgittPeriode.getStønadskontotype() != null ? Optional.of(oppgittPeriode.getStønadskontotype()) : utledKonto(oppgittPeriode);
    }

    RegelResultatBehandlerResultat manuellBehandling(OppgittPeriode oppgittPeriode, FastsettePerioderRegelresultat regelresultat) {
        var stønadskontotype = konto(oppgittPeriode);
        var resultat = new UttakPeriode(oppgittPeriode, Perioderesultattype.MANUELL_BEHANDLING, regelresultat.getManuellbehandlingårsak(),
            regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(),
            lagAktiviteter(oppgittPeriode, regelresultat, false, null),
            regnSamtidigUttaksprosentMotGradering(oppgittPeriode), stønadskontotype.orElse(null));
        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private Optional<Stønadskontotype> utledKonto(OppgittPeriode oppgittPeriode) {
        return velgStønadskonto(oppgittPeriode, regelGrunnlag, saldoUtregning);
    }

    private Set<UttakPeriodeAktivitet> lagAktiviteter(OppgittPeriode oppgittPeriode,
                                                      FastsettePerioderRegelresultat regelresultat,
                                                      boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                      SamtidigUttaksprosent redusertUttaksprosent) {
        return oppgittPeriode.getAktiviteter()
            .stream()
            .map(a -> lagAktivitet(a, regelresultat, overlapperMedInnvilgetPeriodeHosAnnenpart, oppgittPeriode, redusertUttaksprosent))
            .collect(Collectors.toSet());
    }

    private UttakPeriodeAktivitet lagAktivitet(AktivitetIdentifikator identifikator,
                                               FastsettePerioderRegelresultat regelresultat,
                                               boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                               OppgittPeriode oppgittPeriode,
                                               SamtidigUttaksprosent redusertUttaksprosent) {
        var søktGradering = oppgittPeriode.erSøktGradering(identifikator);
        var periodeAktivitetResultat = finnPeriodeAktivitetResultat(oppgittPeriode, overlapperMedInnvilgetPeriodeHosAnnenpart, identifikator,
            regelresultat, redusertUttaksprosent);
        return new UttakPeriodeAktivitet(identifikator, periodeAktivitetResultat.utbetalingsgrad(), periodeAktivitetResultat.trekkdager(),
            søktGradering);
    }

    private PeriodeAktivitetResultat finnPeriodeAktivitetResultat(OppgittPeriode oppgittPeriode,
                                                                  boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                                  AktivitetIdentifikator aktivitet,
                                                                  FastsettePerioderRegelresultat regelresultat,
                                                                  SamtidigUttaksprosent redusertUttaksprosent) {
        //Må sjekke saldo her, ved flere arbeidsforhold kan det reglene ha gått til sluttpunkt som trekkes dager selv om ett av arbeidsforholdene er tom
        //På arbeidsforholdet som er tom på konto skal det settes 0 trekkdager
        var stønadskonto = konto(oppgittPeriode);
        var harIgjenTrekkdager = isHarIgjenTrekkdager(oppgittPeriode, aktivitet, regelresultat, stønadskonto.orElse(null));

        var manuellBehandling = manuellBehandling(regelresultat);
        if (overlapperMedInnvilgetPeriodeHosAnnenpart || (!manuellBehandling && !harIgjenTrekkdager)) {
            return new PeriodeAktivitetResultat(Utbetalingsgrad.ZERO, Trekkdager.ZERO);
        }

        var utbetalingsgrad = Utbetalingsgrad.ZERO;
        if (regelresultat.skalUtbetale()) {
            utbetalingsgrad = UtbetalingsgradUtil.beregnUtbetalingsgradFor(oppgittPeriode, aktivitet, redusertUttaksprosent);
        }
        var trekkdager = Trekkdager.ZERO;
        if (regelresultat.trekkDagerFraSaldo() && !(manuellBehandling && stønadskonto.isEmpty())) {
            var graderingInnvilget = regelresultat.getGraderingIkkeInnvilgetÅrsak() == null && oppgittPeriode.erSøktGradering(aktivitet);
            trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(oppgittPeriode, graderingInnvilget, utbetalingsgrad);
        }

        return new PeriodeAktivitetResultat(utbetalingsgrad, trekkdager);
    }

    private boolean isHarIgjenTrekkdager(OppgittPeriode oppgittPeriode,
                                         AktivitetIdentifikator aktivitet,
                                         FastsettePerioderRegelresultat regelresultat,
                                         Stønadskontotype stønadskonto) {
        var nettosaldo = saldoUtregning.nettoSaldoJustertForMinsterett(stønadskonto, aktivitet, oppgittPeriode.kanTrekkeAvMinsterett());
        if (regelresultat.getAvklaringÅrsak() != null && regelresultat.getAvklaringÅrsak().trekkerMinsterett()) {
            var minsterettSaldo = saldoUtregning.restSaldoMinsterett(aktivitet);
            var utenAktivitetskravSaldo = saldoUtregning.restSaldoDagerUtenAktivitetskrav();
            return nettosaldo.merEnn0() && (minsterettSaldo.merEnn0() || utenAktivitetskravSaldo.merEnn0());
        }
        return nettosaldo.merEnn0();
    }

    private boolean manuellBehandling(FastsettePerioderRegelresultat regelresultat) {
        return regelresultat.getUtfallType().equals(UtfallType.MANUELL_BEHANDLING);
    }

    private void validerKnekkpunkt(OppgittPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        if (!uttakPeriode.overlapper(knekkpunkt.dato())) {
            throw new IllegalArgumentException("Knekkpunkt må være i periode. " + knekkpunkt.dato() + " - " + uttakPeriode);
        }
    }

    private record PeriodeAktivitetResultat(Utbetalingsgrad utbetalingsgrad, Trekkdager trekkdager) {
    }
}
