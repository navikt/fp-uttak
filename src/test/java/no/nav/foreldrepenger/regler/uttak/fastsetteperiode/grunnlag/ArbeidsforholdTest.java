package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

public class ArbeidsforholdTest {

    @Test
    public void skal_finne_stillingsprosent() {
        var arbeidsforhold = new Arbeidsforhold(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder());

        arbeidsforhold.leggTilEndringIStilling(new EndringAvStilling(LocalDate.of(2019, 1, 1), BigDecimal.valueOf(50)));
        arbeidsforhold.leggTilEndringIStilling(new EndringAvStilling(LocalDate.of(2018, 1, 1), BigDecimal.ZERO));
        arbeidsforhold.leggTilEndringIStilling(new EndringAvStilling(LocalDate.of(2020, 1, 1), BigDecimal.valueOf(100)));

        assertThat(arbeidsforhold.getStillingsprosent(LocalDate.of(2017, 1, 1))).isEqualTo(BigDecimal.valueOf(100));
        assertThat(arbeidsforhold.getStillingsprosent(LocalDate.of(2018, 11, 11))).isEqualTo(BigDecimal.ZERO);
        assertThat(arbeidsforhold.getStillingsprosent(LocalDate.of(2019, 12, 12))).isEqualTo(BigDecimal.valueOf(50));
        assertThat(arbeidsforhold.getStillingsprosent(LocalDate.of(2020, 2, 2))).isEqualTo(BigDecimal.valueOf(100));
    }
}
