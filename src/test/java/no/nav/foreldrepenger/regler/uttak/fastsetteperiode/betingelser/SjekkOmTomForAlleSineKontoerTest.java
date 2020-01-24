package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmTomForAlleSineKontoerTest {

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_vær_MK_FP_og_FORELDREPENGER() { // 1

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    private List<Stønadskontotype> stønadskontotypene(RegelGrunnlag grunnlag) {
        return SjekkOmTomForAlleSineKontoer.hentSøkerSineKonto(new FastsettePeriodeGrunnlagImpl(grunnlag, null, null));
    }

    @Test
    public void når_søker_har_kontoene_FPFF_MK_FP_er_søker_ikke_tom_for_alle_sine_konto_selvom_en_konto_er_tom() {
        var periodeStart = LocalDate.of(2018, 1, 8);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, periodeStart, periodeSlutt, null, false);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .medTrekkdager(15 * 5))
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FELLESPERIODE)
                        .medTrekkdager(10 * 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .build();

        var sjekkOmTomForAlleSineKontoer = new SjekkOmTomForAlleSineKontoer();
        uttakPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(),
                kontoer.build(), uttakPeriode.getFom(), grunnlag.getArbeid().getAktiviteter());
        var evaluation = sjekkOmTomForAlleSineKontoer.evaluate(new FastsettePeriodeGrunnlagImpl(grunnlag,
                SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), uttakPeriode));
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 2

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(50));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 3

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_fødsel_og_søker_er_far_og_kun_far_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 4

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(50));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_mor_og_begge_har_rett_skal_søker_sine_kontoer_være_MK_FP_og_FORELDREPENGER() { // 5

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_mor_og_kun_mor_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 6

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(50));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_far_og_begge_har_rett_skal_søker_sine_kontoer_være_FK_FP_og_FORELDREPENGER() { // 7

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void når_søknadstype_er_adopsjon_og_søker_er_far_og_kun_en_har_rett_skal_søker_sine_kontoer_være_FORELDREPENGER() { // 8

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(50));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON))
                .build();

        List<Stønadskontotype> stønadskontotypene = stønadskontotypene(grunnlag);
        assertThat(stønadskontotypene).containsExactly(Stønadskontotype.FORELDREPENGER);
    }
}
