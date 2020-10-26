package no.nav.foreldrepenger.regler.soknadsfrist.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.SøknadsfristUtil;
import no.nav.foreldrepenger.regler.soknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFørsteUttakErInnenforSøknadsfrist.ID)
public class SjekkOmFørsteUttakErInnenforSøknadsfrist extends LeafSpecification<SøknadsfristGrunnlag> {

    public static final String ID = "FK_VK 15.3";

    public static final RuleReasonRefImpl KAN_IKKE_VURDERE_PASSERT_SØKNADSFRIST_FOR_FØRSTE_UTTAK = new RuleReasonRefImpl("5043",
        "Mottatt dato for søknad er senere enn søknadsfristen for første uttak");

    public SjekkOmFørsteUttakErInnenforSøknadsfrist() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(SøknadsfristGrunnlag søknadsfristGrunnlag) {
        LocalDate førsteLovligeUttaksdato = SøknadsfristUtil.finnFørsteLoveligeUttaksdag(søknadsfristGrunnlag.getSøknadMottattDato());
        LocalDate førsteUttaksdato = søknadsfristGrunnlag.getFørsteUttaksdato();

        if (førsteUttaksdato.isBefore(førsteLovligeUttaksdato)) {
            return nei();
        }
        return ja();
    }
}
