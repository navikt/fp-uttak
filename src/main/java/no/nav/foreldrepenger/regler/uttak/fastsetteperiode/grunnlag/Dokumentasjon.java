package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.List;

public class Dokumentasjon {
    private final List<GyldigGrunnPeriode> gyldigGrunnPerioder = new ArrayList<>();
    private final List<PeriodeUtenOmsorg> perioderUtenOmsorg = new ArrayList<>();
    private final List<PeriodeMedSykdomEllerSkade> perioderMedSykdomEllerSkade = new ArrayList<>();
    private final List<PeriodeMedInnleggelse> perioderMedInnleggelse = new ArrayList<>();
    private final List<PeriodeMedBarnInnlagt> perioderMedBarnInnlagt = new ArrayList<>();
    private final List<PeriodeMedHV> perioderMedHv = new ArrayList<>();
    private final List<PeriodeMedTiltakIRegiAvNav> perioderMedTiltakViaNav = new ArrayList<>();
    private final List<PeriodeMedAvklartMorsAktivitet> perioderMedAvklartMorsAktivitet = new ArrayList<>();

    private Dokumentasjon() {

    }

    public List<GyldigGrunnPeriode> getGyldigGrunnPerioder() {
        return gyldigGrunnPerioder;
    }

    public List<PeriodeUtenOmsorg> getPerioderUtenOmsorg() {
        return perioderUtenOmsorg;
    }

    public List<PeriodeMedSykdomEllerSkade> getPerioderMedSykdomEllerSkade() {
        return perioderMedSykdomEllerSkade;
    }

    public List<PeriodeMedInnleggelse> getPerioderMedInnleggelse() {
        return perioderMedInnleggelse;
    }

    public List<PeriodeMedBarnInnlagt> getPerioderMedBarnInnlagt() {
        return perioderMedBarnInnlagt;
    }

    public List<PeriodeMedHV> getPerioderMedHv() {
        return perioderMedHv;
    }

    public List<PeriodeMedTiltakIRegiAvNav> getPerioderMedTiltakViaNav() {
        return perioderMedTiltakViaNav;
    }

    public List<PeriodeMedAvklartMorsAktivitet> getPerioderMedAvklartMorsAktivitet() {
        return perioderMedAvklartMorsAktivitet;
    }

    public static class Builder {

        private final Dokumentasjon kladd = new Dokumentasjon();

        public Builder leggGyldigGrunnPeriode(GyldigGrunnPeriode gyldigGrunnPeriode) {
            kladd.gyldigGrunnPerioder.add(gyldigGrunnPeriode);
            return this;
        }

        public Builder leggPeriodeUtenOmsorg(PeriodeUtenOmsorg periodeUtenOmsorg) {
            kladd.perioderUtenOmsorg.add(periodeUtenOmsorg);
            return this;
        }

        public Builder leggPeriodeMedSykdomEllerSkade(PeriodeMedSykdomEllerSkade periodeMedSykdomEllerSkade) {
            kladd.perioderMedSykdomEllerSkade.add(periodeMedSykdomEllerSkade);
            return this;
        }

        public Builder leggPeriodeMedInnleggelse(PeriodeMedInnleggelse periodeMedInnleggelse) {
            kladd.perioderMedInnleggelse.add(periodeMedInnleggelse);
            return this;
        }

        public Builder leggPeriodeMedBarnInnlagt(PeriodeMedBarnInnlagt periodeMedBarnInnlagt) {
            kladd.perioderMedBarnInnlagt.add(periodeMedBarnInnlagt);
            return this;
        }

        public Builder leggTilPeriodeMedHV(PeriodeMedHV periodeMedHV) {
            kladd.perioderMedHv.add(periodeMedHV);
            return this;
        }

        public Builder leggTilPeriodeMedTiltakViaNav(PeriodeMedTiltakIRegiAvNav periodeMedTiltakIRegiAvNav) {
            kladd.perioderMedTiltakViaNav.add(periodeMedTiltakIRegiAvNav);
            return this;
        }
        public Builder leggTilPeriodeMedAvklartMorsAktivitet(PeriodeMedAvklartMorsAktivitet periodeMedAvklartMorsAktivitet) {
            kladd.perioderMedAvklartMorsAktivitet.add(periodeMedAvklartMorsAktivitet);
            return this;
        }

        public Dokumentasjon build() {
            return kladd;
        }
    }
}
