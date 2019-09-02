package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class FastsattPeriodeAnnenPart extends LukketPeriode {

    private List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter = new ArrayList<>();

    private boolean samtidigUttak;
    private boolean flerbarnsdager;
    private boolean innvilgetUtsettelse;
    private boolean innvilget;

    private FastsattPeriodeAnnenPart(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }

    public List<UttakPeriodeAktivitet> getUttakPeriodeAktiviteter() {
        return Collections.unmodifiableList(uttakPeriodeAktiviteter);
    }

    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    public boolean isInnvilgetUtsettelse() {
        return innvilgetUtsettelse;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public boolean isInnvilget() {
        return innvilget;
    }

    public FastsattPeriodeAnnenPart kopiMedNyPeriode(LocalDate fom, LocalDate tom, List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter) {
        return new FastsattPeriodeAnnenPart.Builder(fom, tom)
                .medSamtidigUttak(this.samtidigUttak)
                .medFlerbarnsdager(this.flerbarnsdager)
                .medInnvilgetUtsettelse(this.innvilgetUtsettelse)
                .medUttakPeriodeAktiviteter(uttakPeriodeAktiviteter).build();
    }

    public static class Builder {
        private FastsattPeriodeAnnenPart kladd;

        public Builder(LocalDate fom, LocalDate tom) {
            kladd = new FastsattPeriodeAnnenPart(fom, tom);
            kladd.innvilget = true;
        }

        public Builder medUttakPeriodeAktivitet(UttakPeriodeAktivitet uttakPeriodeAktivitet) {
            kladd.uttakPeriodeAktiviteter.add(uttakPeriodeAktivitet);
            return this;
        }

        public Builder medUttakPeriodeAktiviteter(List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter) {
            kladd.uttakPeriodeAktiviteter.addAll(uttakPeriodeAktiviteter);
            return this;
        }

        public Builder medSamtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        public Builder medInnvilgetUtsettelse(boolean innvilgetUtsettelse) {
            kladd.innvilgetUtsettelse = innvilgetUtsettelse;
            return this;
        }

        public Builder medInnvilget(boolean innvilget) {
            kladd.innvilget = innvilget;
            return this;
        }

        public Builder medFlerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public FastsattPeriodeAnnenPart build() {
            return kladd;
        }

    }
}
