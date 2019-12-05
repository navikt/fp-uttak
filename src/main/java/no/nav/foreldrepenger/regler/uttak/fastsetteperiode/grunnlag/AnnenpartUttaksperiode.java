package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class AnnenpartUttaksperiode extends LukketPeriode {

    private Set<UttakPeriodeAktivitet> aktiviteter = new HashSet<>();

    private boolean samtidigUttak;
    private boolean flerbarnsdager;
    private boolean utsettelse;
    private boolean oppholdsperiode;
    private Oppholdårsaktype oppholdårsaktype;
    private boolean innvilget;

    private AnnenpartUttaksperiode(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }

    public Set<UttakPeriodeAktivitet> getAktiviteter() {
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
        return oppholdsperiode;
    }

    public Oppholdårsaktype getOppholdårsaktype() {
        return oppholdårsaktype;
    }

    public AnnenpartUttaksperiode kopiMedNyPeriode(LocalDate fom, LocalDate tom, List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter) {
        return AnnenpartUttaksperiode.Builder.uttak(fom, tom)
                .medSamtidigUttak(this.samtidigUttak)
                .medFlerbarnsdager(this.flerbarnsdager)
                .medUtsettelse(this.innvilget)
                .medOppholdsperiode(this.oppholdsperiode)
                .medInnvilget(this.innvilget)
                .medOppholdsårsak(this.oppholdårsaktype)
                .medUttakPeriodeAktiviteter(uttakPeriodeAktiviteter).build();
    }

    public static class Builder {
        private AnnenpartUttaksperiode kladd;

        public static Builder utsettelse(LocalDate fom, LocalDate tom) {
            return new Builder(fom, tom).medUtsettelse(true);
        }

        public static Builder opphold(LocalDate fom, LocalDate tom, Oppholdårsaktype oppholdårsaktype) {
            return new Builder(fom, tom).medOppholdsperiode(true).medOppholdsårsak(oppholdårsaktype);
        }

        public static Builder uttak(LocalDate fom, LocalDate tom) {
            return new Builder(fom, tom);
        }

        private Builder(LocalDate fom, LocalDate tom) {
            kladd = new AnnenpartUttaksperiode(fom, tom);
            kladd.innvilget = true;
        }

        public Builder medUttakPeriodeAktivitet(UttakPeriodeAktivitet uttakPeriodeAktivitet) {
            kladd.aktiviteter.add(uttakPeriodeAktivitet);
            return this;
        }

        public Builder medUttakPeriodeAktiviteter(List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter) {
            kladd.aktiviteter.addAll(uttakPeriodeAktiviteter);
            return this;
        }

        public Builder medSamtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        private Builder medUtsettelse(boolean utsettelse) {
            kladd.utsettelse = utsettelse;
            return this;
        }

        private Builder medOppholdsperiode(boolean oppholdsperiode) {
            kladd.oppholdsperiode = oppholdsperiode;
            return this;
        }

        public Builder medInnvilget(boolean innvilget) {
            kladd.innvilget = innvilget;
            return this;
        }

        public Builder medOppholdsårsak(Oppholdårsaktype oppholdårsaktype) {
            kladd.oppholdårsaktype = oppholdårsaktype;
            return this;
        }

        public Builder medFlerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public AnnenpartUttaksperiode build() {
            return kladd;
        }

    }
}
