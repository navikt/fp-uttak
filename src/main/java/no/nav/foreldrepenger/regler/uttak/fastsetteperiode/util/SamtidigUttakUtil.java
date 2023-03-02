package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.PerioderUtenHelgUtil;

public final class SamtidigUttakUtil {

    private static final Set<Stønadskontotype> KONTI_FOR150 = Set.of(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FEDREKVOTE, FELLESPERIODE);
    private static final Set<Stønadskontotype> KONTI_KVOTE = Set.of(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FEDREKVOTE);

    private SamtidigUttakUtil() {
    }

    public static boolean annenpartHarSamtidigPeriodeMedUtbetaling(FastsettePeriodeGrunnlag grunnlag) {
        return finnOverlappendeAnnenpartPeriode(grunnlag, SamtidigUttakUtil::periodeHarUtbetaling).isPresent();
    }

    public static boolean erTapendePeriodeRegel(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAktuellPeriode().getSenestMottattDato().isPresent() && annenpartHarPeriodeMottattSenere(grunnlag, false);
    }

    public static boolean erTapendePeriodeUtregning(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAktuellPeriode().getSenestMottattDato().isPresent() && annenpartHarPeriodeMottattSenere(grunnlag, true);
    }

    public static boolean søktSamtidigUttakForPeriode(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAktuellPeriode().erSøktSamtidigUttak() ||
            finnOverlappendeAnnenpartPeriode(grunnlag, AnnenpartUttakPeriode::isSamtidigUttak).isPresent();
    }

    public static boolean akseptert200ProsentSamtidigUttak(FastsettePeriodeGrunnlag grunnlag) {
        return gjelderFlerbarnsdager(grunnlag) || gjelderFarRundtFødsel(grunnlag);
    }

    public static boolean merEnn100ProsentSamtidigUttak(FastsettePeriodeGrunnlag grunnlag) {
        var uttaksprosentSøker = uttaksprosent(grunnlag.getAktuellPeriode());
        var uttaksprosentAnnenpart = uttaksprosentAnnenpart(grunnlag);
        return uttaksprosentSøker.add(uttaksprosentAnnenpart).merEnn100();
    }

    public static boolean akseptert150ProsentSamtidigUttak(FastsettePeriodeGrunnlag grunnlag) {
        // Ser etter kombo MK/FK (<= 100%) + Fellesperiode (<= 50%)
        boolean er150ProsentKonfigurasjon = er150ProsentKonfigurasjon(grunnlag);
        if (er150ProsentKonfigurasjon && FELLESPERIODE.equals(grunnlag.getAktuellPeriode().getStønadskontotype())) {
            return !uttaksprosent(grunnlag.getAktuellPeriode()).merEnn50();
        } else if (er150ProsentKonfigurasjon) {
            return !uttaksprosentAnnenpart(grunnlag).merEnn50();
        } else {
            return false;
        }
    }

    public static boolean kanReduseresTil100ProsentForRegel(FastsettePeriodeGrunnlag grunnlag) {
        // Sjekker om annenparts utbetalingsgrad <=80 slik at gjenværende utbetaling etter reduksjon er >= 20% (i første omgang)
        // Dessuten avventer vi tilfelle av gradering og flere aktiviteter - reduser dersom 1 aktivitet eller ikke gradering
        return !er150ProsentKonfigurasjon(grunnlag) &&
            uttaksprosentSomGir100ProsentSamtidigUttak(grunnlag).compareTo(SamtidigUttaksprosent.TWENTY) >= 0 &&
            !(grunnlag.getAktuellPeriode().getAktiviteter().size() > 1 && grunnlag.getAktuellPeriode().erSøktGradering());
    }

    public static boolean gjelderFlerbarnsdager(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.getAktuellPeriode().isFlerbarnsdager() ||
            finnOverlappendeAnnenpartPeriode(grunnlag, AnnenpartUttakPeriode::isFlerbarnsdager).isPresent();
    }

    public static boolean gjelderFarRundtFødsel(FastsettePeriodeGrunnlag grunnlag) {
        var farRundtFødselIntervall = grunnlag.periodeFarRundtFødsel().orElse(null);
        if (farRundtFødselIntervall == null) {
            return false;
        }
        return grunnlag.getAktuellPeriode().erOmsluttetAv(farRundtFødselIntervall) ||
            finnOverlappendeAnnenpartPeriode(grunnlag, app -> app.erOmsluttetAv(farRundtFødselIntervall)).isPresent();
    }

    public static boolean kanRedusereUtbetalingsgradForTapende(FastsettePeriodeGrunnlag periodeGrunnlag, RegelGrunnlag regelGrunnlag) {
        // Er det ikkejusterbar periode, samtidig uttak under 100% eller 150/200% tilfelle?
        var kanReduseres = regelGrunnlag.getBehandling().isBerørtBehandling() || erTapendePeriodeUtregning(periodeGrunnlag);
        if (!kanReduseres || !annenpartHarSamtidigPeriodeMedUtbetaling(periodeGrunnlag) || !merEnn100ProsentSamtidigUttak(periodeGrunnlag) ||
            akseptert200ProsentSamtidigUttak(periodeGrunnlag) || akseptert150ProsentSamtidigUttak(periodeGrunnlag)) {
            return false;
        }
        // Sjekker om annenparts utbetalingsgrad <=80 slik at gjenværende utbetaling etter reduksjon er >= 20% (i første omgang)
        return kanReduseresTil100ProsentForRegel(periodeGrunnlag);
    }

