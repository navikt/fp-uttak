package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Map;

import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;

public class KontoEvaluation extends SingleEvaluation {
    private Map<StønadskontoBeregningStønadskontotype, Integer> kontoer;
    private Integer antallExtraBarnDager;
    private Integer antallPrematurDager;

    private KontoEvaluation(Resultat resultat, String ruleIdentification, String ruleDescription, RuleReasonRef outcome, Object... stringformatArguments) {
        super(resultat, ruleIdentification, ruleDescription, outcome, stringformatArguments);
    }

    public static KontoEvaluation konti() {
        return new KontoEvaluation(Resultat.JA, "Opprett kontoer", "", (RuleReasonRef)null, new Object[0]);
    }

    public Map<StønadskontoBeregningStønadskontotype, Integer> getKontoer() {
        return kontoer;
    }

    public Integer getAntallExtraBarnDager() {
        return antallExtraBarnDager;
    }

    public Integer getAntallPrematurDager() {
        return antallPrematurDager;
    }

    public KontoEvaluation medKontoer(Map<StønadskontoBeregningStønadskontotype, Integer> kontoer) {
        this.kontoer = kontoer;
        return this;
    }

    public KontoEvaluation medAntallExtraBarnDager(Integer antallExtraBarnDager) {
        this.antallExtraBarnDager = antallExtraBarnDager;
        return this;
    }

    public KontoEvaluation medAntallPrematurDager(Integer antallPrematurDager) {
        this.antallPrematurDager = antallPrematurDager;
        return this;
    }
}
