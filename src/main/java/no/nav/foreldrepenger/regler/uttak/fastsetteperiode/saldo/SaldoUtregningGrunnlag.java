package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class SaldoUtregningGrunnlag {
    private List<FastsattUttakPeriode> søkersFastsattePerioder;
    private LocalDate utregningsdato;
    private boolean tapendeBehandling;
    private List<AnnenpartUttaksperiode> annenpartsPerioder;
    private Set<Arbeidsforhold> arbeidsforhold;
    private List<LukketPeriode> søktePerioder;
    private Kontoer kontoer;

    private SaldoUtregningGrunnlag(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                   LocalDate utregningsdato,
                                   boolean tapendeBehandling,
                                   List<AnnenpartUttaksperiode> annenpartsPerioder,
                                   Set<Arbeidsforhold> arbeidsforhold,
                                   List<LukketPeriode> søktePerioder,
                                   Kontoer kontoer) {
        this.søkersFastsattePerioder = søkersFastsattePerioder;
        this.utregningsdato = utregningsdato;
        this.tapendeBehandling = tapendeBehandling;
        this.annenpartsPerioder = annenpartsPerioder;
        this.arbeidsforhold = arbeidsforhold;
        this.søktePerioder = søktePerioder;
        this.kontoer = kontoer;
    }

    public static SaldoUtregningGrunnlag forUtregningAvHeleUttaket(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                   boolean tapendeBehandling,
                                                                   List<AnnenpartUttaksperiode> annenpartsPerioder,
                                                                   Set<Arbeidsforhold> arbeidsforhold,
                                                                   Kontoer kontoer) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, LocalDate.MAX, tapendeBehandling, annenpartsPerioder, arbeidsforhold,
                List.of(), kontoer);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttak(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                    List<AnnenpartUttaksperiode> annenpartsPerioder,
                                                                    Set<Arbeidsforhold> arbeidsforhold,
                                                                    Kontoer kontoer,
                                                                    LocalDate utregningsdato) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, false, annenpartsPerioder,
                arbeidsforhold, List.of(), kontoer);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttakTapendeBehandling(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                                     List<AnnenpartUttaksperiode> annenpartsPerioder,
                                                                                     Set<Arbeidsforhold> arbeidsforhold,
                                                                                     Kontoer kontoer,
                                                                                     LocalDate utregningsdato,
                                                                                     List<LukketPeriode> søktePerioder) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, true, annenpartsPerioder,
                arbeidsforhold, søktePerioder, kontoer);
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

    List<AnnenpartUttaksperiode> getAnnenpartsPerioder() {
        return annenpartsPerioder;
    }

    Set<Arbeidsforhold> getArbeidsforhold() {
        return arbeidsforhold;
    }

    public List<LukketPeriode> getSøktePerioder() {
        return søktePerioder;
    }

    public Kontoer getKontoer() {
        return kontoer;
    }
}
