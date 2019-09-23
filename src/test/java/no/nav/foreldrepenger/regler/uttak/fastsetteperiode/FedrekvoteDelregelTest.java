package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class FedrekvoteDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void fedrekvote_etter_6_uker_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(10 * 5))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void UT1020_fedrekvote_før_fødsel_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(1));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(10 * 5))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1292_fedrekvote_uten_at_mor_har_rett_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        kontoer.put(ARBEIDSFORHOLD_1, new Kontoer.Builder().leggTilKonto(konto(FEDREKVOTE, 10 * 5)).build());

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9).minusDays(1));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(false)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medSøknad(søknad(søknadsperiode))
                .medKontoer(kontoer)
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_IKKE_RETT_FK);
    }

    private Konto konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager)
                .build();
    }

    @Test
    public void fedrekvote_bli_avslått_når_søker_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(100))))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(10 * 5))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void UT1025_far_førUke7_etterTermin_utenGyldigGrunn_ikkeOmsorg_flerbarnsdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4), null, true);
        søknadsperiode.setPeriodeVurderingType(PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
            .medSøknad(søknad(søknadsperiode, new PeriodeUtenOmsorg(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4))))
            .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteOgFlerbarnsdagerKonto(1000, 85))
            .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void UT1032_mor_søker_fedrekvote_men_ikke_overføring() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, null, false);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    @Test
    public void UT1033_mor_overføring_innleggelse_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1034_mor_overføring_sykdom_skade_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1296_mor_overføring_aleneomsorg_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.ALENEOMSORG_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1297_mor_overføring_annen_forelder_ikke_rett_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        StønadsPeriode søknadsperiode = overføringsperiode(fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_IKKE_RETT_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1026_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_ikkeGradert() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4));
        søknadsperiode.setPeriodeVurderingType(PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(1000))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1217_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_gradert_avklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(1000))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1031_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_ikkeGradert() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(1000))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void fom_akkurat_6_uker_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = søknadsperiode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6).plusDays(1));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(1000))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1218_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_gradert_avklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        StønadsPeriode søknadsperiode = gradertSøknadsperiode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medSøknad(søknad(søknadsperiode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .leggTilKontoer(ARBEIDSFORHOLD_1, fedrekvoteKonto(1000))
                .build();

        Regelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void opphold_fedrekvote_annenforelder() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        OppholdPeriode periode = new OppholdPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.KVOTE_ANNEN_FORELDER,
                fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15), null, false);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .medSøknadsperioder(Collections.singletonList(periode))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(periode, grunnlag);

        assertInnvilgetOpphold(regelresultat);
    }

    @Test
    public void opphold_fedrekvote_annenforelder_tom_for_konto() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        OppholdPeriode periode = new OppholdPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.KVOTE_ANNEN_FORELDER,
                fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15), null, false);
        Kontoer.Builder kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE).build())
                .leggTilKonto(new Konto.Builder().medTrekkdager(0).medType(Stønadskontotype.FEDREKVOTE).build());
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .medSøknadsperioder(Collections.singletonList(periode))
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, kontoer.build())
                .build();

        Regelresultat regelresultat = kjørRegel(periode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER);
    }


    private void assertInnvilgetOpphold(Regelresultat regelresultat) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isNull();
    }

    private StønadsPeriode søknadsperiode(LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    private Søknad søknad(StønadsPeriode søknadsperiode) {
        return fødselssøknadMedEnPeriode(søknadsperiode).build();
    }

    private Søknad.Builder fødselssøknadMedEnPeriode(StønadsPeriode søknadsperiode) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .leggTilSøknadsperiode(søknadsperiode);
    }

    private Regelresultat kjørRegel(UttakPeriode søknadsperiode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag,
                Trekkdagertilstand.ny(grunnlag, Collections.singletonList(søknadsperiode)), søknadsperiode)));
    }

    private StønadsPeriode gradertSøknadsperiode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return StønadsPeriode.medGradering(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom,
                Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, vurderingType);
    }

    private Kontoer fedrekvoteKonto(int trekkdager) {
        return new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FEDREKVOTE)
                        .medTrekkdager(trekkdager)
                        .build())
                .build();
    }

    private Kontoer fedrekvoteOgFlerbarnsdagerKonto(int fedrekvoteTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder()
            .leggTilKonto(new Konto.Builder()
                .medType(Stønadskontotype.FEDREKVOTE)
                .medTrekkdager(fedrekvoteTrekkdager)
                .build())
            .leggTilKonto(new Konto.Builder()
                .medType(Stønadskontotype.FLERBARNSDAGER)
                .medTrekkdager(flerbarnsdagerTrekkdager)
                .build())
            .build();
    }

    private Søknad søknad(StønadsPeriode søknadsperiode, GyldigGrunnPeriode gyldigGrunnPeriode) {
        return fødselssøknadMedEnPeriode(søknadsperiode)
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggGyldigGrunnPerioder(gyldigGrunnPeriode)
                        .build())
                .build();
    }

    private Søknad søknad(StønadsPeriode søknadsperiode, PeriodeUtenOmsorg periodeUtenOmsorg) {
        return fødselssøknadMedEnPeriode(søknadsperiode)
                .medDokumentasjon(new Dokumentasjon.Builder()
                        .leggPerioderUtenOmsorg(periodeUtenOmsorg)
                        .build())
                .build();
    }

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return RegelGrunnlagTestBuilder.create()
                .medInngangsvilkår(new Inngangsvilkår.Builder().build())
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(fødselsdato.withDayOfMonth(1).minusMonths(3))
                        .medFødsel(fødselsdato)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medFarHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());
    }

    private RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(false).build());
    }

    private RegelGrunnlag.Builder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(true).build());
    }

    private StønadsPeriode overføringsperiode(LocalDate fom, LocalDate tom, OverføringÅrsak årsak, PeriodeVurderingType vurderingType) {
        return StønadsPeriode.medOverføringAvKvote(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, årsak,
                vurderingType, null, false);
    }
}
