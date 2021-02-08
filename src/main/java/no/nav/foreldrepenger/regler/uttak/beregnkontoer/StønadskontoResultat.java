package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class StønadskontoResultat {

    private final Map<Stønadskontotype, Integer> stønadskontoer;
    private final String evalueringResultat;
    private final String innsendtGrunnlag;
    private final Integer antallFlerbarnsdager;
    private final Integer antallPrematurDager;

    public StønadskontoResultat(Map<Stønadskontotype, Integer> stønadskontoer,
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

    public Map<Stønadskontotype, Integer> getStønadskontoer() {
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
