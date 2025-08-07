package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

public class SaldoUtregningGrunnlag {
    private final List<FastsattUttakPeriode> søkersFastsattePerioder;
    private final LocalDate utregningsdato;
    private final boolean tapendeBehandling;
    private final List<AnnenpartUttakPeriode> annenpartsPerioder;
    private final List<LukketPeriode> søktePerioder;
    private final Map<Stønadskontotype, Trekkdager> stønadskonti = new EnumMap<>(Stønadskontotype.class);
    private final Map<Spesialkontotype, Trekkdager> spesialkonti = new EnumMap<>(Spesialkontotype.class);
    private final Set<AktivitetIdentifikator> aktiviteter;
    private final LocalDateTime sisteSøknadMottattTidspunktSøker;
    private final LocalDateTime sisteSøknadMottattTidspunktAnnenpart;
    private final LocalDate sammenhengendeUttakTomDato;
    private final boolean annenpartEøs;

    private SaldoUtregningGrunnlag(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                   LocalDate utregningsdato,
                                   boolean tapendeBehandling,
                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                   List<LukketPeriode> søktePerioder,
                                   Kontoer kontoer,
                                   Set<AktivitetIdentifikator> aktiviteter,
                                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                                   LocalDate sammenhengendeUttakTomDato,
                                   boolean annenpartEøs) {
        this.søkersFastsattePerioder = søkersFastsattePerioder;
        this.utregningsdato = utregningsdato;
        this.tapendeBehandling = tapendeBehandling;
        this.annenpartsPerioder = annenpartsPerioder;
        this.søktePerioder = søktePerioder;
        this.aktiviteter = aktiviteter;
        this.sisteSøknadMottattTidspunktSøker = sisteSøknadMottattTidspunktSøker;
        this.sisteSøknadMottattTidspunktAnnenpart = sisteSøknadMottattTidspunktAnnenpart;
        this.sammenhengendeUttakTomDato = sammenhengendeUttakTomDato;
        this.annenpartEøs = annenpartEøs;
        kontoer.getStønadskontotyper().forEach(k -> this.stønadskonti.put(k, new Trekkdager(kontoer.getStønadskontoTrekkdager(k))));
        kontoer.getSpesialkontotyper().forEach(k -> this.spesialkonti.put(k, new Trekkdager(kontoer.getSpesialkontoTrekkdager(k))));
    }

    // Brukes av fpsak til utregning av alt
    public static SaldoUtregningGrunnlag forUtregningAvHeleUttaket(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                   boolean tapendeBehandling,
                                                                   List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                   Kontoer kontoer,
                                                                   LocalDateTime sisteSøknadMottattTidspunktSøker,
                                                                   LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                                                                   LocalDate sammenhengendeUttakTomDato,
                                                                   boolean annenpartEøs) {
        var aktiviteter = søkersFastsattePerioder.stream()
            .flatMap(p -> p.getAktiviteter().stream())
            .map(a -> a.getAktivitetIdentifikator())
            .collect(Collectors.toSet());
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, LocalDate.MAX, tapendeBehandling, annenpartsPerioder, List.of(), kontoer,
            aktiviteter, sisteSøknadMottattTidspunktSøker, sisteSøknadMottattTidspunktAnnenpart, sammenhengendeUttakTomDato, annenpartEøs);
    }

    // Brukes som input til fastsettingsregler - inneholder tidligere vedtatte før endringsdato + perioder opp til aktuell periode
    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttak(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                    List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                    RegelGrunnlag grunnlag,
                                                                    LocalDate utregningsdato) {
        var sisteSøknadMottattTidspunktAnnenpart = Optional.ofNullable(grunnlag.getAnnenPart())
            .map(AnnenPart::getSisteSøknadMottattTidspunkt)
            .orElse(null);
        var annenpartEøs = Optional.ofNullable(grunnlag.getAnnenPart()).stream().anyMatch(AnnenPart::isEøs);
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, false, annenpartsPerioder, List.of(), grunnlag.getKontoer(),
            grunnlag.getArbeid().getAktiviteter(), grunnlag.getSøknad().getMottattTidspunkt(), sisteSøknadMottattTidspunktAnnenpart,
            grunnlag.getBehandling().getSammenhengendeUttakTomDato(), annenpartEøs);
    }

    // Brukes som input til fastsettingsregler for berørte/eøs behandlinger
    public static SaldoUtregningGrunnlag forUtregningAvDelerAvUttakTapendeBehandling(List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                                                     List<AnnenpartUttakPeriode> annenpartsPerioder,
                                                                                     RegelGrunnlag grunnlag,
                                                                                     LocalDate utregningsdato,
                                                                                     List<LukketPeriode> søktePerioder) {
        var annenpartEøs = Optional.ofNullable(grunnlag.getAnnenPart()).stream().anyMatch(AnnenPart::isEøs);
        return new SaldoUtregningGrunnlag(søkersFastsattePerioder, utregningsdato, true, annenpartsPerioder, søktePerioder, grunnlag.getKontoer(),
            grunnlag.getArbeid().getAktiviteter(), null, null, grunnlag.getBehandling().getSammenhengendeUttakTomDato(), annenpartEøs);
    }

    List<FastsattUttakPeriode> getSøkersFastsattePerioder() {
        return søkersFastsattePerioder.stream().filter(p -> p.kreverSammenhengendeUttak(sammenhengendeUttakTomDato) || !p.isOpphold()).toList();
    }

    LocalDate getUtregningsdato() {
        return utregningsdato;
    }

    boolean isTapendeBehandling() {
        return tapendeBehandling;
    }

    List<AnnenpartUttakPeriode> getAnnenpartsPerioder() {
        return annenpartsPerioder.stream().filter(p -> p.kreverSammenhengendeUttak(sammenhengendeUttakTomDato) || !p.isOppholdsperiode()).toList();
    }

    public List<LukketPeriode> getSøktePerioder() {
        return søktePerioder;
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

    public Map<Stønadskontotype, Trekkdager> getStønadskonti() {
        return stønadskonti;
    }

    public Map<Spesialkontotype, Trekkdager> getSpesialkonti() {
        return spesialkonti;
    }

    public boolean isAnnenpartEøs() {
        return annenpartEøs;
    }
}
