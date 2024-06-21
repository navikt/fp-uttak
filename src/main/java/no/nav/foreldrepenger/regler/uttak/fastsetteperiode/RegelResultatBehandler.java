package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoIdentifiserer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.SamtidigUttakUtil;

class RegelResultatBehandler {

    // TODO: Rydd opp i bruk fields vs statisk implementasjon. Nå er det en blanding som en helst vil unngå
    private final SaldoUtregning saldoUtregning;
    private final RegelGrunnlag regelGrunnlag;

    RegelResultatBehandler(SaldoUtregning saldoUtregning, RegelGrunnlag regelGrunnlag) {
        this.saldoUtregning = saldoUtregning;
        this.regelGrunnlag = regelGrunnlag;
    }

    RegelResultatBehandlerResultat behandleRegelResultatForPeriode(FastsettePeriodeGrunnlag grunnlag, FastsettePerioderRegelresultat regelresultat) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var knekkpunktOpt = finnKnekkpunkt(aktuellPeriode, regelGrunnlag, grunnlag.getSaldoUtregning(), regelresultat,
            grunnlag.periodeFarRundtFødsel().orElse(null));
        if (knekkpunktOpt.isPresent()) {
            var knekkpunkt = knekkpunktOpt.get().dato();
            var periodeFørKnekk = aktuellPeriode.kopiMedNyPeriode(aktuellPeriode.getFom(), knekkpunkt.minusDays(1));
            var periodeEtterKnekk = aktuellPeriode.kopiMedNyPeriode(knekkpunkt, aktuellPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(behandleResultatForPeriode(periodeFørKnekk, grunnlag, regelresultat), periodeEtterKnekk);
        } else {
            return RegelResultatBehandlerResultat.utenKnekk(behandleResultatForPeriode(aktuellPeriode, grunnlag, regelresultat));
        }
    }

    private Optional<TomKontoKnekkpunkt> finnKnekkpunkt(OppgittPeriode aktuellPeriode,
                                                        RegelGrunnlag regelGrunnlag,
                                                        SaldoUtregning saldoUtregning,
                                                        FastsettePerioderRegelresultat regelresultat,
                                                        LukketPeriode farRundtFødselIntervall) {
        if (regelresultat.getUtfallType().equals(UtfallType.MANUELL_BEHANDLING)) {
            return Optional.empty();
        }
        if (Stønadskontotype.FORELDREPENGER_FØR_FØDSEL.equals(aktuellPeriode.getStønadskontotype())) {
            return Optional.empty();
        }
        var stønadskontotype = utledKonto(aktuellPeriode, regelGrunnlag, saldoUtregning);
        var startdatoNesteStønadsperiode = regelGrunnlag.getDatoer().getStartdatoNesteStønadsperiode().orElse(null);
        return TomKontoIdentifiserer.identifiser(aktuellPeriode, new ArrayList<>(aktuellPeriode.getAktiviteter()), saldoUtregning,
            stønadskontotype.orElse(null), farRundtFødselIntervall, startdatoNesteStønadsperiode, regelresultat.trekkDagerFraSaldo(),
            regelresultat.getAvklaringÅrsak(), regelresultat.getUtfallType());
    }

    private UttakPeriode behandleResultatForPeriode(OppgittPeriode periode,
                                            FastsettePeriodeGrunnlag grunnlag,
                                            FastsettePerioderRegelresultat regelresultat) {
        var redusertUttaksprosentPgaSamtidigUttakMedSamletUttak100 = SamtidigUttakUtil.kanRedusereUtbetalingsgradForTapende(grunnlag)
            ? SamtidigUttaksprosent.HUNDRED.subtract(SamtidigUttakUtil.uttaksprosentAnnenpart(grunnlag))
            : null;

        return switch (regelresultat.getUtfallType()) {
            case AVSLÅTT -> avslåAktuellPeriode(grunnlag, periode, regelresultat);
            case INNVILGET -> innvilgAktuellPeriode(grunnlag, periode, regelresultat, redusertUttaksprosentPgaSamtidigUttakMedSamletUttak100);
            case MANUELL_BEHANDLING -> manuellBehandling(grunnlag, regelresultat);
        };
    }

    private UttakPeriode innvilgAktuellPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                       OppgittPeriode innvilgPeriode,
                                       FastsettePerioderRegelresultat regelresultat,
                                       SamtidigUttaksprosent redusertUttaksprosentPgaSamtidigUttakMedSamletUttak100) {
        var aktiviteter = lagAktiveteter(innvilgPeriode, regelresultat, redusertUttaksprosentPgaSamtidigUttakMedSamletUttak100);
        var samtidigUttaksprosent = samtidigUttaksprosentFra(fastsettePeriodeGrunnlag, aktiviteter);
        return new UttakPeriode(innvilgPeriode, Perioderesultattype.INNVILGET, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosent, innvilgPeriode.getStønadskontotype());
    }


    private UttakPeriode avslåAktuellPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag, OppgittPeriode avslåPeriode, FastsettePerioderRegelresultat regelresultat) {
        var overlapperInnvilgetAnnenpartsPeriode = overlapperMedInnvilgetAnnenpartsPeriode(fastsettePeriodeGrunnlag);
        var aktiviteter = overlapperInnvilgetAnnenpartsPeriode
            ? lagAktiviteterUtenTrekkOgUtbetaling(avslåPeriode)
            : lagAktiveteter(avslåPeriode, regelresultat, null);
        return new UttakPeriode(avslåPeriode, Perioderesultattype.AVSLÅTT, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosentFra(fastsettePeriodeGrunnlag, aktiviteter),
            utledKonto(avslåPeriode, regelGrunnlag, saldoUtregning).orElse(null));
    }

    private UttakPeriode manuellBehandling(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag, FastsettePerioderRegelresultat regelresultat) {
        var oppgittPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        var stønadskontotype = utledKonto(oppgittPeriode, regelGrunnlag, saldoUtregning);
        var aktiviteter = lagAktiveteter(oppgittPeriode, regelresultat,null);
        return new UttakPeriode(oppgittPeriode, Perioderesultattype.MANUELL_BEHANDLING, regelresultat.getManuellbehandlingårsak(),
            regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter,
            samtidigUttaksprosentFra(fastsettePeriodeGrunnlag, aktiviteter), stønadskontotype.orElse(null));
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

    private Optional<Stønadskontotype> utledKonto(OppgittPeriode aktuellPeriode, RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning) {
        return Optional.ofNullable(aktuellPeriode.getStønadskontotype())
            .or(() -> ValgAvStønadskontoTjeneste.velgStønadskonto(aktuellPeriode, regelGrunnlag, saldoUtregning));
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
        var stønadskonto = utledKonto(oppgittPeriode, regelGrunnlag, saldoUtregning);
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

    private record PeriodeAktivitetResultat(Utbetalingsgrad utbetalingsgrad, Trekkdager trekkdager) {
    }
}
