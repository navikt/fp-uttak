package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.feil.UttakRegelFeil;
import no.nav.foreldrepenger.regler.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class StønadskontoRegelOrkestrering {

    private final JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public StønadskontoResultat beregnKontoer(BeregnKontoerGrunnlag grunnlag) {
        return beregnKontoer(grunnlag, Konfigurasjon.STANDARD);
    }

    public StønadskontoResultat beregnKontoer(BeregnKontoerGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        var grunnlagJson = toJson(grunnlag);

        var beregnKontoer = new BeregnKontoer(konfigurasjon);
        var evaluation = beregnKontoer.evaluer(grunnlag);
        var evaluationJson = EvaluationSerializer.asJson(evaluation);

        var summary = new EvaluationSummary(evaluation);
        var stønadskontoer = hentStønadskontoer(summary);
        var antallFlerbarnsdager = hentAntallFlerbarnsdager(summary);
        var antallPrematurDager = hentAntallPrematurDager(summary);

        return new StønadskontoResultat(stønadskontoer, antallFlerbarnsdager, evaluationJson, grunnlagJson, antallPrematurDager);
    }

    private Map<StønadskontoBeregningStønadskontotype, Integer> hentStønadskontoer(EvaluationSummary evaluationSummary) {
        if (Regelresultat.oppfylt(evaluationSummary)) {
            return evaluationSummary.leafEvaluations().stream()
                .filter(KontoEvaluation.class::isInstance)
                .map(e -> ((KontoEvaluation) e).getKontoer())
                .findFirst().orElseThrow(() -> new IllegalStateException("Noe har gått galt, har ikke fått beregnet noen stønadskontoer"));
        }
        return Collections.emptyMap();
    }

    private Integer hentAntallFlerbarnsdager(EvaluationSummary evaluationSummary) {
        return evaluationSummary.leafEvaluations().stream()
            .filter(KontoEvaluation.class::isInstance)
            .map(e -> ((KontoEvaluation) e).getAntallExtraBarnDager())
            .findFirst().orElse(0);
    }

    private Integer hentAntallPrematurDager(EvaluationSummary evaluationSummary) {
        return evaluationSummary.leafEvaluations().stream()
            .filter(KontoEvaluation.class::isInstance)
            .map(e -> ((KontoEvaluation) e).getAntallPrematurDager())
            .findFirst().orElse(0);
    }

    private String toJson(BeregnKontoerGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for beregning av stønadskontoer.", e);
        }
    }
}
