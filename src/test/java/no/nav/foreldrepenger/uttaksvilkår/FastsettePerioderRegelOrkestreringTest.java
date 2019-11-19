package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periodetype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePerioderRegelOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void skal_innvilge_to_perioder_med_med_mødrekvote_på_under_10_uker() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))
                ));

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(resultat).hasSize(3);
        resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .forEach(uttakPeriode -> assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET));
    }

    @Test
    public void skal_knekke_mødrekvote_dersom_det_ikke_er_flere_dager_igjen() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 3);

        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(12).minusDays(1))
                ));

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        List<UttakPeriode> uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);
        /* Innvilget foreldrepenger før fødsel*/
        assertThat(uttakPerioder.get(0).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(0).getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPerioder.get(0).getTom()).isEqualTo(fødselsdato.minusDays(1));

        /* Innvilget mødrekvote etter fødsel frem til og med uke 6*/
        assertThat(uttakPerioder.get(1).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(1).getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPerioder.get(1).getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        /* Innvilget mødrekvote etter fødsel, etter uke 6 */
        assertThat(uttakPerioder.get(2).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(2).getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPerioder.get(2).getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        /* Avslått mødrekvote, ikke nok dager */
        assertThat(uttakPerioder.get(3).getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(uttakPerioder.get(3).getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(uttakPerioder.get(3).getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPerioder.get(3).getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void mødrekvoteMedUtsattOppstartUtenGyldigGrunnSkalTrekkeDagerPåSaldo() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 8);
        LocalDate sluttGyldigUtsattPeriode = fødselsdato.plusDays(6);
        LocalDate startUgyldigPeriode = fødselsdato.plusDays(7);
        LocalDate sluttUgyldigPeriode = startUgyldigPeriode.plusDays(4);

        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, sluttUgyldigPeriode.plusDays(1), sluttUgyldigPeriode.plusWeeks(10)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(fødselsdato, sluttGyldigUtsattPeriode))
                                .build())
                        .build());

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag, new FeatureTogglesForTester());
        List<UttakPeriode> uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());
        assertThat(uttakPerioder).hasSize(6);

        /* FPFF blir innvilget. */
        UttakPeriode foreldrepengerFørFødselPeriode = uttakPerioder.get(0);
        assertThat(foreldrepengerFørFødselPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(foreldrepengerFørFødselPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(foreldrepengerFørFødselPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(foreldrepengerFørFødselPeriode.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);


        /* Første del av opphold-perioden er gyldig utsettelse, men skal likevel behandles manuelt. */
        UttakPeriode gyldigUtsettelsePeriode = uttakPerioder.get(1);
        assertThat(gyldigUtsettelsePeriode.getTom()).isEqualTo(sluttGyldigUtsattPeriode);
        assertThat(gyldigUtsettelsePeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(gyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(gyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(gyldigUtsettelsePeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);


        UttakPeriode ugyldigUtsettelsePeriode = uttakPerioder.get(2);
        assertThat(ugyldigUtsettelsePeriode.getFom()).isEqualTo(sluttGyldigUtsattPeriode.plusDays(1));
        assertThat(ugyldigUtsettelsePeriode.getTom()).isEqualTo(sluttUgyldigPeriode);
        assertThat(ugyldigUtsettelsePeriode.getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(ugyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(ugyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(ugyldigUtsettelsePeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);

        /* Splittes ved knekkpunkt ved 6 uker pga regelflyt */
        UttakPeriode uttakPeriode1 = uttakPerioder.get(3);
        assertThat(uttakPeriode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode1.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(uttakPeriode1.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode1.getFom()).isEqualTo(sluttUgyldigPeriode.plusDays(1));
        assertThat(uttakPeriode1.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode1.getManuellbehandlingårsak()).isNull();

        UttakPeriode uttakPeriode2 = uttakPerioder.get(4);
        assertThat(uttakPeriode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode2.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(uttakPeriode2.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode2.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode2.getTom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(8).plusDays(2));
        assertThat(uttakPeriode2.getManuellbehandlingårsak()).isNull();

        //Det er tom for konto for siste del siden allerede trekk fra saldo for forrige perioder
        // (gyldigutsett + ugyldigutsett) som gikk til manuell behandling
        UttakPeriode uttakPeriode3 = uttakPerioder.get(5);
        assertThat(uttakPeriode3.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode3.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(uttakPeriode3.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode3.getFom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(8).plusDays(3));
        assertThat(uttakPeriode3.getTom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(10));
        assertThat(uttakPeriode3.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    public void delvisUgyldigUtsattMødrekvote() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 8);
        LocalDate gyldigUtsettelseStart = fødselsdato.plusDays(5);
        LocalDate gyldigUtsettelseSlutt = fødselsdato.plusDays(10);

        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, gyldigUtsettelseSlutt.plusDays(1), fødselsdato.plusWeeks(6).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigUtsettelseStart, gyldigUtsettelseSlutt))
                                .build())
                        .build());

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        List<UttakPeriode> uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);

        // Første del av opphold-perioden blir manuell behandling
        UttakPeriode ugyldigUtsattPeriode = uttakPerioder.get(1);
        assertThat(ugyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(ugyldigUtsattPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(ugyldigUtsattPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(ugyldigUtsattPeriode.getTom()).isEqualTo(fødselsdato.plusDays(4));
        assertThat(ugyldigUtsattPeriode.getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(ugyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        UttakPeriode gyldigUtsattPeriode = uttakPerioder.get(2);
        assertThat(gyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(gyldigUtsattPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(gyldigUtsattPeriode.getFom()).isEqualTo(gyldigUtsettelseStart);
        assertThat(gyldigUtsattPeriode.getTom()).isEqualTo(gyldigUtsettelseSlutt);
        assertThat(gyldigUtsattPeriode.getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(gyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        UttakPeriode innvilgetUttakPeriode = uttakPerioder.get(3);
        assertThat(innvilgetUttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(innvilgetUttakPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(innvilgetUttakPeriode.getFom()).isEqualTo(gyldigUtsettelseSlutt.plusDays(1));
        assertThat(innvilgetUttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(innvilgetUttakPeriode.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(innvilgetUttakPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    public void helePeriodenUtenforSøknadsfrist() {
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        grunnlag.medDatoer(datoer(fødselsdato, fødselsdato.plusWeeks(6)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                ));

        // Act
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        // Assert
        assertThat(resultat).hasSize(2);

        Optional<UttakPeriode> førFødsel = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> FORELDREPENGER_FØR_FØDSEL.equals(p.getStønadskontotype())).findFirst();
        assertThat(førFødsel).isPresent();
        assertThat(førFødsel.get().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(førFødsel.get().getManuellbehandlingårsak()).isNull();
        assertThat(førFødsel.get().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);


        Optional<UttakPeriode> mødrekvote = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> MØDREKVOTE.equals(p.getStønadskontotype())).findFirst();
        assertThat(mødrekvote).isPresent();
        assertThat(mødrekvote.get().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(mødrekvote.get().getManuellbehandlingårsak()).isNull();
        assertThat(mødrekvote.get().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    private Datoer datoer(LocalDate fødselsdato, LocalDate førsteLovligeUttaksdag) {
        return new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .build();
    }

    @Test
    public void skalKnekkePeriodenVedGrenseForSøknadsfrist() {
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        LocalDate lovligeUttaksdag = fødselsdato.plusWeeks(1);
        grunnlag.medDatoer(datoer(fødselsdato, lovligeUttaksdag))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                ));

        // Act
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());


        // Assert
        assertThat(resultat).hasSize(3);

        UttakPeriode førFødsel = resultat.get(0).getUttakPeriode();
        assertThat(førFødsel.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(førFødsel.getManuellbehandlingårsak()).isNull();
        assertThat(førFødsel.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);


        UttakPeriode mødrekvoteFørKnekk = resultat.get(1).getUttakPeriode();
        assertThat(mødrekvoteFørKnekk.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(mødrekvoteFørKnekk.getFom()).isEqualTo(fødselsdato);
        assertThat(mødrekvoteFørKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(mødrekvoteFørKnekk.getManuellbehandlingårsak()).isNull();
        assertThat(mødrekvoteFørKnekk.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);

        UttakPeriode mødrekvoteEtterKnekk = resultat.get(2).getUttakPeriode();
        assertThat(mødrekvoteEtterKnekk.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(mødrekvoteEtterKnekk.getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(mødrekvoteEtterKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(mødrekvoteEtterKnekk.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void skal_ikke_innvilge_etter_eller_på_barnets_3årsdag_selv_om_det_er_nok_på_saldoen() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.normal().medDatoer(datoer(fødselsdato, LocalDate.of(2017, 10, 1)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusYears(4))
                ))
                .medKontoer(Map.of(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(MØDREKVOTE, 10000000))
                        .leggTilKonto(konto(FEDREKVOTE, 0))
                        .leggTilKonto(konto(FELLESPERIODE, 0))
                        .build()));

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode frem til 3-årsdag (eksklusiv) innvilges
        //periode knekkes alltid knekt ved 6 uker pga regelflyt
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(3).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //resten av søknadsperide avslås
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(3));
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(4));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isZero();
    }

    @Test
    public void skal_ikke_innvilge_periode_som_starter_på_barnets_3årsdag_eller_som_starter_senere() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(datoer(fødselsdato, LocalDate.of(2017, 10, 1)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusYears(3).minusDays(1)),
                        søknadsperiode(FELLESPERIODE, fødselsdato.plusYears(3), fødselsdato.plusYears(4).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusYears(4), fødselsdato.plusYears(5))
                ))
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(MØDREKVOTE, 10000000))
                        .leggTilKonto(konto(FEDREKVOTE, 15))
                        .leggTilKonto(konto(FELLESPERIODE, 10000))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode frem til 3-årsdag (eksklusiv) innvilges
        //periode knekkes alltid knekt ved 6 uker pga regelflyt
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(3).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode som starter på 3årsdag avslås
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(3));
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(4).minusDays(1));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);

        //periode som starter etter 3årsdag avslås
        assertThat(resultat.get(4).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(4));
        assertThat(resultat.get(4).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(5));
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }


    @Test
    public void skal_avslå_første_del_pga_manglende_omsorg_og_andre_del_pga_tom_på_kvote_siden_dager_trekkes() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(søknadsperiode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusYears(1)))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FEDREKVOTE, 5))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(resultat).hasSize(2);

        UttakPeriode uttakPeriode1 = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode1 instanceof StønadsPeriode);
        assertThat(uttakPeriode1.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode1.getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(1));
        assertThat(uttakPeriode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode1.getManuellbehandlingårsak()).isNull();
        assertThat(uttakPeriode1.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);

        UttakPeriode uttakPeriode2 = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode2 instanceof StønadsPeriode);
        assertThat(uttakPeriode2.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode2.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode2.getManuellbehandlingårsak()).isNull();
        assertThat(uttakPeriode2.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void hele_perioden_skal_avslås_automatisk_når_det_er_søkt_for_sent() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate periodeFom = fødselsdato.plusYears(1);
        LocalDate periodeTom = periodeFom.plusDays(20);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(datoer(fødselsdato, fødselsdato.plusYears(2)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(FEDREKVOTE, periodeFom, periodeTom)))
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FEDREKVOTE, 15))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(1);

        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(periodeFom);
        assertThat(stønadsPeriode.getTom()).isEqualTo(periodeTom);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    @Test
    public void skal_sette_arbeidsprosent_på_søknadsperioder() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 5);
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forFrilans();
        BigDecimal arbeidsprosent = BigDecimal.TEN;
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(datoer(fødselsdato, fødselsdato.plusYears(2)))
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                ))
                .medKontoer(Map.of(aktivitet, new Kontoer.Builder()
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(MØDREKVOTE, 100))
                        .build()))
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(aktivitet, new ArbeidTidslinje.Builder()
                                        .medArbeid(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(20), Arbeid.forFrilans(arbeidsprosent))
                                        .build()))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(2);

        assertThat(resultat.get(0).getUttakPeriode().getProsentArbeid(aktivitet)).isEqualTo(arbeidsprosent);
        assertThat(resultat.get(1).getUttakPeriode().getProsentArbeid(aktivitet)).isEqualTo(arbeidsprosent);
    }

    @Test
    public void skal_sette_arbeidsprosent_på_oppholdsperioder() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 5);
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forFrilans();
        BigDecimal arbeidsprosent = BigDecimal.TEN;
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.normal()
                .medDatoer(datoer(fødselsdato, fødselsdato.plusYears(2)))
                .medSøknad(søknad(
                        //ikke søkt fpff
                        Søknadstype.FØDSEL, søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        //opphold mellom mødrekvote
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(10))
                ))
                .medKontoer(Map.of(aktivitet, new Kontoer.Builder()
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(MØDREKVOTE, 100))
                        .build()))
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(aktivitet, new ArbeidTidslinje.Builder()
                                        .medArbeid(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(20), Arbeid.forFrilans(arbeidsprosent))
                                        .build()))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);

        assertThat(resultat.get(0).getUttakPeriode().getProsentArbeid(aktivitet)).isEqualTo(arbeidsprosent);
        assertThat(resultat.get(2).getUttakPeriode().getProsentArbeid(aktivitet)).isEqualTo(arbeidsprosent);
    }

    @Test
    public void UT1290_skal_være_ugyldige_stønadskonto_hvis_søker_på_konto_som_man_ikke_har_tilgang_til() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10)))
                        .build());

        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(2).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(resultat.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(build.getArbeid().getAktiviteter().get(0)).decimalValue()).isNotZero();
        assertThat(resultat.get(2).getUttakPeriode().getUtbetalingsgrad(build.getArbeid().getAktiviteter().get(0))).isZero();
    }

    @Test
    public void skal_sette_arbeidsprosent() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8).minusDays(1), BigDecimal.valueOf(44));
        Kontoer kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(FELLESPERIODE, 130))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .leggTilKonto(konto(FEDREKVOTE, 50))
                .build();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(FEDREKVOTE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8).minusDays(1))));

        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        assertThat(resultat).hasSize(1);
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);

        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(44));
    }

    @Test
    public void skal_knekke_periode_og_gå_til_manuell_behandling_ved_ikke_nok_flerbarnsdager() {
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forArbeid("1234", "12345");

        LocalDate fødselsdato = LocalDate.of(2019, 3, 13);
        LocalDate tom = Virkedager.plusVirkedager(fødselsdato.plusWeeks(6), 5);
        AnnenpartUttaksperiode annenPartPeriode1 = AnnenpartUttaksperiode.Builder.uttak(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, new Trekkdager(15), BigDecimal.TEN))
                .build();
        AnnenpartUttaksperiode annenPartPeriode2 = AnnenpartUttaksperiode.Builder.uttak(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.MØDREKVOTE, new Trekkdager(30), BigDecimal.TEN))
                .build();
        AnnenpartUttaksperiode annenPartPeriode3 = AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(6), tom)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FELLESPERIODE,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), tom)), BigDecimal.TEN))
                .build();
        RegelGrunnlag periodeGrunnlag = RegelGrunnlagTestBuilder.normal()
                .medKontoer(Map.of(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FEDREKVOTE, 50))
                        .leggTilKonto(konto(MØDREKVOTE, 50))
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .leggTilKonto(konto(FELLESPERIODE, 130))
                        .leggTilKonto(konto(FLERBARNSDAGER, 5))
                        .build()))
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6), tom, true,
                        PeriodeVurderingType.PERIODE_OK, null)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenPartPeriode1)
                        .leggTilUttaksperiode(annenPartPeriode2)
                        .leggTilUttaksperiode(annenPartPeriode3)
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(periodeGrunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(2);
        UttakPeriode førstePeriode = resultat.get(0).getUttakPeriode();
        assertThat(førstePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(førstePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(førstePeriode.getTom()).isEqualTo(tom.minusDays(1));

        UttakPeriode andrePeriode = resultat.get(1).getUttakPeriode();
        assertThat(andrePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(andrePeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(andrePeriode.getFom()).isEqualTo(tom);
        assertThat(andrePeriode.getTom()).isEqualTo(tom);
    }

    @Test
    public void skal_håndtere_at_både_flerbarnsdager_og_foreldrepenger_kvote_går_tom_i_samme_søknadsperiode_på_forskjellig_dato() {
        LocalDate fødselsdato = LocalDate.of(2019, 3, 13);

        RegelGrunnlag periodeGrunnlag = RegelGrunnlagTestBuilder.normal()
                .medKontoer(Map.of(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        //Går først tom for foreldrepenger, deretter tom på flerbarnsdager
                        .leggTilKonto(konto(FORELDREPENGER, 75))
                        .leggTilKonto(konto(FLERBARNSDAGER, 150))
                        .build()))
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), false, PeriodeVurderingType.PERIODE_OK, null),
                        søknadsperiode(Stønadskontotype.FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(100), true, PeriodeVurderingType.PERIODE_OK, null)
                ))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(periodeGrunnlag, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    public void skalIkkeKasteExceptionVedUtsettelseFraDerSaldoGårUt() {
        LocalDate fødselsdato = LocalDate.of(2018, 8, 20);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.annenAktivitet();
        RegelGrunnlag regelGrunnlag = new RegelGrunnlag.Builder()
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter().leggTil(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build()))
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .leggTilKontoer(aktivitetIdentifikator, new Kontoer.Builder()
                        .leggTilKonto(konto(MØDREKVOTE, 75))
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .medMottattDato(fødselsdato.minusWeeks(4))
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 7, 30), LocalDate.of(2018, 8, 19)))
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, LocalDate.of(2018, 8, 20), LocalDate.of(2018, 12, 2)))
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31),
                                Utsettelseårsaktype.INNLAGT_HELSEINSTITUSJON, PeriodeVurderingType.PERIODE_OK, null, false))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderMedInnleggelse(new PeriodeMedInnleggelse(LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31)))
                                .build())
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build())
                .build();

        assertThatCode(() -> fastsettePerioderRegelOrkestrering.fastsettePerioder(regelGrunnlag, new FeatureTogglesForTester())).doesNotThrowAnyException();
    }

    @Test
    public void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_mor() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(13).minusDays(1))
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        UttakPeriode uttakPeriode = resultat.get(4).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_filtrere_bort_perioden_som_kun_er_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        UttakPeriode periodeSomKunErHelg = søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(6).plusDays(5), fødselsdato.plusWeeks(6).plusDays(6));
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6).plusDays(4)),
                        periodeSomKunErHelg,
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8))
                ));

        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        // Søker om 5 perioder, men den ene blir filtrert bort, derav 4
        assertThat(resultat).hasSize(4);
    }


    @Test
    public void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_far() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FEDREKVOTE, 15))
                        .leggTilKonto(konto(FELLESPERIODE, 50))
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, søknadsperiode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1)),
                        søknadsperiode(FEDREKVOTE, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(10).minusDays(1)),
                        søknadsperiode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1)),
                        søknadsperiode(FELLESPERIODE, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(21).minusDays(1))
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        UttakPeriode uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_gå_regne_ubetalingsprosent_ut_fra_samtidig_uttaksprosent_hvis_ikke_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 5 * 15))
                .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), false,
                                PeriodeVurderingType.PERIODE_OK, new SamtidigUttak(BigDecimal.valueOf(50)))
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        UttakPeriode periodeMedSamtidigUttak = resultat.get(2).getUttakPeriode();

        assertThat(periodeMedSamtidigUttak.getUtbetalingsgrad(build.getArbeid().getAktiviteter().get(0))).isEqualTo(BigDecimal.valueOf(50));
    }

    @Test
    public void skal_gå_regne_trekkdager_ut_fra_samtidig_uttaksprosent_hvis_ikke_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 5 * 15))
                .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        søknadsperiode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), false,
                                PeriodeVurderingType.PERIODE_OK, new SamtidigUttak(BigDecimal.valueOf(80)))
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        UttakPeriode periodeMedSamtidigUttak = resultat.get(2).getUttakPeriode();

        assertThat(periodeMedSamtidigUttak.getTrekkdager(build.getArbeid().getAktiviteter().get(0))).isEqualTo(new Trekkdager(8));
    }

    @Test
    public void skal_gå_til_manuell_behandling_når_søker_er_tom_for_sine_konto_men_søkt_om_overføring() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FEDREKVOTE, 15))
                .leggTilKonto(konto(MØDREKVOTE, 15))
                .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(StønadsPeriode.medOverføringAvKvote(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(9),
                                fødselsdato.plusWeeks(11).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .leggTilSøknadsperiode(søknadsperiode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(11).minusDays(1)))
                                .build())
                        .build());

        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        UttakPeriode uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til manuell behandling
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_OVERFØRING);
        assertThat(stønadsPeriode.getÅrsak()).isNull();
        assertThat(stønadsPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(10));
        assertThat(stønadsPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void oppholdsperiode_skal_knekke_og_bevare_årsaken() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 100))
                .leggTilKonto(konto(FEDREKVOTE, 5))
                .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilSøknadsperiode(søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        //Går tom for fedrekvote i oppholdsperioden
                        .leggTilSøknadsperiode(new OppholdPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.KVOTE_ANNEN_FORELDER,
                                fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8), null, false))
                        .build());

        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(2).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        assertThat(((OppholdPeriode) resultat.get(2).getUttakPeriode()).getOppholdårsaktype()).isEqualTo(Oppholdårsaktype.KVOTE_ANNEN_FORELDER);
        assertThat(resultat.get(3).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        assertThat(((OppholdPeriode) resultat.get(3).getUttakPeriode()).getOppholdårsaktype()).isEqualTo(Oppholdårsaktype.KVOTE_ANNEN_FORELDER);
    }

    @Test
    public void skal_knekke_på_riktig_dato_når_konto_går_tom_og_periode_har_samtidig_uttak() {
        LocalDate fødselsdato = LocalDate.of(2019, 3, 19);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        LocalDate sisteUttaksdato = LocalDate.of(2019, 5, 13);
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 5))
                .build())
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                        )
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        søknadsperiode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2019, 2, 27), LocalDate.of(2019, 3, 18)),
                        søknadsperiode(MØDREKVOTE, LocalDate.of(2019, 4, 19), sisteUttaksdato, false,
                                PeriodeVurderingType.IKKE_VURDERT, new SamtidigUttak(BigDecimal.valueOf(80)))
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());

        assertThat(resultat.get(resultat.size() - 1).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2019, 4, 30));
        assertThat(resultat.get(resultat.size() - 1).getUttakPeriode().getTom()).isEqualTo(sisteUttaksdato);
    }

    @Test
    public void far_skal_kunne_få_overført_perioder_fra_mor_i_revurdering_med_endringsdato() {
        var fødselsdato = LocalDate.of(2019, 1, 24);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(MØDREKVOTE, 75 - 28))
                .build())
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                        )
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medErTapende(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(LocalDate.of(2019, 3, 4)).build())
                //far har fått overført mange dager av mor. Det er noen dager igjen til mor. Skal avslå alle mors perioder og ikke trekk dager
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 6))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.MØDREKVOTE, new Trekkdager(3), BigDecimal.valueOf(100)))
                                .build())
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(LocalDate.of(2019, 3, 7), LocalDate.of(2019, 3, 31))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.MØDREKVOTE, new Trekkdager(17), BigDecimal.valueOf(100)))
                                .build())
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 5, 3))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.MØDREKVOTE, new Trekkdager(25), BigDecimal.valueOf(100)))
                                .build())
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        søknadsperiode(MØDREKVOTE, LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 15))
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isZero();
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isZero();
    }

    @Test
    public void skal_knekke_på_riktig_dato_når_flerbarnskonto_går_tom() {
        LocalDate fødselsdato = LocalDate.of(2019, 3, 19);
        grunnlag = RegelGrunnlagTestBuilder.normal();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 20))
                .leggTilKonto(konto(FELLESPERIODE, 20))
                .leggTilKonto(konto(FLERBARNSDAGER, 5))
                .build())
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                        )
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        søknadsperiode(FELLESPERIODE, fødselsdato.plusWeeks(6), LocalDate.of(2020, 5, 13), true,
                                PeriodeVurderingType.IKKE_VURDERT, null)
                ));


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());
        assertThat(resultat).hasSize(3);
        //Flerbarnsdager går tom, trekkdager satt til 5 dager som er resten av flerbansdager kontoen
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(7).minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));

        //15 dager fellesperiode igjen
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(15));

        //Resten blir avslått
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2020, 5, 13));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(0));
    }

    @Test
    public void skal_håndtere_flere_knekk_i_samme_søknadsperiode_ved_flere_arbeidsforhold() {
        LocalDate fødselsdato = LocalDate.of(2019, 5, 28);
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder();
        grunnlag.leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 50)).build())
                .leggTilKontoer(ARBEIDSFORHOLD_2, new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 35)).build())
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter()
                                .leggTil(ARBEIDSFORHOLD_1, new ArbeidTidslinje.Builder().build())
                                .leggTil(ARBEIDSFORHOLD_2, new ArbeidTidslinje.Builder().build())
                        )
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medAleneomsorg(true)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(6)))
                        .leggTilSøknadsperiode(søknadsperiode(FORELDREPENGER, fødselsdato.plusWeeks(6).plusDays(1), fødselsdato.plusWeeks(15)))
                        .build()
                )
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());


        RegelGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build, new FeatureTogglesForTester());
    }
}
