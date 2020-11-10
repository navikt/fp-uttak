package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class MødrekvoteOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void mødrekvoteperiode_før_familiehendelse() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusWeeks(1).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.minusWeeks(1),
                                fødselsdato.plusWeeks(6).minusDays(1))));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(1).minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(1));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
    }

    @Test
    public void overføring_av_mødrekvote_grunnet_sykdom_skade_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null))
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getOverføringÅrsak()).isEqualTo(OverføringÅrsak.SYKDOM_ELLER_SKADE);

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getOverføringÅrsak()).isEqualTo(OverføringÅrsak.SYKDOM_ELLER_SKADE);

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void overføring_av_mødrekvote_grunnet_sykdom_skade_skal_gå_til_manuell_behandling_hvis_ikke_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null))
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1))));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
    }

    @Test
    public void overføring_av_mødrekvote_ugyldig_årsak_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.UAVKLART_PERIODE,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null))
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1))));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote skal til manuell behandling
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote skal til manuell behandling
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote skal til manuell behandling
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void overføring_av_mødrekvote_grunnet_sykdom_skade_men_far_har_ikke_omsorg_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK,
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null))
                        .leggTilOppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(
                                new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1006
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1006
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote avslått
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET); // UT1031
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }
}
