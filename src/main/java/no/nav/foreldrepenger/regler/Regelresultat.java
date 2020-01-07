package no.nav.foreldrepenger.regler;

import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodePropertyType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Årsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class Regelresultat {

    private EvaluationSummary evaluationSummary;

    public Regelresultat(Evaluation evaluation) {
        this.evaluationSummary = new EvaluationSummary(evaluation);
    }

    public <T> T getProperty(String tag, Class<T> clazz) {
        Object obj = getProperty(tag);
        if (obj != null && !clazz.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("Kan ikke hente property " + tag + ". Forventet " + clazz.getSimpleName() + " men fant " + obj.getClass());
        }
        return (T) obj;
    }

    public UtfallType getGradering() {
        return getProperty(FastsettePeriodePropertyType.GRADERING, UtfallType.class);
    }

    public UtfallType getUtfallType() {
        return getProperty(FastsettePeriodePropertyType.UTFALL, UtfallType.class);
    }

    public Manuellbehandlingårsak getManuellbehandlingårsak() {
        return getProperty(FastsettePeriodePropertyType.MANUELL_BEHANDLING_ÅRSAK, Manuellbehandlingårsak.class);
    }

    public Årsak getAvklaringÅrsak() {
        return getProperty(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, Årsak.class);
    }

    public Årsak getInnvilgetÅrsak() {
        return getProperty(FastsettePeriodePropertyType.INNVILGET_ÅRSAK, Årsak.class);
    }

    public GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return getProperty(FastsettePeriodePropertyType.GRADERING_IKKE_OPPFYLT_ÅRSAK, GraderingIkkeInnvilgetÅrsak.class);
    }

    public boolean trekkDagerFraSaldo() {
        Boolean trekkDagerFraSaldo = getProperty(FastsettePeriodePropertyType.TREKK_DAGER_FRA_SALDO, Boolean.class);
        return trekkDagerFraSaldo != null && trekkDagerFraSaldo;
    }

    public boolean skalUtbetale() {
        Boolean skalUtbetale = getProperty(FastsettePeriodePropertyType.UTBETAL, Boolean.class);
        return skalUtbetale != null && skalUtbetale;
    }

    public boolean oppfylt() {
        return !evaluationSummary.leafEvaluations(Resultat.JA).isEmpty();
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
