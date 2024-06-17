package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.gradertPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class StebarnsadopsjonDelRegelTest {

    @Test
    void UT1240_stebarnsadopsjon_far_ikke_omsorg() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        var uttakPeriode = oppgittPeriode(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2));

        var grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode).rettOgOmsorg(
                new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON).oppgittPeriode(uttakPeriode))
            .build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();

    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom) {
        return DelRegelTestUtil.oppgittPeriode(FEDREKVOTE, fom, tom);
    }

    @Test
    void UT1241_stebarnsadopsjon_far_omsorg_disponible_dager_og_ingen_gradering() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        var uttakPeriode = oppgittPeriode(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2));

        var grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode)

            .build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    void UT1242_stebarnsadopsjon_far_omsorg_disponible_dager_gradering_og_avklart_periode() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        var uttakPeriode = gradertPeriode(Stønadskontotype.FEDREKVOTE, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2),
            Set.of(ARBEIDSFORHOLD_1));

        var grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode).build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    void UT1244_stebarnsadopsjon_far_omsorg_ikke_disponible_stønadsdager() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        var uttakPeriode = oppgittPeriode(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2));

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(50))
            .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(0))
            .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(130));
        var grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode).arbeid(
            new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    void UT1285_stebarnsadopsjon_uttak_før_omsorgsovertakelse() {
        var omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        var uttakPeriode = oppgittPeriode(omsorgsovertakelseDato.minusDays(3), omsorgsovertakelseDato.plusWeeks(2));

        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(50))
            .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(0))
            .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(130));
        var grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode).arbeid(
            new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))).kontoer(kontoer).build();

        var regelresultat = kjørRegel(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    private RegelGrunnlag.Builder grunnlagFar(LocalDate familiehendelseDato, OppgittPeriode oppgittPeriode) {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(50))
            .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(50))
            .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(130));
        return RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON).oppgittPeriode(oppgittPeriode))
            .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelseDato))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
            .kontoer(kontoer)
            .behandling(new Behandling.Builder().søkerErMor(false))
            .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true).samtykke(true))
            .adopsjon(new Adopsjon.Builder().ankomstNorge(null).stebarnsadopsjon(true))
            .inngangsvilkår(
                new Inngangsvilkår.Builder().adopsjonOppfylt(true).foreldreansvarnOppfylt(true).fødselOppfylt(true).opptjeningOppfylt(true));
    }
}
