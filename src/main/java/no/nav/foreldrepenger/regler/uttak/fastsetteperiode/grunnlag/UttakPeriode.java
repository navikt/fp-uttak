package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Årsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public abstract class UttakPeriode extends LukketPeriode {

    private Stønadskontotype stønadskontotype;
    private BigDecimal gradertArbeidsprosent;
    private List<AktivitetIdentifikator> gradertAktiviteter = List.of();
    private OverføringÅrsak overføringÅrsak;
    private PeriodeVurderingType periodeVurderingType;
    private PeriodeKilde periodeKilde;
    private final boolean flerbarnsdager;
    private final SamtidigUttak samtidigUttak;

    //TODO PFP-8744 dele opp klassen. En input-periode og en output
    private Perioderesultattype perioderesultattype = Perioderesultattype.IKKE_FASTSATT;
    private Manuellbehandlingårsak manuellbehandlingårsak;
    private Årsak årsak;
    private GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak;
    private Map<AktivitetIdentifikator, BigDecimal> utbetalingsgrader = new HashMap<>();
    private Map<AktivitetIdentifikator, Boolean> skalTrekkedager = new HashMap<>();
    private Set<AktivitetIdentifikator> aktiviteter = new HashSet<>();

    public UttakPeriode(Stønadskontotype stønadskontotype,
                        PeriodeKilde periodeKilde,
                        LocalDate fom,
                        LocalDate tom,
                        SamtidigUttak samtidigUttak,
                        boolean flerbarnsdager) {
        super(fom, tom);
        this.samtidigUttak = samtidigUttak;
        this.flerbarnsdager = flerbarnsdager;
        Objects.requireNonNull(stønadskontotype);
        Objects.requireNonNull(periodeKilde);
        this.stønadskontotype = stønadskontotype;
        this.periodeKilde = periodeKilde;
    }

    protected UttakPeriode(UttakPeriode kilde, LocalDate fom, LocalDate tom) {
        this(kilde.stønadskontotype, kilde.periodeKilde, fom, tom, kilde.getSamtidigUttak().orElse(null), kilde.isFlerbarnsdager());
        perioderesultattype = kilde.perioderesultattype;
        årsak = kilde.årsak;
        manuellbehandlingårsak = kilde.manuellbehandlingårsak;
        gradertArbeidsprosent = kilde.gradertArbeidsprosent;
        gradertAktiviteter = kilde.gradertAktiviteter;
        overføringÅrsak = kilde.overføringÅrsak;
        periodeVurderingType = kilde.periodeVurderingType;
        skalTrekkedager = new HashMap<>(kilde.skalTrekkedager);
        aktiviteter = new HashSet<>(kilde.aktiviteter);
    }

    public Optional<SamtidigUttak> getSamtidigUttak() {
        return Optional.ofNullable(samtidigUttak);
    }

    public Optional<BigDecimal> getSamtidigUttaksprosent() {
        if (samtidigUttak == null) {
            return Optional.empty();
        }
        if (isGradering()) {
            return Optional.of(BigDecimal.valueOf(100).subtract(getGradertArbeidsprosent()));
        }
        return Optional.of(samtidigUttak.getProsent());
    }

    public PeriodeVurderingType getPeriodeVurderingType() {
        return periodeVurderingType;
    }

    public BigDecimal getUtbetalingsgrad(AktivitetIdentifikator aktivitetIdentifikator) {
        return utbetalingsgrader.get(aktivitetIdentifikator);
    }

    public void setUtbetalingsgrad(AktivitetIdentifikator aktivitetIdentifikator, BigDecimal utbetalingsgrad) {
        this.utbetalingsgrader.put(aktivitetIdentifikator, utbetalingsgrad);
    }

    void setGradertAktivitet(List<AktivitetIdentifikator> gradertAktivitet, BigDecimal prosentArbeid) {
        Objects.requireNonNull(gradertAktivitet);
        Objects.requireNonNull(prosentArbeid);
        this.gradertAktiviteter = gradertAktivitet;
        this.gradertArbeidsprosent = prosentArbeid;
    }

    public List<AktivitetIdentifikator> getGradertAktiviteter() {
        return gradertAktiviteter;
    }

    public boolean isGradering() {
        return !gradertAktiviteter.isEmpty() && graderingIkkeInnvilgetÅrsak == null;
    }

    public boolean isGradering(AktivitetIdentifikator aktivitetIdentifikator) {
        if (graderingIkkeInnvilgetÅrsak != null) {
            return false;
        }
        return søktGradering(aktivitetIdentifikator);
    }

    public boolean søktGradering(AktivitetIdentifikator aktivitetIdentifikator) {
        for (AktivitetIdentifikator gradertAktivitet : gradertAktiviteter) {
            if (Objects.equals(gradertAktivitet, aktivitetIdentifikator)) {
                return true;
            }
        }
        return false;
    }

    public boolean harSøktOmOverføringAvKvote() {
        return overføringÅrsak != null;
    }

    public OverføringÅrsak getOverføringÅrsak() {
        return overføringÅrsak;
    }

    public void setOverføringÅrsak(OverføringÅrsak overføringÅrsak) {
        this.overføringÅrsak = overføringÅrsak;
    }

    public abstract <T extends UttakPeriode> T kopiMedNyPeriode(LocalDate fom, LocalDate tom);

    public PeriodeKilde getPeriodeKilde() {
        return periodeKilde;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public Perioderesultattype getPerioderesultattype() {
        return perioderesultattype;
    }

    public Årsak getÅrsak() {
        return årsak;
    }

    public Manuellbehandlingårsak getManuellbehandlingårsak() {
        return manuellbehandlingårsak;
    }

    public Trekkdager getTrekkdager(AktivitetIdentifikator aktivitetIdentifikator) {
        return getTrekkdagerFraSluttpunkt(aktivitetIdentifikator);
    }

    abstract Trekkdager getTrekkdagerFraSluttpunkt(AktivitetIdentifikator aktivitetIdentifikator);

    public BigDecimal getGradertArbeidsprosent() {
        return gradertArbeidsprosent;
    }

    public void opphevGradering(GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        this.graderingIkkeInnvilgetÅrsak = graderingIkkeInnvilgetÅrsak;
    }

    public GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return graderingIkkeInnvilgetÅrsak;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public void setPeriodeVurderingType(PeriodeVurderingType periodeVurderingType) {
        this.periodeVurderingType = periodeVurderingType;
    }

    public void setSluttpunktTrekkerDager(AktivitetIdentifikator aktivitetIdentifikator, boolean sluttpunktTrekkerDager) {
        this.skalTrekkedager.put(aktivitetIdentifikator, sluttpunktTrekkerDager);
    }

    public boolean getSluttpunktTrekkerDager(AktivitetIdentifikator aktivitetIdentifikator) {
        var trekkerDager = skalTrekkedager.get(aktivitetIdentifikator);
        if (trekkerDager != null) {
            return trekkerDager;
        }
        throw new IllegalArgumentException("Ukjent aktivitet: " + aktivitetIdentifikator);
    }


    public boolean getSkalTrekkedager() {
        return skalTrekkedager.values().stream().anyMatch(b -> b);
    }

    public void setStønadskontotype(Stønadskontotype stønadskontotype) {
        this.stønadskontotype = stønadskontotype;
    }

    public void setPerioderesultattype(Perioderesultattype perioderesultattype) {
        this.perioderesultattype = perioderesultattype;
    }

    public void setÅrsak(Årsak årsak) {
        this.årsak = årsak;
    }

    public void setManuellbehandlingårsak(Manuellbehandlingårsak manuellbehandlingårsak) {
        this.manuellbehandlingårsak = manuellbehandlingårsak;
    }

    public boolean isSamtidigUttak() {
        return getSamtidigUttak().isPresent();
    }

    public abstract boolean isUtsettelsePgaFerie();

    public Set<AktivitetIdentifikator> getAktiviteter() {
        return new HashSet<>(aktiviteter);
    }

    public void setAktiviteter(Set<AktivitetIdentifikator> aktiviteter) {
        this.aktiviteter = new HashSet<>(aktiviteter);
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
        UttakPeriode that = (UttakPeriode) o;
        return stønadskontotype == that.stønadskontotype &&
                perioderesultattype == that.perioderesultattype &&
                periodeKilde == that.periodeKilde &&
                manuellbehandlingårsak == that.manuellbehandlingårsak &&
                Objects.equals(årsak, that.årsak) &&
                Objects.equals(gradertArbeidsprosent, that.gradertArbeidsprosent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stønadskontotype, perioderesultattype, periodeKilde,
                manuellbehandlingårsak, årsak, gradertArbeidsprosent);
    }

    @Override
    public String toString() {
        return "UttakPeriode{" +
                "stønadskontotype=" + stønadskontotype +
                ", gradertArbeidsprosent=" + gradertArbeidsprosent +
                ", gradertAktiviteter=" + gradertAktiviteter +
                ", overføringÅrsak=" + overføringÅrsak +
                ", periodeVurderingType=" + periodeVurderingType +
                ", periodeKilde=" + periodeKilde +
                ", flerbarnsdager=" + flerbarnsdager +
                ", samtidigUttak=" + samtidigUttak +
                ", perioderesultattype=" + perioderesultattype +
                ", manuellbehandlingårsak=" + manuellbehandlingårsak +
                ", årsak=" + årsak +
                ", graderingIkkeInnvilgetÅrsak=" + graderingIkkeInnvilgetÅrsak +
                ", utbetalingsgrader=" + utbetalingsgrader +
                ", skalTrekkedager=" + skalTrekkedager +
                ", aktiviteter=" + aktiviteter +
                '}';
    }
}
