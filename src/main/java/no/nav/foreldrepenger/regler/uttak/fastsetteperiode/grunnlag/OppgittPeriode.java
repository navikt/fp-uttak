package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class OppgittPeriode extends LukketPeriode {

    private final Stønadskontotype stønadskontotype;
    private final BigDecimal arbeidsprosent;
    private final Set<AktivitetIdentifikator> gradertAktiviteter;
    private final OverføringÅrsak overføringÅrsak;
    private final boolean flerbarnsdager;
    private final SamtidigUttaksprosent samtidigUttaksprosent;
    private final UtsettelseÅrsak utsettelseÅrsak;
    private final OppholdÅrsak oppholdÅrsak;
    private final boolean manglendeSøktPeriode;
    private final LocalDate senestMottattDato;
    private final LocalDate tidligstMottattDato;
    private final MorsAktivitet morsAktivitet;
    private final MorsStillingsprosent morsStillingsprosent;
    private final DokumentasjonVurdering dokumentasjonVurdering;
    private Set<AktivitetIdentifikator> aktiviteter = Set.of();

    private OppgittPeriode(Stønadskontotype stønadskontotype,
                           LocalDate fom,
                           LocalDate tom,
                           boolean manglendeSøktPeriode,
                           BigDecimal arbeidsprosent,
                           Set<AktivitetIdentifikator> gradertAktiviteter,
                           OverføringÅrsak overføringÅrsak,
                           SamtidigUttaksprosent samtidigUttaksprosent,
                           boolean flerbarnsdager,
                           UtsettelseÅrsak utsettelseÅrsak,
                           OppholdÅrsak oppholdÅrsak,
                           LocalDate senestMottattDato,
                           LocalDate tidligstMottattDato,
                           MorsAktivitet morsAktivitet,
                           MorsStillingsprosent morsStillingsprosent,
                           DokumentasjonVurdering dokumentasjonVurdering) {
        super(fom, tom);
        this.arbeidsprosent = arbeidsprosent;
        this.gradertAktiviteter = gradertAktiviteter;
        this.overføringÅrsak = overføringÅrsak;
        this.samtidigUttaksprosent = samtidigUttaksprosent;
        this.flerbarnsdager = flerbarnsdager;
        this.utsettelseÅrsak = utsettelseÅrsak;
        this.oppholdÅrsak = oppholdÅrsak;
        this.stønadskontotype = stønadskontotype;
        this.manglendeSøktPeriode = manglendeSøktPeriode;
        this.senestMottattDato = senestMottattDato;
        this.tidligstMottattDato = tidligstMottattDato;
        this.morsAktivitet = morsAktivitet;
        this.morsStillingsprosent = morsStillingsprosent;
        this.dokumentasjonVurdering = dokumentasjonVurdering;
    }

    public OppgittPeriode kopiMedNyPeriode(LocalDate nyFom, LocalDate nyTom) {
        var kopi = new OppgittPeriode(stønadskontotype, nyFom, nyTom, manglendeSøktPeriode, arbeidsprosent, gradertAktiviteter, overføringÅrsak,
            samtidigUttaksprosent, flerbarnsdager, utsettelseÅrsak, oppholdÅrsak, senestMottattDato, tidligstMottattDato, morsAktivitet,
            morsStillingsprosent, dokumentasjonVurdering);
        kopi.aktiviteter = aktiviteter;
        return kopi;
    }

    public boolean erSøktGradering() {
        return arbeidsprosent != null && getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

    public Optional<LocalDate> getTidligstMottattDato() {
        return Optional.ofNullable(tidligstMottattDato);
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

    public DokumentasjonVurdering getDokumentasjonVurdering() {
        return dokumentasjonVurdering;
    }

    public static OppgittPeriode forManglendeSøkt(Stønadskontotype type, LocalDate fom, LocalDate tom) {
        return new OppgittPeriode(type, fom, tom, true, null, Set.of(), null, null, false, null, null, null, null, null, null, null);
    }

    public static OppgittPeriode forUtsettelse(LocalDate fom,
                                               LocalDate tom,
                                               UtsettelseÅrsak utsettelseÅrsak,
                                               LocalDate senestMottattDato,
                                               LocalDate tidligstMottattDato,
                                               MorsAktivitet morsAktivitet,
                                               DokumentasjonVurdering dokumentasjonVurdering) {
        return new OppgittPeriode(null, fom, tom, false, null, Set.of(), null, null, false, utsettelseÅrsak, null, senestMottattDato,
            tidligstMottattDato, morsAktivitet, null, dokumentasjonVurdering);
    }

    public static OppgittPeriode forOverføring(Stønadskontotype stønadskontotype,
                                               LocalDate fom,
                                               LocalDate tom,
                                               OverføringÅrsak overføringÅrsak,
                                               LocalDate senestMottattDato,
                                               LocalDate tidligstMottattDato,
                                               DokumentasjonVurdering dokumentasjonVurdering) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, null, Set.of(), overføringÅrsak, null, false, null, null, senestMottattDato,
            tidligstMottattDato, null, null, dokumentasjonVurdering);
    }

    public static OppgittPeriode forOpphold(LocalDate fom,
                                            LocalDate tom,
                                            OppholdÅrsak oppholdÅrsak,
                                            LocalDate senestMottattDato,
                                            LocalDate tidligstMottattDato) {
        return new OppgittPeriode(OppholdÅrsak.map(oppholdÅrsak), fom, tom, false, null, Set.of(), null, null, false, null, oppholdÅrsak,
            senestMottattDato, tidligstMottattDato, null, null, null);
    }

    public static OppgittPeriode forGradering(Stønadskontotype stønadskontotype,
                                              LocalDate fom,
                                              LocalDate tom,
                                              BigDecimal arbeidsprosent,
                                              SamtidigUttaksprosent samtidigUttaksprosent,
                                              boolean flerbarnsdager,
                                              Set<AktivitetIdentifikator> gradertAktiviteter,
                                              LocalDate senestMottattDato,
                                              LocalDate tidligstMottattDato,
                                              MorsAktivitet morsAktivitet,
                                              MorsStillingsprosent morsStillingsprosent,
                                              DokumentasjonVurdering dokumentasjonVurdering) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, arbeidsprosent, gradertAktiviteter, null, samtidigUttaksprosent, flerbarnsdager,
            null, null, senestMottattDato, tidligstMottattDato, morsAktivitet, morsStillingsprosent, dokumentasjonVurdering);
    }

    public static OppgittPeriode forVanligPeriode(Stønadskontotype stønadskontotype,
                                                  LocalDate fom,
                                                  LocalDate tom,
                                                  SamtidigUttaksprosent samtidigUttaksprosent,
                                                  boolean flerbarnsdager,
                                                  LocalDate senestMottattDato,
                                                  LocalDate tidligstMottattDato,
                                                  MorsAktivitet morsAktivitet,
                                                  MorsStillingsprosent morsStillingsprosent,
                                                  DokumentasjonVurdering dokumentasjonVurdering) {
        return new OppgittPeriode(stønadskontotype, fom, tom, false, null, Set.of(), null, samtidigUttaksprosent, flerbarnsdager, null, null,
            senestMottattDato, tidligstMottattDato, morsAktivitet, morsStillingsprosent, dokumentasjonVurdering);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (OppgittPeriode) o;
        return flerbarnsdager == that.flerbarnsdager && manglendeSøktPeriode == that.manglendeSøktPeriode && stønadskontotype == that.stønadskontotype
            && Objects.equals(arbeidsprosent, that.arbeidsprosent) && overføringÅrsak == that.overføringÅrsak && Objects.equals(samtidigUttaksprosent,
            that.samtidigUttaksprosent) && utsettelseÅrsak == that.utsettelseÅrsak && oppholdÅrsak == that.oppholdÅrsak && Objects.equals(
            senestMottattDato, that.senestMottattDato) && Objects.equals(tidligstMottattDato, that.tidligstMottattDato)
            && morsAktivitet == that.morsAktivitet && dokumentasjonVurdering == that.dokumentasjonVurdering;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stønadskontotype, arbeidsprosent, overføringÅrsak, flerbarnsdager, samtidigUttaksprosent, utsettelseÅrsak, oppholdÅrsak,
            manglendeSøktPeriode, senestMottattDato, tidligstMottattDato, morsAktivitet, dokumentasjonVurdering);
    }

    @Override
    public String toString() {
        return "OppgittPeriode{" + "stønadskontotype=" + stønadskontotype + ", fom=" + getFom() + ", tom=" + getTom() + ", arbeidsprosent="
            + arbeidsprosent + ", gradertAktiviteter=" + gradertAktiviteter + ", overføringÅrsak=" + overføringÅrsak + ", manglendeSøktPeriode="
            + manglendeSøktPeriode + ", flerbarnsdager=" + flerbarnsdager + ", samtidigUttak=" + samtidigUttaksprosent + ", utsettelseÅrsak="
            + utsettelseÅrsak + ", oppholdÅrsak=" + oppholdÅrsak + ", senestMottattDato=" + senestMottattDato + ", tidligstMottattDato="
            + tidligstMottattDato + ", aktiviteter=" + aktiviteter + ", dokumentasjonVurdering=" + dokumentasjonVurdering + '}';
    }

    public MorsStillingsprosent getMorsStillingsprosent() {
        return morsStillingsprosent;
    }

}
