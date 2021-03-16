package no.nav.foreldrepenger.regler.soknadsfrist;

import no.nav.foreldrepenger.regler.KanIkkeVurdere;
import no.nav.foreldrepenger.regler.Oppfylt;
import no.nav.foreldrepenger.regler.soknadsfrist.betingelser.SjekkOmFørsteUttakErInnenforSøknadsfrist;
import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = SøknadsfristRegel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=254450376")
public class SøknadsfristRegel implements RuleService<SøknadsfristGrunnlag> {

    public static final String ID = "FP_VK XX7";

    @Override
    public Evaluation evaluer(SøknadsfristGrunnlag grunnlag) {
        return getSpecification().evaluate(grunnlag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<SøknadsfristGrunnlag> getSpecification() {
        var rs = new Ruleset<SøknadsfristGrunnlag>();
        return rs.hvisRegel(SjekkOmFørsteUttakErInnenforSøknadsfrist.ID, "Er første uttaksdag innenfor søknadsfristen?")
                .hvis(new SjekkOmFørsteUttakErInnenforSøknadsfrist(), new Oppfylt<>())
                .ellers(new KanIkkeVurdere<>(
                        SjekkOmFørsteUttakErInnenforSøknadsfrist.KAN_IKKE_VURDERE_PASSERT_SØKNADSFRIST_FOR_FØRSTE_UTTAK));
    }
}
