package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Medlemskap;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class RevurderingTest {

    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    void revurderingSøknadUtenSamtykkeOgOverlappendePerioderSkalFørTilAvslagPgaSamtykke() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).medRettOgOmsorg(samtykke(false))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, false)))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_SAMTYKKE);
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarInnvilgetUtsettelseSkalInnvilges() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode)
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true).medFarHarRett(true).medSamtykke(true))
                .medAnnenPart(annenPart(AnnenpartUttakPeriode.Builder.utsettelse(FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12)).medInnvilget(true).build()))
                .medBehandling(berørtBehandling().medSøkerErMor(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarInnvilgetUtsettelseSkalAvslåsHvisBehandlingKreverSammenhengendeUttak() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode)
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true).medFarHarRett(true).medSamtykke(true))
                .medAnnenPart(annenPart(AnnenpartUttakPeriode.Builder.utsettelse(FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12)).medInnvilget(true).build()))
                .medBehandling(berørtBehandling().medSøkerErMor(true).medKreverSammenhengendeUttak(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslås() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, false)))
                .medBehandling(berørtBehandling())
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslåsOgKnekkes() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, false)))
                .medBehandling(berørtBehandling())
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakSkalAvslås() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).medRettOgOmsorg(samtykke(true))
                .medAnnenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, true)))
                .medBehandling(berørtBehandling())
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    @Test
    void revurdering_søknad_der_opphørsdato_ligger_i_perioden() {
        var uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));

        var grunnlag = basicBuilder(uttaksperiode).medMedlemskap(
                new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getFom().plusWeeks(1))).build();

        var regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void revurdering_søknad_der_opphørsdato_ligger_før_perioden() {
        var uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));

        var grunnlag = basicBuilder(uttaksperiode).medMedlemskap(
                new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getFom().minusWeeks(1))).build();

        var regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void revurdering_søknad_der_opphørsdato_ligger_etter_perioden() {
        var uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));

        var grunnlag = basicBuilder(uttaksperiode).medRevurdering(new Revurdering.Builder())
                .medRettOgOmsorg(samtykke(true))
                .medMedlemskap(new Medlemskap.Builder().medOpphørsdato(uttaksperiode.getTom().plusWeeks(1)))
                .build();

        var regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
    }

    private Behandling.Builder berørtBehandling() {
        return new Behandling.Builder().medErBerørtBehandling(true);
    }

    private AnnenPart.Builder annenPart(AnnenpartUttakPeriode periode) {
        return new AnnenPart.Builder().leggTilUttaksperiode(periode);
    }

    private RettOgOmsorg.Builder samtykke(boolean samtykke) {
        return new RettOgOmsorg.Builder().medSamtykke(samtykke);
    }

    private AnnenpartUttakPeriode lagPeriode(Stønadskontotype stønadskontotype,
                                             LocalDate fom,
                                             LocalDate tom,
                                             Utbetalingsgrad utbetalingsgrad,
                                             boolean samtidigUttak) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .medSamtidigUttak(samtidigUttak)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forArbeid(new Orgnummer("000000003"), null),
                                stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), utbetalingsgrad))
                .build();
    }

    private RegelGrunnlag.Builder basicBuilder(OppgittPeriode oppgittPeriode) {
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(Stønadskontotype.FELLESPERIODE).medTrekkdager(13 * 5));
        return RegelGrunnlagTestBuilder.create()
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(oppgittPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medFødsel(FAMILIEHENDELSE_DATO))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private OppgittPeriode uttakPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, PeriodeVurderingType.IKKE_VURDERT, null, null,
                null);
    }
}
