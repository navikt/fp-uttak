package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;

public final class SaldoUtregningTjeneste {

    private SaldoUtregningTjeneste() {
    }

    public static SaldoUtregning lagUtregning(SaldoUtregningGrunnlag grunnlag) {
        var annenpartsPerioder = finnRelevanteAnnenpartsPerioder(grunnlag.isBerørtBehandling(), grunnlag.getUtregningsdato(),
            grunnlag.getAnnenpartsPerioder(), grunnlag.getSøktePerioder());

        var søkersFastsattePerioder = knekkSøkersOppholdsperioder(annenpartsPerioder, grunnlag.getSøkersFastsattePerioder()).stream()
            .filter(p -> !p.isInnvilgetGyldigUtsettelse())
            .toList();
        return new SaldoUtregning(grunnlag.getStønadskonti(), søkersFastsattePerioder, annenpartsPerioder, grunnlag);
    }

    private static List<FastsattUttakPeriode> knekkSøkersOppholdsperioder(List<FastsattUttakPeriode> annenpartsPerioder,
                                                                          List<FastsattUttakPeriode> søkersFastsattePerioder1) {
        var knekkpunkter = annenpartsPerioder.stream().flatMap(ap -> Stream.of(ap.getFom().minusDays(1), ap.getTom())).toList();
        return søkersFastsattePerioder1.stream().flatMap(p -> {
            if (p.isOpphold()) {
                return knekkOpphold(p, knekkpunkter).stream();
            }
            return Stream.of(p);
        }).toList();
    }

    private static List<FastsattUttakPeriode> knekkOpphold(FastsattUttakPeriode opphold, List<LocalDate> knekkpunkter) {

        var etterKnekk = new ArrayList<FastsattUttakPeriode>();
        var sortedKnekk = knekkpunkter.stream().sorted(Comparator.naturalOrder()).toList();
        var etterKnekkOpphold = opphold;
        for (var knekk : sortedKnekk) {
            var tidsperiode = new LukketPeriode(etterKnekkOpphold.getFom(), etterKnekkOpphold.getTom());
            if (tidsperiode.overlapper(knekk) && !etterKnekkOpphold.getTom().isEqual(knekk)) {
                var førKnekkOpphold = new FastsattUttakPeriode.Builder(etterKnekkOpphold).tidsperiode(etterKnekkOpphold.getFom(), knekk).build();
                etterKnekkOpphold = new FastsattUttakPeriode.Builder(etterKnekkOpphold).tidsperiode(knekk.plusDays(1), etterKnekkOpphold.getTom())
                    .build();
                etterKnekk.add(førKnekkOpphold);
            }
        }
        etterKnekk.add(etterKnekkOpphold);
        return etterKnekk;

    }

    private static List<FastsattUttakPeriode> finnRelevanteAnnenpartsPerioder(boolean isBerørtBehandling,
                                                                              LocalDate utregningsdato,
                                                                              List<AnnenpartUttakPeriode> annenPartUttaksperioder,
                                                                              List<LukketPeriode> søktePerioder) {
        var annenpartsPerioder = annenPartUttaksperioder;
        if (isBerørtBehandling) {
            annenpartsPerioder = annenpartsPerioder.stream().flatMap(ap -> finnDelerAvOppholdsperiode(søktePerioder, ap)).toList();
        } else {
            annenpartsPerioder = annenpartsPerioder.stream().filter(ap -> ap.getFom().isBefore(utregningsdato)).map(ap -> {
                if (ap.overlapper(utregningsdato)) {
                    return knekk(ap, ap.getFom(), utregningsdato.minusDays(1));
                }
                return ap;
            }).toList();
        }

        return annenpartsPerioder.stream().map(SaldoUtregningTjeneste::map).toList();
    }

    private static Stream<AnnenpartUttakPeriode> finnDelerAvOppholdsperiode(List<LukketPeriode> søktePerioder, AnnenpartUttakPeriode ap) {
        for (var søktPeriode : søktePerioder) {
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

    private static FastsattUttakPeriode map(AnnenpartUttakPeriode annenpartsPeriode) {
        return new FastsattUttakPeriode.Builder().periodeResultatType(map(annenpartsPeriode.isInnvilget()))
            .samtidigUttak(annenpartsPeriode.isSamtidigUttak())
            .flerbarnsdager(annenpartsPeriode.isFlerbarnsdager())
            .oppholdÅrsak(annenpartsPeriode.getOppholdÅrsak())
            .tidsperiode(annenpartsPeriode.getFom(), annenpartsPeriode.getTom())
            .aktiviteter(mapAktiviteter(annenpartsPeriode))
            .mottattDato(annenpartsPeriode.getSenestMottattDato().orElse(null))
            .build();
    }

    private static Perioderesultattype map(boolean innvilget) {
        return innvilget ? Perioderesultattype.INNVILGET : Perioderesultattype.AVSLÅTT;
    }

    private static List<FastsattUttakPeriodeAktivitet> mapAktiviteter(AnnenpartUttakPeriode annenpartsPeriode) {
        return annenpartsPeriode.getAktiviteter()
            .stream()
            .map(aktivitet -> new FastsattUttakPeriodeAktivitet(aktivitet.getTrekkdager(), aktivitet.getStønadskontotype(),
                aktivitet.getAktivitetIdentifikator()))
            .toList();
    }

    private static List<AnnenpartUttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt(AnnenpartUttakPeriode periode,
                                                                                           LocalDate nyFom,
                                                                                           LocalDate nyTom) {
        var virkedagerInnenfor = Virkedager.beregnAntallVirkedager(nyFom, nyTom);
        var virkedagerHele = periode.virkedager();

        List<AnnenpartUttakPeriodeAktivitet> annenpartUttakPeriodeAktivitetMedNyttTrekkDager = new ArrayList<>();

        for (var annenpartUttakPeriodeAktivitet : periode.getAktiviteter()) {
            var opprinneligeTrekkdager = annenpartUttakPeriodeAktivitet.getTrekkdager();
            final BigDecimal vektetTrekkdager;
            if (virkedagerInnenfor > 0 && opprinneligeTrekkdager.merEnn0()) {
                vektetTrekkdager = opprinneligeTrekkdager.decimalValue()
                    .multiply(BigDecimal.valueOf(virkedagerInnenfor))
                    .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN);

            } else {
                vektetTrekkdager = BigDecimal.ZERO;
            }
            annenpartUttakPeriodeAktivitetMedNyttTrekkDager.add(
                new AnnenpartUttakPeriodeAktivitet(annenpartUttakPeriodeAktivitet.getAktivitetIdentifikator(),
                    annenpartUttakPeriodeAktivitet.getStønadskontotype(), new Trekkdager(vektetTrekkdager),
                    annenpartUttakPeriodeAktivitet.getUtbetalingsgrad()));
        }

        return annenpartUttakPeriodeAktivitetMedNyttTrekkDager;
    }
}
