package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;

public class SaldoUtregningGrunnlag {
    private List<FastsattUttakPeriode> søkersFastsattePerioder;
    private LocalDate utregningsdato;
    private boolean tapendeBehandling;
    private List<AnnenpartUttaksperiode> annenpartsPerioder;
    private Set<Arbeidsforhold> arbeidsforhold;

    private SaldoUtregningGrunnlag(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                   LocalDate utregningsdato,
                                   boolean tapendeBehandling,
                                   List<AnnenpartUttaksperiode> annenpartsPerioder,
                                   Set<Arbeidsforhold> arbeidsforhold) {
        this.søkersFastsattePerioder = søkersFastsattePerioder;
        this.utregningsdato = utregningsdato;
        this.tapendeBehandling = tapendeBehandling;
        this.annenpartsPerioder = annenpartsPerioder;
        this.arbeidsforhold = arbeidsforhold;
    }

    public static SaldoUtregningGrunnlag forUtregningAvHeleUttaket(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                   boolean tapendeBehandling,
                                                                   List<AnnenpartUttaksperiode> annenpartsPerioder,
                                                                   Set<Arbeidsforhold> arbeidsforhold) {
        return forUtregningAvDelerAvUttak(søkersFastsattePerioder, tapendeBehandling, annenpartsPerioder, arbeidsforhold, LocalDate.MAX);
    }

    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttak(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                    boolean tapendeBehandling,
                                                                    List<AnnenpartUttaksperiode> annenpartsPerioder,
                                                                    Set<Arbeidsforhold> arbeidsforhold,
                                                                    LocalDate utregningsdato) {
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, tapendeBehandling, annenpartsPerioder, arbeidsforhold);
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
}
