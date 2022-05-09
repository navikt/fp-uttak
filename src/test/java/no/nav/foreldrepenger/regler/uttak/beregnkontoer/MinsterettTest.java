package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.BFHR_MINSTERETT_DAGER;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.FAR_UTTAK_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.GENERELL_MINSTERETT;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.MINSTEDAGER_UFØRE_100_PROSENT;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.MINSTEDAGER_UFØRE_80_PROSENT;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.MOR_TO_TETTE_MINSTERETT_DAGER;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.UTTAK_RUNDT_FØDSEL_DAGER;
import static no.nav.foreldrepenger.regler.uttak.beregnkontoer.Minsterett.finnMinsterett;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.Dekningsgrad;

class MinsterettTest {

    @Test
    void bfhr_mor_ufør() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(LocalDate.MIN)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag80)).containsEntry(GENERELL_MINSTERETT, MINSTEDAGER_UFØRE_80_PROSENT);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(LocalDate.MIN)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag100)).containsEntry(GENERELL_MINSTERETT, MINSTEDAGER_UFØRE_100_PROSENT);
    }

    @Test
    void begge_rett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(true)
                .familieHendelseDato(LocalDate.MIN)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .bareFarHarRett(false)
                .aleneomsorg(false)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(FAR_UTTAK_RUNDT_FØDSEL, UTTAK_RUNDT_FØDSEL_DAGER);
    }

    @Test
    void bfhr() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .familieHendelseDato(LocalDate.MIN)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .bareFarHarRett(true)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(GENERELL_MINSTERETT, BFHR_MINSTERETT_DAGER);
    }

    @Test
    void uten_minsterett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(false)
                .mor(true)
                .familieHendelseDato(LocalDate.MIN)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag)).isEmpty();
    }

    @Test
    void både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .aleneomsorg(true)
                .bareFarHarRett(true)
                .morHarUføretrygd(true)
                .familieHendelseDato(LocalDate.MIN)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag)).doesNotContainKey(GENERELL_MINSTERETT);
    }

    @Test
    void totette_gir_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(true)
                .familieHendelseDato(LocalDate.MIN)
                .familieHendelseDatoNesteSak(LocalDate.MIN.plusWeeks(40))
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(GENERELL_MINSTERETT, MOR_TO_TETTE_MINSTERETT_DAGER);
    }

    @Test
    void ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(true)
                .familieHendelseDato(LocalDate.MIN)
                //Barna ikke tett nok til minsteretten
                .familieHendelseDatoNesteSak(LocalDate.MIN.plusWeeks(50))
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag)).doesNotContainKey(GENERELL_MINSTERETT);
    }

}
