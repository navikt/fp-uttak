package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt.FastsettePeriodeRegel;
import org.junit.jupiter.api.Test;

class FastsettePeriodeRegelTest {

    @Test
    void regel_skal_kunne_instansieres_via_default_constructor_for_dokumentasjonsgenerering() {
        var fastsettePeriodeRegel = new FastsettePeriodeRegel();
        var specification = fastsettePeriodeRegel.getSpecification();

        assertThat(specification).isNotNull();
    }
}
