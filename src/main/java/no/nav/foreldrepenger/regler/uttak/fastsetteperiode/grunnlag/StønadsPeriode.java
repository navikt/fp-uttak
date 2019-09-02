package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class StønadsPeriode extends UttakPeriode {

    public StønadsPeriode(Stønadskontotype stønadskontotype,
                          PeriodeKilde periodeKilde,
                          LocalDate fom,
                          LocalDate tom,
                          SamtidigUttak samtidigUttak,
                          boolean flerbarnsdager) {
        super(stønadskontotype, Periodetype.STØNADSPERIODE, periodeKilde, fom, tom, samtidigUttak, flerbarnsdager);
    }

    private StønadsPeriode(StønadsPeriode kilde, LocalDate fom, LocalDate tom) {
        super(kilde, fom, tom);
    }

    public static StønadsPeriode medGradering(Stønadskontotype stønadskontotype,
                                              PeriodeKilde periodeKilde,
                                              LocalDate fom,
                                              LocalDate tom,
                                              List<AktivitetIdentifikator> gradertAktiviteter,
                                              BigDecimal prosentArbeid,
                                              PeriodeVurderingType periodeResultat) {
        return medGradering(stønadskontotype, periodeKilde, fom, tom, gradertAktiviteter, prosentArbeid, periodeResultat, null, false);
    }

    public static StønadsPeriode medGradering(Stønadskontotype stønadskontotype,
                                              PeriodeKilde periodeKilde,
                                              LocalDate fom,
                                              LocalDate tom,
                                              List<AktivitetIdentifikator> gradertAktiviteter,
                                              BigDecimal prosentArbeid,
                                              PeriodeVurderingType periodeVurderingType,
                                              SamtidigUttak samtidigUttak,
                                              boolean flerbarnsdager) {
        StønadsPeriode periode = new StønadsPeriode(stønadskontotype, periodeKilde, fom, tom, samtidigUttak, flerbarnsdager);
        periode.setGradertAktivitet(gradertAktiviteter, prosentArbeid);
        periode.setPeriodeVurderingType(periodeVurderingType);
        return periode;
    }

    public static StønadsPeriode medOverføringAvKvote(Stønadskontotype stønadskontotype,
                                                      PeriodeKilde periodeKilde,
                                                      LocalDate fom,
                                                      LocalDate tom,
                                                      OverføringÅrsak overføringÅrsak,
                                                      PeriodeVurderingType periodeVurderingType,
                                                      SamtidigUttak samtidigUttak,
                                                      boolean flerbarnsdager) {
        StønadsPeriode periode = new StønadsPeriode(stønadskontotype, periodeKilde, fom, tom, samtidigUttak, flerbarnsdager);
        periode.setOverføringÅrsak(overføringÅrsak);
        periode.setPeriodeVurderingType(periodeVurderingType);
        return periode;
    }

    @Override
    public StønadsPeriode kopiMedNyPeriode(LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(this, fom, tom);
    }

    @Override
    public Trekkdager getTrekkdagerFraSluttpunkt(AktivitetIdentifikator aktivitetIdentifikator) {
        return getTrekkdager(isGradering(aktivitetIdentifikator));
    }

    @Override
    public boolean isUtsettelsePgaFerie() {
        return false;
    }

    private Trekkdager getTrekkdager(boolean gradert) {
        if (getSluttpunktTrekkerDager()) {
            return TrekkdagerUtregningUtil.trekkdagerFor(this, gradert, getGradertArbeidsprosent(),
                    getSamtidigUttaksprosent().orElse(null));
        }
        return Trekkdager.ZERO;
    }
}
