package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.SamtidigUttakUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAkseptertSamtidigUttak.ID)
public class SjekkOmAkseptertSamtidigUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.12";
    public static final String BESKRIVELSE = "Har partene søkt tillatt samtidig uttak?";

    public SjekkOmAkseptertSamtidigUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        // Inntil 200% samtidig uttak ved flerbarnsdager eller perioden rundt fødsel
        if (SamtidigUttakUtil.gjelderFlerbarnsdager(grunnlag)
                || SamtidigUttakUtil.gjelderFarRundtFødsel(grunnlag)) {
            return ja();
        }
        // Inntil 100% samtidig uttak er alltid OK
        if (!SamtidigUttakUtil.merEnn100ProsentSamtidigUttak(grunnlag)) {
            return ja();
        }
        // Inntil 100% kvote samtidig med inntil 50% fellesperiode er OK
        if (SamtidigUttakUtil.akseptert150ProsentSamtidigUttak(grunnlag)) {
            return ja();
        }
        // Tilfelle som kan håndteres ved å redusere utbetalingsgrad(er) for tapende behandling
        // For tiden reduksjon til min = 20% og ikke ved gradering+flereAkiviteter
        if (SamtidigUttakUtil.kanReduseresTil100ProsentForRegel(grunnlag)) {
            return ja();
        }
        return nei();
    }
}
