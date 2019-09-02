package no.nav.foreldrepenger.uttaksvilkår;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringFedrekvoteTest extends FastsettePerioderRegelOrkestreringTestBase {


    @Test
    public void fedrekvote_med_tidlig_oppstart_og_gyldig_grunn_fra_første_dag_til_midten_av_perioden_blir_innvilget_med_knekkpunkt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(farBehandling())
                .medSøknad(søknad(
                    Søknadstype.FØDSEL,
                    søknadsperiode(FEDREKVOTE, fødselsdato, fødselsdato.plusWeeks(1).minusDays(1)),
                    søknadsperiode(FEDREKVOTE, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2), false, PeriodeVurderingType.UAVKLART_PERIODE, null)
                ))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(2);

        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), INNVILGET, FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2), MANUELL_BEHANDLING, FEDREKVOTE);
    }


    @Test
    public void skal_gi_ikke_innvilget_når_far_har_gyldig_grunn_til_tidlig_oppstart_men_ikke_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(søknadsperiode(FEDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(FEDREKVOTE)
                                .medTrekkdager(100)
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> periodeResultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(periodeResultater).hasSize(1);
        List<UttakPeriode> perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserAvslåttPeriode(perioder.get(0), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void fedrekvote_fra_1_dag_før_6_uker_skal_behandles_manuelt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 3);
        LocalDate førsteLovligeUttaksdag = fødselsdato.withDayOfMonth(1).minusMonths(3);

        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                    søknadsperiode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6).minusDays(1), fødselsdato.plusWeeks(10).minusDays(1),
                            false, PeriodeVurderingType.UAVKLART_PERIODE, null)))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(2);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6).minusDays(1), fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }


    @Test
    public void fedrekvote_før_6_uker_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        grunnlag.medDatoer(datoer(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(farBehandling())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                    søknadsperiode(Stønadskontotype.FEDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), false,
                            PeriodeVurderingType.UAVKLART_PERIODE, null)))
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(2);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
    }

    @Test
    public void fedrekvote_bli_ikke_innvilget_når_søker_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(farBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(søknadsperiode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(100)))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(FEDREKVOTE)
                                .medTrekkdager(100)
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void overføring_av_fedrekvote_grunnet_sykdom_skade_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                        .leggTilSøknadsperiode(overføringPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        //assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void overføring_av_fedrekvote_grunnet_sykdom_skade_skal_gå_til_manuell_behandling_hvis_ikke_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(
                    Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)),
                        overføringPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK)
                ))
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    public void overføring_av_fedrekvote_ugyldig_årsak_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag
                .medDatoer(datoer(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medBehandling(morBehandling())
                .medSøknad(søknad(
                    Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)),
                        overføringPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), null, PeriodeVurderingType.UAVKLART_PERIODE)
                        )
                )
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote skal til manuell behandling
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    private UttakPeriode overføringPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, OverføringÅrsak årsak, PeriodeVurderingType vurderingType) {
        return StønadsPeriode.medOverføringAvKvote(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, årsak, vurderingType, null, false);
    }

    private Behandling morBehandling() {
        return new Behandling.Builder().medSøkerErMor(true).build();
    }

    private Datoer datoer(LocalDate fødselsdato) {
        return new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .build();
    }

    private Behandling farBehandling() {
        return new Behandling.Builder().medSøkerErMor(false).build();
    }
}
