package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FastsattUttakPeriode {
    private Perioderesultattype perioderesultattype;
    private List<FastsattUttakPeriodeAktivitet> aktiviteter;
    private boolean flerbarnsdager;
    private OppholdÅrsak oppholdÅrsak;
    private LocalDate fom;
    private LocalDate tom;
    private boolean samtidigUttak;
    private LocalDate mottattDato;

    private FastsattUttakPeriode() {
    }

    private FastsattUttakPeriode(FastsattUttakPeriode periode) {
        this.perioderesultattype = periode.perioderesultattype;
        this.aktiviteter = periode.aktiviteter;
        this.flerbarnsdager = periode.flerbarnsdager;
        this.oppholdÅrsak = periode.oppholdÅrsak;
        this.fom = periode.fom;
        this.tom = periode.tom;
        this.samtidigUttak = periode.samtidigUttak;
        this.mottattDato = periode.mottattDato;
    }

    public Perioderesultattype getPerioderesultattype() {
        return perioderesultattype;
    }

    public List<FastsattUttakPeriodeAktivitet> getAktiviteter() {
        return aktiviteter == null ? List.of() : aktiviteter;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public OppholdÅrsak getOppholdÅrsak() {
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

    public Optional<LocalDate> getMottattDato() {
        return Optional.ofNullable(mottattDato);
    }

    public static class Builder {

        private final FastsattUttakPeriode kladd;

        public Builder() {
            kladd = new FastsattUttakPeriode();
        }

        public Builder(FastsattUttakPeriode periode) {
            kladd = new FastsattUttakPeriode(periode);
        }

        public Builder periodeResultatType(Perioderesultattype perioderesultattype) {
            kladd.perioderesultattype = perioderesultattype;
            return this;
        }

        public Builder aktiviteter(List<FastsattUttakPeriodeAktivitet> aktiviteter) {
            kladd.aktiviteter = aktiviteter;
            return this;
        }

        public Builder flerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public Builder oppholdÅrsak(OppholdÅrsak oppholdÅrsak) {
            kladd.oppholdÅrsak = oppholdÅrsak;
            return this;
        }

        public Builder tidsperiode(LocalDate fom, LocalDate tom) {
            kladd.fom = fom;
            kladd.tom = tom;
            return this;
        }

        public Builder samtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        public Builder mottattDato(LocalDate mottattDato) {
            kladd.mottattDato = mottattDato;
            return this;
        }

        public FastsattUttakPeriode build() {
            return kladd;
        }
    }
}
