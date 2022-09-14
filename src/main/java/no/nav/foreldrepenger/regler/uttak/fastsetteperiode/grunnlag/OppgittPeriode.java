package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

//TODO builder
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
    private final LocalDate senestMottattDato;
    private final LocalDate tidligstMottattDato;
    private final MorsAktivitet morsAktivitet;
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
                           LocalDate senestMottattDato,
                           LocalDate tidligstMottattDato,
                           MorsAktivitet morsAktivitet) {
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
        this.senestMottattDato = senestMottattDato;
        this.tidligstMottattDato = tidligstMottattDato;
        this.morsAktivitet = morsAktivitet;
    }

    public OppgittPeriode kopiMedNyPeriode(LocalDate nyFom, LocalDate nyTom) {
        var kopi = new OppgittPeriode(stønadskontotype, nyFom, nyTom, manglendeSøktPeriode, arbeidsprosent, gradertAktiviteter,
                overføringÅrsak, periodeVurderingType, samtidigUttaksprosent, flerbarnsdager, utsettelseÅrsak, oppholdÅrsak,
                senestMottattDato, tidligstMottattDato, morsAktivitet);
        kopi.aktiviteter = aktiviteter;
        return kopi;
    }

    public boolean erSøktGradering() {
        return arbeidsprosent != null && getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

    public Optional<LocalDate> getTidligstMottattDato() {
        return Optional.ofNullable(tidligstMottattDato);
    }

    public PeriodeVurderingType getPeriodeVurderingType() {
        return periodeVurderingType;
    }

    public Optional<LocalDate> getSenestMottattDato() {
        return Optional.ofNullable(senestMottattDato);
    }

    public Set<AktivitetIdentifikator> getGradertAktiviteter() {
        return gradertAktiviteter;
    }

    public boolean erSøktGradering(AktivitetIdentifikator aktivitetIdentifikator) {
        for (var gradertAktivitet : gradertAktiviteter) {
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

    public MorsAktivitet getMorsAktivitet() {
        return morsAktivitet;
    }

    public boolean kanTrekkeAvMinsterett() {
        return !manglendeSøktPeriode && oppholdÅrsak == null && utsettelseÅrsak == null;
    }

    public boolean gjelderPeriodeMinsterett() {
        return kanTrekkeAvMinsterett();
    }

    public static OppgittPeriode forManglendeSøkt(Stønadskontotype type, LocalDate fom, LocalDate tom) {
        return new OppgittPeriode(type, fom, tom, true, null, Set.of(), null, PeriodeVurderingType.IKKE_VURDERT, null, false, null,
                null, null, null, null);
    }

    public static OppgittPeriode forUtsettelse(LocalDate fom,
                                               LocalDate tom,
                                               UtsettelseÅrsak utsettelseÅrsak,
                                               LocalDate senestMottattDato,
                                               LocalDate tidligstMottattDato,
                                               MorsAktivitet morsAktivitet) {
        return new OppgittPeriode(null, fom, tom, false, null, Set.of(), null, PeriodeVurderingType.IKKE_VURDERT, null, false, utsettelseÅrsak,
                null, senestMottattDato, tidligstMottattDato, morsAktivitet);
    }

    public static OppgittPeriode forOverføring(Stønadskontotype stønadskontotype,
                                               LocalDate fom,
                                               LocalDate tom,
                                               OverføringÅrsak overføringÅrsak,
                                               LocalDate senestMottattDato,
                                               LocalDate tidligstMottattDato) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, null, Set.of(), overføringÅrsak, PeriodeVurderingType.IKKE_VURDERT, null,
                false, null, null, senestMottattDato, tidligstMottattDato, null);
    }

    public static OppgittPeriode forOpphold(LocalDate fom, LocalDate tom, OppholdÅrsak oppholdÅrsak, LocalDate senestMottattDato,
                                            LocalDate tidligstMottattDato) {
        return new OppgittPeriode(OppholdÅrsak.map(oppholdÅrsak), fom, tom, false, null, Set.of(), null,
                PeriodeVurderingType.IKKE_VURDERT, null, false, null, oppholdÅrsak, senestMottattDato, tidligstMottattDato, null);
    }

    public static OppgittPeriode forGradering(Stønadskontotype stønadskontotype,
                                              LocalDate fom,
                                              LocalDate tom,
                                              BigDecimal arbeidsprosent,
                                              SamtidigUttaksprosent samtidigUttaksprosent,
                                              boolean flerbarnsdager,
                                              Set<AktivitetIdentifikator> gradertAktiviteter,
                                              PeriodeVurderingType vurderingType,
                                              LocalDate senestMottattDato,
                                              LocalDate tidligstMottattDato,
                                              MorsAktivitet morsAktivitet) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, arbeidsprosent, gradertAktiviteter, null, vurderingType,
                samtidigUttaksprosent, flerbarnsdager, null, null, senestMottattDato, tidligstMottattDato, morsAktivitet);
    }

    public static OppgittPeriode forVanligPeriode(Stønadskontotype stønadskontotype,
                                                  LocalDate fom,
                                                  LocalDate tom,
                                                  SamtidigUttaksprosent samtidigUttaksprosent,
                                                  boolean flerbarnsdager,
                                                  PeriodeVurderingType periodeVurderingType,
                                                  LocalDate senestMottattDato,
                                                  LocalDate tidligstMottattDato,
                                                  MorsAktivitet morsAktivitet) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, null, Set.of(), null, periodeVurderingType, samtidigUttaksprosent,
                flerbarnsdager, null, null, senestMottattDato, tidligstMottattDato, morsAktivitet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (OppgittPeriode) o;
        return flerbarnsdager == that.flerbarnsdager && manglendeSøktPeriode == that.manglendeSøktPeriode
                && stønadskontotype == that.stønadskontotype && Objects.equals(arbeidsprosent, that.arbeidsprosent)
                && overføringÅrsak == that.overføringÅrsak && periodeVurderingType == that.periodeVurderingType && Objects.equals(
                samtidigUttaksprosent, that.samtidigUttaksprosent) && utsettelseÅrsak == that.utsettelseÅrsak
                && oppholdÅrsak == that.oppholdÅrsak && Objects.equals(senestMottattDato, that.senestMottattDato)
                && Objects.equals(tidligstMottattDato, that.tidligstMottattDato)
                && morsAktivitet == that.morsAktivitet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stønadskontotype, arbeidsprosent, overføringÅrsak, periodeVurderingType, flerbarnsdager,
                samtidigUttaksprosent, utsettelseÅrsak, oppholdÅrsak, manglendeSøktPeriode, senestMottattDato, tidligstMottattDato,
                morsAktivitet);
    }

    @Override
    public String toString() {
        return "OppgittPeriode{" + "stønadskontotype=" + stønadskontotype + ", fom=" + getFom() + ", tom=" + getTom()
                + ", arbeidsprosent=" + arbeidsprosent + ", gradertAktiviteter=" + gradertAktiviteter + ", overføringÅrsak="
                + overføringÅrsak + ", periodeVurderingType=" + periodeVurderingType + ", manglendeSøktPeriode=" + manglendeSøktPeriode
                + ", flerbarnsdager=" + flerbarnsdager + ", samtidigUttak=" + samtidigUttaksprosent + ", utsettelseÅrsak="
                + utsettelseÅrsak + ", oppholdÅrsak=" + oppholdÅrsak + ", senestMottattDato=" + senestMottattDato
                + ", tidligstMottattDato=" + tidligstMottattDato + ", aktiviteter=" + aktiviteter
                + '}';
    }
}
