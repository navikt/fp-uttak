package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;

public class FastsattUttakPeriode {
    private Perioderesultattype perioderesultattype;
    private List<FastsattUttakPeriodeAktivitet> aktiviteter;
    private boolean flerbarnsdager;
    private Oppholdårsaktype oppholdÅrsak;
    private LocalDate fom;
    private LocalDate tom;
    private boolean samtidigUttak;

    private FastsattUttakPeriode() { }

    private FastsattUttakPeriode(FastsattUttakPeriode periode) {
        this.perioderesultattype = periode.perioderesultattype;
        this.aktiviteter = periode.aktiviteter;
        this.flerbarnsdager = periode.flerbarnsdager;
        this.oppholdÅrsak = periode.oppholdÅrsak;
        this.fom = periode.fom;
        this.tom = periode.tom;
        this.samtidigUttak = periode.samtidigUttak;
    }

    public Perioderesultattype getPerioderesultattype() {
        return perioderesultattype;
    }

    public List<FastsattUttakPeriodeAktivitet> getAktiviteter() {
        return aktiviteter;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public Oppholdårsaktype getOppholdÅrsak() {
        return oppholdÅrsak;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    public static class Builder {

        private FastsattUttakPeriode kladd;

        public Builder() {
            kladd = new FastsattUttakPeriode();
        }

        public Builder(FastsattUttakPeriode periode) {
            kladd = new FastsattUttakPeriode(periode);
        }

        public Builder medPeriodeResultatType(Perioderesultattype perioderesultattype) {
            kladd.perioderesultattype = perioderesultattype;
            return this;
        }

        public Builder medAktiviteter(List<FastsattUttakPeriodeAktivitet> aktiviteter) {
            kladd.aktiviteter = aktiviteter;
            return this;
        }

        public Builder medFlerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public Builder medOppholdÅrsak(Oppholdårsaktype oppholdårsaktype) {
            kladd.oppholdÅrsak = oppholdårsaktype;
            return this;
        }

        public Builder medTidsperiode(LocalDate fom, LocalDate tom) {
            kladd.fom = fom;
            kladd.tom = tom;
            return this;
        }

        public Builder medSamtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        public FastsattUttakPeriode build() {
            return kladd;
        }
    }
}