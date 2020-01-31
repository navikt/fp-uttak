package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public final class SaldoUtregningTjeneste {

    private SaldoUtregningTjeneste() {
    }

    public static SaldoUtregning lagUtregning(SaldoUtregningGrunnlag grunnlag) {
        var annenpartsPerioder = finnRelevanteAnnenpartsPerioder(grunnlag.isTapendeBehandling(), grunnlag.getUtregningsdato(),
                grunnlag.getAnnenpartsPerioder(), grunnlag.getSøktePerioder());
        var søkersFastsattePerioder = grunnlag.getSøkersFastsattePerioder();
        var stønadskontoer = lagStønadskontoer(grunnlag);
        return new SaldoUtregning(stønadskontoer, søkersFastsattePerioder, annenpartsPerioder, grunnlag.isTapendeBehandling(), grunnlag.getAktiviteter());
    }

    private static List<FastsattUttakPeriode> finnRelevanteAnnenpartsPerioder(boolean isTapendeBehandling,
                                                                              LocalDate utregningsdato,
                                                                              List<AnnenpartUttakPeriode> annenPartUttaksperioder,
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

    private static Stream<AnnenpartUttakPeriode> finnDelerAvOppholdsperiode(List<LukketPeriode> søktePerioder, AnnenpartUttakPeriode ap) {
        for (LukketPeriode søktPeriode : søktePerioder) {
            if (ap.isOppholdsperiode() && ap.overlapper(søktPeriode.getFom())) {
                if (søktPeriode.getFom().isEqual(ap.getFom()) && søktPeriode.getTom().isEqual(ap.getTom())) {
                    return Stream.of();
                }
                var etterknekk = new ArrayList<AnnenpartUttakPeriode>();
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

    private static AnnenpartUttakPeriode knekk(AnnenpartUttakPeriode ap, LocalDate nyFom, LocalDate nyTom) {
        var aktiviteterForPeriodeFørKnekkpunkt = aktiviteterForPeriodeFørKnekkpunkt(ap, nyFom, nyTom);
        return ap.kopiMedNyPeriode(nyFom, nyTom, aktiviteterForPeriodeFørKnekkpunkt);
    }

    private static Set<Stønadskonto> lagStønadskontoer(SaldoUtregningGrunnlag grunnlag) {
        return grunnlag.getKontoer().getKontoList().stream()
                .map(konto -> new Stønadskonto(konto.getType(), new Trekkdager(konto.getTrekkdager())))
                .collect(Collectors.toSet());
    }

    private static FastsattUttakPeriode map(AnnenpartUttakPeriode annenpartsPeriode) {
        return new FastsattUttakPeriode.Builder()
                .medPeriodeResultatType(map(annenpartsPeriode.isInnvilget()))
                .medSamtidigUttak(annenpartsPeriode.isSamtidigUttak())
                .medFlerbarnsdager(annenpartsPeriode.isFlerbarnsdager())
                .medOppholdÅrsak(annenpartsPeriode.getOppholdÅrsak())
                .medTidsperiode(annenpartsPeriode.getFom(), annenpartsPeriode.getTom())
                .medAktiviteter(mapAktiviteter(annenpartsPeriode))
                .build();
    }

    private static Perioderesultattype map(boolean innvilget) {
        return innvilget ? Perioderesultattype.INNVILGET : Perioderesultattype.AVSLÅTT;
    }

    private static List<FastsattUttakPeriodeAktivitet> mapAktiviteter(AnnenpartUttakPeriode annenpartsPeriode) {
        return annenpartsPeriode.getAktiviteter().stream()
                .map(aktivitet -> new FastsattUttakPeriodeAktivitet(aktivitet.getTrekkdager(), aktivitet.getStønadskontotype(), aktivitet.getAktivitetIdentifikator()))
                .collect(Collectors.toList());
    }

    private static List<AnnenpartUttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt(AnnenpartUttakPeriode periode,
                                                                                           LocalDate nyFom,
                                                                                           LocalDate nyTom) {
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(nyFom, nyTom);
        int virkedagerHele = periode.virkedager();

        List<AnnenpartUttakPeriodeAktivitet> annenpartUttakPeriodeAktivitetMedNyttTrekkDager = new ArrayList<>();

        for (AnnenpartUttakPeriodeAktivitet annenpartUttakPeriodeAktivitet : periode.getAktiviteter()) {
            Trekkdager opprinneligeTrekkdager = annenpartUttakPeriodeAktivitet.getTrekkdager();
            if (virkedagerInnenfor > 0 && opprinneligeTrekkdager.merEnn0()) {
                BigDecimal vektetTrekkdager = opprinneligeTrekkdager.decimalValue().multiply(BigDecimal.valueOf(virkedagerInnenfor))
                        .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN);
                annenpartUttakPeriodeAktivitetMedNyttTrekkDager
                        .add(new AnnenpartUttakPeriodeAktivitet(annenpartUttakPeriodeAktivitet.getAktivitetIdentifikator(),
                                annenpartUttakPeriodeAktivitet.getStønadskontotype(),
                                new Trekkdager(vektetTrekkdager),
                                annenpartUttakPeriodeAktivitet.getUtbetalingsgrad()));
            }
        }

        return annenpartUttakPeriodeAktivitetMedNyttTrekkDager;
    }
}
