package no.nav.foreldrepenger.regler;

import java.util.Optional;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class Regelresultat {

    final EvaluationSummary evaluationSummary;

    public Regelresultat(Evaluation evaluation) {
        this.evaluationSummary = new EvaluationSummary(evaluation);
    }

    public boolean oppfylt() {
        return !evaluationSummary.leafEvaluations(Resultat.JA).isEmpty();
    }

    public <T> T getProperty(String tag, Class<T> clazz) {
        Object obj = getProperty(tag);
        if (obj != null && !clazz.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("Kan ikke hente property " + tag + ". Forventet " + clazz.getSimpleName() + " men fant " + obj.getClass());
        }
        return (T) obj;
    }

    public String sluttpunktId() {
        Optional<Evaluation> first = evaluationSummary.leafEvaluations().stream()
                .filter(e -> e.ruleIdentification() != null)
                .findFirst();

        return first.map(Evaluation::ruleIdentification).orElse(null);
    }

    private Object getProperty(String tag) {
        Optional<Evaluation> first = evaluationSummary.leafEvaluations().stream()
                .filter(e -> e.getEvaluationProperties() != null)
                .findFirst();

        return first.map(evaluation -> evaluation.getEvaluationProperties().get(tag)).orElse(null);
    }
}
