package no.nav.foreldrepenger.regler.uttak.beregnkontoer;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.felles.PrematurukerUtil;

class PrematurukerUtilTest {

    @Test
    void false_hvis_fødselsdato_er_null() {
        var resultat = PrematurukerUtil.oppfyllerKravTilPrematuruker(null, LocalDate.of(2019, 7, 1));
        assertThat(resultat).isFalse();
    }

    @Test
    void false_hvis_termindato_er_null() {
        var resultat = PrematurukerUtil.oppfyllerKravTilPrematuruker(LocalDate.of(2019, 7, 1), null);
        assertThat(resultat).isFalse();
    }

    @Test
    void false_hvis_fødselsdato_er_før_første_juli_og_fødsel_er_mer_enn_7_uker_før_termin() {
        var fødselsdato = LocalDate.of(2019, 6, 30);
        var resultat = PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, LocalDate.of(2019, 8, 20));
        assertThat(resultat).isFalse();
    }

    @Test
    void false_hvis_fødselsdato_er_etter_første_juli_og_fødsel_er_akkurat_7_uker_før_termin() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var resultat = PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, LocalDate.of(2019, 8, 19));
        assertThat(resultat).isFalse();
    }

    @Test
    void false_hvis_fødselsdato_er_etter_første_juli_og_fødsel_er_etter_termin() {
        var fødselsdato = LocalDate.of(2019, 8, 1);
        var resultat = PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, fødselsdato);
        assertThat(resultat).isFalse();
    }

    @Test
    void true_hvis_fødselsdato_er_etter_første_juli_og_fødsel_er_mer_enn_7_uker_før_termin() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var resultat = PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, LocalDate.of(2019, 8, 23));
        assertThat(resultat).isTrue();
    }
}
