package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Medlemskap;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;

public class RevurderingTest {

    private static final LocalDate FØRSTE_LOVLIGE_UTTAKSDAG = LocalDate.of(2018, 5, 5);
    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    public void revurderingSøknadUtenSamtykkeOgOverlappendePerioderSkalFørTilAvslagPgaSamtykke() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(false))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false)))
                .build();

        Regelresultat regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_SAMTYKKE);
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    public void revurderingsøknadAvTapendeBehandlingHvorDenAndrePartenHarInnvilgetUtsettelseSkalAvslås() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(AnnenpartUttaksperiode.Builder.utsettelse(FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12)).medInnvilget(true).build()))
                .medBehandling(tapendeBehandling())
                .build();

        Regelresultat regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE);
    }

    @Test
    public void revurderingsøknadAvTapendeBehandlingHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslås() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false)))
                .medBehandling(tapendeBehandling())
                .build();

        Regelresultat regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurderingsøknadAvTapendeBehandlingHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslåsOgKnekkes() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false)))
                .medBehandling(tapendeBehandling())
                .build();

        Regelresultat regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurderingsøknadAvTapendeBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakSkalAvslås() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, true)))
                .medBehandling(tapendeBehandling())
                .build();

        Regelresultat regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurdering_søknad_der_opphørsdato_ligger_i_perioden() {
        UttakPeriode uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.TIDLIGERE_VEDTAK,
                FAMILIEHENDELSE_DATO.plusWeeks(10), FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);

        RegelGrunnlag grunnlag = basicBuilder(uttaksperiode)
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getFom().plusWeeks(1)))
                .build();

        Regelresultat regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    public void revurdering_søknad_der_opphørsdato_ligger_før_perioden() {
        UttakPeriode uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.TIDLIGERE_VEDTAK,
                FAMILIEHENDELSE_DATO.plusWeeks(10), FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);

        RegelGrunnlag grunnlag = basicBuilder(uttaksperiode)
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getFom().minusWeeks(1)))
                .build();

        Regelresultat regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    public void revurdering_søknad_der_opphørsdato_ligger_etter_perioden() {
        UttakPeriode uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.TIDLIGERE_VEDTAK,
                FAMILIEHENDELSE_DATO.plusWeeks(10), FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);

        RegelGrunnlag grunnlag = basicBuilder(uttaksperiode)
                .medRevurdering(new Revurdering.Builder())
                .medRettOgOmsorg(samtykke(true))
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getTom().plusWeeks(1)))
                .build();

        Regelresultat regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
    }

    private Behandling.Builder tapendeBehandling() {
        return new Behandling.Builder().medErTapende(true);
    }

    private AnnenPart.Builder annenPart(AnnenpartUttaksperiode periode) {
        return new AnnenPart.Builder().leggTilUttaksperiode(periode);
    }

    private RettOgOmsorg.Builder samtykke(boolean samtykke) {
        return new RettOgOmsorg.Builder().medSamtykke(samtykke);
    }

    private AnnenpartUttaksperiode lagPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom,
                                              BigDecimal utbetalingsgrad, boolean samtidigUttak) {
        return AnnenpartUttaksperiode.Builder.uttak(fom, tom)
                .medSamtidigUttak(samtidigUttak)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forArbeid("000000003", null),
                        stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), utbetalingsgrad))
                .build();
    }

    private RegelGrunnlag.Builder basicBuilder(UttakPeriode uttakPeriode) {
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .medTrekkdager(50))
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FELLESPERIODE)
                        .medTrekkdager(13 * 5));
        return RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(FØRSTE_LOVLIGE_UTTAKSDAG)
                        .medFødsel(FAMILIEHENDELSE_DATO))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private UttakPeriode uttakPeriode(Stønadskontotype stønadskontotype, PeriodeKilde kilde, LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        StønadsPeriode stønadsPeriode = new StønadsPeriode(stønadskontotype, kilde, fom, tom, null, false);
        stønadsPeriode.setPeriodeVurderingType(vurderingType);
        return stønadsPeriode;
    }
}
