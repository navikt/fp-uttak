package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.feil.UttakRegelFeil;
import no.nav.foreldrepenger.regler.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.evaluation.Resultat;
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
        if (!evaluationSummary.leafEvaluations(Resultat.JA).isEmpty()) {
            return evaluationSummary.allOutcomes().stream()
                .filter(KontoOutcome.class::isInstance)
                .map(e -> ((KontoOutcome) e).getKontoer())
                .findFirst().orElseThrow(() -> new IllegalStateException("Noe har gått galt, har ikke fått beregnet noen stønadskontoer"));
        }
        return Collections.emptyMap();
    }

    private int hentAntallFlerbarnsdager(EvaluationSummary evaluationSummary) {
        return evaluationSummary.allOutcomes().stream()
            .filter(KontoOutcome.class::isInstance)
            .map(e -> ((KontoOutcome) e).getAntallExtraBarnDager())
            .findFirst().orElse(0);
    }

    private int hentAntallPrematurDager(EvaluationSummary evaluationSummary) {
        return evaluationSummary.allOutcomes().stream()
            .filter(KontoOutcome.class::isInstance)
            .map(e -> ((KontoOutcome) e).getAntallPrematurDager())
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
