package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ValgAvStønadskontoTjeneste.velgStønadskonto;

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

    RegelResultatBehandlerResultat innvilgAktuellPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                         FastsettePerioderRegelresultat regelresultat,
                                                         Optional<TomKontoKnekkpunkt> knekkpunktOpt) {
        var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        var innvilgPeriode = knekkpunktOpt
            .map(TomKontoKnekkpunkt::dato)
            .map(k -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), k.minusDays(1)))
            .orElse(oppgittPeriode);

        // Vi skal redusere søker i forhold til annenparts uttaksprosent slik at de til sammen har 100% uttaksprosent
        // TODO: Sjekke via nare? Nå er det 2 sannheter
        var redusertUttaksprosentPgaSamtidigUttakMedSamletUttak100 = SamtidigUttakUtil.kanRedusereUtbetalingsgradForTapende(fastsettePeriodeGrunnlag)
            ? SamtidigUttaksprosent.HUNDRED.subtract(SamtidigUttakUtil.uttaksprosentAnnenpart(fastsettePeriodeGrunnlag))
            : null;

        var aktiviteter = lagAktiveteter(innvilgPeriode, regelresultat, redusertUttaksprosentPgaSamtidigUttakMedSamletUttak100);
        var samtidigUttaksprosent = samtidigUttaksprosentFra(fastsettePeriodeGrunnlag, aktiviteter);
        var innvilget = new UttakPeriode(innvilgPeriode, Perioderesultattype.INNVILGET, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosent, innvilgPeriode.getStønadskontotype());

        if (knekkpunktOpt.isEmpty()) {
            return RegelResultatBehandlerResultat.utenKnekk(innvilget);
        } else {
            validerKnekkpunkt(innvilgPeriode, knekkpunktOpt.get());
            var etterKnekk = innvilgPeriode.kopiMedNyPeriode(knekkpunktOpt.get().dato(), innvilgPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(innvilget, etterKnekk);
        }
    }

    RegelResultatBehandlerResultat avslåAktuellPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                       FastsettePerioderRegelresultat regelresultat,
                                                       Optional<TomKontoKnekkpunkt> knekkpunktOpt) {
        var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        var overlapperInnvilgetAnnenpartsPeriode = overlapperMedInnvilgetAnnenpartsPeriode(fastsettePeriodeGrunnlag);
        var avslåPeriode = knekkpunktOpt.map(TomKontoKnekkpunkt::dato)
            .filter(d -> !overlapperInnvilgetAnnenpartsPeriode)
            .map(knekkdato -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), knekkdato.minusDays(1)))
            .orElse(oppgittPeriode);

        var aktiviteter = overlapperInnvilgetAnnenpartsPeriode
            ? lagAktiveteter(avslåPeriode, regelresultat, null)
            : lagAktiviteterUtenTrekkOgUtbetaling(avslåPeriode);

        var avslått = new UttakPeriode(avslåPeriode, Perioderesultattype.AVSLÅTT, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosentFra(fastsettePeriodeGrunnlag, aktiviteter),
            konto(avslåPeriode).orElse(null));

        if (!overlapperInnvilgetAnnenpartsPeriode && knekkpunktOpt.isPresent()) {
            validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
            var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().dato(), oppgittPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(avslått, etterKnekk);
        } else {
            return RegelResultatBehandlerResultat.utenKnekk(avslått);
        }
    }

    RegelResultatBehandlerResultat manuellBehandling(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag, FastsettePerioderRegelresultat regelresultat) {
        var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        var stønadskontotype = konto(oppgittPeriode);
        var aktiviteter = lagAktiveteter(oppgittPeriode, regelresultat,null);
        var resultat = new UttakPeriode(oppgittPeriode, Perioderesultattype.MANUELL_BEHANDLING, regelresultat.getManuellbehandlingårsak(),
            regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter,
            samtidigUttaksprosentFra(fastsettePeriodeGrunnlag, aktiviteter), stønadskontotype.orElse(null));
        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private static SamtidigUttaksprosent samtidigUttaksprosentFra(FastsettePeriodeGrunnlag grunnlag, Set<UttakPeriodeAktivitet> aktiviteter) {
        if (!SamtidigUttakUtil.søktSamtidigUttakForPeriode(grunnlag)) {
            return null;
        }

        var utbetalingsgrad = aktiviteter.stream()
            .map(UttakPeriodeAktivitet::getUtbetalingsgrad)
            .max(Utbetalingsgrad::compareTo)
            .orElseThrow();

        if (utbetalingsgrad.equals(Utbetalingsgrad.ZERO)) {
            return grunnlag.getAktuellPeriode().getSamtidigUttaksprosent();
        }

        return new SamtidigUttaksprosent(utbetalingsgrad.decimalValue());
    }

    private static boolean overlapperMedInnvilgetAnnenpartsPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        return fastsettePeriodeGrunnlag.getAnnenPartUttaksperioder().stream()
            .anyMatch(annenpartsPeriode -> annenpartsPeriode.overlapper(oppgittPeriode) && annenpartsPeriode.isInnvilget());
    }

    private Optional<Stønadskontotype> konto(OppgittPeriode oppgittPeriode) {
        return oppgittPeriode.getStønadskontotype() != null ? Optional.of(oppgittPeriode.getStønadskontotype()) : utledKonto(oppgittPeriode);
    }

    private Optional<Stønadskontotype> utledKonto(OppgittPeriode oppgittPeriode) {
        return velgStønadskonto(oppgittPeriode, regelGrunnlag, saldoUtregning);
    }

    private static Set<UttakPeriodeAktivitet> lagAktiviteterUtenTrekkOgUtbetaling(OppgittPeriode oppgittPeriode) {
        return oppgittPeriode.getAktiviteter().stream()
            .map(a -> lagAktiviteterUtenTrekkOgUtbetaling(a, oppgittPeriode))
            .collect(Collectors.toSet());
    }

    private static UttakPeriodeAktivitet lagAktiviteterUtenTrekkOgUtbetaling(AktivitetIdentifikator identifikator, OppgittPeriode oppgittPeriode) {
        return new UttakPeriodeAktivitet(identifikator, Utbetalingsgrad.ZERO, Trekkdager.ZERO, oppgittPeriode.erSøktGradering(identifikator));
    }

    private Set<UttakPeriodeAktivitet> lagAktiveteter(OppgittPeriode oppgittPeriode,
                                                      FastsettePerioderRegelresultat regelresultat,
                                                      SamtidigUttaksprosent avgrensetUttaksprosentForÅOppnåSamtidigUttak100) {
        return oppgittPeriode.getAktiviteter().stream()
            .map(a -> lagAktivitet(a, regelresultat, oppgittPeriode, avgrensetUttaksprosentForÅOppnåSamtidigUttak100))
            .collect(Collectors.toSet());
    }


    private UttakPeriodeAktivitet lagAktivitet(AktivitetIdentifikator identifikator,
                                               FastsettePerioderRegelresultat regelresultat,
                                               OppgittPeriode oppgittPeriode,
                                               SamtidigUttaksprosent avgrensetUttaksprosentForÅOppnåSamtidigUttak100) {
        var søktGradering = oppgittPeriode.erSøktGradering(identifikator);
        var periodeAktivitetResultat = finnPeriodeAktivitetResultat(oppgittPeriode, identifikator, regelresultat, avgrensetUttaksprosentForÅOppnåSamtidigUttak100);
        return new UttakPeriodeAktivitet(identifikator, periodeAktivitetResultat.utbetalingsgrad(), periodeAktivitetResultat.trekkdager(), søktGradering);
    }

    private PeriodeAktivitetResultat finnPeriodeAktivitetResultat(OppgittPeriode oppgittPeriode,
                                                                  AktivitetIdentifikator aktivitet,
                                                                  FastsettePerioderRegelresultat regelresultat,
                                                                  SamtidigUttaksprosent avgrensetUttaksprosentForÅOppnåSamtidigUttak100) {
        //Må sjekke saldo her, ved flere arbeidsforhold kan det reglene ha gått til sluttpunkt som trekkes dager selv om ett av arbeidsforholdene er tom
        //På arbeidsforholdet som er tom på konto skal det settes 0 trekkdager
        var stønadskonto = konto(oppgittPeriode);
        var harIgjenTrekkdager = isHarIgjenTrekkdager(oppgittPeriode, aktivitet, regelresultat, stønadskonto.orElse(null));

        var manuellBehandling = manuellBehandling(regelresultat);
        if (!manuellBehandling && !harIgjenTrekkdager) {
            return new PeriodeAktivitetResultat(Utbetalingsgrad.ZERO, Trekkdager.ZERO);
        }

        var utbetalingsgrad = regelresultat.skalUtbetale()
            ? UtbetalingsgradUtil.beregnUtbetalingsgradFor(oppgittPeriode, aktivitet, avgrensetUttaksprosentForÅOppnåSamtidigUttak100)
            : Utbetalingsgrad.ZERO;

        var trekkdager = regelresultat.trekkDagerFraSaldo() && !(manuellBehandling && stønadskonto.isEmpty())
            ? TrekkdagerUtregningUtil.beregnTrekkdagerFor(oppgittPeriode, aktivitet, utbetalingsgrad, regelresultat.skalUtbetale(), regelresultat.getGraderingIkkeInnvilgetÅrsak())
            : Trekkdager.ZERO;

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
