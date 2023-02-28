package no.nav.foreldrepenger.regler.uttak.konfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class StandardKonfigurasjonTest {

    @Test
    void test_standard_konfiguration() {
        assertThat(Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
                LocalDate.of(2017, 12, 5))).isEqualTo(6);
        assertThat(Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
                LocalDate.of(2017, 12, 5))).isEqualTo(6);
    }

    @Test
    void hent_parameter_utenfor_periode_skal_gi_exception() {
        assertThrows(IllegalArgumentException.class,
                () -> Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
                        LocalDate.of(1970, 12, 5)));
    }

}
