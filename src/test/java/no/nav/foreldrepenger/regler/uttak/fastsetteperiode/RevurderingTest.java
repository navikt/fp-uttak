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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

class RevurderingTest {

    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    void revurderingSøknadUtenSamtykkeOgOverlappendePerioderSkalFørTilAvslagPgaSamtykke() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).rettOgOmsorg(samtykke(false))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, false).build()))
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
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true).farHarRett(true).samtykke(true))
                .annenPart(annenPart(AnnenpartUttakPeriode.Builder.utsettelse(FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12)).innvilget(true).build()))
                .behandling(berørtBehandling().søkerErMor(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarInnvilgetUtsettelseSkalAvslåsHvisBehandlingKreverSammenhengendeUttak() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode)
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true).farHarRett(true).samtykke(true))
                .annenPart(annenPart(AnnenpartUttakPeriode.Builder.utsettelse(FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12)).innvilget(true).build()))
                .behandling(berørtBehandling().søkerErMor(true).kreverSammenhengendeUttak(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslås() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).rettOgOmsorg(samtykke(true))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, false).build()))
                .behandling(berørtBehandling())
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslåsOgKnekkes() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).rettOgOmsorg(samtykke(true))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, false).build()))
                .behandling(berørtBehandling())
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakSkalManueltVurderes() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode).rettOgOmsorg(samtykke(true))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.HUNDRED.subtract(Utbetalingsgrad.TEN), true).build()))
                .behandling(berørtBehandling())
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakSkalAutomatiskReduseres() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
            FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode)
            .rettOgOmsorg(samtykke(true).morHarRett(true).farHarRett(true))
            .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), new Utbetalingsgrad(60), true).build()))
            .behandling(berørtBehandling().søkerErMor(true))
            .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakFlerbarnsdagerSkalFlyteVidere() {
        var oppgittPeriode = uttakPeriodeFlerbarnsdager(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));
        var grunnlag = basicBuilder(oppgittPeriode, true, true)
                .rettOgOmsorg(samtykke(true).morHarRett(true).farHarRett(true))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                        FAMILIEHENDELSE_DATO.plusWeeks(12), Utbetalingsgrad.TEN, true).build()))
                .behandling(berørtBehandling().søkerErMor(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakFlerbarnsdagerSkalFlyteVidereMKAP() {
        var oppgittPeriode = uttakPeriodeFlerbarnsdager(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO,
                FAMILIEHENDELSE_DATO.plusWeeks(10));
        var grunnlag = basicBuilder(oppgittPeriode, false,true)
                .rettOgOmsorg(samtykke(true).morHarRett(true).farHarRett(true))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.MØDREKVOTE, FAMILIEHENDELSE_DATO,
                        FAMILIEHENDELSE_DATO.plusWeeks(10), Utbetalingsgrad.TEN, true).build()))
                .behandling(berørtBehandling().søkerErMor(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER);
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }


    @Test
    void berørtBehandlingHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakFlerbarnsdagerSkalFlyteVidereMK() {
        var oppgittPeriode = uttakPeriode(Stønadskontotype.MØDREKVOTE, FAMILIEHENDELSE_DATO,
                FAMILIEHENDELSE_DATO.plusWeeks(6).minusDays(1));
        var grunnlag = basicBuilder(oppgittPeriode, true,true)
                .rettOgOmsorg(samtykke(true).morHarRett(true).farHarRett(true))
                .annenPart(annenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO,
                        FAMILIEHENDELSE_DATO.plusWeeks(10).minusDays(1), Utbetalingsgrad.TEN, true).flerbarnsdager(true).build()))
                .behandling(berørtBehandling().søkerErMor(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void revurdering_søknad_der_opphørsdato_ligger_i_perioden() {
        var uttaksperiode = uttakPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12));

        var grunnlag = basicBuilder(uttaksperiode).medlemskap(
                new Medlemskap.Builder().opphørsdato(uttaksperiode.getFom().plusWeeks(1))).build();

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

        var grunnlag = basicBuilder(uttaksperiode).medlemskap(
                new Medlemskap.Builder().opphørsdato(uttaksperiode.getFom().minusWeeks(1))).build();

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

        var grunnlag = basicBuilder(uttaksperiode).revurdering(new Revurdering.Builder())
                .rettOgOmsorg(samtykke(true))
                .medlemskap(new Medlemskap.Builder().opphørsdato(uttaksperiode.getTom().plusWeeks(1)))
                .build();

        var regelresultat = kjørRegel(uttaksperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM);
    }

    private Behandling.Builder berørtBehandling() {
        return new Behandling.Builder().berørtBehandling(true);
    }

    private AnnenPart.Builder annenPart(AnnenpartUttakPeriode periode) {
        return new AnnenPart.Builder().uttaksperiode(periode);
    }

    private RettOgOmsorg.Builder samtykke(boolean samtykke) {
        return new RettOgOmsorg.Builder().samtykke(samtykke);
    }

    private AnnenpartUttakPeriode.Builder lagPeriode(Stønadskontotype stønadskontotype,
                                             LocalDate fom,
                                             LocalDate tom,
                                             Utbetalingsgrad utbetalingsgrad,
                                             boolean samtidigUttak) {
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom)
                .samtidigUttak(samtidigUttak)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forArbeid(new Orgnummer("000000003"), null),
                                stønadskontotype, new Trekkdager(Virkedager.beregnAntallVirkedager(fom, tom)), utbetalingsgrad))
                ;
    }

    private RegelGrunnlag.Builder basicBuilder(OppgittPeriode oppgittPeriode) {
        return basicBuilder(oppgittPeriode, true, false);
    }

    private RegelGrunnlag.Builder basicBuilder(OppgittPeriode oppgittPeriode, boolean erMor, boolean flerbarnskonto) {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(50))
                .konto(new Konto.Builder().type(Stønadskontotype.FELLESPERIODE).trekkdager(13 * 5));
        if (flerbarnskonto) {
            kontoer.flerbarnsdager(5*13);
        }
        return RegelGrunnlagTestBuilder.create()
                .behandling(new Behandling.Builder().søkerErMor(erMor))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(new Datoer.Builder().fødsel(FAMILIEHENDELSE_DATO))
                .inngangsvilkår(new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                        .foreldreansvarnOppfylt(true)
                        .fødselOppfylt(true)
                        .opptjeningOppfylt(true));
    }

    private OppgittPeriode uttakPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, null, null,
                null, null);
    }

    private OppgittPeriode uttakPeriodeFlerbarnsdager(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, true, null, null,
                null, null);
    }
}
