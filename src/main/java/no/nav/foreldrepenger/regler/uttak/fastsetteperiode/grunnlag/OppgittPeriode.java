package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public final class OppgittPeriode extends LukketPeriode {

    private final Stønadskontotype stønadskontotype;
    private final BigDecimal arbeidsprosent;
    private final Set<AktivitetIdentifikator> gradertAktiviteter;
    private final OverføringÅrsak overføringÅrsak;
    private final PeriodeVurderingType periodeVurderingType;
    private final boolean flerbarnsdager;
    private final SamtidigUttaksprosent samtidigUttaksprosent;
    private final UtsettelseÅrsak utsettelseÅrsak;
    private final OppholdÅrsak oppholdÅrsak;
    private final boolean manglendeSøktPeriode;
    private final LocalDate mottattDato;
    private Set<AktivitetIdentifikator> aktiviteter = Set.of();

    private OppgittPeriode(Stønadskontotype stønadskontotype,
                           LocalDate fom,
                           LocalDate tom,
                           boolean manglendeSøktPeriode,
                           BigDecimal arbeidsprosent,
                           Set<AktivitetIdentifikator> gradertAktiviteter,
                           OverføringÅrsak overføringÅrsak,
                           PeriodeVurderingType periodeVurderingType,
                           SamtidigUttaksprosent samtidigUttaksprosent,
                           boolean flerbarnsdager,
                           UtsettelseÅrsak utsettelseÅrsak,
                           OppholdÅrsak oppholdÅrsak,
                           LocalDate mottattDato) {
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
        this.manglendeSøktPeriode = manglendeSøktPeriode;
        this.mottattDato = mottattDato;
    }

    public OppgittPeriode kopiMedNyPeriode(LocalDate nyFom, LocalDate nyTom) {
        var kopi = new OppgittPeriode(stønadskontotype, nyFom, nyTom, manglendeSøktPeriode, arbeidsprosent, gradertAktiviteter, overføringÅrsak,
                periodeVurderingType, samtidigUttaksprosent, flerbarnsdager, utsettelseÅrsak, oppholdÅrsak, mottattDato);
        kopi.aktiviteter = aktiviteter;
        return kopi;
    }

    public boolean erSøktGradering() {
        return arbeidsprosent != null && getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

    public PeriodeVurderingType getPeriodeVurderingType() {
        return periodeVurderingType;
    }

    public Optional<LocalDate> getMottattDato() {
        return Optional.ofNullable(mottattDato);
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
        return manglendeSøktPeriode;
    }

    public static OppgittPeriode forManglendeSøkt(Stønadskontotype type, LocalDate fom, LocalDate tom) {
        return new OppgittPeriode(type, fom, tom, true, null, Set.of(), null,
                PeriodeVurderingType.IKKE_VURDERT, null, false, null, null, null);
    }

    public static OppgittPeriode forUtsettelse(LocalDate fom,
                                               LocalDate tom,
                                               PeriodeVurderingType periodeVurderingType,
                                               UtsettelseÅrsak utsettelseÅrsak,
                                               LocalDate mottattDato) {
        return new OppgittPeriode(null, fom, tom, false, null, Set.of(), null,
                periodeVurderingType, null, false, utsettelseÅrsak, null, mottattDato);
    }

    public static OppgittPeriode forOverføring(Stønadskontotype stønadskontotype,
                                               LocalDate fom,
                                               LocalDate tom,
                                               PeriodeVurderingType periodeVurderingType,
                                               OverføringÅrsak overføringÅrsak,
                                               LocalDate mottattDato) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, null, Set.of(), overføringÅrsak,
                periodeVurderingType, null, false, null, null, mottattDato);
    }

    public static OppgittPeriode forOpphold(LocalDate fom,
                                            LocalDate tom,
                                            OppholdÅrsak oppholdÅrsak,
                                            LocalDate mottattDato) {
        return new OppgittPeriode(OppholdÅrsak.map(oppholdÅrsak), fom, tom, false, null, Set.of(), null,
                PeriodeVurderingType.IKKE_VURDERT, null, false, null, oppholdÅrsak, mottattDato);
    }

    public static OppgittPeriode forGradering(Stønadskontotype stønadskontotype,
                                              LocalDate fom,
                                              LocalDate tom,
                                              BigDecimal arbeidsprosent,
                                              SamtidigUttaksprosent samtidigUttaksprosent,
                                              boolean flerbarnsdager,
                                              Set<AktivitetIdentifikator> gradertAktiviteter,
                                              PeriodeVurderingType vurderingType,
                                              LocalDate mottattDato) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, arbeidsprosent, gradertAktiviteter, null,
                vurderingType, samtidigUttaksprosent, flerbarnsdager, null, null, mottattDato);
    }

    public static OppgittPeriode forVanligPeriode(Stønadskontotype stønadskontotype,
                                                  LocalDate fom,
                                                  LocalDate tom,
                                                  SamtidigUttaksprosent samtidigUttaksprosent,
                                                  boolean flerbarnsdager,
                                                  PeriodeVurderingType periodeVurderingType,
                                                  LocalDate mottattDato) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, null, Set.of(), null,
                periodeVurderingType, samtidigUttaksprosent, flerbarnsdager, null, null, mottattDato);
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
                manglendeSøktPeriode == that.manglendeSøktPeriode &&
                Objects.equals(samtidigUttaksprosent, that.samtidigUttaksprosent) &&
                Objects.equals(mottattDato, that.mottattDato) &&
                utsettelseÅrsak == that.utsettelseÅrsak &&
                oppholdÅrsak == that.oppholdÅrsak &&
                Objects.equals(aktiviteter, that.aktiviteter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stønadskontotype, arbeidsprosent, gradertAktiviteter, overføringÅrsak, periodeVurderingType, manglendeSøktPeriode,
                flerbarnsdager, samtidigUttaksprosent, utsettelseÅrsak, oppholdÅrsak, aktiviteter, mottattDato);
    }

    @Override
    public String toString() {
        return "OppgittPeriode{" +
                "stønadskontotype=" + stønadskontotype +
                ", fom=" + getFom() +
                ", tom=" + getTom() +
                ", arbeidsprosent=" + arbeidsprosent +
                ", gradertAktiviteter=" + gradertAktiviteter +
                ", overføringÅrsak=" + overføringÅrsak +
                ", periodeVurderingType=" + periodeVurderingType +
                ", manglendeSøktPeriode=" + manglendeSøktPeriode +
                ", flerbarnsdager=" + flerbarnsdager +
                ", samtidigUttak=" + samtidigUttaksprosent +
                ", utsettelseÅrsak=" + utsettelseÅrsak +
                ", oppholdÅrsak=" + oppholdÅrsak +
                ", mottattDato=" + mottattDato +
                ", aktiviteter=" + aktiviteter +
                '}';
    }
}
