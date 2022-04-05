package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class StønadskontoResultat {

    private final Map<StønadskontoBeregningStønadskontotype, Integer> stønadskontoer;
    private final String evalueringResultat;
    private final String innsendtGrunnlag;
    private final Integer antallFlerbarnsdager;
    private final Integer antallPrematurDager;

    public StønadskontoResultat(Map<StønadskontoBeregningStønadskontotype, Integer> stønadskontoer,
                                Integer antallFlerbarnsdager,
                                String evalueringResultat,
                                String innsendtGrunnlag,
                                Integer antallPrematurDager) {
        this.antallPrematurDager = antallPrematurDager;
        Objects.requireNonNull(stønadskontoer);
        Objects.requireNonNull(evalueringResultat);
        Objects.requireNonNull(innsendtGrunnlag);
        this.stønadskontoer = stønadskontoer;
        this.antallFlerbarnsdager = antallFlerbarnsdager;
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
    }

    public Map<StønadskontoBeregningStønadskontotype, Integer> getStønadskontoer() {
        return Collections.unmodifiableMap(stønadskontoer);
    }

    public String getEvalueringResultat() {
        return evalueringResultat;
    }

    public String getInnsendtGrunnlag() {
        return innsendtGrunnlag;
    }

    public Integer getAntallFlerbarnsdager() {
        return antallFlerbarnsdager;
    }

    public Integer getAntallPrematurDager() {
        return antallPrematurDager;
    }
}
