package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

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
    private final SaldoUtregningFlerbarnsdager saldoUtregningFlerbarnsdager;
    private final Optional<LukketPeriode> farUttakRundtFødselPeriode;
    private final Trekkdager farUttakRundtFødselDager;

    SaldoUtregning(Set<Stønadskonto> stønadskontoer, // NOSONAR
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   SaldoUtregningGrunnlag grunnlag) {
        this.stønadskontoer = stønadskontoer;
        this.søkersPerioder = søkersPerioder;
        this.annenpartsPerioder = fjernOppholdsperioderEtterSisteUttaksdato(søkersPerioder, annenpartsPerioder);
        this.søkersAktiviteter = grunnlag.getAktiviteter();
        this.sisteSøknadMottattTidspunktSøker = grunnlag.getSisteSøknadMottattTidspunktSøker().orElse(null);
        this.sisteSøknadMottattTidspunktAnnenpart = grunnlag.getSisteSøknadMottattTidspunktAnnenpart().orElse(null);
        this.berørtBehandling = grunnlag.isBerørtBehandling();
        this.minsterettDager = new Trekkdager(grunnlag.getKontoer().getMinsterettDager());
        this.utenAktivitetskravDager = new Trekkdager(grunnlag.getKontoer().getUtenAktivitetskravDager());
        this.saldoUtregningFlerbarnsdager = new SaldoUtregningFlerbarnsdager(søkersPerioder, this.annenpartsPerioder,
                søkersAktiviteter, new Trekkdager(grunnlag.getKontoer().getFlerbarnsdager()));
        this.farUttakRundtFødselPeriode = grunnlag.getFarUttakRundtFødselPeriode();
        this.farUttakRundtFødselDager = new Trekkdager(grunnlag.getKontoer().getFarUttakRundtFødselDager());
    }


    SaldoUtregning(Set<Stønadskonto> stønadskontoer, // NOSONAR
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean berørtBehandling,
                   Set<AktivitetIdentifikator> søkersAktiviteter,
                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                   Trekkdager minsterettDager,
                   Trekkdager utenAktivitetskravDager,
                   Trekkdager flerbarnsdager,
                   Optional<LukketPeriode> farUttakRundtFødselPeriode,
                   Trekkdager farUttakRundtFødselDager) {
        this.stønadskontoer = stønadskontoer;
        this.søkersPerioder = søkersPerioder;
        this.søkersAktiviteter = søkersAktiviteter;
        this.sisteSøknadMottattTidspunktSøker = sisteSøknadMottattTidspunktSøker;
        this.sisteSøknadMottattTidspunktAnnenpart = sisteSøknadMottattTidspunktAnnenpart;
        this.annenpartsPerioder = fjernOppholdsperioderEtterSisteUttaksdato(søkersPerioder, annenpartsPerioder);
        this.berørtBehandling = berørtBehandling;
        this.minsterettDager = minsterettDager;
        this.utenAktivitetskravDager = utenAktivitetskravDager;
        this.saldoUtregningFlerbarnsdager = new SaldoUtregningFlerbarnsdager(søkersPerioder, this.annenpartsPerioder,
                søkersAktiviteter, flerbarnsdager);
        this.farUttakRundtFødselPeriode = farUttakRundtFødselPeriode;
        this.farUttakRundtFødselDager = farUttakRundtFødselDager;
    }

    SaldoUtregning(Set<Stønadskonto> stønadskontoer,
                   List<FastsattUttakPeriode> søkersPerioder,
                   List<FastsattUttakPeriode> annenpartsPerioder,
                   boolean berørtBehandling,
                   Set<AktivitetIdentifikator> søkersAktiviteter,
                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart) {
        this(stønadskontoer, søkersPerioder, annenpartsPerioder, berørtBehandling, søkersAktiviteter,
                sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart,
                Trekkdager.ZERO, Trekkdager.ZERO, Trekkdager.ZERO, Optional.empty(), Trekkdager.ZERO);
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
        if (Trekkdager.ZERO.equals(minsterettDager)) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvMinsterett(aktivitet);
        return minsterettDager.subtract(forbruk);
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
        if (Trekkdager.ZERO.equals(utenAktivitetskravDager)) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvMinsterett(aktivitet);
        return utenAktivitetskravDager.subtract(forbruk);
    }

    private Trekkdager forbruktAvMinsterett(AktivitetIdentifikator aktivitet) {
        return stønadskontoer().stream()
                .map(k -> forbruktSøkersMinsterett(k, aktivitet, søkersPerioder))
                .reduce(Trekkdager.ZERO, Trekkdager::add);
    }

    public Trekkdager getFarUttakRundtFødselDager() {
        return farUttakRundtFødselDager;
    }

    public boolean harFarUttakRundtFødselPeriode() {
        return farUttakRundtFødselPeriode.isPresent();
    }

    public boolean erPeriodeRelevantForFarUttakRundtFødselDager(OppgittPeriode periode) {
        return farUttakRundtFødselPeriode.filter(periode::erOmsluttetAv).isPresent();
    }

    public Trekkdager restSaldoFarUttakRundtFødsel() {
        if (Trekkdager.ZERO.equals(getFarUttakRundtFødselDager()) || farUttakRundtFødselPeriode.isEmpty()) {
            return Trekkdager.ZERO;
        }
        return aktiviteterForSøker().stream()
                .map(this::restSaldoFarUttakRundtFødsel)
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    public Trekkdager restSaldoFarUttakRundtFødsel(AktivitetIdentifikator aktivitet) {
        if (Trekkdager.ZERO.equals(getFarUttakRundtFødselDager()) || farUttakRundtFødselPeriode.isEmpty()) {
            return Trekkdager.ZERO;
        }
        var forbruk = forbruktAvFarRundtFødsel(aktivitet);
        return farUttakRundtFødselDager.subtract(forbruk);
    }

    private Trekkdager forbruktAvFarRundtFødsel(AktivitetIdentifikator aktivitet) {
        return stønadskontoer().stream()
                .map(k -> forbruktFarRundtFødsel(k, aktivitet, søkersPerioder))
                .reduce(Trekkdager.ZERO, Trekkdager::add);
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
            if ((Objects.equals(aktivitet.getStønadskontotype(), stønadskonto)) && (aktivitetMedMinstTrekkdager == null
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

    /**
     * Forenklet implementasjon til bruk ifm berørt-vurderinger
     */
    private boolean sjekkNegativSaldoKonservativ(Stønadskontotype stønadskontoType,
                                                 List<FastsattUttakPeriode> eneparten,
                                                 Set<AktivitetIdentifikator> aktiviteterEneparten,
                                                 List<FastsattUttakPeriode> andreparten) {
        var startSaldo = getMaxDagerITrekkdager(stønadskontoType);
        var forbruktAnnenpart = minForbruktAvPerioder(stønadskontoType, andreparten);
        for (var aktivitet : aktiviteterEneparten) {
            var forbruktSøker = forbruktSøker(stønadskontoType, aktivitet, eneparten);
            var saldoTD =  startSaldo.subtract(forbruktSøker).subtract(forbruktAnnenpart);
            if (saldoTD.mindreEnn0()) {
                return true;
            }
        }
        return false;
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

    public boolean negativSaldoPåNoenKontoKonservativ() {
        return stønadskontoer.stream()
                .anyMatch(stønadskonto -> sjekkNegativSaldoKonservativ(stønadskonto.getStønadskontotype(), søkersPerioder, søkersAktiviteter, annenpartsPerioder));
    }

    public boolean negativSaldoPåNoenKontoByttParterKonservativ() {
        var aktiviteter = aktiviteterForAnnenpart();
        return stønadskontoer.stream()
                .anyMatch(stønadskonto -> sjekkNegativSaldoKonservativ(stønadskonto.getStønadskontotype(), annenpartsPerioder, aktiviteter, søkersPerioder));
    }

    public int getMaxDager(Stønadskontotype stønadskontotype) {
        return getMaxDagerITrekkdager(stønadskontotype).decimalValue().intValue();
    }

    public Trekkdager getMaxDagerUtenAktivitetskrav() {
        return Optional.ofNullable(utenAktivitetskravDager).orElse(Trekkdager.ZERO);
    }

    public Trekkdager getMaxDagerMinsterett() {
        return Optional.ofNullable(minsterettDager).orElse(Trekkdager.ZERO);
    }

    public Trekkdager getMaxDagerFlerbarnsdager() {
        return saldoUtregningFlerbarnsdager.getMaxDagerFlerbarnsdager();
    }

    private Trekkdager getMaxDagerITrekkdager(Stønadskontotype stønadskontotype) {
        return stønadskontoer.stream()
                .filter(stønadskonto -> stønadskonto.getStønadskontotype().equals(stønadskontotype))
                .map(stønadskonto -> stønadskonto.getMaksdager())
                .findFirst()
                .orElse(Trekkdager.ZERO);
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
                FastsattUttakPeriode::isOpphold, this::trekkdagerForOppholdsperiode);
    }

    private Trekkdager forbruktSøkersMinsterett(Stønadskontotype stønadskonto,
                                                AktivitetIdentifikator aktivitet,
                                                List<FastsattUttakPeriode> søkersPerioder) {
        return ForbruksTeller.forbruksTellerKontoKunForbruk(stønadskonto, aktivitet, søkersPerioder, p -> !p.isForbrukMinsterett());
    }



    private Trekkdager forbruktFarRundtFødsel(Stønadskontotype stønadskonto,
                                              AktivitetIdentifikator aktivitet,
                                              List<FastsattUttakPeriode> søkersPerioder) {
        return ForbruksTeller.forbruksTellerKontoKunForbruk(stønadskonto, aktivitet, søkersPerioder,
                p -> !(new LukketPeriode(p.getFom(), p.getTom())).erOmsluttetAv(farUttakRundtFødselPeriode.orElseThrow()));
    }

    private Trekkdager trekkdagerForUttaksperiode(Stønadskontotype stønadskonto,
                                                  AktivitetIdentifikator aktivitet,
                                                  FastsattUttakPeriode periode) {
        for (var periodeAktivitet : periode.getAktiviteter()) {
            if (periodeAktivitet.getAktivitetIdentifikator().equals(aktivitet) &&
                    (Objects.equals(periodeAktivitet.getStønadskontotype(), stønadskonto))) {
                return periodeAktivitet.getTrekkdager();
            }
        }
        return Trekkdager.ZERO;
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
            return trekkDagerFraDelAvPeriode(søkersSistePeriodeMedTrekkdager.getFom(), annenpartPeriode.getTom(),
                    annenpartPeriode.getFom(), annenpartPeriode.getTom(),
                    annenPartAktivitetMedKonto.get().getTrekkdager());
        }
        return Trekkdager.ZERO;
    }

    private Trekkdager minForbrukteDager(FastsattUttakPeriode periode, Stønadskontotype stønadskontoType) {
        if (periode.isOpphold()) {
            return trekkdagerForOppholdsperiode(stønadskontoType, periode);
        }
        Trekkdager minForbrukt = null;
        for (var aktivitet : periode.getAktiviteter()) {
            if (Objects.equals(stønadskontoType, aktivitet.getStønadskontotype()) && (minForbrukt == null
                    || minForbrukt.compareTo(aktivitet.getTrekkdager()) > 0)) {
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
        var periode = sorted.stream().filter(this::harTrekkdager).filter(p -> overlappendePeriode(p, annenpartsPerioder).isEmpty()).findFirst();
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
                .max(Comparator.comparing(FastsattUttakPeriode::getTom))
                .map(FastsattUttakPeriode::getTom);
    }
}
