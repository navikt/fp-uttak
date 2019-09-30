package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringKnekkpunktTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void foreldrepengerFørFødssel_søknad_fra_3_uker_før_fødsel_til_og_med_fødsel_blir_knekt_ved_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato)))
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)
                                .medTrekkdager(3 * 5)
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato);
    }


    @Test
    public void foreldrepengerFørFødsel_periode_dekker_alle_ukene_før_fødsel_slutter_etter_fødsel_blir_hvor_ukene_etter_fødsel_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(4))))
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)
                                .medTrekkdager(3 * 5)
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4));
    }

    @Test
    public void riktig_knekk_ved_tom_for_dager_ifm_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 5, 15);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(søknad(
                    Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, LocalDate.of(2018, 6, 30)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, LocalDate.of(2018, 7, 1), LocalDate.of(2018, 12, 21))
                ))
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)
                                .medTrekkdager(15)
                                .build())
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.MØDREKVOTE)
                                .medTrekkdager(15 * 5)
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 6, 26));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 6, 30));

        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 7, 1));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 8, 27));

        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(4).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(4).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 8, 28));
        assertThat(perioder.get(4).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 12, 21));
    }

    @Test
    public void periode_etter_knekk_skal_gå_videre() {
        LocalDate fødselsdato = LocalDate.of(2018, 5, 15);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(søknad(
                    Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15)),
                        søknadsperiode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(15).plusDays(1), fødselsdato.plusWeeks(15).plusDays(2))
                ))
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)
                                .medTrekkdager(15)
                                .build())
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.MØDREKVOTE)
                                .medTrekkdager(15 * 5)
                                .build())
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FELLESPERIODE)
                                .medTrekkdager(2)
                                .build())
                        .build())
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //tom for mødrekvote
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void en_lang_manglende_søkt_før_første_uttaksperiode_skal_gå_til_manuell_når_alle_dager_brukes_opp() {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 13);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .medAleneomsorg(false)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD,
                                LocalDate.of(2019, 3, 20), LocalDate.of(2019, 12, 24),
                                null, false))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(200).build()).build());

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }
}
