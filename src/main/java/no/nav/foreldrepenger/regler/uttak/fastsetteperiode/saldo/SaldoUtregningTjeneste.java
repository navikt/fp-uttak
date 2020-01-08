package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public final class SaldoUtregningTjeneste {

    private SaldoUtregningTjeneste() {
    }

    public static SaldoUtregning lagUtregning(SaldoUtregningGrunnlag grunnlag) {
        var annenpartsPerioder = finnRelevanteAnnenpartsPerioder(grunnlag.isTapendeBehandling(), grunnlag.getUtregningsdato(),
                grunnlag.getAnnenpartsPerioder(), grunnlag.getSøktePerioder());
        var søkersFastsattePerioder = grunnlag.getSøkersFastsattePerioder();
        var stønadskontoer = lagStønadskontoer(grunnlag);
        return new SaldoUtregning(stønadskontoer, søkersFastsattePerioder, annenpartsPerioder, grunnlag.isTapendeBehandling());
    }

    private static List<FastsattUttakPeriode> finnRelevanteAnnenpartsPerioder(boolean isTapendeBehandling,
                                                                              LocalDate utregningsdato,
                                                                              List<AnnenpartUttaksperiode> annenPartUttaksperioder,
                                                                              List<LukketPeriode> søktePerioder) {
        var annenpartsPerioder = annenPartUttaksperioder;
        if (isTapendeBehandling) {
            annenpartsPerioder = annenpartsPerioder.stream()
                    .flatMap(ap -> finnDelerAvOppholdsperiode(søktePerioder, ap))
                    .collect(Collectors.toList());
        } else {
            annenpartsPerioder = annenpartsPerioder.stream()
                    .filter(ap -> ap.getFom().isBefore(utregningsdato))
                    .map(ap -> {
                        if (ap.overlapper(utregningsdato)) {
                            return knekk(ap, ap.getFom(), utregningsdato.minusDays(1));
                        }
                        return ap;
                    })
                    .collect(Collectors.toList());
        }

        return annenpartsPerioder.stream()
                .map(SaldoUtregningTjeneste::map)
                .collect(Collectors.toList());
    }

    private static Stream<AnnenpartUttaksperiode> finnDelerAvOppholdsperiode(List<LukketPeriode> søktePerioder, AnnenpartUttaksperiode ap) {
        for (LukketPeriode søktPeriode : søktePerioder) {
            if (ap.isOppholdsperiode() && ap.overlapper(søktPeriode.getFom())) {
                if (søktPeriode.getFom().isEqual(ap.getFom()) && søktPeriode.getTom().isEqual(ap.getTom())) {
                    return Stream.of();
                }
                var etterknekk = new ArrayList<AnnenpartUttaksperiode>();
                if (ap.getFom().isBefore(søktPeriode.getFom())) {
                    etterknekk.add(knekk(ap, ap.getFom(), søktPeriode.getFom().minusDays(1)));
                }
                if (ap.getTom().isAfter(søktPeriode.getTom())) {
                    etterknekk.add(knekk(ap, søktPeriode.getTom().plusDays(1), ap.getTom()));
                }
                return etterknekk.stream();
            }
        }
        return Stream.of(ap);
    }

    private static AnnenpartUttaksperiode knekk(AnnenpartUttaksperiode ap, LocalDate nyFom, LocalDate nyTom) {
        var aktiviteterForPeriodeFørKnekkpunkt = aktiviteterForPeriodeFørKnekkpunkt(ap, nyFom, nyTom);
        return ap.kopiMedNyPeriode(nyFom, nyTom, aktiviteterForPeriodeFørKnekkpunkt);
    }

    private static Set<KontoForArbeidsforhold> lagStønadskontoer(SaldoUtregningGrunnlag grunnlag) {
        return grunnlag.getArbeidsforhold().stream()
                .filter(arbeidsforhold -> grunnlag.getArbeidsforhold().size() == 1  || !arbeidsforhold.getStartdato().isAfter(grunnlag.getUtregningsdato()))
                .map(arbeidsforhold -> lagKontoer(arbeidsforhold, grunnlag))
                .collect(Collectors.toSet());
    }

    private static KontoForArbeidsforhold lagKontoer(Arbeidsforhold arbeidsforhold,
                                                     SaldoUtregningGrunnlag grunnlag) {
        var stønadskontoer = arbeidsforhold.getKontoer().getKontoList().stream()
                .map(konto -> lagKonto(arbeidsforhold, grunnlag, konto))
                .collect(Collectors.toSet());
        return new KontoForArbeidsforhold(arbeidsforhold.getIdentifikator(), stønadskontoer);
    }

    private static Stønadskonto lagKonto(Arbeidsforhold arbeidsforhold, SaldoUtregningGrunnlag grunnlag, Konto konto) {

        if (grunnlag.getArbeidsforhold().size() > 1 && arbeidsforholdStarterEtterFørsteUttaksdag(grunnlag, arbeidsforhold.getStartdato())) {
            var dato = arbeidsforhold.getStartdato().isBefore(grunnlag.getUtregningsdato()) ? arbeidsforhold.getStartdato() : grunnlag.getUtregningsdato();
            var antallDagerBrukt = antallDagerBruktFramTilDato(konto.getType(), grunnlag.getSøkersFastsattePerioder(), dato,
                    arbeidsforhold.getIdentifikator(), startDatoer(grunnlag));
            return new Stønadskonto(konto.getType(), new Trekkdager(konto.getTrekkdager()).subtract(antallDagerBrukt));
        }
        return new Stønadskonto(konto.getType(), new Trekkdager(konto.getTrekkdager()));
    }

    private static Map<AktivitetIdentifikator, LocalDate> startDatoer(SaldoUtregningGrunnlag grunnlag) {
        var resultat = new HashMap<AktivitetIdentifikator, LocalDate>();
        for (Arbeidsforhold arbeidsforhold : grunnlag.getArbeidsforhold()) {
            resultat.put(arbeidsforhold.getIdentifikator(), arbeidsforhold.getStartdato());
        }
        return resultat;
    }

    private static Trekkdager antallDagerBruktFramTilDato(Stønadskontotype stønadskontoType,
                                                          List<FastsattUttakPeriode> fastsatteUttaksperioder,
                                                          LocalDate dato,
                                                          AktivitetIdentifikator aktivitetIdentifikator,
                                                          Map<AktivitetIdentifikator, LocalDate> startdatoer) {

        var forbrukteDager = Trekkdager.ZERO;
        var aktiviteterFørDato = fastsattePerioderVedStartdato(fastsatteUttaksperioder, dato)
                .stream()
                .flatMap(periode -> periode.getAktiviteter().stream())
                .collect(Collectors.toSet());
        var aktiviteter = aktiviteterFørDato.stream()
                .filter(aktivitet -> aktivitet.getAktivitetIdentifikator().equals(aktivitetIdentifikator))
                .filter(aktivitet -> Objects.equals(aktivitet.getTrekkonto(), stønadskontoType))
                .collect(Collectors.toList());
        for (var aktivitet : aktiviteter) {
            forbrukteDager = forbrukteDager.add(aktivitet.getTrekkdager());
        }
        var startdatoForAktivitet = startdatoer.get(aktivitetIdentifikator);
        if (!fastsatteUttaksperioder.isEmpty() && startdatoForAktivitet.isAfter(fastsatteUttaksperioder.get(0).getFom())) {
            //Skal arve saldo fra aktivitet som har igjen mest
            var minimumTrektDagerFørDato = startdatoer.entrySet().stream()
                    .filter(entry -> entry.getValue().isBefore(dato))
                    .map(entry -> entry.getKey())
                    .map(ai -> antallDagerBruktFramTilDato(stønadskontoType, fastsatteUttaksperioder, startdatoForAktivitet.isBefore(dato) ? startdatoForAktivitet : dato, ai, startdatoer))
                    .min((o1, o2) -> o1.compareTo(o2))
                    .orElse(Trekkdager.ZERO);
            forbrukteDager = forbrukteDager.add(minimumTrektDagerFørDato);
        }
        return forbrukteDager;
    }

    private static List<FastsattUttakPeriode> fastsattePerioderVedStartdato(List<FastsattUttakPeriode> fastsattePerioder, LocalDate utregningsdato) {
        return fastsattePerioder.stream()
                .filter(p -> p.getTom().isBefore(utregningsdato))
                .collect(Collectors.toList());
    }

    private static boolean arbeidsforholdStarterEtterFørsteUttaksdag(SaldoUtregningGrunnlag grunnlag, LocalDate arbeidsforholdStartdato) {
        var søkersFastsattePerioder = grunnlag.getSøkersFastsattePerioder();
        var søkersFørsteUttaksdato = søkersFastsattePerioder.isEmpty() ? grunnlag.getUtregningsdato() : søkersFastsattePerioder.get(0).getFom();
        return arbeidsforholdStartdato.isAfter(søkersFørsteUttaksdato);
    }

    private static FastsattUttakPeriode map(AnnenpartUttaksperiode annenpartsPeriode) {
        return new FastsattUttakPeriode.Builder()
                .medPeriodeResultatType(map(annenpartsPeriode.isInnvilget()))
                .medSamtidigUttak(annenpartsPeriode.isSamtidigUttak())
                .medFlerbarnsdager(annenpartsPeriode.isFlerbarnsdager())
                .medOppholdÅrsak(annenpartsPeriode.getOppholdårsaktype())
                .medTidsperiode(annenpartsPeriode.getFom(), annenpartsPeriode.getTom())
                .medAktiviteter(mapAktiviteter(annenpartsPeriode))
                .build();
    }

    private static Perioderesultattype map(boolean innvilget) {
        return innvilget ? Perioderesultattype.INNVILGET : Perioderesultattype.AVSLÅTT;
    }

    private static List<FastsattUttakPeriodeAktivitet> mapAktiviteter(AnnenpartUttaksperiode annenpartsPeriode) {
        return annenpartsPeriode.getAktiviteter().stream()
                .map(aktivitet -> new FastsattUttakPeriodeAktivitet(aktivitet.getTrekkdager(), aktivitet.getStønadskontotype(), aktivitet.getAktivitetIdentifikator()))
                .collect(Collectors.toList());
    }

    private static List<no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt(AnnenpartUttaksperiode periode,
                                                                                                                                               LocalDate nyFom, LocalDate nyTom) {
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(nyFom, nyTom);
        int virkedagerHele = periode.virkedager();

        List<no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet> uttakPeriodeAktivitetMedNyttTrekkDager = new ArrayList<>();

        for (no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet uttakPeriodeAktivitet : periode.getAktiviteter()) {
            Trekkdager opprinneligeTrekkdager = uttakPeriodeAktivitet.getTrekkdager();
            if (virkedagerInnenfor > 0 && opprinneligeTrekkdager.merEnn0()) {
                BigDecimal vektetTrekkdager = opprinneligeTrekkdager.decimalValue().multiply(BigDecimal.valueOf(virkedagerInnenfor))
                        .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN);
                uttakPeriodeAktivitetMedNyttTrekkDager
                        .add(new no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet(uttakPeriodeAktivitet.getAktivitetIdentifikator(),
                                uttakPeriodeAktivitet.getStønadskontotype(),
                                new Trekkdager(vektetTrekkdager),
                                uttakPeriodeAktivitet.getUtbetalingsgrad(),
                                uttakPeriodeAktivitet.getGradertArbeidsprosent()));
            }
        }

        return uttakPeriodeAktivitetMedNyttTrekkDager;
    }
}
