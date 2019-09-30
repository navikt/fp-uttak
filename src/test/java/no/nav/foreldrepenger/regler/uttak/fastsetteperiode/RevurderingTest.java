package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class RevurderingTest {

    private static final LocalDate FØRSTE_LOVLIGE_UTTAKSDAG = LocalDate.of(2018, 5, 5);
    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);
    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void revurderingSøknadUtenSamtykkeOgOverlappendePerioderSkalFørTilAvslagPgaSamtykke() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(false))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, true, false)))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_SAMTYKKE);
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarInnvilgetUtsettelseSkalAvslås() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, true, false)))
                .medBehandling(revurderingBerørtSakBehandling())
                .build();

        Regelresultat regelresultat = evaluerBerørtSaken(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE);
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslås() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false, false)))
                .medBehandling(revurderingBerørtSakBehandling())
                .build();

        Regelresultat regelresultat = evaluerBerørtSaken(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslåsOgKnekkes() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false, false)))
                .medBehandling(revurderingBerørtSakBehandling())
                .build();

        Regelresultat regelresultat = evaluerBerørtSaken(uttakPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakSkalAvslås() {
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicBuilder(uttakPeriode)
                .medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false, true)))
                .medBehandling(revurderingBerørtSakBehandling())
                .build();

        Regelresultat regelresultat = evaluerBerørtSaken(uttakPeriode, grunnlag);

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurdering_søknad_der_opphørsdato_ligger_i_perioden() {
        UttakPeriode uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.TIDLIGERE_VEDTAK,
                FAMILIEHENDELSE_DATO.plusWeeks(10), FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK);

        RegelGrunnlag grunnlag = basicBuilder(uttaksperiode)
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getFom().plusWeeks(1)).build())
                .build();

        Regelresultat regelresultat = evaluer(uttaksperiode, grunnlag);

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
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getFom().minusWeeks(1)).build())
                .build();

        Regelresultat regelresultat = evaluer(uttaksperiode, grunnlag);

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
                .medRevurdering(new Revurdering.Builder().build())
                .medRettOgOmsorg(samtykke(true))
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getTom().plusWeeks(1)).build())
                .build();

        Regelresultat regelresultat = evaluer(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
    }

    private Behandling revurderingBerørtSakBehandling() {
        return new Behandling.Builder()
                .medType(Behandlingtype.REVURDERING_BERØRT_SAK)
                .build();
    }

    private AnnenPart annenPart(AnnenpartUttaksperiode periode) {
        return new AnnenPart.Builder()
                .leggTilUttaksperiode(periode)
                .build();
    }

    private RettOgOmsorg samtykke(boolean samtykke) {
        return new RettOgOmsorg.Builder()
                .medSamtykke(samtykke)
                .build();
    }

    private Regelresultat evaluer(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode)));
    }

    private Regelresultat evaluerBerørtSaken(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.forBerørtSak(grunnlag), uttakPeriode)));
    }

    private AnnenpartUttaksperiode lagPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom,
                                              BigDecimal utbetalingsgrad, boolean innvilgetUtsettelse, boolean samtidigUttak) {
        return new AnnenpartUttaksperiode.Builder(fom, tom)
                .medSamtidigUttak(samtidigUttak)
                .medInnvilgetUtsettelse(innvilgetUtsettelse)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forArbeid("000000003", null),
                        stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), utbetalingsgrad))
                .build();
    }

    private RegelGrunnlag.Builder basicBuilder(UttakPeriode uttakPeriode) {
        return RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medType(Behandlingtype.REVURDERING)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .build())
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.MØDREKVOTE)
                                .medTrekkdager(50)
                                .build())
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.FELLESPERIODE)
                                .medTrekkdager(13 * 5)
                                .build())
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(FØRSTE_LOVLIGE_UTTAKSDAG)
                        .medFødsel(FAMILIEHENDELSE_DATO)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());
    }

    private UttakPeriode uttakPeriode(Stønadskontotype stønadskontotype, PeriodeKilde kilde, LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        StønadsPeriode stønadsPeriode = new StønadsPeriode(stønadskontotype, kilde, fom, tom, null, false);
        stønadsPeriode.setPeriodeVurderingType(vurderingType);
        return stønadsPeriode;
    }
}
