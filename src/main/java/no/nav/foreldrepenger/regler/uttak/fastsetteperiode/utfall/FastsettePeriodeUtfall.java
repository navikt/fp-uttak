package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodePropertyType;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class FastsettePeriodeUtfall extends LeafSpecification<FastsettePeriodeGrunnlag> {
    private final UtfallType utfallType;
    private final RuleReasonRef ruleReasonRef;
    private final List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere;

    private FastsettePeriodeUtfall(String id,
                                   UtfallType utfallType,
                                   RuleReasonRef ruleReasonRef,
                                   List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere) {
        super(id);
        if (utfallType == null) {
            throw new IllegalArgumentException("UtfallType kan ikke være null.");
        }
        this.utfallType = utfallType;
        this.ruleReasonRef = ruleReasonRef;
        this.utfallSpesifiserere = utfallSpesifiserere;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var utfall = getHovedUtfall();
        spesifiserUtfall(utfall, grunnlag);
        return utfall;
    }

    private void spesifiserUtfall(SingleEvaluation utfall, FastsettePeriodeGrunnlag grunnlag) {
        if (utfallSpesifiserere.isEmpty()) {
            return;
        }
        utfallSpesifiserere.forEach(utfallSpesifiserer -> utfallSpesifiserer.accept(utfall, grunnlag));
    }

    private SingleEvaluation getHovedUtfall() {
        return switch (utfallType) {
            case INNVILGET -> ja();
            case AVSLÅTT, MANUELL_BEHANDLING -> nei(ruleReasonRef);
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UtfallType hovedUtfall;
        private RuleReasonRef ruleReasonRef;
        private String id;
        private final List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere = new ArrayList<>();

        public Builder ikkeOppfylt(IkkeOppfyltÅrsak årsak) {
            this.hovedUtfall = UtfallType.AVSLÅTT;
            this.ruleReasonRef = new RuleReasonRefImpl(String.valueOf(årsak.getId()), årsak.getBeskrivelse());
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.UTFALL, UtfallType.AVSLÅTT);
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, årsak);
            });
            return this;
        }

        public Builder oppfylt(InnvilgetÅrsak innvilgetÅrsak) {
            this.hovedUtfall = UtfallType.INNVILGET;
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.UTFALL, UtfallType.INNVILGET);
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, innvilgetÅrsak);
            });
            return this;
        }


        public Builder medId(String id) {
            this.id = id;
            return this;
        }

        public Builder medTrekkDagerFraSaldo(boolean trekkDagerFraSaldo) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.TREKK_DAGER_FRA_SALDO, trekkDagerFraSaldo));
            return this;
        }

        public Builder medAvslåttGradering(GraderingIkkeInnvilgetÅrsak graderingAvslagÅrsak) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.GRADERING_IKKE_OPPFYLT_ÅRSAK, graderingAvslagÅrsak));
            return this;
        }

        public Builder manuellBehandling(PeriodeResultatÅrsak periodeResultatÅrsak, Manuellbehandlingårsak manuellbehandlingårsak) {
            this.hovedUtfall = UtfallType.MANUELL_BEHANDLING;
            if (periodeResultatÅrsak != null) {
                this.ruleReasonRef = new RuleReasonRefImpl(String.valueOf(periodeResultatÅrsak.getId()),
                        periodeResultatÅrsak.getBeskrivelse());
            }
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.UTFALL, UtfallType.MANUELL_BEHANDLING);
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.MANUELL_BEHANDLING_ÅRSAK, manuellbehandlingårsak);
                singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, periodeResultatÅrsak);
            });
            return this;
        }

        public FastsettePeriodeUtfall create() {
            return new FastsettePeriodeUtfall(id, hovedUtfall, ruleReasonRef, utfallSpesifiserere);
        }

        public Builder utbetal(boolean utbetal) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> singleEvaluation.setEvaluationProperty(FastsettePeriodePropertyType.UTBETAL, utbetal));
            return this;
        }
    }
}
