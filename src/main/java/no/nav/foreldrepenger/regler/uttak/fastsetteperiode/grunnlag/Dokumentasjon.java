package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.List;

public class Dokumentasjon {
    private final List<PeriodeUtenOmsorg> perioderUtenOmsorg = new ArrayList<>();

    private Dokumentasjon() {

    }

    public List<PeriodeUtenOmsorg> getPerioderUtenOmsorg() {
        return perioderUtenOmsorg;
    }

    public static class Builder {

        private final Dokumentasjon kladd = new Dokumentasjon();

        public Builder periodeUtenOmsorg(PeriodeUtenOmsorg periodeUtenOmsorg) {
            kladd.perioderUtenOmsorg.add(periodeUtenOmsorg);
            return this;
        }

        public Dokumentasjon build() {
            return kladd;
        }
    }
}
