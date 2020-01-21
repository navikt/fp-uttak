package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class SaldoUtregning {

    private final Set<KontoForArbeidsforhold> stønadskontoer;
    private final List<FastsattUttakPeriode> søkersPerioder;
    private final List<FastsattUttakPeriode> annenpartsPerioder;
    private final boolean tapendeBehandling;

    SaldoUtregning(Set<KontoForArbeidsforhold> stønadskontoer,
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean tapendeBehandling) {
        this.stønadskontoer = stønadskontoer;
        this.søkersPerioder = søkersPerioder;
        this.annenpartsPerioder = fjernOppholdsperioderEtterSisteUttaksdato(søkersPerioder, annenpartsPerioder);
        this.tapendeBehandling = tapendeBehandling;
    }

    private List<FastsattUttakPeriode> fjernOppholdsperioderEtterSisteUttaksdato(List<FastsattUttakPeriode> perioderSøker,
                                                                                 List<FastsattUttakPeriode> perioderAnnenpart) {
        var sisteUttaksdatoSøker = sisteUttaksdato(perioderSøker);
        var sisteUttaksdatoAnnenpart = sisteUttaksdato(perioderAnnenpart);
        if (sisteUttaksdatoSøker.isEmpty() || sisteUttaksdatoAnnenpart.isEmpty()) {
            return perioderAnnenpart;
        }
        var sisteUttaksdatoFelles = sisteUttaksdatoSøker.get().isAfter(sisteUttaksdatoAnnenpart.get()) ? sisteUttaksdatoSøker.get() : sisteUttaksdatoAnnenpart.get();

        var resultat = new ArrayList<FastsattUttakPeriode>();
        for (var periode : perioderAnnenpart) {
            if (erOpphold(periode)) {
                if (!periode.getFom().isAfter(sisteUttaksdatoFelles)) {
                    var nyFom = periode.getFom();
                    //Hvis oppholdsperioder delvis overlapper med annenpart skal deler av oppholdet brukes
                    var nyTom = periode.getTom().isAfter(sisteUttaksdatoFelles) ? sisteUttaksdatoFelles : periode.getTom();
                    resultat.add(kopier(periode, nyFom, nyTom));
                }
            } else {
                resultat.add(periode);
            }
        }
        return resultat;
    }

    private FastsattUttakPeriode kopier(FastsattUttakPeriode periode, LocalDate nyFom, LocalDate nyTom) {
        return new FastsattUttakPeriode.Builder(periode).medTidsperiode(nyFom, nyTom).build();
    }

    private Optional<LocalDate> sisteUttaksdato(List<FastsattUttakPeriode> perioder) {
        return perioder.stream()
                .filter(periode -> !erOpphold(periode))
                .min(Comparator.comparing(FastsattUttakPeriode::getTom))
                .map(FastsattUttakPeriode::getTom);
    }

    /**
     * Saldo for angitt stønadskonto og aktivitet.
     *
     * @param stønadskonto angitt stønadskonto.
     * @param aktivitet    angitt aktivitet.
     * @return antall gjenstående dager for angitt stønadskonto og aktivitet.
     */
    public int saldo(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet) {
        var saldo = saldoITrekkdager(stønadskonto, aktivitet);
        if (saldo.mindreEnn0()) {
            return saldo.decimalValue().setScale(0, RoundingMode.DOWN).intValue();
        }
        return saldo.decimalValue().setScale(0, RoundingMode.UP).intValue();
    }

    /**
     * Saldo for angitt stønadskonto og aktivitet.
     *
     * @param stønadskonto angitt stønadskonto.
     * @param aktivitet    angitt aktivitet.
     * @return antall gjenstående dager for angitt stønadskonto og aktivitet.
     */
    public Trekkdager saldoITrekkdager(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet) {
        BigDecimal forbruktSøker = forbruktSøker(stønadskonto, aktivitet);
        int forbruktAnnenpart = minForbruktAnnenpart(stønadskonto);
        //frigitte dager er dager fra annenpart som blir ledig når søker tar uttak i samme periode
        int frigitteDager = frigitteDager(stønadskonto);
        return getMaxDager(stønadskonto, aktivitet)
                .subtract(new Trekkdager(forbruktSøker))
                .subtract(new Trekkdager(forbruktAnnenpart))
                .add(new Trekkdager(frigitteDager));
    }

    /**
     * Saldo for angitt stønadskonto. Dersom saldo der forskjellige på aktivitetene, så blir største saldo valgt.
     *
     * @param stønadskonto angitt stønadskonto.
     * @return antall gjenstående dager for angitt stønadskonto.
     */
    public Trekkdager saldoITrekkdager(Stønadskontotype stønadskonto) {
        Trekkdager max = Trekkdager.ZERO;
        for (AktivitetIdentifikator aktivitet : aktiviteterForSøker()) {
            var saldo = saldoITrekkdager(stønadskonto, aktivitet);
            if (max.compareTo(saldo) < 0) {
                max = saldo;
            }
        }
        return max;
    }

    /**
     * Saldo for angitt stønadskonto. Dersom saldo der forskjellige på aktivitetene, så blir største saldo valgt.
     *
     * @param stønadskonto angitt stønadskonto.
     * @return antall gjenstående dager for angitt stønadskonto.
     */
    public int saldo(Stønadskontotype stønadskonto) {
        Integer max = null;
        for (AktivitetIdentifikator aktivitet : aktiviteterForSøker()) {
            int saldo = saldo(stønadskonto, aktivitet);
            if (max == null || saldo > max) {
                max = saldo;
            }
        }
        return max == null ? 0 : max;
    }

    /**
     * Aktivitet med stønadskonto. Hvis flere aktiviteter har stønadskonto velges den med minst trekkdager
     */
    private Optional<FastsattUttakPeriodeAktivitet> aktivitetMedStønadskonto(Stønadskontotype stønadskonto, FastsattUttakPeriode periode) {
        FastsattUttakPeriodeAktivitet aktivitetMedMinstTrekkdager = null;
        for (FastsattUttakPeriodeAktivitet aktivitet : periode.getAktiviteter()) {
            if (aktivitet.getTrekkonto().equals(stønadskonto) || (periode.isFlerbarnsdager() && stønadskonto.equals(Stønadskontotype.FLERBARNSDAGER))) {
                if (aktivitetMedMinstTrekkdager == null || aktivitet.getTrekkdager().compareTo(aktivitetMedMinstTrekkdager.getTrekkdager()) < 0) {
                    aktivitetMedMinstTrekkdager = aktivitet;
                }
            }
        }
        return Optional.ofNullable(aktivitetMedMinstTrekkdager);
    }

    /**
     * Hvilke stønadskontoer er opprettet.
     *
     * @return et sett med stønadskontotyper.
     */
    public Set<Stønadskontotype> stønadskontoer() {
        return stønadskontoer.stream()
                .flatMap(s -> s.getStønadskontoer().stream())
                .map(Stønadskonto::getStønadskontotype)
                .collect(Collectors.toSet());
    }

    private Optional<KontoForArbeidsforhold> kontoForArbeidsforhold(AktivitetIdentifikator aktivitetIdentifikator) {
        return stønadskontoer.stream()
                .filter(kontoForArbeidsforhold -> kontoForArbeidsforhold.getAktivitetIdentifikator().equals(aktivitetIdentifikator))
                .findFirst();
    }

    /**
     * Har konto negativ saldo, ser på laveste saldo ved flere arbeidsforhold
     */
    public boolean negativSaldo(Stønadskontotype stønadskontoType) {
        return minSaldo(stønadskontoType) < 0;
    }

    public boolean nokDagerÅFrigiPåAnnenpart(Stønadskontotype stønadskontoType) {
        int saldo = minSaldo(stønadskontoType);
        if (saldo >= 0) {
            return true;
        }

        Trekkdager antallDagerAnnenpartKanFrigi = antallDagerAnnenpartKanFrigi(stønadskontoType);

        return antallDagerAnnenpartKanFrigi.compareTo(new Trekkdager(Math.abs(saldo))) >= 0;
    }

    public boolean søktSamtidigUttak(Stønadskontotype stønadskontoType) {
        return søkersPerioder.stream()
                .filter(p -> søktSamtidigUttak(stønadskontoType, p))
                .anyMatch(FastsattUttakPeriode::isSamtidigUttak);
    }

    public boolean negativSaldoPåNoenKonto() {
        return stønadskontoer.stream()
                .flatMap(kontoForArbeidsforhold -> kontoForArbeidsforhold.getStønadskontoer().stream())
                .anyMatch(stønadskonto -> negativSaldo(stønadskonto.getStønadskontotype()));
    }

    public int getMaxDager(Stønadskontotype stønadskontotype) {
        return stønadskontoer.stream()
                .flatMap(konto -> konto.getStønadskontoer().stream())
                .filter(stønadskonto -> stønadskonto.getStønadskontotype().equals(stønadskontotype))
                .map(stønadskonto -> stønadskonto.getMaksdager().decimalValue().intValue())
                .max(Integer::compareTo)
                .orElse(0);
    }

    private Trekkdager getMaxDager(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitetIdentifikator) {
        var kontoForArbeidsforhold = kontoForArbeidsforhold(aktivitetIdentifikator);
        if (kontoForArbeidsforhold.isEmpty()) {
            return Trekkdager.ZERO;
        }
        return kontoForArbeidsforhold.get().getStønadskontoer().stream()
                .filter(konto -> konto.getStønadskontotype().equals(stønadskonto))
                .map(Stønadskonto::getMaksdager)
                .findFirst()
                .orElse(Trekkdager.ZERO);
    }

    private int frigitteDager(Stønadskontotype stønadskonto) {
        int sum = 0;
        for (FastsattUttakPeriode periode : søkersPerioder) {
            List<FastsattUttakPeriode> overlappendePerioder = overlappendeAnnenpartPeriode(periode);
            for (FastsattUttakPeriode overlappendePeriode : overlappendePerioder) {
                if (erOpphold(periode) && innvilgetMedTrekkdager(overlappendePeriode)) {
                    sum += trekkdagerForOppholdsperiode(stønadskonto, periode).intValue();
                } else if (!tapendeBehandling && innvilgetMedTrekkdager(periode)) {
                    if (stønadskonto.equals(Stønadskontotype.FLERBARNSDAGER)) {
                        sum += frigitteDagerFlerbarnsdager(stønadskonto, periode, overlappendePeriode);
                    } else {
                        sum += frigitteDagerVanligeStønadskontoer(stønadskonto, periode, overlappendePeriode);
                    }
                } else if (tapendeBehandling && erOpphold(overlappendePeriode)) {
                    var delFom = overlappendePeriode.getFom().isBefore(periode.getFom()) ? periode.getFom() : overlappendePeriode.getFom();
                    var delTom = overlappendePeriode.getTom().isBefore(periode.getTom()) ? overlappendePeriode.getTom() : periode.getTom();
                    sum += trekkdagerForOppholdsperiode(stønadskonto, overlappendePeriode.getOppholdÅrsak(), delFom, delTom).intValue();
                }
            }
        }
        return sum;
    }

    private boolean innvilgetMedTrekkdager(FastsattUttakPeriode periode) {
        return !periode.getPerioderesultattype().equals(Perioderesultattype.AVSLÅTT) ||
                periode.getAktiviteter().stream().anyMatch(aktivitet -> aktivitet.getTrekkdager().merEnn0());
    }

    private int frigitteDagerFlerbarnsdager(Stønadskontotype stønadskonto, FastsattUttakPeriode periode, FastsattUttakPeriode overlappende) {
        if (periode.isFlerbarnsdager() && overlappende.isFlerbarnsdager()) {
            Optional<FastsattUttakPeriodeAktivitet> annenpartAktivitet = aktivitetMedStønadskonto(stønadskonto, overlappende);
            if (annenpartAktivitet.isPresent()) {
                return trekkDagerFraDelAvPeriode(periode.getFom(), periode.getTom(), overlappende.getFom(), overlappende.getTom(), annenpartAktivitet.get().getTrekkdager().decimalValue());
            }
        }
        return 0;
    }

    private int frigitteDagerVanligeStønadskontoer(Stønadskontotype stønadskonto, FastsattUttakPeriode periode, FastsattUttakPeriode overlappende) {
        if (periode.isSamtidigUttak() || overlappende.isSamtidigUttak()) {
            return 0;
        }
        int frigitte = 0;
        var delFom = periode.getFom().isBefore(overlappende.getFom()) ? overlappende.getFom() : periode.getFom();
        var delTom = periode.getTom().isBefore(overlappende.getTom()) ? periode.getTom() : overlappende.getTom();
        if (erOpphold(overlappende)) {
            frigitte = trekkDagerFraDelAvPeriode(
                    delFom,
                    delTom,
                    overlappende.getFom(),
                    overlappende.getTom(),
                    trekkdagerForOppholdsperiode(stønadskonto, overlappende)
            );
        } else {
            Optional<FastsattUttakPeriodeAktivitet> annenPartAktivitetMedKonto = aktivitetMedStønadskonto(stønadskonto, overlappende);
            if (annenPartAktivitetMedKonto.isPresent()) {
                frigitte = trekkDagerFraDelAvPeriode(
                        delFom,
                        delTom,
                        overlappende.getFom(),
                        overlappende.getTom(),
                        annenPartAktivitetMedKonto.get().getTrekkdager().decimalValue()
                );
            }
        }
        return frigitte;
    }

    private int trekkDagerFraDelAvPeriode(LocalDate delFom, LocalDate delTom, LocalDate periodeFom, LocalDate periodeTom, BigDecimal periodeTrekkdager) {
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(delFom, delTom);
        int virkedagerHele = Virkedager.beregnAntallVirkedager(periodeFom, periodeTom);
        if (virkedagerHele == 0) {
            return 0;
        }
        return periodeTrekkdager.multiply(BigDecimal.valueOf(virkedagerInnenfor))
                .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN)
                .intValue();
    }

    private List<FastsattUttakPeriode> overlappendeAnnenpartPeriode(FastsattUttakPeriode periode) {
        return overlappendePeriode(periode, annenpartsPerioder);
    }

    private List<FastsattUttakPeriode> overlappendePeriode(FastsattUttakPeriode periode, List<FastsattUttakPeriode> perioder) {
        var resultat = new ArrayList<FastsattUttakPeriode>();
        for (FastsattUttakPeriode periode2 : perioder) {
            if (overlapper(periode, periode2)) {
                resultat.add(periode2);
            }
        }
        return resultat;
    }

    private boolean overlapper(FastsattUttakPeriode periode, FastsattUttakPeriode periode2) {
        return !periode2.getFom().isAfter(periode.getTom()) && !periode2.getTom().isBefore(periode.getFom());
    }

    private int minForbruktAnnenpart(Stønadskontotype stønadskonto) {
        Map<AktivitetIdentifikator, BigDecimal> forbrukte = new HashMap<>();
        for (FastsattUttakPeriode periode : annenpartsPerioder) {
            for (AktivitetIdentifikator annenpartAktivitet : aktiviteterForAnnenpart()) {
                final BigDecimal trekkdager;
                if (erOpphold(periode)) {
                    trekkdager = trekkdagerForOppholdsperiode(stønadskonto, periode);
                } else {
                    if (!aktivitetIPeriode(periode, annenpartAktivitet)) {
                        trekkdager = minForbrukteDager(periode, stønadskonto).decimalValue();
                    } else {
                        trekkdager = trekkdagerForUttaksperiode(stønadskonto, annenpartAktivitet, periode);
                    }
                }
                forbrukte.put(annenpartAktivitet,
                        forbrukte.getOrDefault(annenpartAktivitet, BigDecimal.ZERO).add(trekkdager));
            }
        }
        return forbrukte.values().stream().mapToInt(BigDecimal::intValue).min().orElse(0);
    }

    private boolean aktivitetIPeriode(FastsattUttakPeriode periode, AktivitetIdentifikator annenpartAktivitet) {
        return periode.getAktiviteter().stream()
                .map(a -> a.getAktivitetIdentifikator())
                .anyMatch(aktivitetIdentifikator -> aktivitetIdentifikator.equals(annenpartAktivitet));
    }

    private Set<AktivitetIdentifikator> aktiviteterForAnnenpart() {
        return aktiviteterIPerioder(annenpartsPerioder);
    }

    private BigDecimal forbruktSøker(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet) {
        BigDecimal sum = BigDecimal.ZERO;

        for (FastsattUttakPeriode periodeSøker : søkersPerioder) {
            if (erOpphold(periodeSøker)) {
                sum = sum.add(trekkdagerForOppholdsperiode(stønadskonto, periodeSøker));
            } else {
                sum = sum.add(trekkdagerForUttaksperiode(stønadskonto, aktivitet, periodeSøker));
            }
        }
        return sum;
    }

    private BigDecimal trekkdagerForUttaksperiode(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet, FastsattUttakPeriode periode) {
        for (FastsattUttakPeriodeAktivitet periodeAktivitet : periode.getAktiviteter()) {
            if (periodeAktivitet.getAktivitetIdentifikator().equals(aktivitet)) {
                if (periodeAktivitet.getTrekkonto().equals(stønadskonto)) {
                    return periodeAktivitet.getTrekkdager().decimalValue();
                } else if (stønadskonto.equals(Stønadskontotype.FLERBARNSDAGER) && periode.isFlerbarnsdager()) {
                    return periodeAktivitet.getTrekkdager().decimalValue();
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private boolean erOpphold(FastsattUttakPeriode periodeSøker) {
        return periodeSøker.getOppholdÅrsak() != null;
    }

    private BigDecimal trekkdagerForOppholdsperiode(Stønadskontotype stønadskonto, Oppholdårsaktype årsak, LocalDate delFom, LocalDate delTom) {
        Stønadskontotype stønadskontoTypeOpt = Oppholdårsaktype.map(årsak);
        if (Objects.equals(stønadskontoTypeOpt, stønadskonto)) {
            return BigDecimal.valueOf(Virkedager.beregnAntallVirkedager(delFom, delTom));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal trekkdagerForOppholdsperiode(Stønadskontotype stønadskonto, FastsattUttakPeriode periode) {
        return trekkdagerForOppholdsperiode(stønadskonto, periode.getOppholdÅrsak(), periode.getFom(), periode.getTom());
    }

    public Set<AktivitetIdentifikator> aktiviteterForSøker() {
        return stønadskontoer.stream().map(kontoForArbeidsforhold -> kontoForArbeidsforhold.getAktivitetIdentifikator()).collect(Collectors.toSet());
    }

    private Set<AktivitetIdentifikator> aktiviteterIPerioder(List<FastsattUttakPeriode> perioder) {
        return perioder.stream()
                .flatMap(p -> p.getAktiviteter().stream())
                .map(FastsattUttakPeriodeAktivitet::getAktivitetIdentifikator)
                .collect(Collectors.toSet());
    }

    private boolean søktSamtidigUttak(Stønadskontotype stønadskontoType, FastsattUttakPeriode periode) {
        return periode.getAktiviteter().stream().anyMatch(a -> a.getTrekkonto().equals(stønadskontoType));
    }

    private Trekkdager antallDagerAnnenpartKanFrigi(Stønadskontotype stønadskontoType) {
        FastsattUttakPeriode søkersSistePeriodeMedTrekkdager = søkersSistePeriodeMedTrekkdager();
        List<FastsattUttakPeriode> annenpartPerioderEtterSøkersSistePeriodeMedTrekkdager = finnAnnenpartPerioderEtterPeriode(søkersSistePeriodeMedTrekkdager);

        Trekkdager forbrukteDager = Trekkdager.ZERO;
        for (FastsattUttakPeriode annenpartPeriode : annenpartPerioderEtterSøkersSistePeriodeMedTrekkdager) {
            final Trekkdager trekkdager = antallDagerFrigitt(stønadskontoType, søkersSistePeriodeMedTrekkdager, annenpartPeriode);
            forbrukteDager = forbrukteDager.add(trekkdager);
        }
        return forbrukteDager;
    }

    private Trekkdager antallDagerFrigitt(Stønadskontotype stønadskontoType, FastsattUttakPeriode søkersSistePeriodeMedTrekkdager, FastsattUttakPeriode annenpartPeriode) {
        final Trekkdager trekkdager;
        if (overlapper(søkersSistePeriodeMedTrekkdager, annenpartPeriode)) {
            trekkdager = forbruktFraDelAvAnnenpartsPeriode(stønadskontoType, søkersSistePeriodeMedTrekkdager, annenpartPeriode);
        } else {
            trekkdager = minForbrukteDager(annenpartPeriode, stønadskontoType);
        }
        return trekkdager;
    }

    private Trekkdager forbruktFraDelAvAnnenpartsPeriode(Stønadskontotype stønadskontoType, FastsattUttakPeriode søkersSistePeriodeMedTrekkdager, FastsattUttakPeriode annenpartPeriode) {
        Optional<FastsattUttakPeriodeAktivitet> annenPartAktivitetMedKonto = aktivitetMedStønadskonto(stønadskontoType, annenpartPeriode);
        if (annenPartAktivitetMedKonto.isPresent()) {
            var frigitte = trekkDagerFraDelAvPeriode(
                    søkersSistePeriodeMedTrekkdager.getFom(),
                    annenpartPeriode.getTom(),
                    annenpartPeriode.getFom(),
                    annenpartPeriode.getTom(),
                    annenPartAktivitetMedKonto.get().getTrekkdager().decimalValue()
            );
            return new Trekkdager(frigitte);
        }
        return Trekkdager.ZERO;
    }

    private Trekkdager minForbrukteDager(FastsattUttakPeriode periode, Stønadskontotype stønadskontoType) {
        if (erOpphold(periode)) {
            return new Trekkdager(trekkdagerForOppholdsperiode(stønadskontoType, periode));
        }
        Trekkdager minForbrukt = null;
        for (FastsattUttakPeriodeAktivitet aktivitet : periode.getAktiviteter()) {
            if (Objects.equals(stønadskontoType, aktivitet.getTrekkonto())) {
                if (minForbrukt == null || minForbrukt.compareTo(aktivitet.getTrekkdager()) > 0)
                    minForbrukt = aktivitet.getTrekkdager();
            }
        }
        return minForbrukt == null ? Trekkdager.ZERO : minForbrukt;
    }

    private List<FastsattUttakPeriode> finnAnnenpartPerioderEtterPeriode(FastsattUttakPeriode periode) {
        return annenpartsPerioder.stream()
                .filter(p -> overlapper(periode, p) || periode.getTom().isBefore(p.getFom()))
                .collect(Collectors.toList());
    }

    private FastsattUttakPeriode søkersSistePeriodeMedTrekkdager() {
        return sortByReversedTom(søkersPerioder).stream().filter(this::harTrekkdager).findFirst().orElseThrow();
    }

    private boolean harTrekkdager(FastsattUttakPeriode periode) {
        for (FastsattUttakPeriodeAktivitet aktivitet : periode.getAktiviteter()) {
            if (aktivitet.getTrekkdager().merEnn0()) {
                return true;
            }
        }
        return false;
    }

    private List<FastsattUttakPeriode> sortByReversedTom(List<FastsattUttakPeriode> perioder) {
        return perioder.stream().sorted((p1, p2) -> p2.getTom().compareTo(p1.getTom())).collect(Collectors.toList());
    }

    private int minSaldo(Stønadskontotype stønadskonto) {
        Integer min = null;
        for (AktivitetIdentifikator aktivitet : aktiviteterForSøker()) {
            int saldo = saldo(stønadskonto, aktivitet);
            if (min == null || saldo < min) {
                min = saldo;
            }
        }
        return min == null ? 0 : min;
    }
}
