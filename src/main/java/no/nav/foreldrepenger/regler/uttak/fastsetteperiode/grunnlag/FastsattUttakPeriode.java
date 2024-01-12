package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode.ResultatÅrsak.UTSETTELSE_GYLDIG;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode.ResultatÅrsak.IKKE_OPPFYLT_SØKNADSFRIST;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode.ResultatÅrsak.INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode.ResultatÅrsak.INNVILGET_GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT;

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
    private ResultatÅrsak resultatÅrsak;
    private boolean utsettelse;
    private boolean fratrekkPleiepenger;

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
        this.resultatÅrsak = periode.resultatÅrsak;
        this.utsettelse = periode.utsettelse;
        this.fratrekkPleiepenger = periode.fratrekkPleiepenger;
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

    public boolean isForbrukMinsterett() {
        return trekkerMinsterett(perioderesultattype, resultatÅrsak, utsettelse);
    }

    public static boolean trekkerMinsterett(Perioderesultattype perioderesultattype,
                                            ResultatÅrsak resultatÅrsak,
                                            boolean utsettelse) {
        return (Perioderesultattype.INNVILGET.equals(perioderesultattype) && !erPeriodeMedGodkjentAktivitet(resultatÅrsak))
                || (Perioderesultattype.AVSLÅTT.equals(perioderesultattype) && IKKE_OPPFYLT_SØKNADSFRIST.equals(resultatÅrsak))
                || (Perioderesultattype.MANUELL_BEHANDLING.equals(perioderesultattype) && !utsettelse);
    }

    private static boolean erPeriodeMedGodkjentAktivitet(ResultatÅrsak resultatÅrsak) {
        // Inntil videre: Perioder med godkjent aktivitet iht 14-14 første ledd skal ikke gå til fratrekk på rett etter tredje ledd
        // Når logikken skal utvides til andre tilfelle - vær obs på flerbarnsdager
        return INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT.equals(resultatÅrsak) ||
                INNVILGET_GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT.equals(resultatÅrsak);
    }

    public Optional<LocalDate> getMottattDato() {
        return Optional.ofNullable(mottattDato);
    }

    public boolean isOpphold() {
        return getOppholdÅrsak() != null;
    }

    public boolean isInnvilgetGyldigUtsettelse() {
        return utsettelse && resultatÅrsak.equals(UTSETTELSE_GYLDIG);
    }

    public boolean isFratrekkPleiepenger() {
        return fratrekkPleiepenger;
    }

    public boolean isUtsettelse() {
        return utsettelse;
    }

    @Override
    public String toString() {
        return "FastsattUttakPeriode{" + "fom=" + fom + ", tom=" + tom + '}';
    }

    public enum ResultatÅrsak {
        IKKE_OPPFYLT_SØKNADSFRIST,
        INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT,
        INNVILGET_GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT,
        UTSETTELSE_GYLDIG,
        ANNET,
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

        public Builder resultatÅrsak(ResultatÅrsak resultatÅrsak) {
            kladd.resultatÅrsak = resultatÅrsak;
            return this;
        }

        public Builder utsettelse(boolean utsettelse) {
            kladd.utsettelse = utsettelse;
            return this;
        }

        public Builder oppholdÅrsak(OppholdÅrsak oppholdÅrsak) {
            kladd.oppholdÅrsak = oppholdÅrsak;
            return this;
        }

        public Builder tidsperiode(LocalDate fom, LocalDate tom) {
            if (tom.isBefore(fom)) {
                throw new IllegalArgumentException("Tom(" + tom + ") kan ikke ligge før fom(" + fom + ")");
            }
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

        public Builder fratrekkPleiepenger(boolean fratrekkPleiepenger) {
            kladd.fratrekkPleiepenger = fratrekkPleiepenger;
            return this;
        }

        public FastsattUttakPeriode build() {
            return kladd;
        }
    }
}
