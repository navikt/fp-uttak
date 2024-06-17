package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class ForeldrepengerFørFødselOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void foreldrepengerFørFødsel_happy_case() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var fpff = oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(15));
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

    }

    @Test
    void foreldrepengerFørFødsel_far_søker_fpff() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(Søknadstype.FØDSEL,
            oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
    }

    @Test
    void foreldrepengerFørFødsel_for_lang_fpff_periode_før_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(Søknadstype.FØDSEL,
            oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(4), fødselsdato.minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(4));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(perioder.get(0).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).evalueringResultat()).isNotNull();

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).uttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(1).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).evalueringResultat()).isNotNull();

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(3));
        assertThat(perioder.get(2).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(2).evalueringResultat()).isNotNull();
    }

    @Test
    void foreldrepengerFørFødsel_for_lang_fpff_periode_etter_fødsel() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var fpff = oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(2).minusDays(1));
        var mødrekvote = oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(6).minusDays(1));
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(0).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).evalueringResultat()).isNotNull();

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(2).minusDays(1));
        assertThat(perioder.get(1).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).evalueringResultat()).isNotNull();

        //Mødrekvoten blir satt til manuell pga forrige periode ble manuell. Ingen årsak eller regelvurdering.
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).uttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(2));
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(2).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(2).evalueringResultat()).isNotNull();
    }

    @Test
    void foreldrepengerFørFødsel_for_kort_fpff_periode_slutter_for_tidlig() {
        var fødselsdato = LocalDate.of(2018, 3, 1);
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(Søknadstype.FØDSEL,
            oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3).minusDays(1), fødselsdato.minusWeeks(2))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(perioder.get(0).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).evalueringResultat()).isNotNull();

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).uttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(2));
        assertThat(perioder.get(1).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).evalueringResultat()).isNotNull();

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(2).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(2).evalueringResultat()).isNotNull();
    }

    @Test
    void foreldrepengerFørFødsel_for_kort_fpff_starter_for_sent() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagMor(fødselsdato).søknad(søknad(Søknadstype.FØDSEL,
            oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(1), fødselsdato.minusDays(1))));
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(1));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(0).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).evalueringResultat()).isNotNull();

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(3));
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(30));
        assertThat(perioder.get(1).innsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).evalueringResultat()).isNotNull();
    }
}
