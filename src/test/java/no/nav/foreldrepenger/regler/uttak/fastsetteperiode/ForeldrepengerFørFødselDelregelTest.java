package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.gradertPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.oppgittPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.create;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class ForeldrepengerFørFødselDelregelTest {

    @Test
    void UT1070_mor_utenFor3UkerFørFødsel() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.plusWeeks(8),
                familiehendelseDato.plusWeeks(9));
        var kontoer = kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100);
        var grunnlag = basicGrunnlagMor(familiehendelseDato).kontoer(kontoer).søknad(søknad(uttakPeriode)).build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertManuell(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    void UT1071_mor_innenFor3UkerFørFødsel_ikkeManglendeSøktPeriode_ikkeGradering() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2),
                familiehendelseDato.minusWeeks(1));
        var kontoer = kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100);
        var grunnlag = basicGrunnlagMor(familiehendelseDato).kontoer(kontoer).søknad(søknad(uttakPeriode)).build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
    }

    @Test
    void UT1072_mor_innenFor3UkerFørFødsel_ikkeManglendeSøktPeriode_gradering() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = gradertPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2),
                familiehendelseDato.minusWeeks(1));
        var kontoer = kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100);
        var grunnlag = basicGrunnlagMor(familiehendelseDato).kontoer(kontoer).søknad(søknad(uttakPeriode)).build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    void UT1073_mor_innenFor3UkerFørFødsel_manglendeSøktPeriode() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var msp = manglendeSøktPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2),
                familiehendelseDato.minusWeeks(1));
        var kontoer = kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100);
        var grunnlag = basicGrunnlagMor(familiehendelseDato).kontoer(kontoer).søknad(søknad(msp)).build();

        var regelresultat = kjørRegel(msp, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_FØR_FØDSEL);
    }

    @Test
    void UT1076_far_søker_fpff() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var uttakPeriode = oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2),
                familiehendelseDato.minusWeeks(1));
        var kontoer = new Kontoer.Builder().konto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100))
                //Må ha ellers å faller vi ut på FP_VK 10.5.1 - SjekkOmTomForAlleSineKontoer
                .konto(konto(Stønadskontotype.FEDREKVOTE, 100));
        var grunnlag = basicGrunnlag(familiehendelseDato).kontoer(kontoer)
                .behandling(søkerErFarBehandling())
                .søknad(søknad(uttakPeriode))
                .build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertManuell(regelresultat, IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL, Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL,
                false, false);
    }

    private Kontoer.Builder kontoer(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Kontoer.Builder().konto(konto(stønadskontotype, trekkdager));
    }

    private OppgittPeriode manglendeSøktPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forManglendeSøkt(stønadskontotype, fom, tom);
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder().type(stønadskontotype).trekkdager(trekkdager);
    }

    private Søknad.Builder søknad(OppgittPeriode oppgittPeriode) {
        return new Søknad.Builder().oppgittPeriode(oppgittPeriode).type(Søknadstype.FØDSEL);
    }

    private Behandling.Builder søkerErFarBehandling() {
        return new Behandling.Builder().søkerErMor(false);
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, PeriodeResultatÅrsak innvilgetPeriodeResultatÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(innvilgetPeriodeResultatÅrsak);
    }

    private void assertManuell(FastsettePerioderRegelresultat regelresultat,
                               PeriodeResultatÅrsak periodeResultatÅrsak,
                               Manuellbehandlingårsak manuellbehandlingårsak,
                               boolean trekkDager,
                               boolean utbetal) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkDager);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(periodeResultatÅrsak);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(manuellbehandlingårsak);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate familiehendelseDato) {
        return create().datoer(new Datoer.Builder().fødsel(familiehendelseDato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).farHarRett(true).morHarRett(true))
                .inngangsvilkår(new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                        .foreldreansvarnOppfylt(true)
                        .fødselOppfylt(true)
                        .opptjeningOppfylt(true));
    }

    private RegelGrunnlag.Builder basicGrunnlagMor(LocalDate familiehendelseDato) {
        return basicGrunnlag(familiehendelseDato).behandling(new Behandling.Builder().søkerErMor(true));
    }
}
