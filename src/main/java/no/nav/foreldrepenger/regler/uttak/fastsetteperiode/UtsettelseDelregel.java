package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = UtsettelseDelregel.ID)
public class UtsettelseDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    //TODO fritt uttak

    public static final String ID = "FP_VK 18";
    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();
    private Konfigurasjon konfigurasjon;

    public UtsettelseDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    public UtsettelseDelregel() {
        // For regeldokumentasjon
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return Oppfylt.opprett("TODO fritt uttak", InnvilgetÅrsak.UTSETTELSE_GYLDIG, false, false);
    }
}
