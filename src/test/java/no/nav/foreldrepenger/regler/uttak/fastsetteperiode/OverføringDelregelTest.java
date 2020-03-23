package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.overføringsperiode;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class OverføringDelregelTest {

    @Test
    public void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    public void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    public void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    public void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        var søknadsperiode = overføringsperiode(Stønadskontotype.FEDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    public void UT1172_far_overføring_sykdom_skade_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1173_far_overføring_innleggelse_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1174_far_overføring_aleneomsorg_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    public void UT1175_far_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var søknadsperiode = overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medKontoer(kontoer)
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    private Søknad.Builder fødselssøknadMedEnPeriode(OppgittPeriode oppgittPeriode) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode);
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode, GyldigGrunnPeriode gyldigGrunnPeriode) {
        return fødselssøknadMedEnPeriode(oppgittPeriode)
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggGyldigGrunnPeriode(gyldigGrunnPeriode));
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return RegelGrunnlagTestBuilder.create()
                .medInngangsvilkår(new Inngangsvilkår.Builder())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(fødselsdato.withDayOfMonth(1).minusMonths(3))
                        .medFødsel(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medMorHarRett(true)
                        .medFarHarRett(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(false));
    }

    private RegelGrunnlag.Builder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(true));
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(innvilgetÅrsak);
    }

}
