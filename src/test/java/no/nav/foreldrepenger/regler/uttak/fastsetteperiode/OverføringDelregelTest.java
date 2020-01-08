package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
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
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    public void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_før_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    public void UT1172_mor_overføring_sykdom_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1173_mor_overføring_innleggelse_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1174_mor_overføring_aleneomsorg_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    public void UT1175_mor_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn_etter_7uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(8);
        LocalDate tom = fødselsdato.plusWeeks(9);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.FEDREKVOTE, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }

    @Test
    public void UT1172_far_overføring_sykdom_skade_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.MØDREKVOTE, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);
    }

    @Test
    public void UT1173_far_overføring_innleggelse_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.MØDREKVOTE, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);
    }

    @Test
    public void UT1174_far_overføring_aleneomsorg_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.MØDREKVOTE, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ALENEOMSORG);
    }

    @Test
    public void UT1175_far_overføring_annen_forelder_ikke_rett_avklart_gyldig_grunn() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, Stønadskontotype.MØDREKVOTE, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT);
    }


    private StønadsPeriode overføringsperiode(LocalDate fom, LocalDate tom, Stønadskontotype stønadskontotype, OverføringÅrsak årsak, PeriodeVurderingType vurderingType) {
        return StønadsPeriode.medOverføringAvKvote(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, årsak,
                vurderingType, null, false);
    }

    private Søknad.Builder fødselssøknadMedEnPeriode(StønadsPeriode søknadsperiode) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .leggTilSøknadsperiode(søknadsperiode);
    }

    private Søknad.Builder søknad(StønadsPeriode søknadsperiode, GyldigGrunnPeriode gyldigGrunnPeriode) {
        return fødselssøknadMedEnPeriode(søknadsperiode)
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggGyldigGrunnPerioder(gyldigGrunnPeriode));
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

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

}
