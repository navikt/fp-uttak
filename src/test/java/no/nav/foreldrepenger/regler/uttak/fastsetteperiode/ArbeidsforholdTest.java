package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.EndringAvStilling;

class ArbeidsforholdTest {

    @Test
    void skal_finne_stillingsprosent() {
        var arbeidBuilder = new Arbeid.Builder();

        arbeidBuilder.endringAvStilling(new EndringAvStilling(LocalDate.of(2019, 1, 1), BigDecimal.valueOf(50)));
        arbeidBuilder.endringAvStilling(new EndringAvStilling(LocalDate.of(2018, 1, 1), BigDecimal.ZERO));
        arbeidBuilder.endringAvStilling(new EndringAvStilling(LocalDate.of(2020, 1, 1), BigDecimal.valueOf(100)));

        var arbeid = arbeidBuilder.build();

        assertThat(arbeid.getStillingsprosent(LocalDate.of(2017, 1, 1))).isEqualTo(BigDecimal.valueOf(100));
        assertThat(arbeid.getStillingsprosent(LocalDate.of(2018, 11, 11))).isEqualTo(BigDecimal.ZERO);
        assertThat(arbeid.getStillingsprosent(LocalDate.of(2019, 12, 12))).isEqualTo(BigDecimal.valueOf(50));
        assertThat(arbeid.getStillingsprosent(LocalDate.of(2020, 2, 2))).isEqualTo(BigDecimal.valueOf(100));
    }

}
