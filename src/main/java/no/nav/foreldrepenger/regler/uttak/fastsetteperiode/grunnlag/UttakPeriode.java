package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public final class UttakPeriode extends LukketPeriode {

    private final Perioderesultattype perioderesultattype;
    private final Manuellbehandlingårsak manuellbehandlingårsak;
    private final PeriodeResultatÅrsak periodeResultatÅrsak;
    private final GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak;
    private final Set<UttakPeriodeAktivitet> aktiviteter;
    private final boolean flerbarnsdager;
    private final SamtidigUttaksprosent samtidigUttaksprosent;
    private final OppholdÅrsak oppholdÅrsak;
    private final Stønadskontotype stønadskontotype;
    private final BigDecimal arbeidsprosent;
    private final UtsettelseÅrsak utsettelseÅrsak;
    private final OverføringÅrsak overføringÅrsak;
    private final boolean manglendeSøktPeriode;

    public UttakPeriode(LocalDate fom,
                        LocalDate tom,
                        Perioderesultattype perioderesultattype,
                        Manuellbehandlingårsak manuellbehandlingårsak,
                        PeriodeResultatÅrsak periodeResultatÅrsak,
                        GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                        Set<UttakPeriodeAktivitet> aktiviteter,
                        boolean flerbarnsdager,
                        SamtidigUttaksprosent samtidigUttaksprosent,
                        OppholdÅrsak oppholdÅrsak,
                        Stønadskontotype stønadskontotype,
                        BigDecimal arbeidsprosent,
                        UtsettelseÅrsak utsettelseÅrsak,
                        OverføringÅrsak overføringÅrsak,
                        boolean erManglendeSøktPeriode) {
        super(fom, tom);
        this.perioderesultattype = perioderesultattype;
        this.manuellbehandlingårsak = manuellbehandlingårsak;
        this.periodeResultatÅrsak = periodeResultatÅrsak;
        this.graderingIkkeInnvilgetÅrsak = graderingIkkeInnvilgetÅrsak;
        this.aktiviteter = aktiviteter;
        this.flerbarnsdager = flerbarnsdager;
        this.samtidigUttaksprosent = samtidigUttaksprosent;
        this.oppholdÅrsak = oppholdÅrsak;
        this.stønadskontotype = stønadskontotype;
        this.arbeidsprosent = arbeidsprosent;
        this.utsettelseÅrsak = utsettelseÅrsak;
        this.overføringÅrsak = overføringÅrsak;
        this.manglendeSøktPeriode = erManglendeSøktPeriode;
        validerKontoVedTrekkdager();
    }

    private void validerKontoVedTrekkdager() {
        if (stønadskontotype == null && getAktiviteter().stream().anyMatch(a -> a.getTrekkdager().compareTo(Trekkdager.ZERO) > 0)) {
            throw new IllegalStateException("Kan ikke trekke dager ved ukjent stønadskonto");
        }
    }

    public Perioderesultattype getPerioderesultattype() {
        return perioderesultattype;
    }

    public Manuellbehandlingårsak getManuellbehandlingårsak() {
        return manuellbehandlingårsak;
    }

    public PeriodeResultatÅrsak getPeriodeResultatÅrsak() {
        return periodeResultatÅrsak;
    }

    public GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return graderingIkkeInnvilgetÅrsak;
    }

    public Set<UttakPeriodeAktivitet> getAktiviteter() {
        return aktiviteter;
    }

    public Utbetalingsgrad getUtbetalingsgrad(AktivitetIdentifikator aktivitetIdentifikator) {
        return finnAktivitet(aktivitetIdentifikator).getUtbetalingsgrad();
    }

    public boolean erGraderingInnvilget() {
        return getAktiviteter().stream().anyMatch(a -> erGraderingInnvilget(a.getIdentifikator()));
    }

    public boolean erGraderingInnvilget(AktivitetIdentifikator aktivitetIdentifikator) {
        if (graderingIkkeInnvilgetÅrsak != null) {
            return false;
        }
        return finnAktivitet(aktivitetIdentifikator).isSøktGradering();
    }

    public Trekkdager getTrekkdager(AktivitetIdentifikator aktivitetIdentifikator) {
        return finnAktivitet(aktivitetIdentifikator).getTrekkdager();
    }

    private UttakPeriodeAktivitet finnAktivitet(AktivitetIdentifikator aktivitetIdentifikator) {
        return getAktiviteter().stream().filter(a -> a.getIdentifikator().equals(aktivitetIdentifikator)).findFirst().orElseThrow();
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public boolean isManglendeSøktPeriode() {
        return manglendeSøktPeriode;
    }

    public SamtidigUttaksprosent getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    public boolean erSamtidigUttak() {
        return getSamtidigUttaksprosent() != null;
    }

    public OppholdÅrsak getOppholdÅrsak() {
        return oppholdÅrsak;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    public UtsettelseÅrsak getUtsettelseÅrsak() {
        return utsettelseÅrsak;
    }

    public OverføringÅrsak getOverføringÅrsak() {
        return overføringÅrsak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (UttakPeriode) o;
        return flerbarnsdager == that.flerbarnsdager && perioderesultattype == that.perioderesultattype
                && manuellbehandlingårsak == that.manuellbehandlingårsak && Objects.equals(periodeResultatÅrsak,
                that.periodeResultatÅrsak) && graderingIkkeInnvilgetÅrsak == that.graderingIkkeInnvilgetÅrsak && Objects.equals(
                aktiviteter, that.aktiviteter) && Objects.equals(samtidigUttaksprosent, that.samtidigUttaksprosent)
                && oppholdÅrsak == that.oppholdÅrsak && stønadskontotype == that.stønadskontotype && Objects.equals(arbeidsprosent,
                that.arbeidsprosent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(perioderesultattype, manuellbehandlingårsak, periodeResultatÅrsak, graderingIkkeInnvilgetÅrsak,
                aktiviteter, flerbarnsdager, samtidigUttaksprosent, oppholdÅrsak, stønadskontotype, arbeidsprosent);
    }

    @Override
    public String toString() {
        return "UttakPeriode{" + "perioderesultattype=" + perioderesultattype + ", fom=" + getFom() + ", tom=" + getTom()
                + ", manuellbehandlingårsak=" + manuellbehandlingårsak + ", periodeResultatÅrsak=" + periodeResultatÅrsak
                + ", graderingIkkeInnvilgetÅrsak=" + graderingIkkeInnvilgetÅrsak + ", aktiviteter=" + aktiviteter + ", flerbarnsdager="
                + flerbarnsdager + ", samtidigUttak=" + samtidigUttaksprosent + ", oppholdÅrsak=" + oppholdÅrsak
                + ", stønadskontotype=" + stønadskontotype + ", arbeidsprosent=" + arbeidsprosent + '}';
    }
}
