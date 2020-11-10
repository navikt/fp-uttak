package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.gradertPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.create;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dødsdatoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class MødrekvoteDelregelTest {

    @Test
    public void mødrekvoteperiode_med_nok_dager_på_konto() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void mødrekvote_slutter_på_fredag_og_første_uker_slutter_på_søndag_blir_innvilget() {
        LocalDate fødselsdato = LocalDate.of(2017, 12, 31);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato, fødselsdato.plusWeeks(6).minusDays(2));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);
        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void mødrekvote_de_første_6_ukene_etter_fødsel_skal_innvilges_også_når_mor_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode, new PeriodeUtenOmsorg(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(15))))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    public void mødrekvote_etter_første_6_ukene_etter_fødsel_skal_ikke_innvilges_når_mor_har_nok_på_kvoten_men_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode, new PeriodeUtenOmsorg(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(15))))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode, PeriodeUtenOmsorg periodeUtenOmsorg) {
        return new Søknad.Builder().leggTilOppgittPeriode(oppgittPeriode)
                .medType(Søknadstype.FØDSEL)
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(periodeUtenOmsorg));
    }

    @Test
    public void mødrekvote_etter_første_6_ukene_etter_fødsel_skal_ikke_innvilges_når_mor_har_noe_men_ikke_nok_på_kvoten_og_ikke_har_omsorg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 1);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode, new PeriodeUtenOmsorg(fødselsdato.minusWeeks(10), fødselsdato.plusWeeks(15))))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    public void UT1007_mor_etterTermin_innenFor6Uker_ikkeGradering_disponibleDager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(4));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1008_mor_innenFor6UkerEtterFødsel_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = gradertPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(3),
                fødselsdato.plusWeeks(4));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1221_mor_etterTermin_etter6Uker_omsorg_disponibleDager_gradering_avklart() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = gradertPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(10),
                fødselsdato.plusWeeks(11));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void UT1251_fødselsvilkår_ikke_oppfylt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(11));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medFødselOppfylt(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FØDSELSVILKÅRET_IKKE_OPPFYLT);
    }

    @Test
    public void UT1252_adopsjonsvilkår_ikke_oppfylt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(11));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medFødselOppfylt(true).medAdopsjonOppfylt(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.ADOPSJONSVILKÅRET_IKKE_OPPFYLT);
    }

    @Test
    public void UT1253_foreldreansvarsvilkår_ikke_oppfylt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(11));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medInngangsvilkår(
                        new Inngangsvilkår.Builder().medFødselOppfylt(true).medAdopsjonOppfylt(true).medForeldreansvarnOppfylt(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FORELDREANSVARSVILKÅRET_IKKE_OPPFYLT);
    }

    @Test
    public void UT1254_opptjeningsvilkår_ikke_oppfylt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(11));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 100);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medFødselOppfylt(true)
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medOpptjeningOppfylt(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.AVSLÅTT);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPTJENINGSVILKÅRET_IKKE_OPPFYLT);
    }

    @Test
    public void opphold_mødrekvote_annenforelder() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        var oppholdsperiode = DelRegelTestUtil.oppholdPeriode(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(30),
                OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100))
                .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 100));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppholdsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppholdsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
    }

    @Test
    public void opphold_mødrekvote_annenforelder_tom_for_konto() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        var oppholdsperiode = DelRegelTestUtil.oppholdPeriode(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(30),
                OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100))
                .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 0));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppholdsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppholdsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER);
    }

    @Test
    public void UT1275_søkte_mødrekvoteperiode_men_søker_døde_i_mellomtiden() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(6).minusDays(1));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato)
                        .medDødsdatoer(new Dødsdatoer.Builder().medSøkersDødsdato(fødselsdato.plusDays(3))))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.DØDSFALL);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKER_DØD);
    }

    @Test
    public void UT1289_søkte_mødrekvoteperiode_men_barn_døde_i_mellomtiden() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(13).minusDays(1));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato)
                        .medDødsdatoer(new Dødsdatoer.Builder().medBarnsDødsdato(fødselsdato.plusDays(3)).medErAlleBarnDøde(true)))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARN_DØD);
    }

    @Test
    public void Innvilge_ikke_UT1289_søkte_mødrekvoteperiode_og_barn_døde_men_mindre_enn_6_uker_siden() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate barnsDødsdato = fødselsdato.plusDays(1);
        OppgittPeriode oppgittPeriode = oppgittMødrekvote(fødselsdato, barnsDødsdato.plusWeeks(6).minusDays(2));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato)
                        .medDødsdatoer(new Dødsdatoer.Builder().medBarnsDødsdato(barnsDødsdato).medErAlleBarnDøde(true)))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    @Test
    public void Innvilge_ikke_UT1289_søkte_mødrekvoteperiode_barn_døde_i_mellomtiden_men_alle_barn_er_ikke_døde() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittMødrekvote(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(13).minusDays(1));
        var kontoer = enKonto(Stønadskontotype.MØDREKVOTE, 10 * 5);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato).medKontoer(kontoer)
                .medSøknad(søknad(oppgittPeriode))
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato)
                        .medDødsdatoer(new Dødsdatoer.Builder().medBarnsDødsdato(fødselsdato.plusDays(3)).medErAlleBarnDøde(false)))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE);
    }

    private OppgittPeriode oppgittMødrekvote(LocalDate fom, LocalDate tom) {
        return DelRegelTestUtil.oppgittPeriode(Stønadskontotype.MØDREKVOTE, fom, tom);
    }

    @Test
    public void UT1015_far_søker_mødrekvote_men_ikke_overføring() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = oppgittMødrekvote(fom, tom);
        Kontoer.Builder kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    @Test
    public void UT1016_far_overføring_innleggelse_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(fom, tom, OverføringÅrsak.INNLEGGELSE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1017_far_overføring_sykdom_skade_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(fom, tom, OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1294_far_overføring_aleneomsorg_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(fom, tom, OverføringÅrsak.ALENEOMSORG, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.ALENEOMSORG_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    private OppgittPeriode overføringsperiode(LocalDate fom,
                                              LocalDate tom,
                                              OverføringÅrsak årsak,
                                              PeriodeVurderingType vurderingType) {
        return DelRegelTestUtil.overføringsperiode(Stønadskontotype.MØDREKVOTE, fom, tom, årsak, vurderingType);
    }

    @Test
    public void UT1295_far_overføring_ikke_rett_men_ikke_gyldig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        LocalDate fom = fødselsdato.plusWeeks(3);
        LocalDate tom = fødselsdato.plusWeeks(4);
        var oppgittPeriode = overføringsperiode(fom, tom, OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT, PeriodeVurderingType.PERIODE_OK);
        Kontoer.Builder kontoer = new Kontoer.Builder().leggTilKonto(
                new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.MØDREKVOTE))
                .leggTilKonto(new Konto.Builder().medTrekkdager(1000).medType(Stønadskontotype.FEDREKVOTE));
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato).medKontoer(kontoer).medSøknad(søknad(oppgittPeriode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.MANUELL_BEHANDLING);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.DEN_ANDRE_PART_IKKE_RETT_IKKE_OPPFYLT);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    private RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return create().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true).medMorHarRett(true).medFarHarRett(true))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode) {
        return new Søknad.Builder().leggTilOppgittPeriode(oppgittPeriode).medType(Søknadstype.FØDSEL);
    }

    private Kontoer.Builder enKonto(Stønadskontotype type, int trekkdager) {
        return new Kontoer.Builder().leggTilKonto(konto(type, trekkdager));
    }

    private Konto.Builder konto(Stønadskontotype type, int trekkdager) {
        return new Konto.Builder().medType(type).medTrekkdager(trekkdager);
    }

    private RegelGrunnlag.Builder basicGrunnlagMor(LocalDate fødselsdato) {
        return create().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true).medMorHarRett(true).medFarHarRett(true))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }
}
