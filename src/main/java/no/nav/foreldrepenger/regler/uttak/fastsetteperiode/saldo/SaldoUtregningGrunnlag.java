package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class SaldoUtregningGrunnlag {
    private final List<FastsattUttakPeriode> søkersFastsattePerioder;
    private final LocalDate utregningsdato;
    private final boolean berørtBehandling;
    private final List<AnnenpartUttakPeriode> annenpartsPerioder;
    private final List<LukketPeriode> søktePerioder;
    private final Kontoer kontoer;
    private final Set<AktivitetIdentifikator> aktiviteter;
    private final LocalDateTime sisteSøknadMottattTidspunktSøker;
    private final LocalDateTime sisteSøknadMottattTidspunktAnnenpart;
    private final Optional<LukketPeriode> farUttakRundtFødselPeriode;

    private SaldoUtregningGrunnlag(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                   LocalDate utregningsdato,
                                   boolean berørtBehandling,
                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                   List<LukketPeriode> søktePerioder,
                                   Kontoer kontoer,
                                   Set<AktivitetIdentifikator> aktiviteter,
                                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                                   Optional<LukketPeriode> farUttakRundtFødselPeriode) {
        this.søkersFastsattePerioder = søkersFastsattePerioder;
        this.utregningsdato = utregningsdato;
        this.berørtBehandling = berørtBehandling;
        this.annenpartsPerioder = annenpartsPerioder;
        this.søktePerioder = søktePerioder;
        this.kontoer = kontoer;
        this.farUttakRundtFødselPeriode = farUttakRundtFødselPeriode;
        this.aktiviteter = aktiviteter;
        this.sisteSøknadMottattTidspunktSøker = sisteSøknadMottattTidspunktSøker;
        this.sisteSøknadMottattTidspunktAnnenpart = sisteSøknadMottattTidspunktAnnenpart;
    }

    @Deprecated(forRemoval = true)
    public static SaldoUtregningGrunnlag forUtregningAvHeleUttaket(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                   boolean berørtBehandling,
                                                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                   Kontoer kontoer,
                                                                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                                                                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart) {
        var aktiviteter = søkersFastsattePerioder.stream()
                .flatMap(p -> p.getAktiviteter().stream())
                .map(a -> a.getAktivitetIdentifikator())
                .collect(Collectors.toSet());
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, LocalDate.MAX, berørtBehandling, annenpartsPerioder, List.of(),
                kontoer, aktiviteter, sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart, Optional.empty());
    }

    public static SaldoUtregningGrunnlag forUtregningAvHeleUttaket(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                   boolean berørtBehandling,
                                                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                   Kontoer kontoer,
                                                                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                                                                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                                                                   Optional<LukketPeriode> farUttakRundtFødselPeriode) {
        var aktiviteter = søkersFastsattePerioder.stream()
                .flatMap(p -> p.getAktiviteter().stream())
                .map(a -> a.getAktivitetIdentifikator())
                .collect(Collectors.toSet());
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, LocalDate.MAX, berørtBehandling, annenpartsPerioder, List.of(),
                kontoer, aktiviteter, sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart, farUttakRundtFødselPeriode);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttak(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                    List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                    Kontoer kontoer,
                                                                    LocalDate utregningsdato,
                                                                    Set<AktivitetIdentifikator> aktiviteter,
                                                                    LocalDateTime sisteSøknadMottattTidspunktSøker,
                                                                    LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                                                                    Optional<LukketPeriode> farUttakRundtFødselPeriode) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, false, annenpartsPerioder, List.of(), kontoer,
                aktiviteter, sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart, farUttakRundtFødselPeriode);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttakBerørtBehandling(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                                    List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                                    Kontoer kontoer,
                                                                                    LocalDate utregningsdato,
                                                                                    List<LukketPeriode> søktePerioder,
                                                                                    Set<AktivitetIdentifikator> aktiviteter,
                                                                                    Optional<LukketPeriode> farUttakRundtFødselPeriode) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, true, annenpartsPerioder, søktePerioder, kontoer,
                aktiviteter, null, null, farUttakRundtFødselPeriode);
    }

    List<FastsattUttakPeriode> getSøkersFastsattePerioder() {
        return søkersFastsattePerioder;
    }

    LocalDate getUtregningsdato() {
        return utregningsdato;
    }

    boolean isBerørtBehandling() {
        return berørtBehandling;
    }

    List<AnnenpartUttakPeriode> getAnnenpartsPerioder() {
        return annenpartsPerioder;
    }

    public List<LukketPeriode> getSøktePerioder() {
        return søktePerioder;
    }

    public Kontoer getKontoer() {
        return kontoer;
    }

    public Optional<LukketPeriode> getFarUttakRundtFødselPeriode() {
        return farUttakRundtFødselPeriode;
    }

    public Set<AktivitetIdentifikator> getAktiviteter() {
        return aktiviteter;
    }

    public Optional<LocalDateTime> getSisteSøknadMottattTidspunktSøker() {
        return Optional.ofNullable(sisteSøknadMottattTidspunktSøker);
    }

    public Optional<LocalDateTime> getSisteSøknadMottattTidspunktAnnenpart() {
        return Optional.ofNullable(sisteSøknadMottattTidspunktAnnenpart);
    }
}
