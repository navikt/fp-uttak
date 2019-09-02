package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmTomForAlleSineKontoerTest {

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_vær_MK_FP_og_FORELDREPENGER() { // 1

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    private List<Stønadskontotype> stønadskontotypene(RegelGrunnlag grunnlag) {
        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();

        return sjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.emptyList()), null));
    }

    @Test
    public void når_søker_har_kontoene_FPFF_MK_FP_er_søker_ikke_tom_for_alle_sine_konto_selvom_en_konto_er_tom() {
        LocalDate periodeStart = LocalDate.of(2018, 1, 8);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.MØDREKVOTE)
                                .medTrekkdager(15 * 5)
                                .build())
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FELLESPERIODE)
                                .medTrekkdager(10 * 5)
                                .build())
                        .build())
                .build();

        SjekkOmTomForAlleSineKontoer sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();
        Evaluation evaluation = sjekkOmTomForAlleSineKontoer.evaluate(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode));
        Assertions.assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 2

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER)
                                .medTrekkdager(50)
                                .build())
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 3

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_far_og_kun_far_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 4

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER)
                                .medTrekkdager(50)
                                .build())
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_være_MK_FP_og_FORELDREPENGER() { // 5

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 6

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER)
                                .medTrekkdager(50)
                                .build())
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 7

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_far_og_kun_en_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 8

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER)
                                .medTrekkdager(50)
                                .build())
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .build())
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        Assertions.assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }
}
