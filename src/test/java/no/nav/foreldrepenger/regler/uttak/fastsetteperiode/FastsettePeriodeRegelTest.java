package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelTest {

    @Test
    public void regel_skal_kunne_instansieres_via_default_constructor_for_dokumentasjonsgenerering() {
        var fastsettePeriodeRegel = new FastsettePeriodeRegel(null, new FeatureTogglesForTester());
        var specification = fastsettePeriodeRegel.getSpecification();

        assertThat(specification).isNotNull();
    }
}
