package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.feil.UttakRegelFeil;
import no.nav.foreldrepenger.regler.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerPropertyType;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

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

        var stønadskontoer = hentStønadskontoer(evaluation);
        var antallFlerbarnsdager = hentAntallFlerbarnsdager(evaluation);
        var antallPrematurDager = hentAntallPrematurDager(evaluation);

        return new StønadskontoResultat(stønadskontoer, antallFlerbarnsdager, evaluationJson, grunnlagJson, antallPrematurDager);
    }

    private Map<StønadskontoBeregningStønadskontotype, Integer> hentStønadskontoer(Evaluation evaluation) {
        var resultat = new Regelresultat(evaluation);
        if (resultat.oppfylt()) {
            var kontoer = resultat.getProperty(BeregnKontoerPropertyType.KONTOER, Map.class);
            if (kontoer != null) {
                return kontoer;
            }
            throw new IllegalStateException("Noe har gått galt, har ikke fått beregnet noen stønadskontoer");
        }
        return Collections.emptyMap();
    }

    private Integer hentAntallFlerbarnsdager(Evaluation evaluation) {
        var regelresultat = new Regelresultat(evaluation);
        if (regelresultat.oppfylt()) {
            return regelresultat.getProperty(BeregnKontoerPropertyType.ANTALL_FLERBARN_DAGER, Integer.class);
        }
        return 0;
    }

    private Integer hentAntallPrematurDager(Evaluation evaluation) {
        var regelresultat = new Regelresultat(evaluation);
        if (regelresultat.oppfylt()) {
            return regelresultat.getProperty(BeregnKontoerPropertyType.ANTALL_PREMATUR_DAGER, Integer.class);
        }
        return 0;
    }

    private String toJson(BeregnKontoerGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for beregning av stønadskontoer.", e);
        }
    }
}
