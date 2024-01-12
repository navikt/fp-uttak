package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AnnenpartUttakPeriode extends LukketPeriode {

    private final Set<AnnenpartUttakPeriodeAktivitet> aktiviteter = new HashSet<>();

    private boolean samtidigUttak;
    private boolean flerbarnsdager;
    private boolean utsettelse;
    private OppholdÅrsak oppholdÅrsak;
    private boolean innvilget;
    private LocalDate senestMottattDato;

    private AnnenpartUttakPeriode(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }

    public Set<AnnenpartUttakPeriodeAktivitet> getAktiviteter() {
        return new HashSet<>(aktiviteter);
    }

    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public boolean isInnvilget() {
        return innvilget;
    }

    public boolean isUtsettelse() {
        return utsettelse;
    }

    public boolean isOppholdsperiode() {
        return getOppholdÅrsak() != null;
    }

    public OppholdÅrsak getOppholdÅrsak() {
        return oppholdÅrsak;
    }

    public AnnenpartUttakPeriode kopiMedNyPeriode(LocalDate fom,
                                                  LocalDate tom,
                                                  List<AnnenpartUttakPeriodeAktivitet> annenpartUttakPeriodeAktiviteter) {
        return Builder.uttak(fom, tom)
                .samtidigUttak(this.samtidigUttak)
                .flerbarnsdager(this.flerbarnsdager)
                .utsettelse(this.innvilget)
                .innvilget(this.innvilget)
                .oppholdsårsak(this.oppholdÅrsak)
                .uttakPeriodeAktiviteter(annenpartUttakPeriodeAktiviteter)
                .senestMottattDato(this.senestMottattDato)
                .build();
    }

    public boolean harTrekkdager() {
        return getAktiviteter().stream().anyMatch(a -> a.getTrekkdager().merEnn0());
    }

    public boolean harUtbetaling() {
        return getAktiviteter().stream().anyMatch(a -> a.getUtbetalingsgrad().harUtbetaling());
    }

    public Optional<LocalDate> getSenestMottattDato() {
        return Optional.ofNullable(senestMottattDato);
    }

    public boolean isFratrekkPleiepenger() {
        var avslåttUtsettelse = isUtsettelse() && !isInnvilget();
        var trekkerFellesEllerForeldrepenger = aktiviteter.stream()
            .anyMatch(a -> a.getTrekkdager().merEnn0() && !a.getUtbetalingsgrad().harUtbetaling() && Set.of(FELLESPERIODE, FORELDREPENGER)
                .contains(a.getStønadskontotype()));
        return avslåttUtsettelse && trekkerFellesEllerForeldrepenger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        var that = (AnnenpartUttakPeriode) o;
        return samtidigUttak == that.samtidigUttak && flerbarnsdager == that.flerbarnsdager && utsettelse == that.utsettelse
                && innvilget == that.innvilget && Objects.equals(aktiviteter, that.aktiviteter) && oppholdÅrsak == that.oppholdÅrsak
            && Objects.equals(senestMottattDato, that.senestMottattDato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aktiviteter, samtidigUttak, flerbarnsdager, utsettelse, oppholdÅrsak,
                innvilget, senestMottattDato);
    }

    public static class Builder {
        private final AnnenpartUttakPeriode kladd;

        public static Builder utsettelse(LocalDate fom, LocalDate tom) {
            return new Builder(fom, tom).utsettelse(true);
        }

        public static Builder opphold(LocalDate fom, LocalDate tom, OppholdÅrsak oppholdÅrsak) {
            return new Builder(fom, tom).oppholdsårsak(oppholdÅrsak);
        }

        public static Builder uttak(LocalDate fom, LocalDate tom) {
            return new Builder(fom, tom);
        }

        private Builder(LocalDate fom, LocalDate tom) {
            kladd = new AnnenpartUttakPeriode(fom, tom);
            kladd.innvilget = true;
        }

        public Builder uttakPeriodeAktivitet(AnnenpartUttakPeriodeAktivitet annenpartUttakPeriodeAktivitet) {
            kladd.aktiviteter.add(annenpartUttakPeriodeAktivitet);
            return this;
        }

        public Builder uttakPeriodeAktiviteter(List<AnnenpartUttakPeriodeAktivitet> annenpartUttakPeriodeAktiviteter) {
            kladd.aktiviteter.addAll(annenpartUttakPeriodeAktiviteter);
            return this;
        }

        public Builder samtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        private Builder utsettelse(boolean utsettelse) {
            kladd.utsettelse = utsettelse;
            return this;
        }

        public Builder innvilget(boolean innvilget) {
            kladd.innvilget = innvilget;
            return this;
        }

        public Builder oppholdsårsak(OppholdÅrsak oppholdÅrsak) {
            kladd.oppholdÅrsak = oppholdÅrsak;
            return this;
        }

        public Builder flerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public Builder senestMottattDato(LocalDate senestMottattDato) {
            kladd.senestMottattDato = senestMottattDato;
            return this;
        }

        public AnnenpartUttakPeriode build() {
            return kladd;
        }

    }
}
