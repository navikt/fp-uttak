package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class FastsettePeriodeUtfall extends LeafSpecification<FastsettePeriodeGrunnlag> {
    private static final String UTFALL = "UTFALL";
    private static final String AVKLARING_ÅRSAK = "AVKLARING_ÅRSAK";
    private static final String GRADERING_IKKE_OPPFYLT_ÅRSAK = "GRADERING_IKKE_OPPFYLT_ÅRSAK";
    private static final String MANUELL_BEHANDLING_ÅRSAK = "MANUELL_BEHANDLING_ÅRSAK";
    private static final String TREKK_DAGER_FRA_SALDO = "TREKK_DAGER_FRA_SALDO";
    private static final String UTBETAL = "UTBETAL";

    private final UttakOutcome uttakOutcome;
    private final List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere;

    private FastsettePeriodeUtfall(
            String id,
            UttakOutcome uttakOutcome,
            List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere) {
        super(id);
        if (uttakOutcome.getUtfallType() == null) {
            throw new IllegalArgumentException("UtfallType kan ikke være null.");
        }
        this.uttakOutcome = uttakOutcome;
        this.utfallSpesifiserere = utfallSpesifiserere;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var utfall = getHovedUtfall(uttakOutcome);
        spesifiserUtfall(utfall, grunnlag);
        return utfall;
    }

    private void spesifiserUtfall(SingleEvaluation utfall, FastsettePeriodeGrunnlag grunnlag) {
        if (utfallSpesifiserere.isEmpty()) {
            return;
        }
        utfallSpesifiserere.forEach(utfallSpesifiserer -> utfallSpesifiserer.accept(utfall, grunnlag));
    }

    private SingleEvaluation getHovedUtfall(UttakOutcome uttakOutcome) {
        return switch (uttakOutcome.getUtfallType()) {
            case INNVILGET -> ja(uttakOutcome);
            case AVSLÅTT, MANUELL_BEHANDLING -> nei(uttakOutcome);
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UttakOutcome uttakOutcome;
        private String id;
        private final List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere =
                new ArrayList<>();

        public Builder ikkeOppfylt(IkkeOppfyltÅrsak årsak) {
            this.uttakOutcome = UttakOutcome.ikkeOppfylt(årsak);
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.setEvaluationProperty(UTFALL, UtfallType.AVSLÅTT);
                singleEvaluation.setEvaluationProperty(AVKLARING_ÅRSAK, årsak);
            });
            return this;
        }

        public Builder oppfylt(InnvilgetÅrsak innvilgetÅrsak) {
            this.uttakOutcome = UttakOutcome.oppfylt(innvilgetÅrsak);
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.setEvaluationProperty(UTFALL, UtfallType.INNVILGET);
                singleEvaluation.setEvaluationProperty(AVKLARING_ÅRSAK, innvilgetÅrsak);
            });
            return this;
        }

        public Builder medId(String id) {
            this.id = id;
            return this;
        }

        public Builder medTrekkDagerFraSaldo(boolean trekkDagerFraSaldo) {
            this.uttakOutcome = uttakOutcome.medTrekkDagerFraSaldo(trekkDagerFraSaldo);
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) ->
                    singleEvaluation.setEvaluationProperty(TREKK_DAGER_FRA_SALDO, trekkDagerFraSaldo));
            return this;
        }

        public Builder medAvslåttGradering(GraderingIkkeInnvilgetÅrsak graderingAvslagÅrsak) {
            this.uttakOutcome = uttakOutcome.medGraderingIkkeInnvilgetÅrsak(graderingAvslagÅrsak);
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) ->
                    singleEvaluation.setEvaluationProperty(GRADERING_IKKE_OPPFYLT_ÅRSAK, graderingAvslagÅrsak));
            return this;
        }

        public Builder manuellBehandling(
                PeriodeResultatÅrsak periodeResultatÅrsak, Manuellbehandlingårsak manuellbehandlingårsak) {
            this.uttakOutcome = UttakOutcome.manuell(periodeResultatÅrsak, manuellbehandlingårsak);
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.setEvaluationProperty(UTFALL, UtfallType.MANUELL_BEHANDLING);
                singleEvaluation.setEvaluationProperty(MANUELL_BEHANDLING_ÅRSAK, manuellbehandlingårsak);
                singleEvaluation.setEvaluationProperty(AVKLARING_ÅRSAK, periodeResultatÅrsak);
            });
            return this;
        }

        public FastsettePeriodeUtfall create() {
            return new FastsettePeriodeUtfall(id, uttakOutcome, utfallSpesifiserere);
        }

        public Builder utbetal(boolean utbetal) {
            this.uttakOutcome = uttakOutcome.medSkalUtbetale(utbetal);
            this.utfallSpesifiserere.add(
                    (singleEvaluation, grunnlag) -> singleEvaluation.setEvaluationProperty(UTBETAL, utbetal));
            return this;
        }
    }
}
