package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class UtsettelsePeriode extends UttakPeriode {

    private Utsettelseårsaktype utsettelseårsaktype;

    public UtsettelsePeriode(PeriodeKilde periodeKilde,
                             LocalDate fom,
                             LocalDate tom,
                             Utsettelseårsaktype utsettelseårsaktype,
                             PeriodeVurderingType vurderingType) {
        super(Stønadskontotype.UKJENT, periodeKilde, fom, tom, null, false);
        this.utsettelseårsaktype = utsettelseårsaktype;
        setPeriodeVurderingType(vurderingType);
    }

    public UtsettelsePeriode(UtsettelsePeriode kilde, LocalDate fom, LocalDate tom) {
        super(kilde, fom, tom);
        this.utsettelseårsaktype = kilde.utsettelseårsaktype;
    }

    public Utsettelseårsaktype getUtsettelseårsaktype() {
        return utsettelseårsaktype;
    }

    @Override
    public UtsettelsePeriode kopiMedNyPeriode(LocalDate fom, LocalDate tom) {
        return new UtsettelsePeriode(this, fom, tom);
    }

    @Override
    public Trekkdager getTrekkdagerFraSluttpunkt(AktivitetIdentifikator aktivitetIdentifikator) {
        if (Perioderesultattype.INNVILGET.equals(getPerioderesultattype())) {
            return Trekkdager.ZERO;
        }
        if (getSluttpunktTrekkerDager(aktivitetIdentifikator)) {
            return new Trekkdager(Virkedager.beregnAntallVirkedager(this));
        }
        return Trekkdager.ZERO;
    }

    @Override
    public boolean isUtsettelsePgaFerie() {
        return getUtsettelseårsaktype().equals(Utsettelseårsaktype.FERIE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UtsettelsePeriode that = (UtsettelsePeriode) o;

        return utsettelseårsaktype == that.utsettelseårsaktype;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + utsettelseårsaktype.hashCode();
        return result;
    }
}
