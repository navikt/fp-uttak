package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.List;

public class Dokumentasjon {
    private List<GyldigGrunnPeriode> gyldigGrunnPerioder = new ArrayList<>();
    private List<PeriodeUtenOmsorg> perioderUtenOmsorg = new ArrayList<>();
    private List<PeriodeMedSykdomEllerSkade> perioderMedSykdomEllerSkade = new ArrayList<>();
    private List<PeriodeMedInnleggelse> perioderMedInnleggelse = new ArrayList<>();
    private List<PeriodeMedBarnInnlagt> perioderMedBarnInnlagt = new ArrayList<>();

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

    public static class Builder {

        private final Dokumentasjon kladd = new Dokumentasjon();

        public Builder leggGyldigGrunnPerioder(GyldigGrunnPeriode gyldigGrunnPeriode) {
            kladd.gyldigGrunnPerioder.add(gyldigGrunnPeriode);
            return this;
        }
        public Builder leggPerioderUtenOmsorg(PeriodeUtenOmsorg periodeUtenOmsorg) {
            kladd.perioderUtenOmsorg.add(periodeUtenOmsorg);
            return this;
        }
        public Builder leggPerioderMedSykdomEllerSkade(PeriodeMedSykdomEllerSkade periodeMedSykdomEllerSkade) {
            kladd.perioderMedSykdomEllerSkade.add(periodeMedSykdomEllerSkade);
            return this;
        }
        public Builder leggPerioderMedInnleggelse(PeriodeMedInnleggelse periodeMedInnleggelse) {
            kladd.perioderMedInnleggelse.add(periodeMedInnleggelse);
            return this;
        }
        public Builder leggPerioderMedBarnInnlagt(PeriodeMedBarnInnlagt periodeMedBarnInnlagt) {
            kladd.perioderMedBarnInnlagt.add(periodeMedBarnInnlagt);
            return this;
        }

        public Dokumentasjon build() {
            return kladd;
        }
    }
}
