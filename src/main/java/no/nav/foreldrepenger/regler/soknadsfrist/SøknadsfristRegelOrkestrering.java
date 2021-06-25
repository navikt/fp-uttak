package no.nav.foreldrepenger.regler.soknadsfrist;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.SøknadsfristUtil;
import no.nav.foreldrepenger.regler.feil.UttakRegelFeil;
import no.nav.foreldrepenger.regler.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class SøknadsfristRegelOrkestrering {

    private final JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public SøknadsfristResultat vurderSøknadsfrist(SøknadsfristGrunnlag grunnlag) {
        var søknadsfristRegel = new SøknadsfristRegel();
        var evaluation = søknadsfristRegel.evaluer(grunnlag);

        var grunnlagJson = toJson(grunnlag);
        var evaluationJson = EvaluationSerializer.asJson(evaluation);
        var tidligsteLovligeUttak = SøknadsfristUtil.finnFørsteLoveligeUttaksdag(grunnlag.getSøknadMottattDato());
        var regelResultatBuilder = new SøknadsfristResultat.Builder(evaluationJson, grunnlagJson)
                .tidligsteLovligeUttak(tidligsteLovligeUttak);

        var regelresultat = new Regelresultat(evaluation);
        if (!regelresultat.oppfylt()) {
            var årsakskode = finnÅrsakskode(evaluation);
            årsakskode.ifPresent(regelResultatBuilder::søknadsfristIkkeOppfylt);
        } else {
            regelResultatBuilder.søknadsfristOppfylt();
        }
        return regelResultatBuilder.build();
    }

    private String toJson(SøknadsfristGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for avklaring av søknadsfrist for foreldrepenger.", e);
        }
    }

    private Optional<String> finnÅrsakskode(Evaluation evaluation) {
        var summary = new EvaluationSummary(evaluation);
        for (var e : summary.leafEvaluations(Resultat.IKKE_VURDERT)) {
            if (e.getOutcome() != null) {
                return Optional.of(e.getOutcome().getReasonCode());
            }
        }
        return Optional.empty();
    }

}
