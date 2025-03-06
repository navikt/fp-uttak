package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ValgAvStønadskontoTjeneste.velgStønadskonto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
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

    private RegelResultatBehandler() {
    }

    static RegelResultatBehandlerResultat behandleRegelresultat(FastsettePerioderRegelresultat regelresultat,
                                                                FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var aktuellPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();

        return switch (regelresultat.getUtfallType()) {
            case AVSLÅTT -> RegelResultatBehandler.avslåAktuellPeriode(aktuellPeriode, regelresultat, fastsettePeriodeGrunnlag);
            case INNVILGET -> RegelResultatBehandler.innvilgAktuellPeriode(aktuellPeriode, regelresultat, fastsettePeriodeGrunnlag);
            case MANUELL_BEHANDLING -> RegelResultatBehandler.manuellBehandling(aktuellPeriode, regelresultat, fastsettePeriodeGrunnlag);
        };
    }


    private static RegelResultatBehandlerResultat innvilgAktuellPeriode(OppgittPeriode oppgittPeriode,
                                                                        FastsettePerioderRegelresultat regelresultat,
                                                                        FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {

        // For tilfelle der dette uttaket er "tapende" og skal få redusert utbetalingsgrad automatisk slik at sum samtidig uttak <= 100%.
        var annenpartSamtidigUttaksprosent = SamtidigUttakUtil.kanRedusereUtbetalingsgradForTapende(fastsettePeriodeGrunnlag) ?
            SamtidigUttakUtil.uttaksprosentAnnenpart(fastsettePeriodeGrunnlag) : SamtidigUttaksprosent.ZERO;

        var knekkpunktOpt = finnKnekkpunkt(oppgittPeriode, fastsettePeriodeGrunnlag, regelresultat);

        var innvilgPeriode = knekkpunktOpt.map(TomKontoKnekkpunkt::dato)
            .map(k -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), k.minusDays(1)))
            .orElse(oppgittPeriode);

        // Max uttaksprosent gitt av gradering, samtidig uttak, eller tilfelle som skal automatisk nedjusteres for å få sum samtidig uttak <= 100%
        var samtidigUttaksprosent = regnSamtidigUttaksprosentMotGradering(innvilgPeriode, annenpartSamtidigUttaksprosent);
        var aktiviteter = lagAktiviteter(innvilgPeriode, regelresultat, fastsettePeriodeGrunnlag, false, samtidigUttaksprosent);

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

    static RegelResultatBehandlerResultat avslåAktuellPeriode(OppgittPeriode oppgittPeriode,
                                                              FastsettePerioderRegelresultat regelresultat,
                                                              FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var overlapperInnvilgetAnnenpartsPeriode = overlapperMedInnvilgetAnnenpartsPeriode(oppgittPeriode, fastsettePeriodeGrunnlag.getAnnenPartUttaksperioder());

        var knekkpunktOpt = finnKnekkpunkt(oppgittPeriode, fastsettePeriodeGrunnlag, regelresultat);

        var avslåPeriode = knekkpunktOpt.map(TomKontoKnekkpunkt::dato)
            .filter(d -> !overlapperInnvilgetAnnenpartsPeriode)
            .map(knekkdato -> oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), knekkdato.minusDays(1)))
            .orElse(oppgittPeriode);

        var samtidigUttaksprosent = regnSamtidigUttaksprosentMotGradering(avslåPeriode, SamtidigUttaksprosent.ZERO);
        var aktiviteter = lagAktiviteter(avslåPeriode, regelresultat, fastsettePeriodeGrunnlag, overlapperInnvilgetAnnenpartsPeriode, samtidigUttaksprosent);

        var avslått = new UttakPeriode(avslåPeriode, Perioderesultattype.AVSLÅTT, null, regelresultat.getAvklaringÅrsak(),
            regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosent, konto(avslåPeriode, fastsettePeriodeGrunnlag).orElse(null));

        if (!overlapperInnvilgetAnnenpartsPeriode && knekkpunktOpt.isPresent()) {
            validerKnekkpunkt(oppgittPeriode, knekkpunktOpt.get());
            var etterKnekk = oppgittPeriode.kopiMedNyPeriode(knekkpunktOpt.get().dato(), oppgittPeriode.getTom());
            return RegelResultatBehandlerResultat.medKnekk(avslått, etterKnekk);
        } else {
            return RegelResultatBehandlerResultat.utenKnekk(avslått);
        }
    }

    private static RegelResultatBehandlerResultat manuellBehandling(OppgittPeriode oppgittPeriode,
                                                                    FastsettePerioderRegelresultat regelresultat,
                                                                    FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        var stønadskontotype = konto(oppgittPeriode, fastsettePeriodeGrunnlag);
        var samtidigUttaksprosent = regnSamtidigUttaksprosentMotGradering(oppgittPeriode, SamtidigUttaksprosent.ZERO);
        var aktiviteter = lagAktiviteter(oppgittPeriode, regelresultat, fastsettePeriodeGrunnlag,false, samtidigUttaksprosent);

        var resultat = new UttakPeriode(oppgittPeriode, Perioderesultattype.MANUELL_BEHANDLING, regelresultat.getManuellbehandlingårsak(),
            regelresultat.getAvklaringÅrsak(), regelresultat.getGraderingIkkeInnvilgetÅrsak(), aktiviteter, samtidigUttaksprosent, stønadskontotype.orElse(null));
        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private static Optional<TomKontoKnekkpunkt> finnKnekkpunkt(OppgittPeriode aktuellPeriode,
                                                               FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                               FastsettePerioderRegelresultat regelresultat) {
        if (Stønadskontotype.FORELDREPENGER_FØR_FØDSEL.equals(aktuellPeriode.getStønadskontotype())) {
            return Optional.empty();
        }
        var stønadskontotype = konto(aktuellPeriode, fastsettePeriodeGrunnlag);
        var startdatoNesteStønadsperiode = fastsettePeriodeGrunnlag.erAktuellPeriodeEtterStartNesteStønadsperiode();
        var farRundtFødselIntervall = fastsettePeriodeGrunnlag.periodeFarRundtFødsel().orElse(null);

        return TomKontoIdentifiserer.identifiser(aktuellPeriode, new ArrayList<>(aktuellPeriode.getAktiviteter()), fastsettePeriodeGrunnlag.getSaldoUtregning(),
            stønadskontotype.orElse(null), farRundtFødselIntervall, startdatoNesteStønadsperiode, regelresultat.trekkDagerFraSaldo(),
            regelresultat.getAvklaringÅrsak(), regelresultat.getUtfallType(), regelresultat.getGraderingIkkeInnvilgetÅrsak() != null);
    }

    private static boolean overlapperMedInnvilgetAnnenpartsPeriode(OppgittPeriode aktuellPeriode, List<AnnenpartUttakPeriode> annenPartUttaksperioder) {
        return annenPartUttaksperioder.stream()
            .anyMatch(annenpartsPeriode -> annenpartsPeriode.overlapper(aktuellPeriode) && annenpartsPeriode.isInnvilget());
    }

    private static SamtidigUttaksprosent regnSamtidigUttaksprosentMotGradering(OppgittPeriode oppgittPeriode,
                                                                               SamtidigUttaksprosent annenpartSamtidigUttaksprosent) {
        if (annenpartSamtidigUttaksprosent.merEnn0()) {
            // Samtidig uttak, sum over 100%, returner max uttaksprosent for dette uttaket
            return SamtidigUttaksprosent.HUNDRED.subtract(annenpartSamtidigUttaksprosent);
        }
        if (oppgittPeriode.erSøktSamtidigUttak()) {
            // TODO perioder med både samtidig uttak og gradering - 96% har matchende prosent, noen få eldre tilfelle stemmer ikke overens.
            // Velger gradering over samtidig til avklart om man heller skal velge min(samtidigUttak, 100-arbeid) ?!
            return oppgittPeriode.erSøktGradering() ?
                SamtidigUttaksprosent.HUNDRED.subtract(oppgittPeriode.getArbeidsprosent()) : oppgittPeriode.getSamtidigUttaksprosent();
        }
        return null;
    }

    private static Optional<Stønadskontotype> konto(OppgittPeriode oppgittPeriode, FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        return Optional.ofNullable(oppgittPeriode.getStønadskontotype())
            .or(() -> velgStønadskonto(fastsettePeriodeGrunnlag));
    }

    private static Set<UttakPeriodeAktivitet> lagAktiviteter(OppgittPeriode oppgittPeriode,
                                                             FastsettePerioderRegelresultat regelresultat,
                                                             FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                             boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                             SamtidigUttaksprosent samtidigUttaksprosent) {
        return oppgittPeriode.getAktiviteter()
            .stream()
            .map(a -> lagAktivitet(a, regelresultat, fastsettePeriodeGrunnlag, overlapperMedInnvilgetPeriodeHosAnnenpart, oppgittPeriode, samtidigUttaksprosent))
            .collect(Collectors.toSet());
    }

    private static UttakPeriodeAktivitet lagAktivitet(AktivitetIdentifikator identifikator,
                                                      FastsettePerioderRegelresultat regelresultat,
                                                      FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                      boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                      OppgittPeriode oppgittPeriode,
                                                      SamtidigUttaksprosent samtidigUttaksprosent) {
        var søktGradering = oppgittPeriode.erSøktGradering(identifikator);
        var periodeAktivitetResultat = finnPeriodeAktivitetResultat(oppgittPeriode, overlapperMedInnvilgetPeriodeHosAnnenpart, identifikator,
            regelresultat, fastsettePeriodeGrunnlag, samtidigUttaksprosent);
        return new UttakPeriodeAktivitet(identifikator, periodeAktivitetResultat.utbetalingsgrad(), periodeAktivitetResultat.trekkdager(),
            søktGradering);
    }

    private static PeriodeAktivitetResultat finnPeriodeAktivitetResultat(OppgittPeriode oppgittPeriode,
                                                                         boolean overlapperMedInnvilgetPeriodeHosAnnenpart,
                                                                         AktivitetIdentifikator aktivitet,
                                                                         FastsettePerioderRegelresultat regelresultat,
                                                                         FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                                         SamtidigUttaksprosent samtidigUttaksprosent) {
        //Må sjekke saldo her, ved flere arbeidsforhold kan det reglene ha gått til sluttpunkt som trekkes dager selv om ett av arbeidsforholdene er tom
        //På arbeidsforholdet som er tom på konto skal det settes 0 trekkdager
        var stønadskonto = konto(oppgittPeriode, fastsettePeriodeGrunnlag);
        var harIgjenTrekkdager = isHarIgjenTrekkdager(oppgittPeriode, aktivitet, regelresultat, fastsettePeriodeGrunnlag.getSaldoUtregning(), stønadskonto.orElse(null));

        var manuellBehandling = manuellBehandling(regelresultat);
        if (overlapperMedInnvilgetPeriodeHosAnnenpart || (!manuellBehandling && !harIgjenTrekkdager)) {
            return new PeriodeAktivitetResultat(Utbetalingsgrad.ZERO, Trekkdager.ZERO);
        }

        var graderingInnvilget = regelresultat.getGraderingIkkeInnvilgetÅrsak() == null && oppgittPeriode.erSøktGradering(aktivitet);
        var utbetalingsgrad = regelresultat.skalUtbetale() ? bestemUtbetalingsgrad(oppgittPeriode, aktivitet, samtidigUttaksprosent, graderingInnvilget) : Utbetalingsgrad.ZERO;
        var trekkdager = Trekkdager.ZERO;
        if (regelresultat.trekkDagerFraSaldo() && (!manuellBehandling || stønadskonto.isPresent())) {
            trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(oppgittPeriode, graderingInnvilget, oppgittPeriode.getArbeidsprosent(), samtidigUttaksprosent);
        }
        return new PeriodeAktivitetResultat(utbetalingsgrad, trekkdager);
    }

    private static boolean isHarIgjenTrekkdager(OppgittPeriode oppgittPeriode,
                                                AktivitetIdentifikator aktivitet,
                                                FastsettePerioderRegelresultat regelresultat,
                                                SaldoUtregning saldoUtregning,
                                                Stønadskontotype stønadskonto) {
        var nettosaldo = saldoUtregning.nettoSaldoJustertForMinsterett(stønadskonto, aktivitet, oppgittPeriode.kanTrekkeAvMinsterett());
        if (regelresultat.getAvklaringÅrsak() != null && regelresultat.getAvklaringÅrsak().trekkerMinsterett()) {
            var minsterettSaldo = saldoUtregning.restSaldoMinsterett(aktivitet);
            var utenAktivitetskravSaldo = saldoUtregning.restSaldoDagerUtenAktivitetskrav();
            return nettosaldo.merEnn0() && (minsterettSaldo.merEnn0() || utenAktivitetskravSaldo.merEnn0());
        }
        return nettosaldo.merEnn0();
    }

    private static boolean manuellBehandling(FastsettePerioderRegelresultat regelresultat) {
        return regelresultat.getUtfallType().equals(UtfallType.MANUELL_BEHANDLING);
    }

    private static void validerKnekkpunkt(OppgittPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        if (!uttakPeriode.overlapper(knekkpunkt.dato())) {
            throw new IllegalArgumentException("Knekkpunkt må være i periode. " + knekkpunkt.dato() + " - " + uttakPeriode);
        }
    }

    private static Utbetalingsgrad bestemUtbetalingsgrad(OppgittPeriode oppgittPeriode,
                                                         AktivitetIdentifikator aktivitet,
                                                         SamtidigUttaksprosent samtidigUttaksprosent,
                                                         boolean graderingInnvilget) {
        // Max uttaksprosent for tapende som skal automatisk nedjusteres for å få sum samtidig uttak <= 100% - eller søkt samtidig uttak
        var maksUttaksprosent = Optional.ofNullable(samtidigUttaksprosent).orElse(SamtidigUttaksprosent.HUNDRED);

        var lokalUttaksprosent = maksUttaksprosent;

        // TODO: Se på mulighet for ytterligere forenkling gitt logikken bak samtidigUttaksprosent
        if (graderingInnvilget && oppgittPeriode.erSøktGradering(aktivitet) || erAvslåttGraderingAvFFF(oppgittPeriode, aktivitet, graderingInnvilget)) {
            // Samtidiguttaksprosent med mindre gradering på noen aktiviteter i perioden
            lokalUttaksprosent =  Optional.ofNullable(oppgittPeriode.getArbeidsprosent())
                .map(SamtidigUttaksprosent.HUNDRED::subtract)
                .orElseThrow(() -> new IllegalArgumentException("arbeidstidsprosent kan ikke være null"));
        } else if (samtidigUttaksprosent != null) {
            // TODO: se på presedens i relasjon til regnSamtidigUttaksprosentMotGradering - både gradering og samtidig
            lokalUttaksprosent = Optional.ofNullable(oppgittPeriode.getArbeidsprosent())
                .map(SamtidigUttaksprosent.HUNDRED::subtract)
                .orElse(samtidigUttaksprosent);
        } else if (oppgittPeriode.getMorsStillingsprosent() != null) {
            lokalUttaksprosent = new SamtidigUttaksprosent(oppgittPeriode.getMorsStillingsprosent().decimalValue());
        }

        // Sjekk om må avkortes ned til maksUttaksprosent (satt ut fra behov for nedjustering, eller søkt samtidig uttak m/u gradering)
        if (lokalUttaksprosent.subtract(maksUttaksprosent).merEnn0()) {
            return new Utbetalingsgrad(maksUttaksprosent.decimalValue());
        } else {
            return new Utbetalingsgrad(lokalUttaksprosent.decimalValue());
        }
    }

    private static boolean erAvslåttGraderingAvFFF(OppgittPeriode oppgittPeriode, AktivitetIdentifikator aktivitet, boolean graderingInnvilget) {
        if (Stønadskontotype.FORELDREPENGER_FØR_FØDSEL.equals(oppgittPeriode.getStønadskontotype()) && oppgittPeriode.erSøktGradering(aktivitet) && !graderingInnvilget) {
            return true;
        }
        return false;
    }


    private record PeriodeAktivitetResultat(Utbetalingsgrad utbetalingsgrad, Trekkdager trekkdager) {
    }
}
