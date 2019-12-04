package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;

public final class SaldoUtregningTjeneste {

    private SaldoUtregningTjeneste() {
    }

    public static SaldoUtregning lagUtregning(SaldoUtregningGrunnlag grunnlag) {
        var utregningsdato = grunnlag.getUtregningsdato();
        var annenpartsPerioder = finnRelevanteAnnenpartsPerioder(grunnlag.isTapendeBehandling(), utregningsdato, grunnlag.getAnnenpartsPerioder());
        var søkersFastsattePerioder = grunnlag.getSøkersFastsattePerioder();
        var stønadskontoer = lagStønadskontoer(grunnlag.getArbeidsforhold(), grunnlag, søkersFastsattePerioder, utregningsdato);
        return new SaldoUtregning(stønadskontoer, søkersFastsattePerioder, annenpartsPerioder, grunnlag.isTapendeBehandling());
    }

    private static List<FastsattUttakPeriode> finnRelevanteAnnenpartsPerioder(boolean isTapendeBehandling,
                                                                              LocalDate utregningsdato,
                                                                              List<AnnenpartUttaksperiode> annenPartUttaksperioder) {
        var annenpartsPerioder = annenPartUttaksperioder;
        if (!isTapendeBehandling) {
            annenpartsPerioder = annenpartsPerioder.stream()
                    .filter(ap -> ap.getFom().isBefore(utregningsdato))
                    .map(ap -> {
                        if (ap.overlapper(utregningsdato)) {
                            return knekk(ap, utregningsdato.minusDays(1));
                        }
                        return ap;
                    })
                    .collect(Collectors.toList());
        }

        return annenpartsPerioder.stream()
                .map(SaldoUtregningTjeneste::map)
                .collect(Collectors.toList());
    }

    private static AnnenpartUttaksperiode knekk(AnnenpartUttaksperiode ap, LocalDate nyTom) {
        var aktiviteterForPeriodeFørKnekkpunkt = aktiviteterForPeriodeFørKnekkpunkt(ap, nyTom);
        return ap.kopiMedNyPeriode(ap.getFom(), nyTom, aktiviteterForPeriodeFørKnekkpunkt);
    }

    private static Set<KontoForArbeidsforhold> lagStønadskontoer(Set<Arbeidsforhold> arbeidsforholdListe,
                                                                 SaldoUtregningGrunnlag grunnlag,
                                                                 List<FastsattUttakPeriode> fastsattePerioder,
                                                                 LocalDate utregningsdato) {
        return arbeidsforholdListe.stream()
                .filter(arbeidsforhold -> !arbeidsforhold.getStartdato().isAfter(utregningsdato))
                .map(arbeidsforhold -> lagKontoer(arbeidsforhold, grunnlag, fastsattePerioder, utregningsdato))
                .collect(Collectors.toSet());
    }

    private static KontoForArbeidsforhold lagKontoer(Arbeidsforhold arbeidsforhold,
                                                     SaldoUtregningGrunnlag grunnlag,
                                                     List<FastsattUttakPeriode> fastsattePerioder,
                                                     LocalDate utregningsdato) {
        var stønadskontoer = arbeidsforhold.getKontoer().getKontoList().stream()
                .map(konto -> {
                    int saldo = saldoForArbeidsforhold(arbeidsforhold, grunnlag, fastsattePerioder, konto, utregningsdato);
                    return new Stønadskonto(konto.getType(), saldo);
                })
                .collect(Collectors.toSet());
        return new KontoForArbeidsforhold(arbeidsforhold.getIdentifikator(), stønadskontoer);
    }

    private static int saldoForArbeidsforhold(Arbeidsforhold arbeidsforhold,
                                              SaldoUtregningGrunnlag grunnlag,
                                              List<FastsattUttakPeriode> fastsattePerioder,
                                              Konto konto,
                                              LocalDate utregningsdato) {
        var startdatoArbeidsforhold = arbeidsforhold.getStartdato();
        if (arbeidsforholdStartetEtterFørsteUttaksdag(arbeidsforhold, grunnlag) && !startdatoArbeidsforhold.isAfter(utregningsdato)) {
            var arbeidsforholdFørStartdato = ekisterendeArbeidsforholdVedStartdato(grunnlag, startdatoArbeidsforhold);
            var fastsattePerioderFørStartdato = fastsattePerioderVedStartdato(fastsattePerioder, startdatoArbeidsforhold);
            var annenpart = finnRelevanteAnnenpartsPerioder(grunnlag.isTapendeBehandling(), startdatoArbeidsforhold, grunnlag.getAnnenpartsPerioder());
            var stønadskontoer = lagStønadskontoer(arbeidsforholdFørStartdato, grunnlag, fastsattePerioderFørStartdato, startdatoArbeidsforhold);
            var saldoUtregning = new SaldoUtregning(stønadskontoer, fastsattePerioderFørStartdato, annenpart, grunnlag.isTapendeBehandling());
            return saldoUtregning.saldo(konto.getType());
        }
        return konto.getTrekkdager();
    }

    private static Set<Arbeidsforhold> ekisterendeArbeidsforholdVedStartdato(SaldoUtregningGrunnlag grunnlag, LocalDate startdatoArbeidsforhold) {
        return grunnlag.getArbeidsforhold().stream()
                .filter(a -> a.getStartdato().isBefore(startdatoArbeidsforhold))
                .collect(Collectors.toSet());
    }

    private static List<FastsattUttakPeriode> fastsattePerioderVedStartdato(List<FastsattUttakPeriode> fastsattePerioder, LocalDate utregningsdato) {
        return fastsattePerioder.stream()
                .filter(p -> p.getTom().isBefore(utregningsdato))
                .collect(Collectors.toList());
    }

    private static boolean arbeidsforholdStartetEtterFørsteUttaksdag(Arbeidsforhold arbeidsforhold, SaldoUtregningGrunnlag grunnlag) {
        var søkersFastsattePerioder = grunnlag.getSøkersFastsattePerioder();
        var søkersFørsteUttaksdato = søkersFastsattePerioder.isEmpty() ? grunnlag.getUtregningsdato() : søkersFastsattePerioder.get(0).getFom();
        return arbeidsforhold.getStartdato().isAfter(søkersFørsteUttaksdato);
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

    private static List<no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt(AnnenpartUttaksperiode periode, LocalDate knekkTom) {
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(periode.getFom(), knekkTom);
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
