package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;

public class AnnenpartUttakPeriode extends LukketPeriode {

    private final Set<AnnenpartUttakPeriodeAktivitet> aktiviteter = new HashSet<>();

    private boolean samtidigUttak;
    private boolean flerbarnsdager;
    private boolean utsettelse;
    private OppholdÅrsak oppholdÅrsak;
    private LocalDate senestMottattDato;
    private AnnenpartUttakPeriodeAvslagsårsak avslagsårsak;

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
        return avslagsårsak == null;
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

    public boolean kreverSammenhengendeUttak(LocalDate sammenhengendeUttakTomDato) {
        return !getFom().isAfter(sammenhengendeUttakTomDato);
    }

    public AnnenpartUttakPeriode kopiMedNyPeriode(LocalDate fom,
                                                  LocalDate tom,
                                                  List<AnnenpartUttakPeriodeAktivitet> annenpartUttakPeriodeAktiviteter) {
        return Builder.uttak(fom, tom)
            .samtidigUttak(this.samtidigUttak)
            .flerbarnsdager(this.flerbarnsdager)
            .utsettelse(this.utsettelse)
            .oppholdsårsak(this.oppholdÅrsak)
            .avslagsårsak(this.avslagsårsak)
            .uttakPeriodeAktiviteter(annenpartUttakPeriodeAktiviteter)
            .senestMottattDato(this.senestMottattDato)
            .build();
    }

    public boolean harTrekkdager() {
        return getAktiviteter().stream().anyMatch(a -> a.trekkdager().merEnn0());
    }

    public boolean harUtbetaling() {
        return getAktiviteter().stream().anyMatch(a -> a.utbetalingsgrad().harUtbetaling());
    }

    public Optional<LocalDate> getSenestMottattDato() {
        return Optional.ofNullable(senestMottattDato);
    }

    public Optional<AnnenpartUttakPeriodeAvslagsårsak> getAvslagsårsak() {
        return Optional.ofNullable(avslagsårsak);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var that = (AnnenpartUttakPeriode) o;
        return samtidigUttak == that.samtidigUttak && flerbarnsdager == that.flerbarnsdager && utsettelse == that.utsettelse
            && Objects.equals(aktiviteter, that.aktiviteter) && oppholdÅrsak == that.oppholdÅrsak
            && avslagsårsak == that.avslagsårsak && Objects.equals(senestMottattDato, that.senestMottattDato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aktiviteter, samtidigUttak, flerbarnsdager, utsettelse, oppholdÅrsak, senestMottattDato,
            avslagsårsak);
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

        public static Builder eøs(LocalDate fom, LocalDate tom, Stønadskontotype stønadskontotype, Trekkdager trekkdager) {
            return new Builder(fom, tom)
                .samtidigUttak(false)
                .uttakPeriodeAktivitet(
                    new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.annenAktivitet(), stønadskontotype, trekkdager, Utbetalingsgrad.FULL));
        }

        private Builder(LocalDate fom, LocalDate tom) {
            kladd = new AnnenpartUttakPeriode(fom, tom);
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

        public Builder avslagsårsak(AnnenpartUttakPeriodeAvslagsårsak avslagsårsak) {
            kladd.avslagsårsak = avslagsårsak;
            return this;
        }

        public AnnenpartUttakPeriode build() {
            return kladd;
        }

    }
}