    public static SamtidigUttaksprosent uttaksprosentAnnenpart(FastsettePeriodeGrunnlag grunnlag) {
        return finnOverlappendeAnnenpartPeriode(grunnlag, SamtidigUttakUtil::periodeHarUtbetaling)
            .map(SamtidigUttakUtil::getSamtidigUttaksprosent).orElse(SamtidigUttaksprosent.ZERO);
    }

    private static SamtidigUttaksprosent uttaksprosentSomGir100ProsentSamtidigUttak(FastsettePeriodeGrunnlag grunnlag) {
        var annenpart = uttaksprosentAnnenpart(grunnlag);
        return SamtidigUttaksprosent.HUNDRED.subtract(annenpart);
    }

    private static boolean annenpartHarPeriodeMottattSenere(FastsettePeriodeGrunnlag grunnlag, boolean forNedjustering) {
        var overlappende = finnOverlappendeAnnenpartPeriode(grunnlag,
            aup -> ((aup.isInnvilget() && aup.isUtsettelse()) || aup.harUtbetaling()) && (forNedjustering || !aup.isSamtidigUttak()))
            .flatMap(AnnenpartUttakPeriode::getSenestMottattDato);
        var periodeMottatt = grunnlag.getAktuellPeriode().getSenestMottattDato().orElseThrow();
        if (overlappende.filter(ol -> ol.equals(periodeMottatt)).isPresent()) {
            return grunnlag.getAnnenPartSisteSøknadMottattTidspunkt().isAfter(grunnlag.getSisteSøknadMottattTidspunkt());
        }
        return overlappende.filter(apmottatt -> apmottatt.isAfter(periodeMottatt)).isPresent();
    }

    private static boolean er150ProsentKonfigurasjon(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        Set<Stønadskontotype> annenpartsOverlappKonto = getAnnenpartStønadskontotyper(grunnlag);
        if (!annenpartsOverlappKonto.isEmpty() && aktuellPeriode.getStønadskontotype() != null && KONTI_FOR150.contains(aktuellPeriode.getStønadskontotype())) {
            return (KONTI_KVOTE.contains(aktuellPeriode.getStønadskontotype()) && annenpartsOverlappKonto.contains(Stønadskontotype.FELLESPERIODE)) ||
                (FELLESPERIODE.equals(aktuellPeriode.getStønadskontotype()) && annenpartsOverlappKonto.stream().anyMatch(KONTI_KVOTE::contains));
        }
        return false;
    }

    private static Set<Stønadskontotype> getAnnenpartStønadskontotyper(FastsettePeriodeGrunnlag grunnlag) {
        return finnOverlappendeAnnenpartPeriode(grunnlag, app -> true)
            .map(AnnenpartUttakPeriode::getAktiviteter).orElse(Set.of()).stream()
            .map(AnnenpartUttakPeriodeAktivitet::getStønadskontotype)
            .filter(Objects::nonNull)
            .filter(KONTI_FOR150::contains)
            .collect(Collectors.toSet());
    }

    private static Optional<AnnenpartUttakPeriode> finnOverlappendeAnnenpartPeriode(FastsettePeriodeGrunnlag grunnlag,
                                                                                    Predicate<AnnenpartUttakPeriode> filter) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        return grunnlag.getAnnenPartUttaksperioder().stream()
            .filter(app -> PerioderUtenHelgUtil.perioderUtenHelgOverlapper(aktuellPeriode, app) && filter.test(app))
            .findFirst();
    }

    private static SamtidigUttaksprosent uttaksprosent(OppgittPeriode aktuellPeriode) {
        if (aktuellPeriode.erSøktGradering()) {
            return SamtidigUttaksprosent.HUNDRED.subtract(aktuellPeriode.getArbeidsprosent());
        }
        return aktuellPeriode.erSøktSamtidigUttak() ? aktuellPeriode.getSamtidigUttaksprosent() : SamtidigUttaksprosent.HUNDRED;
    }


    private static SamtidigUttaksprosent getSamtidigUttaksprosent(AnnenpartUttakPeriode ap) {
        if (ap.isUtsettelse() && ap.isInnvilget()) {
            return SamtidigUttaksprosent.HUNDRED;
        }
        return ap.getAktiviteter().stream()
            .filter(a -> a.getUtbetalingsgrad().harUtbetaling())
            .min(Comparator.comparing(AnnenpartUttakPeriodeAktivitet::getUtbetalingsgrad))
            .map(a -> new SamtidigUttaksprosent(a.getUtbetalingsgrad().decimalValue()))
            .orElse(SamtidigUttaksprosent.ZERO);
    }

    private static boolean periodeHarUtbetaling(AnnenpartUttakPeriode periode) {
        return periode.getAktiviteter().stream().map(AnnenpartUttakPeriodeAktivitet::getUtbetalingsgrad).anyMatch(Utbetalingsgrad::harUtbetaling);
    }

}
