package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class StønadskontoRegelOrkestreringTest {

    private static final LocalDate DATO = LocalDate.now();
    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();


    /*
    Totale stønadskoto: 49 uker  (245 stønadsdager)
    Stønadskonto fordeler seg slik:
    - Fellesperiode: 16 uker (80 stønadsdager)
    - Fedrekovte: 15 uker (75 stønadsdager)
    - Mødrekvote: 15 uker (75 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(80);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }


    @Test
    void skal_legge_til_prematurdager_på_fellesperiode() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(fødselsdato)
                .medTermindato(fødselsdato.plusWeeks(7).plusDays(4))
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        var forventetFellesperiode =
                80 + Virkedager.beregnAntallVirkedager(grunnlag.getFødselsdato().get(), grunnlag.getTermindato().get().minusDays(1));
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(forventetFellesperiode);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    @Test
    void skal_legge_til_prematurdager_på_foreldrepenger() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(fødselsdato)
                .medTermindato(fødselsdato.plusWeeks(8))
                .medAntallBarn(1)
                .farAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        var forventetForeldrepenger = 80 + 75 + 75 + Virkedager.beregnAntallVirkedager(grunnlag.getFødselsdato().get(),
                grunnlag.getTermindato().get().minusDays(1));
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(forventetForeldrepenger);
    }

    @Test
    void skal_ikke_legge_til_prematurdager_på_flerbarnsdager() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(fødselsdato)
                .medTermindato(fødselsdato.plusWeeks(7).plusDays(1))
                .medAntallBarn(2)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(85);
    }

    /*
    Totale stønadskoto: 46 uker  (230 stønadsdager)
    Stønadskonto fordeler seg slik:
    - Fellesperiode: 16 uker (80 stønadsdager)
    - Fedrekovte: 15 uker (75 stønadsdager)
    - Mødrekvote: 15 uker (75 stønadsdager)
*/
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(3);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(80);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
    }

    /*
       Totale stønadskonto: - 59 uker (295 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 26 uker (130 stønadsdager)
       - Fedrekovte: 15 uker (75 stønadsdager)
       - Mødrekvote: 15 uker (75 stønadsdager)
       - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
   */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(90);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }


    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 26 uker (130 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(3);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(90);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(95);
    }

    /*
        Totale stønadskonto:  - 49 uker  (245 stønadsdager)
        NB! Samtidig uttak skal være mulig 46 uker (230 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 16 uker (80 stønadsdager) + 46 uker (230 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(5);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(80 + 230);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(230);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    /*
       Totale stønadskonto:  - 46 uker  (230 stønadsdager)
       NB! Samtidig uttak skal være mulig 46 uker (230 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 16 uker (80 stønadsdager) (3 uker forbeholdt mor før fødsel)   + 46 uker (230 stønadsdager)
       - Fedrekovte: 15 uker (75 stønadsdager)
       - Mødrekvote: 15 uker (75 stønadsdager)
   */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(80 + 230);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(230);
    }

    /*
    Totale stønadskonto: - 59 uker (295 stønadsdager)
    NB! Samtidig uttak skal være mulig 56 uker (280 stønadsdager)
     Stønadskonto fordeler seg slik:
    - Fellesperiode: 26 uker (130 stønadsdager) + 56 uker (280 stønadsdager)
    - Fedrekovte: 15 uker (75 stønadsdager)
    - Mødrekvote: 15 uker (75 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(5);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(90 + 280);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(280);
    }

    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        NB! Samtidig uttak skal være mulig 56 uker (280 stønadsdager)
         Stønadskonto fordeler seg slik:
        - Fellesperiode: 26 uker (130 stønadsdager) + 56 uker (280 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(90 + 280);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(280);
    }

    /*
        Totale stønadskonto:  - 49 uker  (245 stønadsdager)
        NB! Samtidig uttak skal være mulig 17 uker (85 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 16 uker (80 stønadsdager) + 17 uker (85 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(5);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(80 + 85);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(85);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    /*
       Totale stønadskonto:  - 46 uker  (230 stønadsdager)
       NB! Samtidig uttak skal være mulig 17 uker (85 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 16 uker (80 stønadsdager) (3 uker forbeholdt mor før fødsel)   + 17 uker (85 stønadsdager)
       - Fedrekovte: 15 uker (75 stønadsdager)
       - Mødrekvote: 15 uker (75 stønadsdager)
   */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(80 + 85);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(85);
    }

    /*
    Totale stønadskonto: - 59 uker (295 stønadsdager)
    NB! Samtidig uttak skal være mulig 21 uker (105 stønadsdager)
     Stønadskonto fordeler seg slik:
    - Fellesperiode: 26 uker (130 stønadsdager) + 21 uker (105 stønadsdager)
    - Fedrekovte: 15 uker (75 stønadsdager)
    - Mødrekvote: 15 uker (75 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(5);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(90 + 105);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(105);
    }

    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        NB! Samtidig uttak skal være mulig 21 uker (105 stønadsdager)
         Stønadskonto fordeler seg slik:
        - Fellesperiode: 26 uker (130 stønadsdager) + 21 uker (105 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(4);
        assertThat(stønadskontoer.get(Stønadskontotype.FELLESPERIODE)).isEqualTo(90 + 105);
        assertThat(stønadskontoer.get(Stønadskontotype.FEDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.MØDREKVOTE)).isEqualTo(95);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(105);
    }

    /*
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 46 uker (230 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 75 + 75);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    /*
    Bare mor har rett til foreldrepenger.
    Foreldrepenger: - 46 uker (230 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(1)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 75 + 75);
    }

    /*
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 56 uker (280 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 75 + 75);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    /*
    Bare mor har rett til foreldrepenger.
    Foreldrepenger: 56 uker (280 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(1)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 75 + 75);
    }

    /*
       Bare mor har rett til foreldrepenger. 2 barn
       Foreldrepenger: 46 uker (230 stønadsdager) + 17 uker (85 stønadsdager)
       Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
   */
    @Test
    void fødsel_bare_mor_rett_begge_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(2)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 85 + 75 + 75);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: - 46 uker (230 stønadsdager) + 17 uker (85 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(2)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 85 + 75 + 75);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 2 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
