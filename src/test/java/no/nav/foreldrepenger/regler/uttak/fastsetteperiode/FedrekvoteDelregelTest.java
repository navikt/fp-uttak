package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.overføringsperiode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;

class FedrekvoteDelregelTest {

    @Test
    void fedrekvote_etter_6_uker_blir_innvilget() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(oppgittPeriode))
                .kontoer(fedrekvoteKonto(10 * 5))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom) {
        return DelRegelTestUtil.oppgittPeriode(FEDREKVOTE, fom, tom);
    }

    @Test
    void UT1020_fedrekvote_før_fødsel_blir_avslått() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(1));
        var kontoer = fedrekvoteKonto(10 * 5);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(oppgittPeriode)).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL);
    }

    @Test
    void UT1292_fedrekvote_uten_at_mor_har_rett_blir_avslått() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(9).minusDays(1));
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 10 * 5));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var grunnlag = basicGrunnlagFar(fødselsdato).kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(false).farHarRett(true).samtykke(true))
                .søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_IKKE_RETT_FK);
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder().type(stønadskontotype).trekkdager(trekkdager);
    }

    @Test
    void fedrekvote_bli_avslått_når_søker_ikke_har_omsorg() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1));
        var kontoer = fedrekvoteKonto(10 * 5);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(oppgittPeriode, new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusWeeks(100)))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    void UT1025_far_førUke7_etterTermin_utenGyldigGrunn_ikkeOmsorg_flerbarnsdager() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FEDREKVOTE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4), null,
                true, PeriodeVurderingType.UAVKLART_PERIODE, null, null, null);
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(oppgittPeriode, new PeriodeUtenOmsorg(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4))))
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold))
                .kontoer(fedrekvoteOgFlerbarnsdagerKonto(1000, 85))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
    }

    @Test
    void UT1032_mor_søker_fedrekvote_men_ikke_overføring() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                søknad(oppgittPeriode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    @Test
    void UT1033_mor_overføring_innleggelse_men_ikke_gyldig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(FEDREKVOTE, fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(oppgittPeriode)).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    void UT1034_mor_overføring_sykdom_skade_men_ikke_gyldig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(FEDREKVOTE, fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(oppgittPeriode)).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    void UT1296_mor_overføring_aleneomsorg_men_ikke_gyldig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(FEDREKVOTE, fom, tom, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(oppgittPeriode)).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.ALENEOMSORG_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    void UT1297_mor_overføring_annen_forelder_ikke_rett_men_ikke_gyldig() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fom = fødselsdato.plusWeeks(3);
        var tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(FEDREKVOTE, fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(oppgittPeriode)).kontoer(kontoer).build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_IKKE_RETT_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    void UT1026_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_ikkeGradert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = DelRegelTestUtil.oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4),
                PeriodeVurderingType.PERIODE_OK);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(oppgittPeriode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .kontoer(fedrekvoteKonto(1000))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    void UT1217_far_førUke7_etterTermin_gyldigGrunn_omsorg_disponibleDager_gradert_avklart() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = gradertPeriode(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4), PeriodeVurderingType.PERIODE_OK);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(oppgittPeriode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .kontoer(fedrekvoteKonto(1000))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    private OppgittPeriode gradertPeriode(LocalDate fom, LocalDate tom) {
        return gradertPeriode(fom, tom, PeriodeVurderingType.IKKE_VURDERT);
    }

    private OppgittPeriode gradertPeriode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return DelRegelTestUtil.gradertPeriode(FEDREKVOTE, fom, tom, Set.of(AktivitetIdentifikator.forFrilans()), vurderingType);
    }

    @Test
    void UT1031_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_ikkeGradert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(oppgittPeriode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .kontoer(fedrekvoteKonto(1000))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    void fom_akkurat_6_uker_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6).plusDays(1));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(oppgittPeriode))
                .kontoer(fedrekvoteKonto(1000))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    void UT1218_far_etterUke7_gyldigGrunn_omsorg_disponibleDager_gradert_avklart() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var oppgittPeriode = gradertPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(
                søknad(oppgittPeriode, new GyldigGrunnPeriode(LocalDate.MIN, LocalDate.MAX)))
                .kontoer(fedrekvoteKonto(1000))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    void opphold_fedrekvote_annenforelder() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var periode = OppgittPeriode.forOpphold(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15),
                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER, null, null);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(1000).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(periode))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(periode, grunnlag);

        assertInnvilgetOpphold(regelresultat);
    }

    @Test
    void opphold_fedrekvote_annenforelder_tom_for_konto() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var periode = OppgittPeriode.forOpphold(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(15).plusWeeks(15),
                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER, null, null);
        var kontoer = new Kontoer.Builder().konto(
                new Konto.Builder().trekkdager(1000).type(Stønadskontotype.MØDREKVOTE))
                .konto(new Konto.Builder().trekkdager(0).type(Stønadskontotype.FEDREKVOTE));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(
                new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(periode))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(periode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER);
    }


    private void assertInnvilgetOpphold(FastsettePerioderRegelresultat regelresultat) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode) {
        return fødselssøknadMedEnPeriode(oppgittPeriode);
    }

    private Søknad.Builder fødselssøknadMedEnPeriode(OppgittPeriode oppgittPeriode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);
    }

    private Kontoer.Builder fedrekvoteKonto(int trekkdager) {
        return new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.FEDREKVOTE).trekkdager(trekkdager));
    }

    private Kontoer.Builder fedrekvoteOgFlerbarnsdagerKonto(int fedrekvoteTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder()
                .konto(new Konto.Builder().type(Stønadskontotype.FEDREKVOTE).trekkdager(fedrekvoteTrekkdager))
                .flerbarnsdager(flerbarnsdagerTrekkdager);
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode, GyldigGrunnPeriode gyldigGrunnPeriode) {
        return fødselssøknadMedEnPeriode(oppgittPeriode).dokumentasjon(
                new Dokumentasjon.Builder().gyldigGrunnPeriode(gyldigGrunnPeriode));
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode, PeriodeUtenOmsorg periodeUtenOmsorg) {
        return fødselssøknadMedEnPeriode(oppgittPeriode).dokumentasjon(
                new Dokumentasjon.Builder().periodeUtenOmsorg(periodeUtenOmsorg));
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return RegelGrunnlagTestBuilder.create()
                .inngangsvilkår(new Inngangsvilkår.Builder())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true).farHarRett(true).samtykke(true))
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
}
