package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.overføringsperiode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.ER_ALENEOMSORG_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.ER_BARE_SØKER_RETT_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.INNLEGGELSE_ANNEN_FORELDER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.SYKDOM_ANNEN_FORELDER_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.ALENEOMSORG;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.INNLEGGELSE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.SYKDOM_ELLER_SKADE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import org.junit.jupiter.api.Test;

class OverføringDelregelTest {

    @Test
    void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.FEDREKVOTE, fom, tom, SYKDOM_ELLER_SKADE, SYKDOM_ANNEN_FORELDER_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.FEDREKVOTE, fom, tom, INNLEGGELSE, INNLEGGELSE_ANNEN_FORELDER_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode =
                overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, ALENEOMSORG, ER_ALENEOMSORG_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.FEDREKVOTE, fom, tom, ANNEN_FORELDER_IKKE_RETT, ER_BARE_SØKER_RETT_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.FEDREKVOTE, fom, tom, SYKDOM_ELLER_SKADE, SYKDOM_ANNEN_FORELDER_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.FEDREKVOTE, fom, tom, INNLEGGELSE, INNLEGGELSE_ANNEN_FORELDER_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode =
                overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, ALENEOMSORG, ER_ALENEOMSORG_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(8);
        var tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.FEDREKVOTE, fom, tom, ANNEN_FORELDER_IKKE_RETT, ER_BARE_SØKER_RETT_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    void UT1172_far_overføring_sykdom_skade_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.MØDREKVOTE, fom, tom, SYKDOM_ELLER_SKADE, SYKDOM_ANNEN_FORELDER_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    void UT1173_far_overføring_innleggelse_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.MØDREKVOTE, fom, tom, INNLEGGELSE, INNLEGGELSE_ANNEN_FORELDER_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    void UT1174_far_overføring_aleneomsorg_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode =
                overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, ALENEOMSORG, ER_ALENEOMSORG_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    void UT1175_far_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(
                Stønadskontotype.MØDREKVOTE,
                fom,
                tom,
                ANNEN_FORELDER_IKKE_RETT,
                DokumentasjonVurdering.ER_BARE_SØKER_RETT_GODKJENT);
        var kontoer = new Kontoer.Builder()
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .søknad(fødselssøknadMedEnPeriode(søknadsperiode))
                .kontoer(kontoer)
                .build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    private Søknad.Builder fødselssøknadMedEnPeriode(OppgittPeriode oppgittPeriode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return RegelGrunnlagTestBuilder.create()
                .inngangsvilkår(new Inngangsvilkår.Builder())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder()
                        .samtykke(true)
                        .morHarRett(true)
                        .farHarRett(true))
                .inngangsvilkår(new Inngangsvilkår.Builder()
                        .adopsjonOppfylt(true)
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
