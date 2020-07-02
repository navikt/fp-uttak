package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde.ORKESTERING_MANGLENDE_SØKT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public final class OppgittPeriode extends LukketPeriode {

    private final Stønadskontotype stønadskontotype;
    private final BigDecimal arbeidsprosent;
    private final Set<AktivitetIdentifikator> gradertAktiviteter;
    private final OverføringÅrsak overføringÅrsak;
    private final PeriodeVurderingType periodeVurderingType;
    private final PeriodeKilde periodeKilde;
    private final boolean flerbarnsdager;
    private final SamtidigUttaksprosent samtidigUttaksprosent;
    private final UtsettelseÅrsak utsettelseÅrsak;
    private final OppholdÅrsak oppholdÅrsak;
    //TODO fjerne
    private Set<AktivitetIdentifikator> aktiviteter = Set.of();

    private OppgittPeriode(Stønadskontotype stønadskontotype,
                           LocalDate fom,
                           LocalDate tom,
                           PeriodeKilde periodeKilde,
                           BigDecimal arbeidsprosent,
                           Set<AktivitetIdentifikator> gradertAktiviteter,
                           OverføringÅrsak overføringÅrsak,
                           PeriodeVurderingType periodeVurderingType,
                           SamtidigUttaksprosent samtidigUttaksprosent,
                           boolean flerbarnsdager,
                           UtsettelseÅrsak utsettelseÅrsak,
                           OppholdÅrsak oppholdÅrsak) {
        super(fom, tom);
        this.arbeidsprosent = arbeidsprosent;
        this.gradertAktiviteter = gradertAktiviteter;
        this.overføringÅrsak = overføringÅrsak;
        this.periodeVurderingType = Objects.requireNonNull(periodeVurderingType);
        this.samtidigUttaksprosent = samtidigUttaksprosent;
        this.flerbarnsdager = flerbarnsdager;
        this.utsettelseÅrsak = utsettelseÅrsak;
        this.oppholdÅrsak = oppholdÅrsak;
        this.stønadskontotype = stønadskontotype;
        this.periodeKilde = Objects.requireNonNull(periodeKilde);
    }

    public OppgittPeriode kopiMedNyPeriode(LocalDate nyFom, LocalDate nyTom) {
        var kopi = new OppgittPeriode(stønadskontotype, nyFom, nyTom, periodeKilde, arbeidsprosent, gradertAktiviteter, overføringÅrsak,
                periodeVurderingType, samtidigUttaksprosent, flerbarnsdager, utsettelseÅrsak, oppholdÅrsak);
        kopi.aktiviteter = aktiviteter;
        return kopi;
    }

    public boolean erSøktGradering() {
        return arbeidsprosent != null && getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

    public PeriodeVurderingType getPeriodeVurderingType() {
        return periodeVurderingType;
    }

    public Set<AktivitetIdentifikator> getGradertAktiviteter() {
        return gradertAktiviteter;
    }

    public boolean erSøktGradering(AktivitetIdentifikator aktivitetIdentifikator) {
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

    public boolean erSøktSamtidigUttak() {
        return getSamtidigUttaksprosent() != null;
    }

    public PeriodeKilde getPeriodeKilde() {
        return periodeKilde;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public SamtidigUttaksprosent getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    public UtsettelseÅrsak getUtsettelseÅrsak() {
        return utsettelseÅrsak;
    }

    public boolean isUtsettelse() {
        return getUtsettelseÅrsak() != null;
    }

    public Set<AktivitetIdentifikator> getAktiviteter() {
        if (aktiviteter.isEmpty()) {
            throw new IllegalStateException("Ingen aktiviteter i periode");
        }
        return aktiviteter;
    }

    public void setAktiviteter(Set<AktivitetIdentifikator> aktiviteter) {
        this.aktiviteter = aktiviteter;
    }

    public boolean isUtsettelsePga(UtsettelseÅrsak årsak) {
        return isUtsettelse() && årsak.equals(getUtsettelseÅrsak());
    }

    public OppholdÅrsak getOppholdÅrsak() {
        return oppholdÅrsak;
    }

    public boolean isOppholdPga(OppholdÅrsak årsak) {
        return isOpphold() && årsak.equals(getOppholdÅrsak());
    }

    public boolean isOpphold() {
        return getOppholdÅrsak() != null;
    }

    public boolean isManglendeSøktPeriode() {
        return Objects.equals(ORKESTERING_MANGLENDE_SØKT, getPeriodeKilde());
    }

    public static OppgittPeriode forManglendeSøkt(Stønadskontotype type, LocalDate fom, LocalDate tom) {
        return new OppgittPeriode(type, fom, tom, ORKESTERING_MANGLENDE_SØKT, null, Set.of(), null,
                PeriodeVurderingType.IKKE_VURDERT, null, false, null, null);
    }

    public static OppgittPeriode forUtsettelse(LocalDate fom,
                                               LocalDate tom,
                                               PeriodeKilde periodeKilde,
                                               PeriodeVurderingType periodeVurderingType,
                                               UtsettelseÅrsak utsettelseÅrsak) {
        return new OppgittPeriode(null, fom, tom, periodeKilde, null, Set.of(), null,
                periodeVurderingType, null, false, utsettelseÅrsak, null);
    }

    public static OppgittPeriode forOverføring(Stønadskontotype stønadskontotype,
                                               LocalDate fom,
                                               LocalDate tom,
                                               PeriodeKilde periodeKilde,
                                               PeriodeVurderingType periodeVurderingType,
                                               OverføringÅrsak overføringÅrsak) {
        return new OppgittPeriode(stønadskontotype, fom, tom, periodeKilde, null, Set.of(), overføringÅrsak,
                periodeVurderingType, null, false, null, null);
    }

    public static OppgittPeriode forOpphold(LocalDate fom,
                                            LocalDate tom,
                                            PeriodeKilde periodeKilde,
                                            OppholdÅrsak oppholdÅrsak) {
        return new OppgittPeriode(OppholdÅrsak.map(oppholdÅrsak), fom, tom, periodeKilde, null, Set.of(), null,
                PeriodeVurderingType.IKKE_VURDERT, null, false, null, oppholdÅrsak);
    }

    public static OppgittPeriode forGradering(Stønadskontotype stønadskontotype,
                                              LocalDate fom,
                                              LocalDate tom,
                                              PeriodeKilde periodeKilde,
                                              BigDecimal arbeidsprosent,
                                              SamtidigUttaksprosent samtidigUttaksprosent,
                                              boolean flerbarnsdager,
                                              Set<AktivitetIdentifikator> gradertAktiviteter,
                                              PeriodeVurderingType vurderingType) {
        return new OppgittPeriode(stønadskontotype, fom, tom, periodeKilde, arbeidsprosent, gradertAktiviteter, null,
                vurderingType, samtidigUttaksprosent, flerbarnsdager, null, null);
    }

    public static OppgittPeriode forVanligPeriode(Stønadskontotype stønadskontotype,
                                                  LocalDate fom,
                                                  LocalDate tom,
                                                  PeriodeKilde periodeKilde,
                                                  SamtidigUttaksprosent samtidigUttaksprosent,
                                                  boolean flerbarnsdager,
                                                  PeriodeVurderingType periodeVurderingType) {
        return new OppgittPeriode(stønadskontotype, fom, tom, periodeKilde, null, Set.of(), null,
                periodeVurderingType, samtidigUttaksprosent, flerbarnsdager, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OppgittPeriode that = (OppgittPeriode) o;
        return flerbarnsdager == that.flerbarnsdager &&
                stønadskontotype == that.stønadskontotype &&
                Objects.equals(arbeidsprosent, that.arbeidsprosent) &&
                Objects.equals(gradertAktiviteter, that.gradertAktiviteter) &&
                overføringÅrsak == that.overføringÅrsak &&
                periodeVurderingType == that.periodeVurderingType &&
                periodeKilde == that.periodeKilde &&
                Objects.equals(samtidigUttaksprosent, that.samtidigUttaksprosent) &&
                utsettelseÅrsak == that.utsettelseÅrsak &&
                oppholdÅrsak == that.oppholdÅrsak &&
                Objects.equals(aktiviteter, that.aktiviteter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stønadskontotype, arbeidsprosent, gradertAktiviteter, overføringÅrsak, periodeVurderingType, periodeKilde,
                flerbarnsdager, samtidigUttaksprosent, utsettelseÅrsak, oppholdÅrsak, aktiviteter);
    }

    @Override
    public String toString() {
        return "OppgittPeriode{" +
                "stønadskontotype=" + stønadskontotype +
                ", tom=" + getTom() +
                ", fom=" + getFom() +
                ", arbeidsprosent=" + arbeidsprosent +
                ", gradertAktiviteter=" + gradertAktiviteter +
                ", overføringÅrsak=" + overføringÅrsak +
                ", periodeVurderingType=" + periodeVurderingType +
                ", periodeKilde=" + periodeKilde +
                ", flerbarnsdager=" + flerbarnsdager +
                ", samtidigUttak=" + samtidigUttaksprosent +
                ", utsettelseÅrsak=" + utsettelseÅrsak +
                ", oppholdÅrsak=" + oppholdÅrsak +
                ", aktiviteter=" + aktiviteter +
                '}';
    }
}
