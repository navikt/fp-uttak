package no.nav.foreldrepenger.uttaksvilkår;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Medlemskap;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class KnekkpunktIdentifisererTest {

    @Test
    public void skal_finne_knekkpunkter_for_søknad_ved_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeSøknadsperiode))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                fødselsdato.minusWeeks(12), //tidligste mulige uttak
                førsteLovligeSøknadsperiode,//ifbm søknadsfrist
                fødselsdato.minusWeeks(3),  //foreldrepenger før fødsel
                fødselsdato,
                fødselsdato.plusWeeks(6),   //slutt på periode reservert mor
                fødselsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_finne_knekkpunkter_for_opphørsdato_for_medlemskap() {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        LocalDate opphørsdato = fødselsdato.plusWeeks(1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeSøknadsperiode))
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(opphørsdato).build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(opphørsdato);
    }

    @Test
    public void skal_finne_knekkpunkter_ved_adopsjon() {
        LocalDate adopsjonsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medOmsorgsovertakelse(adopsjonsdato)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
                førsteLovligeSøknadsperiode,  //ifbm søknadsfrist
                adopsjonsdato,
                adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_lage_knekkpunkt_ved_start_og_dagen_etter_periode_medslutt_av_() {
        LocalDate adopsjonsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(adopsjonsdato.plusDays(100), adopsjonsdato.plusDays(300)))
                                .build())
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medOmsorgsovertakelse(adopsjonsdato)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
                førsteLovligeSøknadsperiode,  //ifbm søknadsfrist
                adopsjonsdato,

                adopsjonsdato.plusDays(100), //første dag uten omsorg
                adopsjonsdato.plusDays(301), //første dag med omsorg

                adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_lage_knekkpunkt_ved_start_og_dagen_etter_periode_for_alle_perioder_som_ikke_er_sammenhengende() {
        LocalDate adopsjonsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(adopsjonsdato.plusDays(100), adopsjonsdato.plusDays(200)))
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(adopsjonsdato.plusDays(300), adopsjonsdato.plusDays(400)))
                                .build())
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medOmsorgsovertakelse(adopsjonsdato)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
                førsteLovligeSøknadsperiode,  //ifbm søknadsfrist
                adopsjonsdato,

                adopsjonsdato.plusDays(100), //første dag uten omsorg
                adopsjonsdato.plusDays(201), //første dag med omsorg
                adopsjonsdato.plusDays(300), //første dag uten omsorg
                adopsjonsdato.plusDays(401), //første dag med omsorg

                adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_finne_knekkpunkter_ved_endring_i_arbeidsprosent() {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        AktivitetIdentifikator arbeidsforhold1 = RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
        LocalDate tirsdagUke18 = LocalDate.of(2018, 5, 1);
        LocalDate fredagUke18 = LocalDate.of(2018, 5, 4);
        LocalDate mandagUke19 = LocalDate.of(2018, 5, 7);
        LocalDate torsdagUke19 = LocalDate.of(2018, 5, 10);
        LocalDate fredagUke19 = LocalDate.of(2018, 5, 11);
        LocalDate fredagUke20 = LocalDate.of(2018, 5, 18);
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(tirsdagUke18, fredagUke18, Arbeid.forOrdinærtArbeid(BigDecimal.valueOf(50), BigDecimal.ZERO))
                .medArbeid(mandagUke19, torsdagUke19, Arbeid.forOrdinærtArbeid(BigDecimal.valueOf(50), BigDecimal.ZERO))
                .medArbeid(fredagUke19, fredagUke20, Arbeid.forOrdinærtArbeid(BigDecimal.valueOf(99), BigDecimal.ZERO))
                .build();

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeSøknadsperiode))
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter().leggTil(arbeidsforhold1, arbeidTidslinje))
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));

        assertThat(knekkpunkter).containsOnly(
                tirsdagUke18, //starter her
                fredagUke19, //endrer fra torsdag-fredag, fredag er den første i perioden
                fredagUke20.plusDays(3) //mandag er den første i neste periode
        );
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart1() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 6, 1);
        LocalDate fødselsdato = uttakStartdato.minusMonths(1);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        LocalDate knekkdato = uttakStartdato.plusDays(1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(new FastsattPeriodeAnnenPart.Builder(uttakStartdato, uttakStartdato.plusDays(10)).build())
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, knekkdato, uttakStartdato.plusDays(10),
                                new SamtidigUttak(BigDecimal.TEN), true))
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medFødsel(fødselsdato)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(uttakStartdato, knekkdato);
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart2() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 10, 1);
        LocalDate fødselsdato = uttakStartdato.minusWeeks(7);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        LocalDate knekkdato = uttakStartdato.plusDays(1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(new FastsattPeriodeAnnenPart.Builder(uttakStartdato.plusDays(1), uttakStartdato.plusDays(10)).build())
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, uttakStartdato, knekkdato,
                                new SamtidigUttak(BigDecimal.TEN), true))
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(knekkdato, uttakStartdato, uttakStartdato.plusDays(11), knekkdato.plusDays(1));
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperMidtI() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 10, 1);
        LocalDate fødselsdato = uttakStartdato.minusWeeks(7);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(new FastsattPeriodeAnnenPart.Builder(uttakStartdato.plusDays(1), uttakStartdato.plusDays(5)).build())
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, uttakStartdato, uttakStartdato.plusDays(6),
                                new SamtidigUttak(BigDecimal.TEN), true))
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medFødsel(fødselsdato)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(uttakStartdato, uttakStartdato.plusDays(7), uttakStartdato.plusDays(1), uttakStartdato.plusDays(6));
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperSluttAvPeriode() {
        final LocalDate stønadsperiodeFom = LocalDate.of(2018, 10, 1);
        LocalDate stønadsperiodeTom = stønadsperiodeFom.plusDays(6);
        LocalDate fødselsdato = stønadsperiodeFom.minusWeeks(7);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);

        LocalDate annenPartPeriodeFom = stønadsperiodeFom.plusDays(4);
        LocalDate annenPartPeriodeTom = stønadsperiodeFom.plusDays(12);

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(new FastsattPeriodeAnnenPart.Builder(annenPartPeriodeFom, annenPartPeriodeTom).build())
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, stønadsperiodeFom, stønadsperiodeTom,
                                new SamtidigUttak(BigDecimal.TEN), true))
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(stønadsperiodeFom, stønadsperiodeTom.plusDays(1), annenPartPeriodeFom, annenPartPeriodeTom.plusDays(1));
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisGraderingStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        StønadsPeriode gradertStønadsperiode = StønadsPeriode.medGradering(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato.minusMonths(1),
                mottattdato.minusWeeks(2), Collections.singletonList(gradertArbeidsforhold),
                BigDecimal.valueOf(30), PeriodeVurderingType.PERIODE_OK, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(mottattdato)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medType(Behandlingtype.REVURDERING)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(gradertStønadsperiode)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisGraderingStarterPåMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(mottattdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(StønadsPeriode.medGradering(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato, mottattdato.plusWeeks(2),
                                Collections.singletonList(gradertArbeidsforhold), BigDecimal.valueOf(30), PeriodeVurderingType.PERIODE_OK,
                                new SamtidigUttak(BigDecimal.TEN), false))
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerIkkeKnekkPåEndringssøknadMottattdatoHvisGraderingStarterEtterMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(mottattdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(StønadsPeriode.medGradering(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato.plusWeeks(1),
                                mottattdato.plusWeeks(2), Collections.singletonList(gradertArbeidsforhold),
                                BigDecimal.valueOf(30), PeriodeVurderingType.PERIODE_OK, null, false))
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).doesNotContain(mottattdato);
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(mottattdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, mottattdato.minusWeeks(2),
                                mottattdato.minusWeeks(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medType(Behandlingtype.REVURDERING)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterPåMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(mottattdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, mottattdato,
                        mottattdato.plusWeeks(2), Utsettelseårsaktype.ARBEID, PeriodeVurderingType.PERIODE_OK, null, false))
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerIkkeKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterEtterMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringssøknadMottattdato(mottattdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, mottattdato.plusWeeks(1),
                        mottattdato.plusWeeks(2), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, null, false))
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).doesNotContain(mottattdato);
    }

    @Test
    public void skal_knekke_på_bevegelige_helligdager() {
        LocalDate fødselsdato = LocalDate.of(2019, 5, 1);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        LocalDate tom = LocalDate.of(2019, 5, 25);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, fødselsdato, tom,
                                Utsettelseårsaktype.FERIE, PeriodeVurderingType.IKKE_VURDERT, null, false))
                        .build())
                .medDatoer(datoer(fødselsdato, førsteLovligeSøknadsperiode))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(LocalDate.of(2019, 5, 17), LocalDate.of(2019, 5, 18));
    }

    @Test
    public void skal_knekke_både_på_termindato_og_fødselsdato_ved_prematuruker() {
        LocalDate fødselsdato = LocalDate.of(2019, 7, 22);
        LocalDate termin = LocalDate.of(2019, 9, 23);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medFødsel(fødselsdato)
                        .medTermin(termin)
                        .build())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(termin, fødselsdato);
    }

    private List<LocalDate> standardKnekkpunktFødsel(LocalDate fødselsdato, LocalDate førsteLovligeSøknadsperiode) {
        return Arrays.asList(
                fødselsdato.minusWeeks(12), //tidligste mulige uttak
                førsteLovligeSøknadsperiode,//ifbm søknadsfrist
                fødselsdato.minusWeeks(3),  //foreldrepenger før fødsel
                fødselsdato,
                fødselsdato.plusWeeks(6),   //slutt på periode reservert mor
                fødselsdato.plusYears(3));  //siste mulige uttak for foreldrepenger
    }


    private Datoer datoer(LocalDate fødselsdato, LocalDate førsteLovligeSøknadsperiode) {
        return new Datoer.Builder()
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                .medFødsel(fødselsdato)
                .build();
    }

}


