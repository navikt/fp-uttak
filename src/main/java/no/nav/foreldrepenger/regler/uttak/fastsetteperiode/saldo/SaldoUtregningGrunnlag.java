package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class SaldoUtregningGrunnlag {
    private List<FastsattUttakPeriode> søkersFastsattePerioder;
    private LocalDate utregningsdato;
    private boolean tapendeBehandling;
    private List<AnnenpartUttakPeriode> annenpartsPerioder;
    private List<LukketPeriode> søktePerioder;
    private Kontoer kontoer;
    private Set<AktivitetIdentifikator> aktiviteter;

    private SaldoUtregningGrunnlag(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                   LocalDate utregningsdato,
                                   boolean tapendeBehandling,
                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                   List<LukketPeriode> søktePerioder,
                                   Kontoer kontoer,
                                   Set<AktivitetIdentifikator> aktiviteter) {
        this.søkersFastsattePerioder = søkersFastsattePerioder;
        this.utregningsdato = utregningsdato;
        this.tapendeBehandling = tapendeBehandling;
        this.annenpartsPerioder = annenpartsPerioder;
        this.søktePerioder = søktePerioder;
        this.kontoer = kontoer;
        this.aktiviteter = aktiviteter;
    }

    public static SaldoUtregningGrunnlag forUtregningAvHeleUttaket(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                   boolean tapendeBehandling,
                                                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                   Kontoer kontoer) {
        var aktiviteter = søkersFastsattePerioder.stream()
                .flatMap(p -> p.getAktiviteter().stream()).map(a -> a.getAktivitetIdentifikator())
                .collect(Collectors.toSet());
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, LocalDate.MAX, tapendeBehandling, annenpartsPerioder,
                List.of(), kontoer, aktiviteter);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttak(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                    List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                    Kontoer kontoer,
                                                                    LocalDate utregningsdato,
                                                                    Set<AktivitetIdentifikator> aktiviteter) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, false, annenpartsPerioder, List.of(), kontoer, aktiviteter);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttakTapendeBehandling(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                                     List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                                     Kontoer kontoer,
                                                                                     LocalDate utregningsdato,
                                                                                     List<LukketPeriode> søktePerioder,
                                                                                     Set<AktivitetIdentifikator> aktiviteter) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, true, annenpartsPerioder,
                søktePerioder, kontoer, aktiviteter);
    }

    List<FastsattUttakPeriode> getSøkersFastsattePerioder() {
        return søkersFastsattePerioder;
    }

    LocalDate getUtregningsdato() {
        return utregningsdato;
    }

    boolean isTapendeBehandling() {
        return tapendeBehandling;
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

    public Set<AktivitetIdentifikator> getAktiviteter() {
        return aktiviteter;
    }
}
