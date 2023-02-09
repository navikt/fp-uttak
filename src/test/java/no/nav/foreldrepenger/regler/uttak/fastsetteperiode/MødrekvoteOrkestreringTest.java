package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.SYKDOM_ANNEN_FORELDER_GODKJENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;

class MødrekvoteOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void mødrekvoteperiode_før_familiehendelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlag(fødselsdato)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3),
                                fødselsdato.minusWeeks(1).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.minusWeeks(1),
                                fødselsdato.plusWeeks(6).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(1).minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(1));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
    }

    @Test
    void overføring_av_mødrekvote_grunnet_sykdom_skade_skal_innvilges() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1),
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null, null, SYKDOM_ANNEN_FORELDER_GODKJENT))
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote innvilges
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getOverføringÅrsak()).isEqualTo(OverføringÅrsak.SYKDOM_ELLER_SKADE);

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getOverføringÅrsak()).isEqualTo(OverføringÅrsak.SYKDOM_ELLER_SKADE);

        //2 neste uker fedrekvote innvilges
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    void overføring_av_mødrekvote_grunnet_sykdom_skade_skal_gå_til_avslag_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1),
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null, null, null))
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote avslås
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
    }

    @Test
    void overføring_av_mødrekvote_ikke_dokumentert_skal_til_avslag() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1),
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null, null, null))
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote skal til avslag
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote skal til avslag
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote skal til innvilget
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    void overføring_av_mødrekvote_grunnet_sykdom_skade_men_far_har_ikke_omsorg_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).harOmsorg(false))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forOverføring(Stønadskontotype.MØDREKVOTE, fødselsdato,
                                fødselsdato.plusWeeks(10).minusDays(1),
                                OverføringÅrsak.SYKDOM_ELLER_SKADE, null, null, SYKDOM_ANNEN_FORELDER_GODKJENT))
                        .oppgittPeriode(oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(10),
                                fødselsdato.plusWeeks(12).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        //6 første uker mødrekvote avslås
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1006
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        //3 neste uker mødrekvote innvilges
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1006
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        //2 neste uker fedrekvote avslått
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT); // UT1086
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }
}
