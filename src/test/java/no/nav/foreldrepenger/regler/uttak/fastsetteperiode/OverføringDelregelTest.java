package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.overføringsperiode;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;

class OverføringDelregelTest {

    @Test
    void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    void UT1172_far_overføring_sykdom_skade_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    void UT1173_far_overføring_innleggelse_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    void UT1174_far_overføring_aleneomsorg_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    void UT1175_far_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    private Søknad.Builder fødselssøknadMedEnPeriode(OppgittPeriode oppgittPeriode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode, GyldigGrunnPeriode gyldigGrunnPeriode) {
        return fødselssøknadMedEnPeriode(oppgittPeriode).dokumentasjon(
                new Dokumentasjon.Builder().gyldigGrunnPeriode(gyldigGrunnPeriode));
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return RegelGrunnlagTestBuilder.create()
                .inngangsvilkår(new Inngangsvilkår.Builder())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true))
                .inngangsvilkår(new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                        .foreldreansvarnOppfylt(true)
                        .fødselOppfylt(true)
                        .opptjeningOppfylt(true));
    }

    private RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato).behandling(new Behandling.Builder().søkerErMor(false));
    }

    private RegelGrunnlag.Builder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato).behandling(new Behandling.Builder().søkerErMor(true));
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(innvilgetÅrsak);
    }

}