*/
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(2)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 105 + 75 + 75);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)).isEqualTo(15);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 2 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(2)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 105 + 75 + 75);
    }

    /*
       Bare mor har rett til foreldrepenger. 3 barn
       Foreldrepenger: 46 uker (230 stønadsdager) + 46 uker (230 stønadsdager)
       Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
   */
    @Test
    void fødsel_bare_mor_rett_begge_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(3)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 230 + 75 + 75);
        assertThat(stønadskontoer.get((Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))).isEqualTo(15);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 3 barn
        Foreldrepenger: - 46 uker (230 stønadsdager) + 46 uker (230 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(3)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 230 + 75 + 75);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(3)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 280 + 75 + 75);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)).isEqualTo(15);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medOmsorgsovertakelseDato(DATO)
                .medAntallBarn(3)
                .farRett(false)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(true)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 280 + 75 + 75);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger
    Foreldrepenger: 46 uker (230 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(true)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 75 + 75);
    }

    /*
    Far har aleneomsorg og rett til  Foreldrepenger
    Foreldrepenger: 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(true)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 75 + 75);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 3 barn
    Foreldrepenger: 46 uker (230 stønadsdager) + 46 uker (230 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(true)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 230 + 75 + 75);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(true)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 280 + 75 + 75);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 2 barn
    Foreldrepenger: 46 uker (230 stønadsdager) + 17 uker (85 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(true)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(80 + 85 + 75 + 75);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 2 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(true)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(130 + 105 + 75 + 75);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad.
       Foreldrepenger: 40 uker (200 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(50 + 75 + 75);
    }

    /*
       Far har rett til Foreldrepenger. 80% dekningsgrad.
       Foreldrepenger: 50 uker (250 stønadsdager)
   */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(1);
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(100 + 75 + 75);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad. 3 barn.
       Foreldrepenger: 40 uker (200 stønadsdager) + 46 uker (230 stønadsdager)
       Flerbarnsdager: 46 uker (230 stønadsdager)

    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2); // FORELDREPENGER og FLERBARNSDAGER
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(50 + 230 + 75 + 75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(230);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 3 barn.
    Foreldrepenger: 50 uker (250 stønadsdager) + 56 uker (280 stønadsdager)
    Flerbarnsdager: 56 uker (280 stønadsdager)

    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(3)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2); // FORELDREPENGER og FLERBARNSDAGER
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(100 + 280 + 75 + 75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(280);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad. 2 barn.
       Foreldrepenger: 40 uker (200 stønadsdager) + 17 uker (85 stønadsdager)
       Flerbarnsdager: 17 uker (85 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2); // FORELDREPENGER og FLERBARNSDAGER
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(50 + 85 + 75 + 75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(85);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 2 barn.
    Foreldrepenger: 50 uker (250 stønadsdager) + 21 uker (105 stønadsdager)
    Flerbarnsdager: 21 uker (105 stønadsdager)

    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(2)
                .farRett(true)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer.size()).isEqualTo(2); // FORELDREPENGER og FLERBARNSDAGER
        assertThat(stønadskontoer.get(Stønadskontotype.FORELDREPENGER)).isEqualTo(100 + 105 + 75 + 75);
        assertThat(stønadskontoer.get(Stønadskontotype.FLERBARNSDAGER)).isEqualTo(105);
    }

    /*
    Hverken mor eller far har opptjent rett til foreldrepenger
    */
    @Test
    void hverken_mor_eller_far_har_rett() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medTermindato(DATO)
                .medAntallBarn(1)
                .farRett(false)
                .morRett(false)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).isEmpty();
    }

    @Test
    void bergegn_kontoer_regel_skal_produsere_sporing_med_json() throws IOException {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                .medFødselsdato(DATO)
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);

        assertThat(new ObjectMapper().readValue(stønadskontoResultat.getInnsendtGrunnlag(), HashMap.class)).isNotNull().isNotEmpty();
        assertThat(new ObjectMapper().readValue(stønadskontoResultat.getEvalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();

    }

    @Test
    void skal_bruke_omsorgsovertakelse_og_ikke_fødselsdato_som_konfig_parameter() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
                //Så tidlig at det vil skape exception hvis bruk ettersom vi ikke har noe konfig for 2016
                .medFødselsdato(LocalDate.of(2016, 1, 1))
                .medOmsorgsovertakelseDato(LocalDate.of(2020, 2, 10))
                .medAntallBarn(1)
                .farRett(true)
                .morRett(true)
                .farAleneomsorg(false)
                .morAleneomsorg(false)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();

        assertThatCode(() -> stønadskontoRegelOrkestrering.beregnKontoer(grunnlag)).doesNotThrowAnyException();
    }

}
