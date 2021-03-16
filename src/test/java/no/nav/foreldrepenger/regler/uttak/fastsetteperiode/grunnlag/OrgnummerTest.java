package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrgnummerTest {

    @Test
    void to_string_maskerer() {
        var orgnr = new Orgnummer("000000000");
        assertThat(orgnr.toString()).isEqualTo("*****0000");
    }

}
