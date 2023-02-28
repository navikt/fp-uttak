package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class KonfigurasjonBuilderTest {

    @Test
    void konfigurasjon_med_en_verdi() {
        var nå = LocalDate.now();
        var konfigurasjon = KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå, null, 6)
                .build();
        assertThat(konfigurasjon.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå)).isEqualTo(6);
    }

    @Test
    void konfigurasjon_med_en_verdi_i_to_intervaller() {
        var nå = LocalDate.now();
        var konfigurasjon = KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå, nå.plusDays(6), 6)
                .leggTilParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå.plusDays(7), null, 7)
                .build();
        assertThat(konfigurasjon.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå)).isEqualTo(6);
        assertThat(konfigurasjon.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå.plusDays(7))).isEqualTo(7);
        assertThat(konfigurasjon.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå.plusDays(70))).isEqualTo(7);
    }

    @Test
    void konfigurasjon_med_en_verdi_i_to_intervaller_med_overlapp() {
        var nå = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå, nå.plusDays(6), 6)
                .leggTilParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, nå.plusDays(5), null, 7));
    }

}
