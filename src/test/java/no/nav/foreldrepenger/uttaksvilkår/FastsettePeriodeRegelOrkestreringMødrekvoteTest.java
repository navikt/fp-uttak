package no.nav.foreldrepenger.uttaksvilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringMødrekvoteTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void mødrekvoteperiode_før_familiehendelse() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3),
                                fødselsdato.minusWeeks(1).minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(1),
                                fødselsdato.plusWeeks(6).minusDays(1), null, false))
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(1).minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(1));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
    }

    @Test
    public void overføring_av_mødrekvote_grunnet_sykdom_skade_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1),
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void overføring_av_mødrekvote_grunnet_sykdom_skade_skal_gå_til_manuell_behandling_hvis_ikke_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), null, false))
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
    }

    @Test
    public void overføring_av_mødrekvote_ugyldig_årsak_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), null, PeriodeVurderingType.UAVKLART_PERIODE, null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), null, false))
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote skal til manuell behandling
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote skal til manuell behandling
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote skal til manuell behandling
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void overføring_av_mødrekvote_grunnet_sykdom_skade_men_far_har_ikke_omsorg_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder()
                    .medFødsel(fødselsdato)
                    .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                    .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1006
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1006
        assertThat(perioder.get(1).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote avslått
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET); // UT1031
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }
}
