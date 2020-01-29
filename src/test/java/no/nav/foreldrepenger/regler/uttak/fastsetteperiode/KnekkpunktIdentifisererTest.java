package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Medlemskap;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class KnekkpunktIdentifisererTest {

    @Test
    public void skal_finne_knekkpunkter_for_søknad_ved_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL))
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
                        .medType(Søknadstype.FØDSEL))
                .medDatoer(datoer(fødselsdato, førsteLovligeSøknadsperiode))
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(opphørsdato))
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
                        .medType(Søknadstype.ADOPSJON))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medOmsorgsovertakelse(adopsjonsdato))
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
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(adopsjonsdato.plusDays(100), adopsjonsdato.plusDays(300)))))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medOmsorgsovertakelse(adopsjonsdato))
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
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(adopsjonsdato.plusDays(300), adopsjonsdato.plusDays(400)))))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medOmsorgsovertakelse(adopsjonsdato))
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
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart1() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 6, 1);
        LocalDate fødselsdato = uttakStartdato.minusMonths(1);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        LocalDate knekkdato = uttakStartdato.plusDays(1);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(uttakStartdato, uttakStartdato.plusDays(10)).build()))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, knekkdato, uttakStartdato.plusDays(10), PeriodeKilde.SØKNAD,
                                BigDecimal.TEN, true, PeriodeVurderingType.IKKE_VURDERT)))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medFødsel(fødselsdato))
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
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(uttakStartdato.plusDays(1), uttakStartdato.plusDays(10)).build()))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, uttakStartdato, knekkdato, PeriodeKilde.SØKNAD,
                                BigDecimal.TEN, true, PeriodeVurderingType.IKKE_VURDERT)))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode))
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
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(uttakStartdato.plusDays(1), uttakStartdato.plusDays(5)).build()))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, uttakStartdato, uttakStartdato.plusDays(6),
                                PeriodeKilde.SØKNAD, BigDecimal.TEN, true, PeriodeVurderingType.IKKE_VURDERT)))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medFødsel(fødselsdato))
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
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(annenPartPeriodeFom, annenPartPeriodeTom).build()))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forVanligPeriode(Stønadskontotype.FELLESPERIODE, stønadsperiodeFom, stønadsperiodeTom, PeriodeKilde.SØKNAD,
                                BigDecimal.TEN, true, PeriodeVurderingType.IKKE_VURDERT)))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(stønadsperiodeFom, stønadsperiodeTom.plusDays(1), annenPartPeriodeFom, annenPartPeriodeTom.plusDays(1));
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisGraderingStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        var gradertStønadsperiode = OppgittPeriode.forGradering(Stønadskontotype.FELLESPERIODE, mottattdato.minusMonths(1),
                mottattdato.minusWeeks(2), PeriodeKilde.SØKNAD,
                BigDecimal.valueOf(30), null, false, Set.of(gradertArbeidsforhold), PeriodeVurderingType.IKKE_VURDERT);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5)))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2018, 5, 5)))
                .medBehandling(new Behandling.Builder())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(gradertStønadsperiode)
                        .medMottattDato(mottattdato))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerKnekkPåFørstegangssøknadMottattdatoHvisGraderingStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        var gradertStønadsperiode = OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, mottattdato.minusMonths(1),
                mottattdato.minusWeeks(2), PeriodeKilde.SØKNAD,
                BigDecimal.valueOf(30), null, false, Set.of(gradertArbeidsforhold), PeriodeVurderingType.IKKE_VURDERT);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(LocalDate.of(2018, 5, 5))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5)))
                .medBehandling(new Behandling.Builder())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(gradertStønadsperiode)
                        .medMottattDato(mottattdato))
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
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5)))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2018, 5, 5)))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, mottattdato, mottattdato.plusWeeks(2),
                                PeriodeKilde.SØKNAD, BigDecimal.valueOf(30), null, false, Set.of(gradertArbeidsforhold), PeriodeVurderingType.IKKE_VURDERT))
                        .medMottattDato(mottattdato))
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
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5)))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2018, 5, 5)))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, mottattdato.plusWeeks(1),
                                mottattdato.plusWeeks(2), PeriodeKilde.SØKNAD,
                                BigDecimal.valueOf(30), null, false, Set.of(gradertArbeidsforhold), PeriodeVurderingType.IKKE_VURDERT))
                        .medMottattDato(mottattdato))
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
                        .medFødsel(LocalDate.of(2018, 5, 5)))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2018, 5, 5)))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forUtsettelse(mottattdato.minusWeeks(2),
                                mottattdato.minusWeeks(1), PeriodeKilde.SØKNAD, PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.FERIE))
                        .medMottattDato(mottattdato))
                .medBehandling(new Behandling.Builder())
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerKnekkPåFørstegangssøknadMottattdatoHvisUtsettelseFerieArbeidStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                        .medFødsel(LocalDate.of(2018, 5, 5)))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forUtsettelse(mottattdato.minusWeeks(2),
                                mottattdato.minusWeeks(1), PeriodeKilde.SØKNAD, PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.FERIE))
                        .medMottattDato(mottattdato))
                .medBehandling(new Behandling.Builder())
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
                        .medFødsel(LocalDate.of(2018, 5, 5)))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2018, 5, 5)))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forUtsettelse(mottattdato, mottattdato.plusWeeks(2),
                                PeriodeKilde.SØKNAD, PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.ARBEID))
                        .medMottattDato(mottattdato))
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
                        .medFødsel(LocalDate.of(2018, 5, 5)))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2018, 5, 5)))
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forUtsettelse(mottattdato.plusWeeks(1), mottattdato.plusWeeks(2),
                                PeriodeKilde.SØKNAD, PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.FERIE))
                        .medMottattDato(mottattdato))
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
                        .medMottattDato(fødselsdato.minusWeeks(4))
                        .leggTilOppgittPeriode(OppgittPeriode.forUtsettelse(fødselsdato, tom, PeriodeKilde.SØKNAD,
                                PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.FERIE)))
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
                        .medType(Søknadstype.FØDSEL))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                        .medFødsel(fødselsdato)
                        .medTermin(termin))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(termin, fødselsdato);
    }

    @Test
    public void skal_knekke_på_startdato_hos_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 9, 23);
        var startdato1 = fødselsdato.plusWeeks(8);
        var startdato2 = fødselsdato.plusWeeks(10);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .medDatoer(datoer(fødselsdato, fødselsdato.minusYears(1)))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans(), startdato1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), startdato2)))
                .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(startdato1, startdato2);
    }

    @Test
    public void skal_ikke_knekke_på_startdato_hos_arbeidsforhold_hvis_bare_ett_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 9, 23);
        var startdato = fødselsdato.plusWeeks(8);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .medDatoer(datoer(fødselsdato, fødselsdato.minusYears(1)))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans(), startdato)))
                .build();

        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).doesNotContain(startdato);
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


    private Datoer.Builder datoer(LocalDate fødselsdato, LocalDate førsteLovligeSøknadsperiode) {
        return new Datoer.Builder()
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                .medFødsel(fødselsdato);
    }

}


