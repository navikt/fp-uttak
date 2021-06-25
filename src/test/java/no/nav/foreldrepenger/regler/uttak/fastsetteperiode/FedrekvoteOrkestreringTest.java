package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class FedrekvoteOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {


    @Test
    void fedrekvote_med_tidlig_oppstart_og_gyldig_grunn_fra_første_dag_til_midten_av_perioden_blir_innvilget_med_knekkpunkt() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), PeriodeVurderingType.PERIODE_OK),
                        oppgittPeriode(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2), PeriodeVurderingType.UAVKLART_PERIODE)));

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(2);

        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1).minusDays(1), INNVILGET,
                FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(2), MANUELL_BEHANDLING,
                FEDREKVOTE);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return OppgittPeriode.forVanligPeriode(FEDREKVOTE, fom, tom, null, false, vurderingType, null, null, null);
    }

    @Test
    void skal_gi_ikke_innvilget_når_far_har_gyldig_grunn_til_tidlig_oppstart_men_ikke_omsorg() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(100));
        grunnlag.datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                                .periodeUtenOmsorg(new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))))
                .kontoer(kontoer);

        var periodeResultater = fastsettPerioder(grunnlag);

        assertThat(periodeResultater).hasSize(1);
        var perioder = periodeResultater.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .sorted(comparing(UttakPeriode::getFom))
                .collect(toList());

        verifiserAvslåttPeriode(perioder.get(0), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), FEDREKVOTE,
                IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    void fedrekvote_fra_1_dag_før_6_uker_skal_behandles_manuelt() {
        var fødselsdato = LocalDate.of(2018, 1, 3);

        var grunnlag = this.grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(fødselsdato.plusWeeks(6).minusDays(1), fødselsdato.plusWeeks(10).minusDays(1),
                                PeriodeVurderingType.UAVKLART_PERIODE)))
                .build();

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(2);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6).minusDays(1),
                fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING, FEDREKVOTE);
        //Denne går til manuell pga uavklart periode
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                MANUELL_BEHANDLING, FEDREKVOTE);
    }

    @Test
    void fedrekvote_før_6_uker_blir_avslått() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var periode = oppgittPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.UAVKLART_PERIODE);
        grunnlag.datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(søknad(Søknadstype.FØDSEL, periode));

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(2);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MANUELL_BEHANDLING,
                FEDREKVOTE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                MANUELL_BEHANDLING, FEDREKVOTE);
    }

    @Test
    void fedrekvote_bli_ikke_innvilget_når_søker_ikke_har_omsorg() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(100));
        grunnlag.datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6),
                                fødselsdato.plusWeeks(10).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(100)))))
                .kontoer(kontoer);

        var resultater = fastsettPerioder(grunnlag);

        assertThat(resultater).hasSize(1);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1),
                FEDREKVOTE, IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    void overføring_av_fedrekvote_grunnet_sykdom_skade_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1)))
                        .oppgittPeriode(
                                oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                        .oppgittPeriode(overføringPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE,
                                PeriodeVurderingType.PERIODE_OK))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(perioder.get(0).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(1).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(2).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        //assertThat(perioder.get(3).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    void overføring_av_fedrekvote_grunnet_sykdom_skade_skal_gå_til_manuell_behandling_hvis_ikke_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1)),
                        oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)),
                        overføringPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE,
                                PeriodeVurderingType.PERIODE_OK)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(perioder.get(0).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(1).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        //assertThat(perioder.get(2).getUttakPeriode().trekkdager(ARBEIDSFORHOLD)).isEqualTo(20);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void overføring_av_fedrekvote_ugyldig_årsak_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusDays(1)),
                        oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)),
                        overføringPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1), null, PeriodeVurderingType.UAVKLART_PERIODE)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);

        //3 uker foreldrepenger før fødsel innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote skal til manuell behandling
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    private OppgittPeriode overføringPeriode(Stønadskontotype stønadskontotype,
                                             LocalDate fom,
                                             LocalDate tom,
                                             OverføringÅrsak årsak,
                                             PeriodeVurderingType vurderingType) {
        return OppgittPeriode.forOverføring(stønadskontotype, fom, tom, vurderingType, årsak, null, null);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().fødsel(fødselsdato);
    }
}
