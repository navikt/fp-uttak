package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringFellesperiodeTest extends FastsettePerioderRegelOrkestreringTestBase {

    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = førsteLovligeUttaksdag(fødselsdato);

    @Test
    public void fellesperiode_mor_etter_uke_7_etter_fødsel_uten_nok_dager_blir_innvilget_med_knekk_og_avslått_periode_på_resten() {
        Kontoer kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(1000).build())
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(1000).build())
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(4 * 5).build())
                .build();
        basicGrunnlagMor()
                .medSøknad(søknad(
                    Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        søknadsperiode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15).minusDays(1))))
                .leggTilKontoer(ARBEIDSFORHOLD, kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    private Kontoer fellesperiodeKonto(int trekkdager) {
        return new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(FELLESPERIODE)
                        .medTrekkdager(trekkdager)
                        .build())
                .build();
    }

    @Test
    public void fellesperiode_far_etter_uke_7_etter_fødsel_blir_manuell_behandling_pga_aktivitetskravet() {
        basicGrunnlagFar()
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15))))
                .leggTilKontoer(ARBEIDSFORHOLD, fellesperiodeKonto(4 * 5));

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15), FELLESPERIODE, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    @Test
    public void for_tidlig_fellesperiode_far_blir_knekt_og_må_behandles_manuelt() {
        basicGrunnlagFar()
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FELLESPERIODE, fødselsdato.minusWeeks(5), fødselsdato.plusWeeks(1))));

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(3);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(3).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1), Perioderesultattype.INNVILGET, FELLESPERIODE);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_etter_3_uker_før_fødsel_blir_innvilget_med_knekk_ved_3_uker_resten_blir_manuell_behandling() {
        Kontoer kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(3*5).build())
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(15*5).build())
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(16*5).build())
                .build();

        var grunnlag = basicGrunnlagMor()
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FELLESPERIODE, fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(1).minusDays(1)),
                        søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(1), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                ))
                .leggTilKontoer(ARBEIDSFORHOLD, kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1), Perioderesultattype.INNVILGET, FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(1).minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.minusWeeks(1), fødselsdato.minusDays(1), Perioderesultattype.INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(3).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), Perioderesultattype.INNVILGET, MØDREKVOTE);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_etter_fødsel_blir_knekt_ved_6_uker_og_må_behandles_manuelt() {
        Kontoer kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(15).medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(50).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(13 * 5).medType(Stønadskontotype.FELLESPERIODE).build())
                .build();
        basicGrunnlagMor()
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10).minusDays(1))))
                .leggTilKontoer(ARBEIDSFORHOLD, kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(4);
        assertThat(resultater.get(0).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(3), Stønadskontotype.FORELDREPENGER_FØR_FØDSEL,
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
        assertThat(resultater.get(1).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        verifiserAvslåttPeriode(resultater.get(1).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(3).minusDays(3), MØDREKVOTE, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserPeriode(resultater.get(3).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), Perioderesultattype.INNVILGET, FELLESPERIODE);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_før_12_uker_blir_avslått_med_knekk_ved_12_uker_før_fødsel() {
        Kontoer kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .medTrekkdager(1000)
                        .build())
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FELLESPERIODE)
                        .medTrekkdager(13 * 5)
                        .build())
                .build();
        basicGrunnlagMor()
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FELLESPERIODE, fødselsdato.minusWeeks(13), fødselsdato)))
                .leggTilKontoer(ARBEIDSFORHOLD, kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultater).hasSize(5);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(13), fødselsdato.minusWeeks(12).minusDays(1), Perioderesultattype.AVSLÅTT, Stønadskontotype.FELLESPERIODE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1), Perioderesultattype.INNVILGET, Stønadskontotype.FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato, fødselsdato, FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(resultater.get(4).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        verifiserAvslåttPeriode(resultater.get(4).getUttakPeriode(), fødselsdato.plusDays(1), fødselsdato.plusWeeks(6).minusDays(3), MØDREKVOTE, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
    }

    private RegelGrunnlag.Builder basicGrunnlagMor() {
        return basicGrunnlag()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build());
    }

    private RegelGrunnlag.Builder basicGrunnlagFar() {
        return basicGrunnlag()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build());
    }

    private RegelGrunnlag.Builder basicGrunnlag() {
        return grunnlag
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                        .medFødsel(fødselsdato)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build());
    }
}
