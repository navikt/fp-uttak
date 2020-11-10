package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FellesperiodeOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    @Test
    public void fellesperiode_mor_etter_uke_7_etter_fødsel_uten_nok_dager_blir_innvilget_med_knekk_og_avslått_periode_på_resten() {
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(1000))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(1000))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(4 * 5));
        basicGrunnlagMor().medSøknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15).minusDays(1))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET,
                FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET,
                MØDREKVOTE);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                INNVILGET, FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato.plusWeeks(10),
                fødselsdato.plusWeeks(15).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    private Kontoer.Builder fellesperiodeKonto(int trekkdager) {
        return new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(trekkdager));
    }

    @Test
    public void fellesperiode_far_etter_uke_7_etter_fødsel_blir_manuell_behandling_pga_aktivitetskravet() {
        var kontoer = fellesperiodeKonto(4 * 5);
        basicGrunnlagFar().medSøknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15),
                FELLESPERIODE, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    @Test
    public void for_tidlig_fellesperiode_far_blir_knekt_og_må_behandles_manuelt() {
        var periode = oppgittPeriode(FELLESPERIODE, fødselsdato.minusWeeks(5), fødselsdato.plusWeeks(1), false, null,
                PeriodeVurderingType.PERIODE_OK);
        basicGrunnlagFar().medSøknad(søknad(Søknadstype.FØDSEL, periode));

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(3);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(5),
                fødselsdato.minusWeeks(3).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG,
                Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                FELLESPERIODE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1), Perioderesultattype.INNVILGET,
                FELLESPERIODE);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_etter_3_uker_før_fødsel_blir_innvilget_med_knekk_ved_3_uker_resten_blir_manuell_behandling() {
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(3 * 5))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(15 * 5))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(16 * 5));

        var grunnlag = basicGrunnlagMor().medSøknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FELLESPERIODE, fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(1).minusDays(1)),
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(1), fødselsdato.minusDays(1)),
                oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1),
                Perioderesultattype.INNVILGET, FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(3),
                fødselsdato.minusWeeks(1).minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.minusWeeks(1), fødselsdato.minusDays(1),
                Perioderesultattype.INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(3).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1),
                Perioderesultattype.INNVILGET, MØDREKVOTE);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_etter_fødsel_blir_knekt_ved_6_uker_og_må_behandles_manuelt() {
        var kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medTrekkdager(15).medType(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))
                .leggTilKonto(new Konto.Builder().medTrekkdager(50).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(13 * 5).medType(Stønadskontotype.FELLESPERIODE));
        basicGrunnlagMor().medSøknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10).minusDays(1))))
                .medKontoer(kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(3);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(3).minusDays(3), MØDREKVOTE,
                IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(3),
                fødselsdato.plusWeeks(6).minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                Perioderesultattype.INNVILGET, FELLESPERIODE);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_før_12_uker_blir_avslått_med_knekk_ved_12_uker_før_fødsel() {
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(1000))
                .leggTilKonto(new Konto.Builder().medType(Stønadskontotype.FELLESPERIODE).medTrekkdager(13 * 5));
        basicGrunnlagMor().medSøknad(
                søknad(Søknadstype.FØDSEL, oppgittPeriode(Stønadskontotype.FELLESPERIODE, fødselsdato.minusWeeks(13), fødselsdato)))
                .medKontoer(kontoer);

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(5);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(13), fødselsdato.minusWeeks(12).minusDays(1),
                Perioderesultattype.AVSLÅTT, Stønadskontotype.FELLESPERIODE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1),
                Perioderesultattype.INNVILGET, Stønadskontotype.FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato, fødselsdato, FELLESPERIODE, null,
                Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserAvslåttPeriode(resultater.get(4).getUttakPeriode(), fødselsdato.plusDays(1), fødselsdato.plusWeeks(6).minusDays(3),
                MØDREKVOTE, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
    }

    @Test
    public void innvilge_fellesperiode_14_uker_før_fødsel_men_ikke_12_uker_før_termin_ved_terminsøknad() {
        var termin = LocalDate.of(2020, 6, 10);
        var grunnlag = basicGrunnlagMor().medDatoer(new Datoer.Builder().medTermin(termin).medFødsel(termin.plusWeeks(2)))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.TERMIN)
                        .medOppgittePerioder(List.of(OppgittPeriode.forVanligPeriode(FELLESPERIODE, termin.minusWeeks(15),
                                termin.minusWeeks(3).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, null),
                                OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, termin.minusWeeks(3), termin.minusDays(1),
                                        null, false, PeriodeVurderingType.IKKE_VURDERT, null),
                                OppgittPeriode.forVanligPeriode(MØDREKVOTE, termin, termin.plusWeeks(4), null, false,
                                        PeriodeVurderingType.IKKE_VURDERT, null))));

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultater.get(0).getUttakPeriode().getFom()).isEqualTo(termin.minusWeeks(15));
        assertThat(resultater.get(0).getUttakPeriode().getTom()).isEqualTo(termin.minusWeeks(12).minusDays(1));
        assertThat(resultater.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultater.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(resultater.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    public void avslå_fellesperiode_mer_enn_12_uker_før_fødsel_ved_fødselsøknad() {
        var termin = LocalDate.of(2020, 6, 10);
        var grunnlag = basicGrunnlagMor().medDatoer(new Datoer.Builder().medTermin(termin).medFødsel(termin.plusWeeks(2)))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .medOppgittePerioder(List.of(OppgittPeriode.forVanligPeriode(FELLESPERIODE, termin.minusWeeks(12),
                                termin.minusWeeks(3).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, null),
                                OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, termin.minusWeeks(3), termin.minusDays(1),
                                        null, false, PeriodeVurderingType.IKKE_VURDERT, null),
                                OppgittPeriode.forVanligPeriode(MØDREKVOTE, termin, termin.plusWeeks(4), null, false,
                                        PeriodeVurderingType.IKKE_VURDERT, null))));

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(grunnlag);

        assertThat(resultater.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    private RegelGrunnlag.Builder basicGrunnlagMor() {
        return basicGrunnlag().medBehandling(morBehandling());
    }

    private RegelGrunnlag.Builder basicGrunnlagFar() {
        return basicGrunnlag().medBehandling(farBehandling());
    }

    private RegelGrunnlag.Builder basicGrunnlag() {
        return grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato)).medRettOgOmsorg(beggeRett());
    }
}
