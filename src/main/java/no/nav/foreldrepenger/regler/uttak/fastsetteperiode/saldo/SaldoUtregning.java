package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import static java.util.Comparator.comparing;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktivitetIPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktiviteterIPerioder;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.innvilgetMedTrekkdager;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.overlappendePeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.overlapper;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.trekkDagerFraDelAvPeriode;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FarUttakRundtFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class SaldoUtregning {

    private final Map<Stønadskontotype, Trekkdager> stønadskonti = new EnumMap<>(Stønadskontotype.class);
    private final Map<Spesialkontotype, Trekkdager> spesialkonti = new EnumMap<>(Spesialkontotype.class);
    private final List<FastsattUttakPeriode> søkersPerioder;
    private final Set<AktivitetIdentifikator> søkersAktiviteter;
    private final List<FastsattUttakPeriode> annenpartsPerioder;
    private final boolean berørtBehandling;
    private final LocalDateTime sisteSøknadMottattTidspunktSøker;
    private final LocalDateTime sisteSøknadMottattTidspunktAnnenpart;
    private final SaldoUtregningFlerbarnsdager saldoUtregningFlerbarnsdager;

    SaldoUtregning(Map<Stønadskontotype, Trekkdager> stønadskontoer, // NOSONAR
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   SaldoUtregningGrunnlag grunnlag) {
        this.søkersPerioder = søkersPerioder;
        this.annenpartsPerioder = fjernOppholdsperioderEtterSisteUttaksdato(søkersPerioder, annenpartsPerioder);
        this.søkersAktiviteter = grunnlag.getAktiviteter();
        this.sisteSøknadMottattTidspunktSøker = grunnlag.getSisteSøknadMottattTidspunktSøker().orElse(null);
        this.sisteSøknadMottattTidspunktAnnenpart = grunnlag.getSisteSøknadMottattTidspunktAnnenpart().orElse(null);
        this.berørtBehandling = grunnlag.isBerørtBehandling();
        this.saldoUtregningFlerbarnsdager = new SaldoUtregningFlerbarnsdager(søkersPerioder, this.annenpartsPerioder,
                søkersAktiviteter, grunnlag.getSpesialkontoTrekkdager(Spesialkontotype.FLERBARN),
                grunnlag.getSpesialkontoTrekkdager(Spesialkontotype.BARE_FAR_MINSTERETT));
        this.stønadskonti.putAll(stønadskontoer);
        Arrays.stream(Spesialkontotype.values()).forEach(k -> this.spesialkonti.put(k, grunnlag.getSpesialkontoTrekkdager(k)));
    }


    SaldoUtregning(Map<Stønadskontotype, Trekkdager> stønadskontoer,
                   // NOSONAR
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean berørtBehandling,
                   Set<AktivitetIdentifikator> søkersAktiviteter,
                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                   Trekkdager minsterettDager,
                   Trekkdager utenAktivitetskravDager,
                   Trekkdager flerbarnsdager,
                   Trekkdager farUttakRundtFødselDager,
                   Trekkdager toTetteFødsler) {
        this.søkersPerioder = søkersPerioder;
        this.søkersAktiviteter = søkersAktiviteter;
        this.sisteSøknadMottattTidspunktSøker = sisteSøknadMottattTidspunktSøker;
        this.sisteSøknadMottattTidspunktAnnenpart = sisteSøknadMottattTidspunktAnnenpart;
        this.annenpartsPerioder = fjernOppholdsperioderEtterSisteUttaksdato(søkersPerioder, annenpartsPerioder);
        this.berørtBehandling = berørtBehandling;
        this.saldoUtregningFlerbarnsdager = new SaldoUtregningFlerbarnsdager(søkersPerioder, this.annenpartsPerioder,
                søkersAktiviteter, flerbarnsdager, minsterettDager);
        this.stønadskonti.putAll(stønadskontoer);
        this.spesialkonti.put(Spesialkontotype.BARE_FAR_MINSTERETT, minsterettDager);
        this.spesialkonti.put(Spesialkontotype.UTEN_AKTIVITETSKRAV, utenAktivitetskravDager);
        this.spesialkonti.put(Spesialkontotype.FAR_RUNDT_FØDSEL, farUttakRundtFødselDager);
        this.spesialkonti.put(Spesialkontotype.TETTE_FØDSLER, toTetteFødsler);
    }

    SaldoUtregning(Map<Stønadskontotype, Trekkdager> stønadskontoer,
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean berørtBehandling,
                   Set<AktivitetIdentifikator> søkersAktiviteter,
                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart) {
        this(stønadskontoer, søkersPerioder, annenpartsPerioder, berørtBehandling, søkersAktiviteter,
                sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart,
                Trekkdager.ZERO, Trekkdager.ZERO, Trekkdager.ZERO, Trekkdager.ZERO, Trekkdager.ZERO);
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
        var forbruktAnnenpart = minForbruktAvPerioder(stønadskonto, annenpartsPerioder);
        //frigitte dager er dager fra annenpart som blir ledig når søker tar uttak i samme periode
        var frigitteDager = frigitteDager(stønadskonto);
        return getMaxDagerITrekkdager(stønadskonto).subtract(forbruktSøker)
                .subtract(forbruktAnnenpart)
                .add(frigitteDager);
    }

    /*
     * Skal sørge for at MSP + opphold/utsettelse ikke reduserer minsteretten
     */
    public Trekkdager nettoSaldoJustertForMinsterett(Stønadskontotype stønadskonto, AktivitetIdentifikator aktivitet, boolean kanTrekkeAvMinsterett) {
        var brutto = saldoITrekkdager(stønadskonto, aktivitet);
        if (!kanTrekkeAvMinsterett) {
            var restSaldoMinsterett = restSaldoMinsterett(aktivitet);
            var reduksjon = restSaldoMinsterett.mindreEnn0() ? Trekkdager.ZERO : restSaldoMinsterett;
            return brutto.subtract(reduksjon);
        }
        return brutto;
    }

    public Trekkdager restSaldoFlerbarnsdager() {
        return saldoUtregningFlerbarnsdager.restSaldo();
    }

    public Trekkdager restSaldoMinsterett() {
        return aktiviteterForSøker().stream()
                .map(this::restSaldoMinsterett)
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    public Trekkdager restSaldoMinsterett(AktivitetIdentifikator aktivitet) {
        if (Trekkdager.ZERO.equals(getMaxDagerMinsterett())) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvMinsterett(aktivitet);
        return getMaxDagerMinsterett().subtract(forbruk);
    }

    public Trekkdager restSaldoEtterNesteStønadsperiode() {
        return aktiviteterForSøker().stream()
                .map(this::restSaldoEtterNesteStønadsperiode)
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    public Trekkdager restSaldoEtterNesteStønadsperiode(AktivitetIdentifikator aktivitet) {
        if (Trekkdager.ZERO.equals(getMaxDagerEtterNesteStønadsperiode())) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvMinsterett(aktivitet);
        var saldo = getMaxDagerEtterNesteStønadsperiode().subtract(forbruk);
        return saldo.mindreEnn0() ? Trekkdager.ZERO : saldo;
    }

    public Trekkdager restSaldoFlerbarnsdager(AktivitetIdentifikator aktivitet) {
        return saldoUtregningFlerbarnsdager.restSaldo(aktivitet);
    }

    public Trekkdager restSaldoDagerUtenAktivitetskrav() {
        return aktiviteterForSøker().stream()
                .map(this::restSaldoDagerUtenAktivitetskrav)
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    public Trekkdager restSaldoDagerUtenAktivitetskrav(AktivitetIdentifikator aktivitet) {
        if (Trekkdager.ZERO.equals(getMaxDagerUtenAktivitetskrav())) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvMinsterett(aktivitet);
        return getMaxDagerUtenAktivitetskrav().subtract(forbruk);
    }

    private Trekkdager forbruktAvMinsterett(AktivitetIdentifikator aktivitet) {
        return stønadskontoer().stream()
                .map(k -> forbruktSøkersMinsterett(k, aktivitet, søkersPerioder))
                .reduce(Trekkdager.ZERO, Trekkdager::add);
    }

    public Trekkdager getFarUttakRundtFødselDager() {
        return spesialkonti.get(Spesialkontotype.FAR_RUNDT_FØDSEL);
    }

    public Trekkdager restSaldoFarUttakRundtFødsel(LukketPeriode farUttakRundtFødselPeriode) {
        if (Trekkdager.ZERO.equals(getFarUttakRundtFødselDager()) || farUttakRundtFødselPeriode == null) {
            return Trekkdager.ZERO;
        }
        return aktiviteterForSøker().stream()
                .map(a -> restSaldoFarUttakRundtFødsel(a, farUttakRundtFødselPeriode))
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    public Trekkdager restSaldoFarUttakRundtFødsel(AktivitetIdentifikator aktivitet, LukketPeriode farUttakRundtFødselPeriode) {
        if (Trekkdager.ZERO.equals(getFarUttakRundtFødselDager()) || farUttakRundtFødselPeriode == null) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvFarRundtFødsel(aktivitet, farUttakRundtFødselPeriode);
        return getFarUttakRundtFødselDager().subtract(forbruk);
    }

    private Trekkdager forbruktAvFarRundtFødsel(AktivitetIdentifikator aktivitet, LukketPeriode rundtFødselIntervall) {
        return stønadskontoer().stream()
                .filter(FarUttakRundtFødsel::erKontoRelevant)
                .map(k -> ForbruksTeller.forbruksTellerKontoKunForbruk(k, aktivitet, søkersPerioder, p -> erRundtFødsel(p, rundtFødselIntervall)))
                .reduce(Trekkdager.ZERO, Trekkdager::add);
    }

    private boolean erRundtFødsel(FastsattUttakPeriode periode, LukketPeriode farUttakRundtFødselPeriode) {
        var p = new LukketPeriode(periode.getFom(), periode.getTom());
        return p.erOmsluttetAv(farUttakRundtFødselPeriode);
    }

    /**
     * Saldo for angitt stønadskonto. Dersom saldo der forskjellige på aktivitetene, så blir største saldo valgt.
     *
     * @param stønadskonto angitt stønadskonto.
     * @return antall gjenstående dager for angitt stønadskonto.
     */
    public Trekkdager saldoITrekkdager(Stønadskontotype stønadskonto) {
        return aktiviteterForSøker().stream()
                .map(a -> saldoITrekkdager(stønadskonto, a))
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    /**
     * Saldo for angitt stønadskonto. Dersom saldo der forskjellige på aktivitetene, så blir største saldo valgt.
     *
     * @param stønadskonto angitt stønadskonto.
     * @return antall gjenstående dager for angitt stønadskonto.
     */
    public int saldo(Stønadskontotype stønadskonto) {
        return aktiviteterForSøker().stream()
                .map(a -> saldo(stønadskonto, a))
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    /**
     * Aktivitet med stønadskonto. Hvis flere aktiviteter har stønadskonto velges den med minst trekkdager
     */
    private Optional<FastsattUttakPeriodeAktivitet> aktivitetMedStønadskonto(Stønadskontotype stønadskonto,
                                                                             FastsattUttakPeriode periode) {
        return periode.getAktiviteter().stream()
                .filter(a -> Objects.equals(a.getStønadskontotype(), stønadskonto))
                .min(Comparator.comparing(FastsattUttakPeriodeAktivitet::getTrekkdager));
    }


    /**
     * Hvilke stønadskontoer er opprettet.
     *
     * @return et sett med stønadskontotyper.
     */
    public Set<Stønadskontotype> stønadskontoer() {
        return stønadskonti.keySet();
    }

    /**
     * Har konto negativ saldo, ser på laveste saldo ved flere arbeidsforhold
     */
    public boolean negativSaldo(Stønadskontotype stønadskontoType) {
        return minSaldo(stønadskontoType) < 0;
    }

    /**
     * Forenklet implementasjon til bruk ifm berørt-vurderinger
     */
    private boolean sjekkNegativSaldoKonservativ(Stønadskontotype stønadskontoType,
                                                 List<FastsattUttakPeriode> eneparten,
                                                 Set<AktivitetIdentifikator> aktiviteterEneparten,
                                                 List<FastsattUttakPeriode> andreparten) {
        var initSaldo = getMaxDagerITrekkdager(stønadskontoType);
        var forbruktAnnenpart = minForbruktAvPerioder(stønadskontoType, andreparten);
        var startSaldo = initSaldo.subtract(forbruktAnnenpart);
        return aktiviteterEneparten.stream()
                .anyMatch(a -> startSaldo.subtract(forbruktSøker(stønadskontoType, a, eneparten)).mindreEnn0());
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
        return stønadskonti.keySet().stream().anyMatch(this::negativSaldo);
    }

    public boolean negativSaldoPåNoenKontoKonservativ() {
        return stønadskonti.keySet().stream()
                .anyMatch(stønadskonto -> sjekkNegativSaldoKonservativ(stønadskonto, søkersPerioder, søkersAktiviteter, annenpartsPerioder));
    }

    public boolean negativSaldoPåNoenKontoByttParterKonservativ() {
        var aktiviteter = aktiviteterForAnnenpart();
        return stønadskonti.keySet().stream()
                .anyMatch(stønadskonto -> sjekkNegativSaldoKonservativ(stønadskonto, annenpartsPerioder, aktiviteter, søkersPerioder));
    }

    public int getMaxDager(Stønadskontotype stønadskontotype) {
        return getMaxDagerITrekkdager(stønadskontotype).decimalValue().intValue();
    }

    public Trekkdager getMaxDagerUtenAktivitetskrav() {
        return spesialkonti.get(Spesialkontotype.UTEN_AKTIVITETSKRAV);
    }

    public Trekkdager getMaxDagerMinsterett() {
        return spesialkonti.get(Spesialkontotype.BARE_FAR_MINSTERETT);
    }

    public Trekkdager getMaxDagerEtterNesteStønadsperiode() {
        return spesialkonti.get(Spesialkontotype.TETTE_FØDSLER);
    }

    public Trekkdager getMaxDagerFlerbarnsdager() {
        return saldoUtregningFlerbarnsdager.getMaxDagerFlerbarnsdager();
    }

    private Trekkdager getMaxDagerITrekkdager(Stønadskontotype stønadskontotype) {
        return Optional.ofNullable(stønadskonti.get(stønadskontotype)).orElse(Trekkdager.ZERO);
    }

    private Trekkdager frigitteDager(Stønadskontotype stønadskonto) {
        var sum = Trekkdager.ZERO;
        for (var periode : søkersPerioder) {
            var overlappendePerioder = overlappendePeriode(periode, annenpartsPerioder);
            for (var overlappendePeriode : overlappendePerioder) {
                if (periode.isOpphold() && innvilgetMedTrekkdager(overlappendePeriode)) {
                    sum = sum.add(trekkdagerForOppholdsperiode(stønadskonto, periode));
                } else if (!tapendePeriode(periode, overlappendePeriode) && innvilgetMedTrekkdager(periode)) {
                    sum = sum.add(frigitteDagerVanligeStønadskontoer(stønadskonto, periode, overlappendePeriode));
                } else if (tapendePeriode(periode, overlappendePeriode) && overlappendePeriode.isOpphold()) {
                    var delFom = overlappendePeriode.getFom()
                            .isBefore(periode.getFom()) ? periode.getFom() : overlappendePeriode.getFom();
                    var delTom = overlappendePeriode.getTom()
                            .isBefore(periode.getTom()) ? overlappendePeriode.getTom() : periode.getTom();
                    sum = sum.add(trekkdagerForOppholdsperiode(stønadskonto, overlappendePeriode.getOppholdÅrsak(), delFom, delTom));
                }
            }
        }
        return sum;
    }

    private boolean tapendePeriode(FastsattUttakPeriode periode, FastsattUttakPeriode overlappendePeriode) {
        if (berørtBehandling) {
            return true;
        }
        var periodeMottattDato = periode.getMottattDato();
        var overlappendePeriodeMottattDato = overlappendePeriode.getMottattDato();
        if (periodeMottattDato.isEmpty() || overlappendePeriodeMottattDato.isEmpty()) {
            return false;
        }
        if (periodeMottattDato.get().isEqual(overlappendePeriodeMottattDato.get())) {
            return sisteSøknadMottattTidspunktSøker.isBefore(sisteSøknadMottattTidspunktAnnenpart);
        }
        return periodeMottattDato.get().isBefore(overlappendePeriodeMottattDato.get());
    }

    private Trekkdager frigitteDagerVanligeStønadskontoer(Stønadskontotype stønadskonto,
                                                   FastsattUttakPeriode periode,
                                                   FastsattUttakPeriode overlappende) {
        if (overlappende.isSamtidigUttak()) {
            return Trekkdager.ZERO;
        }
        var frigitte = Trekkdager.ZERO;
        var delFom = periode.getFom().isBefore(overlappende.getFom()) ? overlappende.getFom() : periode.getFom();
        var delTom = periode.getTom().isBefore(overlappende.getTom()) ? periode.getTom() : overlappende.getTom();
        if (overlappende.isOpphold()) {
            frigitte = trekkDagerFraDelAvPeriode(delFom, delTom, overlappende.getFom(), overlappende.getTom(),
                    trekkdagerForOppholdsperiode(stønadskonto, overlappende));
        } else if (!periode.isSamtidigUttak()) {
            var annenPartAktivitetMedKonto = aktivitetMedStønadskonto(stønadskonto, overlappende);
            if (annenPartAktivitetMedKonto.isPresent()) {
                frigitte = trekkDagerFraDelAvPeriode(delFom, delTom, overlappende.getFom(), overlappende.getTom(),
                        annenPartAktivitetMedKonto.get().getTrekkdager());
            }
        }
        return frigitte;
    }



    private Trekkdager minForbruktAvPerioder(Stønadskontotype stønadskonto, List<FastsattUttakPeriode> perioder) {
        Map<AktivitetIdentifikator, Trekkdager> forbrukte = new HashMap<>();
        for (var periode : perioder) {
            for (var annenpartAktivitet : aktiviteterIPerioder(perioder)) {
                final Trekkdager trekkdager;
                if (periode.isOpphold()) {
                    trekkdager = trekkdagerForOppholdsperiode(stønadskonto, periode);
                } else {
                    if (!aktivitetIPeriode(periode, annenpartAktivitet)) {
                        trekkdager = minForbrukteDager(periode, stønadskonto);
                    } else {
                        trekkdager = trekkdagerForUttaksperiode(stønadskonto, annenpartAktivitet, periode);
                    }
                }
                forbrukte.put(annenpartAktivitet, forbrukte.getOrDefault(annenpartAktivitet, Trekkdager.ZERO).add(trekkdager));
            }
        }
        return forbrukte.values().stream().min(Trekkdager::compareTo).orElse(Trekkdager.ZERO);
    }

    private Trekkdager forbruktSøker(Stønadskontotype stønadskonto,
                                     AktivitetIdentifikator aktivitet,
                                     List<FastsattUttakPeriode> søkersPerioder) {
        return ForbruksTeller.forbruksTellerKontoMedUnntak(stønadskonto, aktivitet, søkersPerioder,
                p -> !p.isOpphold(), this::trekkdagerForOppholdsperiode);
    }

    private Trekkdager forbruktSøkersMinsterett(Stønadskontotype stønadskonto,
                                                AktivitetIdentifikator aktivitet,
                                                List<FastsattUttakPeriode> søkersPerioder) {
        return ForbruksTeller.forbruksTellerKontoKunForbruk(stønadskonto, aktivitet, søkersPerioder,
                FastsattUttakPeriode::isForbrukMinsterett);
    }


    private Trekkdager trekkdagerForUttaksperiode(Stønadskontotype stønadskonto,
                                                  AktivitetIdentifikator aktivitet,
                                                  FastsattUttakPeriode periode) {
        return periode.getAktiviteter().stream()
                .filter(a -> a.getAktivitetIdentifikator().equals(aktivitet) && Objects.equals(a.getStønadskontotype(), stønadskonto))
                .findFirst()
                .map(FastsattUttakPeriodeAktivitet::getTrekkdager).orElse(Trekkdager.ZERO);
    }

    private Trekkdager trekkdagerForOppholdsperiode(Stønadskontotype stønadskonto,
                                                    OppholdÅrsak årsak,
                                                    LocalDate delFom,
                                                    LocalDate delTom) {
        var stønadskontoFraOpphold = OppholdÅrsak.map(årsak);
        if (Objects.equals(stønadskontoFraOpphold, stønadskonto)) {
            return new Trekkdager(Virkedager.beregnAntallVirkedager(delFom, delTom));
        }
        return Trekkdager.ZERO;
    }

    private Trekkdager trekkdagerForOppholdsperiode(Stønadskontotype stønadskonto, FastsattUttakPeriode periode) {
        return trekkdagerForOppholdsperiode(stønadskonto, periode.getOppholdÅrsak(), periode.getFom(), periode.getTom());
    }

    public Set<AktivitetIdentifikator> aktiviteterForSøker() {
        return søkersAktiviteter;
    }

    public Set<AktivitetIdentifikator> aktiviteterForAnnenpart() {
        return  annenpartsPerioder.stream()
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

        return annenpartPerioderEtterSøkersSistePeriodeMedTrekkdager.stream()
                .map(p -> antallDagerFrigitt(stønadskontoType, søkersSistePeriodeMedTrekkdager, p))
                .reduce(Trekkdager.ZERO, Trekkdager::add);
    }

    private Trekkdager antallDagerFrigitt(Stønadskontotype stønadskontoType,
                                          FastsattUttakPeriode søkersSistePeriodeMedTrekkdager,
                                          FastsattUttakPeriode annenpartPeriode) {
        if (overlapper(søkersSistePeriodeMedTrekkdager, annenpartPeriode)) {
            return forbruktFraDelAvAnnenpartsPeriode(stønadskontoType, søkersSistePeriodeMedTrekkdager, annenpartPeriode);
        } else {
            return minForbrukteDager(annenpartPeriode, stønadskontoType);
        }
    }

    private Trekkdager forbruktFraDelAvAnnenpartsPeriode(Stønadskontotype stønadskontoType,
                                                         FastsattUttakPeriode søkersSistePeriodeMedTrekkdager,
                                                         FastsattUttakPeriode annenpartPeriode) {
        return aktivitetMedStønadskonto(stønadskontoType, annenpartPeriode)
                .map(FastsattUttakPeriodeAktivitet::getTrekkdager)
                .map(a -> trekkDagerFraDelAvPeriode(søkersSistePeriodeMedTrekkdager.getFom(), annenpartPeriode.getTom(),
                        annenpartPeriode.getFom(), annenpartPeriode.getTom(), a))
                .orElse(Trekkdager.ZERO);
    }

    private Trekkdager minForbrukteDager(FastsattUttakPeriode periode, Stønadskontotype stønadskontoType) {
        if (periode.isOpphold()) {
            return trekkdagerForOppholdsperiode(stønadskontoType, periode);
        }
        return periode.getAktiviteter().stream()
                .filter(a -> Objects.equals(stønadskontoType, a.getStønadskontotype()))
                .map(FastsattUttakPeriodeAktivitet::getTrekkdager)
                .min(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    private List<FastsattUttakPeriode> finnAnnenpartPerioderEtterPeriode(FastsattUttakPeriode periode) {
        return annenpartsPerioder.stream()
                .filter(p -> overlapper(periode, p) || periode.getTom().isBefore(p.getFom()))
                .collect(Collectors.toList());
    }

    private FastsattUttakPeriode søkersSistePeriodeMedTrekkdagerSomIkkeOverlapper() {
        var sorted = sortByReversedTom(søkersPerioder);
        var periode = sorted.stream().filter(this::harTrekkdager).filter(p -> overlappendePeriode(p, annenpartsPerioder).isEmpty()).findFirst();
        return periode.orElse(sorted.get(sorted.size() - 1));
    }

    private boolean harTrekkdager(FastsattUttakPeriode periode) {
        return periode.getAktiviteter().stream().map(FastsattUttakPeriodeAktivitet::getTrekkdager).anyMatch(Trekkdager::merEnn0);
    }

    private List<FastsattUttakPeriode> sortByReversedTom(List<FastsattUttakPeriode> perioder) {
        return perioder.stream().sorted((p1, p2) -> p2.getTom().compareTo(p1.getTom())).collect(Collectors.toList());
    }

    private int minSaldo(Stønadskontotype stønadskonto) {
        return aktiviteterForSøker().stream()
                .map(a -> saldo(stønadskonto, a))
                .min(Comparator.naturalOrder())
                .orElse(0);
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
            if (periode.isOpphold()) {
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
                .filter(periode -> !periode.isOpphold())
                .max(comparing(FastsattUttakPeriode::getTom))
                .map(FastsattUttakPeriode::getTom);
    }
}
