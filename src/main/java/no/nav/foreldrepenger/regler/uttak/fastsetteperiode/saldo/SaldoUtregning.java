package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class SaldoUtregning {

    private final Set<Stønadskonto> stønadskontoer;
    private final List<FastsattUttakPeriode> søkersPerioder;
    private final Set<AktivitetIdentifikator> søkersAktiviteter;
    private final List<FastsattUttakPeriode> annenpartsPerioder;
    private final boolean berørtBehandling;
    private final LocalDateTime sisteSøknadMottattTidspunktSøker;
    private final LocalDateTime sisteSøknadMottattTidspunktAnnenpart;
    private final Trekkdager minsterettDager;
    private final Trekkdager utenAktivitetskravDager;

    SaldoUtregning(Set<Stønadskonto> stønadskontoer, // NOSONAR
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean berørtBehandling,
                   Set<AktivitetIdentifikator> søkersAktiviteter,
                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                   Trekkdager minsterettDager,
                   Trekkdager utenAktivitetskravDager) {
        this.stønadskontoer = stønadskontoer;
        this.søkersPerioder = søkersPerioder;
        this.søkersAktiviteter = søkersAktiviteter;
        this.sisteSøknadMottattTidspunktSøker = sisteSøknadMottattTidspunktSøker;
        this.sisteSøknadMottattTidspunktAnnenpart = sisteSøknadMottattTidspunktAnnenpart;
        this.annenpartsPerioder = fjernOppholdsperioderEtterSisteUttaksdato(søkersPerioder, annenpartsPerioder);
        this.berørtBehandling = berørtBehandling;
        this.minsterettDager = minsterettDager;
        this.utenAktivitetskravDager = utenAktivitetskravDager;
    }

    SaldoUtregning(Set<Stønadskonto> stønadskontoer,
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean berørtBehandling,
                   Set<AktivitetIdentifikator> søkersAktiviteter,
                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart) {
        this(stønadskontoer, søkersPerioder, annenpartsPerioder, berørtBehandling, søkersAktiviteter,
                sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart, Trekkdager.ZERO, Trekkdager.ZERO);
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
        var forbruktSøker = forbruktSøker(stønadskonto, aktivitet, søkersPerioder);
        var forbruktAnnenpart = minForbruktAnnenpart(stønadskonto);
        //frigitte dager er dager fra annenpart som blir ledig når søker tar uttak i samme periode
        var frigitteDager = frigitteDager(stønadskonto);
        return getMaxDagerITrekkdager(stønadskonto).subtract(new Trekkdager(forbruktSøker))
                .subtract(new Trekkdager(forbruktAnnenpart))
                .add(new Trekkdager(frigitteDager));
    }

    public Trekkdager nettoSaldoJustertForMinsterett(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet, boolean kanTrekkeAvMinsterett) {
        var brutto = saldoITrekkdager(stønadskonto, aktivitet);
        var reduksjon = kanTrekkeAvMinsterett ? Trekkdager.ZERO : restSaldoMinsterett(stønadskonto, aktivitet);
        return brutto.subtract(reduksjon);

    }

    public Trekkdager restSaldoMinsterett(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet) {
        if (Stønadskontotype.FLERBARNSDAGER.equals(stønadskonto) || Trekkdager.ZERO.equals(minsterettDager)) {
            return Trekkdager.ZERO;
        }
        var forbruk = stønadskontoer().stream()
                .filter(k -> !Stønadskontotype.FLERBARNSDAGER.equals(k))
                .map(k -> forbruktSøkersMinsterett(k, aktivitet, søkersPerioder))
                .map(Trekkdager::new)
                .reduce(Trekkdager.ZERO, Trekkdager::add);
        var restsaldo = minsterettDager.subtract(forbruk);
        return restsaldo.compareTo(Trekkdager.ZERO) > 0 ? restsaldo : Trekkdager.ZERO;
    }

    public Trekkdager restSaldoDagerUtenAktivitetskrav(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet) {
        if (Stønadskontotype.FLERBARNSDAGER.equals(stønadskonto) || Trekkdager.ZERO.equals(utenAktivitetskravDager)) {
            return Trekkdager.ZERO;
        }
        var forbruk = stønadskontoer().stream()
                .filter(k -> !Stønadskontotype.FLERBARNSDAGER.equals(k))
                .map(k -> forbruktSøkersMinsterett(k, aktivitet, søkersPerioder))
                .map(Trekkdager::new)
                .reduce(Trekkdager.ZERO, Trekkdager::add);
        var restsaldo = utenAktivitetskravDager.subtract(forbruk);
        return restsaldo.compareTo(Trekkdager.ZERO) > 0 ? restsaldo : Trekkdager.ZERO;
    }

    /**
     * Saldo for angitt stønadskonto. Dersom saldo der forskjellige på aktivitetene, så blir største saldo valgt.
     *
     * @param stønadskonto angitt stønadskonto.
     * @return antall gjenstående dager for angitt stønadskonto.
     */
    public Trekkdager saldoITrekkdager(Stønadskontotype stønadskonto) {
        var max = Trekkdager.ZERO;
        for (var aktivitet : aktiviteterForSøker()) {
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
        for (var aktivitet : aktiviteterForSøker()) {
            var saldo = saldo(stønadskonto, aktivitet);
            if (max == null || saldo > max) {
                max = saldo;
            }
        }
        return max == null ? 0 : max;
    }

    /**
     * Aktivitet med stønadskonto. Hvis flere aktiviteter har stønadskonto velges den med minst trekkdager
     */
    private Optional<FastsattUttakPeriodeAktivitet> aktivitetMedStønadskonto(Stønadskontotype stønadskonto,
                                                                             FastsattUttakPeriode periode) {
        FastsattUttakPeriodeAktivitet aktivitetMedMinstTrekkdager = null;
        for (var aktivitet : periode.getAktiviteter()) {
            if ((Objects.equals(aktivitet.getStønadskontotype(), stønadskonto) || (periode.isFlerbarnsdager() && Objects.equals(
                    stønadskonto, Stønadskontotype.FLERBARNSDAGER))) && (aktivitetMedMinstTrekkdager == null
                    || aktivitet.getTrekkdager().compareTo(aktivitetMedMinstTrekkdager.getTrekkdager()) < 0)) {
                aktivitetMedMinstTrekkdager = aktivitet;
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
        return stønadskontoer.stream().map(Stønadskonto::getStønadskontotype).collect(Collectors.toSet());
    }

    /**
     * Har konto negativ saldo, ser på laveste saldo ved flere arbeidsforhold
     */
    public boolean negativSaldo(Stønadskontotype stønadskontoType) {
        return minSaldo(stønadskontoType) < 0;
    }

    public boolean nokDagerÅFrigiPåAnnenpart(Stønadskontotype stønadskontoType) {
        var saldo = minSaldo(stønadskontoType);
        if (saldo >= 0) {
            return true;
        }
        if (annenpartsPerioder.isEmpty()) {
            return false;
        }

        var antallDagerAnnenpartKanFrigi = antallDagerAnnenpartKanFrigi(stønadskontoType);

        return antallDagerAnnenpartKanFrigi.compareTo(new Trekkdager(Math.abs(saldo))) >= 0;
    }

    public boolean søktSamtidigUttak(Stønadskontotype stønadskontoType) {
        return søkersPerioder.stream()
                .filter(p -> søktSamtidigUttak(stønadskontoType, p))
                .anyMatch(FastsattUttakPeriode::isSamtidigUttak);
    }

    public boolean negativSaldoPåNoenKonto() {
        return stønadskontoer.stream().anyMatch(stønadskonto -> negativSaldo(stønadskonto.getStønadskontotype()));
    }

    public int getMaxDager(Stønadskontotype stønadskontotype) {
        return getMaxDagerITrekkdager(stønadskontotype).decimalValue().intValue();
    }

    private Trekkdager getMaxDagerITrekkdager(Stønadskontotype stønadskontotype) {
        return stønadskontoer.stream()
                .filter(stønadskonto -> stønadskonto.getStønadskontotype().equals(stønadskontotype))
                .map(stønadskonto -> stønadskonto.getMaksdager())
                .findFirst()
                .orElse(Trekkdager.ZERO);
    }

    private int frigitteDager(Stønadskontotype stønadskonto) {
        var sum = 0;
        for (var periode : søkersPerioder) {
            var overlappendePerioder = overlappendeAnnenpartPeriode(periode);
            for (var overlappendePeriode : overlappendePerioder) {
                if (erOpphold(periode) && innvilgetMedTrekkdager(overlappendePeriode)) {
                    sum += trekkdagerForOppholdsperiode(stønadskonto, periode).intValue();
                } else if (!tapendePeriode(periode, overlappendePeriode) && innvilgetMedTrekkdager(periode)) {
                    if (Objects.equals(stønadskonto, Stønadskontotype.FLERBARNSDAGER)) {
                        sum += frigitteDagerFlerbarnsdager(stønadskonto, periode, overlappendePeriode);
                    } else {
                        sum += frigitteDagerVanligeStønadskontoer(stønadskonto, periode, overlappendePeriode);
                    }
                } else if (tapendePeriode(periode, overlappendePeriode) && erOpphold(overlappendePeriode)) {
                    var delFom = overlappendePeriode.getFom()
                            .isBefore(periode.getFom()) ? periode.getFom() : overlappendePeriode.getFom();
                    var delTom = overlappendePeriode.getTom()
                            .isBefore(periode.getTom()) ? overlappendePeriode.getTom() : periode.getTom();
                    sum += trekkdagerForOppholdsperiode(stønadskonto, overlappendePeriode.getOppholdÅrsak(), delFom,
                            delTom).intValue();
                }
            }
        }
        return sum;
    }

    private boolean tapendePeriode(FastsattUttakPeriode periode, FastsattUttakPeriode overlappendePeriode) {
        if (berørtBehandling) {
            return true;
        }
        if (periode.getMottattDato().isEmpty() || overlappendePeriode.getMottattDato().isEmpty()) {
            return false;
        }
        if (periode.getMottattDato().get().isEqual(overlappendePeriode.getMottattDato().get())) {
            return sisteSøknadMottattTidspunktSøker.isBefore(sisteSøknadMottattTidspunktAnnenpart);
        }
        return periode.getMottattDato().get().isBefore(overlappendePeriode.getMottattDato().get());
    }

    private boolean innvilgetMedTrekkdager(FastsattUttakPeriode periode) {
        return !periode.getPerioderesultattype().equals(Perioderesultattype.AVSLÅTT) || periode.getAktiviteter()
                .stream()
                .anyMatch(aktivitet -> aktivitet.getTrekkdager().merEnn0());
    }

    private int frigitteDagerFlerbarnsdager(Stønadskontotype stønadskonto,
                                            FastsattUttakPeriode periode,
                                            FastsattUttakPeriode overlappende) {
        if (periode.isFlerbarnsdager() && overlappende.isFlerbarnsdager()) {
            var annenpartAktivitet = aktivitetMedStønadskonto(stønadskonto, overlappende);
            if (annenpartAktivitet.isPresent()) {
                return trekkDagerFraDelAvPeriode(periode.getFom(), periode.getTom(), overlappende.getFom(), overlappende.getTom(),
                        annenpartAktivitet.get().getTrekkdager().decimalValue());
            }
        }
        return 0;
    }

    private int frigitteDagerVanligeStønadskontoer(Stønadskontotype stønadskonto,
                                                   FastsattUttakPeriode periode,
                                                   FastsattUttakPeriode overlappende) {
        if (overlappende.isSamtidigUttak()) {
            return 0;
        }
        var frigitte = 0;
        var delFom = periode.getFom().isBefore(overlappende.getFom()) ? overlappende.getFom() : periode.getFom();
        var delTom = periode.getTom().isBefore(overlappende.getTom()) ? periode.getTom() : overlappende.getTom();
        if (erOpphold(overlappende)) {
            frigitte = trekkDagerFraDelAvPeriode(delFom, delTom, overlappende.getFom(), overlappende.getTom(),
                    trekkdagerForOppholdsperiode(stønadskonto, overlappende));
        } else if (!periode.isSamtidigUttak()) {
            var annenPartAktivitetMedKonto = aktivitetMedStønadskonto(stønadskonto, overlappende);
            if (annenPartAktivitetMedKonto.isPresent()) {
                frigitte = trekkDagerFraDelAvPeriode(delFom, delTom, overlappende.getFom(), overlappende.getTom(),
                        annenPartAktivitetMedKonto.get().getTrekkdager().decimalValue());
            }
        }
        return frigitte;
    }

    private int trekkDagerFraDelAvPeriode(LocalDate delFom,
                                          LocalDate delTom,
                                          LocalDate periodeFom,
                                          LocalDate periodeTom,
                                          BigDecimal periodeTrekkdager) {
        var virkedagerInnenfor = Virkedager.beregnAntallVirkedager(delFom, delTom);
        var virkedagerHele = Virkedager.beregnAntallVirkedager(periodeFom, periodeTom);
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
        for (var periode2 : perioder) {
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
        for (var periode : annenpartsPerioder) {
            for (var annenpartAktivitet : aktiviteterForAnnenpart()) {
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
                forbrukte.put(annenpartAktivitet, forbrukte.getOrDefault(annenpartAktivitet, BigDecimal.ZERO).add(trekkdager));
            }
        }
        return forbrukte.values().stream().mapToInt(BigDecimal::intValue).min().orElse(0);
    }

    private boolean aktivitetIPeriode(FastsattUttakPeriode periode, AktivitetIdentifikator aktivitet) {
        return periode.getAktiviteter()
                .stream()
                .map(a -> a.getAktivitetIdentifikator())
                .anyMatch(aktivitetIdentifikator -> aktivitetIdentifikator.equals(aktivitet));
    }

    private Set<AktivitetIdentifikator> aktiviteterForAnnenpart() {
        return aktiviteterIPerioder(annenpartsPerioder);
    }

    private BigDecimal forbruktSøker(Stønadskontotype stønadskonto,
                                     AktivitetIdentifikator aktivitet,
                                     List<FastsattUttakPeriode> søkersPerioder) {
        var sum = BigDecimal.ZERO;

        for (var i = 0; i < søkersPerioder.size(); i++) {
            var periodeSøker = søkersPerioder.get(i);
            if (erOpphold(periodeSøker)) {
                sum = sum.add(trekkdagerForOppholdsperiode(stønadskonto, periodeSøker));
            } else {
                var nestePeriodeSomIkkeErOpphold = nestePeriodeSomIkkeErOpphold(søkersPerioder, i);
                if (!aktivitetIPeriode(periodeSøker, aktivitet) &&
                        (nestePeriodeSomIkkeErOpphold.isEmpty() || aktivitetIPeriode(nestePeriodeSomIkkeErOpphold.get(), aktivitet))) {
                    var perioderTomPeriode = søkersPerioder.subList(0, i + 1);
                    var ekisterendeAktiviteter = aktiviteterIPerioder(perioderTomPeriode);
                    ekisterendeAktiviteter.remove(aktivitet);
                    var minForbrukteDagerEksisterendeAktiviteter = ekisterendeAktiviteter.stream()
                            .map(a -> forbruktSøker(stønadskonto, a, perioderTomPeriode))
                            .min(BigDecimal::compareTo)
                            .orElseThrow();
                    sum = sum.add(minForbrukteDagerEksisterendeAktiviteter);
                } else {
                    sum = sum.add(trekkdagerForUttaksperiode(stønadskonto, aktivitet, periodeSøker));
                }
            }
        }

        return sum;
    }

    private Optional<FastsattUttakPeriode> nestePeriodeSomIkkeErOpphold(List<FastsattUttakPeriode> perioder, int index) {
        for (var i = index + 1; i < perioder.size(); i++) {
            var periode = perioder.get(i);
            if (!erOpphold(periode)) {
                return Optional.of(periode);
            }
        }
        return Optional.empty();
    }

    private BigDecimal forbruktSøkersMinsterett(Stønadskontotype stønadskonto,
                                                AktivitetIdentifikator aktivitet,
                                                List<FastsattUttakPeriode> søkersPerioder) {
        var sum = BigDecimal.ZERO;

        for (var i = 0; i < søkersPerioder.size(); i++) {
            var periodeSøker = søkersPerioder.get(i);
            if (!periodeSøker.isForbrukMinsterett()) {
                continue;
            }
            if (erOpphold(periodeSøker)) {
                sum = sum.add(trekkdagerForOppholdsperiode(stønadskonto, periodeSøker));
            } else {
                var nestePeriodeSomForbrukerDager = nestePeriodeSomForbrukerDager(søkersPerioder, i);
                if (!aktivitetIPeriode(periodeSøker, aktivitet) &&
                        (nestePeriodeSomForbrukerDager.isEmpty() || aktivitetIPeriode(nestePeriodeSomForbrukerDager.get(), aktivitet))) {
                    var perioderTomPeriode = søkersPerioder.subList(0, i + 1);
                    var ekisterendeAktiviteter = aktiviteterIPerioder(perioderTomPeriode);
                    ekisterendeAktiviteter.remove(aktivitet);
                    var minForbrukteDagerEksisterendeAktiviteter = ekisterendeAktiviteter.stream()
                            .map(a -> forbruktSøkersMinsterett(stønadskonto, a, perioderTomPeriode))
                            .min(BigDecimal::compareTo)
                            .orElseThrow();
                    sum = sum.add(minForbrukteDagerEksisterendeAktiviteter);
                } else {
                    sum = sum.add(trekkdagerForUttaksperiode(stønadskonto, aktivitet, periodeSøker));
                }
            }
        }

        return sum;
    }

    private Optional<FastsattUttakPeriode> nestePeriodeSomForbrukerDager(List<FastsattUttakPeriode> perioder, int index) {
        for (var i = index + 1; i < perioder.size(); i++) {
            var periode = perioder.get(i);
            if (periode.isForbrukMinsterett()) {
                return Optional.of(periode);
            }
        }
        return Optional.empty();
    }

    private BigDecimal trekkdagerForUttaksperiode(Stønadskontotype stønadskonto,
                                                  AktivitetIdentifikator aktivitet,
                                                  FastsattUttakPeriode periode) {
        for (var periodeAktivitet : periode.getAktiviteter()) {
            if (periodeAktivitet.getAktivitetIdentifikator().equals(aktivitet) && (
                    Objects.equals(periodeAktivitet.getStønadskontotype(), stønadskonto) || (
                            Objects.equals(stønadskonto, Stønadskontotype.FLERBARNSDAGER) && periode.isFlerbarnsdager()))) {
                return periodeAktivitet.getTrekkdager().decimalValue();
            }
        }
        return BigDecimal.ZERO;
    }

    private boolean erOpphold(FastsattUttakPeriode periodeSøker) {
        return periodeSøker.getOppholdÅrsak() != null;
    }

    private BigDecimal trekkdagerForOppholdsperiode(Stønadskontotype stønadskonto,
                                                    OppholdÅrsak årsak,
                                                    LocalDate delFom,
                                                    LocalDate delTom) {
        var stønadskontoTypeOpt = OppholdÅrsak.map(årsak);
        if (Objects.equals(stønadskontoTypeOpt, stønadskonto)) {
            return BigDecimal.valueOf(Virkedager.beregnAntallVirkedager(delFom, delTom));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal trekkdagerForOppholdsperiode(Stønadskontotype stønadskonto, FastsattUttakPeriode periode) {
        return trekkdagerForOppholdsperiode(stønadskonto, periode.getOppholdÅrsak(), periode.getFom(), periode.getTom());
    }

    public Set<AktivitetIdentifikator> aktiviteterForSøker() {
        return søkersAktiviteter;
    }

    private Set<AktivitetIdentifikator> aktiviteterIPerioder(List<FastsattUttakPeriode> perioder) {
        return perioder.stream()
                .flatMap(p -> p.getAktiviteter().stream())
                .map(FastsattUttakPeriodeAktivitet::getAktivitetIdentifikator)
                .collect(Collectors.toSet());
    }

    private boolean søktSamtidigUttak(Stønadskontotype stønadskontoType, FastsattUttakPeriode periode) {
        return periode.getAktiviteter().stream().anyMatch(a -> Objects.equals(a.getStønadskontotype(), stønadskontoType));
    }

    private Trekkdager antallDagerAnnenpartKanFrigi(Stønadskontotype stønadskontoType) {
        var søkersSistePeriodeMedTrekkdager = søkersSistePeriodeMedTrekkdagerSomIkkeOverlapper();
        var annenpartPerioderEtterSøkersSistePeriodeMedTrekkdager = finnAnnenpartPerioderEtterPeriode(søkersSistePeriodeMedTrekkdager);

        var forbrukteDager = Trekkdager.ZERO;
        for (var annenpartPeriode : annenpartPerioderEtterSøkersSistePeriodeMedTrekkdager) {
            var trekkdager = antallDagerFrigitt(stønadskontoType, søkersSistePeriodeMedTrekkdager, annenpartPeriode);
            forbrukteDager = forbrukteDager.add(trekkdager);
        }
        return forbrukteDager;
    }

    private Trekkdager antallDagerFrigitt(Stønadskontotype stønadskontoType,
                                          FastsattUttakPeriode søkersSistePeriodeMedTrekkdager,
                                          FastsattUttakPeriode annenpartPeriode) {
        final Trekkdager trekkdager;
        if (overlapper(søkersSistePeriodeMedTrekkdager, annenpartPeriode)) {
            trekkdager = forbruktFraDelAvAnnenpartsPeriode(stønadskontoType, søkersSistePeriodeMedTrekkdager, annenpartPeriode);
        } else {
            trekkdager = minForbrukteDager(annenpartPeriode, stønadskontoType);
        }
        return trekkdager;
    }

    private Trekkdager forbruktFraDelAvAnnenpartsPeriode(Stønadskontotype stønadskontoType,
                                                         FastsattUttakPeriode søkersSistePeriodeMedTrekkdager,
                                                         FastsattUttakPeriode annenpartPeriode) {
        var annenPartAktivitetMedKonto = aktivitetMedStønadskonto(stønadskontoType,
                annenpartPeriode);
        if (annenPartAktivitetMedKonto.isPresent()) {
            var frigitte = trekkDagerFraDelAvPeriode(søkersSistePeriodeMedTrekkdager.getFom(), annenpartPeriode.getTom(),
                    annenpartPeriode.getFom(), annenpartPeriode.getTom(),
                    annenPartAktivitetMedKonto.get().getTrekkdager().decimalValue());
            return new Trekkdager(frigitte);
        }
        return Trekkdager.ZERO;
    }

    private Trekkdager minForbrukteDager(FastsattUttakPeriode periode, Stønadskontotype stønadskontoType) {
        if (erOpphold(periode)) {
            return new Trekkdager(trekkdagerForOppholdsperiode(stønadskontoType, periode));
        }
        Trekkdager minForbrukt = null;
        for (var aktivitet : periode.getAktiviteter()) {
            if (Objects.equals(stønadskontoType, aktivitet.getStønadskontotype()) && (minForbrukt == null
                    || minForbrukt.compareTo(aktivitet.getTrekkdager()) > 0)
                    || periode.isFlerbarnsdager() && Stønadskontotype.FLERBARNSDAGER.equals(stønadskontoType)) {
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

    private FastsattUttakPeriode søkersSistePeriodeMedTrekkdagerSomIkkeOverlapper() {
        var sorted = sortByReversedTom(søkersPerioder);
        var periode = sorted.stream().filter(this::harTrekkdager).filter(p -> overlappendeAnnenpartPeriode(p).isEmpty()).findFirst();
        return periode.orElse(sorted.get(sorted.size() - 1));
    }

    private boolean harTrekkdager(FastsattUttakPeriode periode) {
        for (var aktivitet : periode.getAktiviteter()) {
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
        for (var aktivitet : aktiviteterForSøker()) {
            var saldo = saldo(stønadskonto, aktivitet);
            if (min == null || saldo < min) {
                min = saldo;
            }
        }
        return min == null ? 0 : min;
    }

    private List<FastsattUttakPeriode> fjernOppholdsperioderEtterSisteUttaksdato(List<FastsattUttakPeriode> perioderSøker,
                                                                                 List<FastsattUttakPeriode> perioderAnnenpart) {
        var sisteUttaksdatoSøker = sisteUttaksdato(perioderSøker);
        var sisteUttaksdatoAnnenpart = sisteUttaksdato(perioderAnnenpart);
        if (sisteUttaksdatoSøker.isEmpty() || sisteUttaksdatoAnnenpart.isEmpty()) {
            return perioderAnnenpart;
        }
        var sisteUttaksdatoFelles = sisteUttaksdatoSøker.get()
                .isAfter(sisteUttaksdatoAnnenpart.get()) ? sisteUttaksdatoSøker.get() : sisteUttaksdatoAnnenpart.get();

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
        return new FastsattUttakPeriode.Builder(periode).tidsperiode(nyFom, nyTom).build();
    }

    private Optional<LocalDate> sisteUttaksdato(List<FastsattUttakPeriode> perioder) {
        return perioder.stream()
                .filter(periode -> !erOpphold(periode))
                .max(Comparator.comparing(FastsattUttakPeriode::getTom))
                .map(FastsattUttakPeriode::getTom);
    }
}
