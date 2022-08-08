package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Map;

import no.nav.fpsak.nare.evaluation.RuleReasonRef;

public class KontoOutcome implements RuleReasonRef {
    private String reasonText = "";
    private Map<StønadskontoBeregningStønadskontotype, Integer> kontoer;
    private Integer antallExtraBarnDager;
    private Integer antallPrematurDager;

    KontoOutcome(Map<StønadskontoBeregningStønadskontotype, Integer> kontoer) {
        this.kontoer = kontoer;
    }

    static KontoOutcome ikkeOppfylt(String text) {
        var outcome = new KontoOutcome(Map.of());
        outcome.reasonText = text;
        return outcome;
    }

    public Map<StønadskontoBeregningStønadskontotype, Integer> getKontoer() {
        return kontoer;
    }

    public int getAntallExtraBarnDager() {
        return antallExtraBarnDager != null ? antallExtraBarnDager : 0;
    }

    public int getAntallPrematurDager() {
        return antallPrematurDager != null ? antallPrematurDager : 0;
    }

    public KontoOutcome medKontoer(Map<StønadskontoBeregningStønadskontotype, Integer> kontoer) {
        this.kontoer = kontoer;
        return this;
    }

    public KontoOutcome medAntallExtraBarnDager(Integer antallExtraBarnDager) {
        this.antallExtraBarnDager = antallExtraBarnDager;
        return this;
    }

    public KontoOutcome medAntallPrematurDager(Integer antallPrematurDager) {
        this.antallPrematurDager = antallPrematurDager;
        return this;
    }

    @Override
    public String getReasonTextTemplate() {
        return reasonText;
    }

    @Override
    public String getReasonCode() {
        return "konto";
    }
}
