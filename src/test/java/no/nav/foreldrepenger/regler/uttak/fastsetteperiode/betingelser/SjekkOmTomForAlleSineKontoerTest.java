package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmTomForAlleSineKontoerTest {

    @Test
    void når_søknadstype_er_fødsel_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_vær_MK_FP_og_FORELDREPENGER() { // 1

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE,
                Stønadskontotype.FORELDREPENGER);
    }

    private List<Stønadskontotype> stønadskontotypene(RegelGrunnlag grunnlag) {
        return SjekkOmTomForAlleSineKontoer.hentSøkerSineKontoer(new FastsettePeriodeGrunnlagImpl(grunnlag, null, null));
    }

    @Test
    void når_søker_har_kontoene_FPFF_MK_FP_er_søker_ikke_tom_for_alle_sine_konto_selvom_en_konto_er_tom() {
        var periodeStart = LocalDate.of(2018, 1, 8);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt, null, false,
                PeriodeVurderingType.IKKE_VURDERT, null, null, null);
        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(15 * 5))
                .leggTilKonto(new Konto.Builder().medType(Stønadskontotype.FELLESPERIODE).medTrekkdager(10 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .build();

        var sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();
        uttakPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), kontoer.build(),
                uttakPeriode.getFom(), grunnlag.getArbeid().getAktiviteter(), periodeStart.atStartOfDay(), null);
        var evaluation = sjekkOmTomForAlleSineKontoer.evaluate(
                new FastsettePeriodeGrunnlagImpl(grunnlag, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), uttakPeriode));
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    void når_søknadstype_er_fødsel_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 2

        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER).medTrekkdager(50));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    void når_søknadstype_er_fødsel_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 3

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE,
                Stønadskontotype.FORELDREPENGER);
    }

    @Test
    void når_søknadstype_er_fødsel_og_søker_er_far_og_kun_far_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 4

        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER).medTrekkdager(50));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    void når_søknadstype_er_adopsjon_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_være_MK_FP_og_FORELDREPENGER() { // 5

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE,
                Stønadskontotype.FORELDREPENGER);
    }

    @Test
    void når_søknadstype_er_adopsjon_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 6

        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER).medTrekkdager(50));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    void når_søknadstype_er_adopsjon_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 7

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE,
                Stønadskontotype.FORELDREPENGER);
    }

    @Test
    void når_søknadstype_er_adopsjon_og_søker_er_far_og_kun_en_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 8

        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medType(Stønadskontotype.FORELDREPENGER).medTrekkdager(50));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON))
                .build();

        var stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }
}
