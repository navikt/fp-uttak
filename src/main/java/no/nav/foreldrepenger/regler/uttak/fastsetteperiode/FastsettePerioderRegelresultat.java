package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UttakOutcome;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

class FastsettePerioderRegelresultat {

    private final EvaluationSummary evaluationSummary;
    private final UttakOutcome utfall;

    FastsettePerioderRegelresultat(Evaluation evaluation) {
        this.evaluationSummary = new EvaluationSummary(evaluation);
        this.utfall = evaluationSummary.allOutcomes()
            .stream()
            .filter(UttakOutcome.class::isInstance)
            .map(UttakOutcome.class::cast)
            .findFirst()
            .orElseThrow();
    }

    public FastsettePerioderRegelresultat(Evaluation evaluation, UttakOutcome utfall) {
        this.evaluationSummary = new EvaluationSummary(evaluation);
        this.utfall = utfall;
    }

    public boolean oppfylt() {
        return !evaluationSummary.leafEvaluations(Resultat.JA).isEmpty();
    }

    UtfallType getUtfallType() {
        return utfall.getUtfallType();
    }

    Manuellbehandlingårsak getManuellbehandlingårsak() {
        return utfall.getManuellbehandlingårsak();
    }

    PeriodeResultatÅrsak getAvklaringÅrsak() {
        return utfall.getPeriodeÅrsak();
    }

    GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return utfall.getGraderingIkkeInnvilgetÅrsak();
    }

    boolean trekkDagerFraSaldo() {
        return utfall.trekkDagerFraSaldo();
    }

    boolean skalUtbetale() {
        return utfall.skalUtbetale();
    }

    public String sluttpunktId() {
        return evaluationSummary.leafEvaluations()
            .stream()
            .filter(e -> e.ruleIdentification() != null)
            .findFirst()
            .map(Evaluation::ruleIdentification)
            .orElse(null);
    }

}
