package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class EndringssøknadOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_trekke_dager_for_perioder_i_vedtak() {
        var fødselsdato = LocalDate.of(2020, 1, 16);
        var endringsdato = fødselsdato.plusWeeks(6);
        var aktivitet = AktivitetIdentifikator.forFrilans();
        var vedtaksperiode = new FastsattUttakPeriode.Builder().tidsperiode(fødselsdato, endringsdato.minusDays(1))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .aktiviteter(
                        List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(30), Stønadskontotype.MØDREKVOTE, aktivitet)));
        var vedtak = new Vedtak.Builder().leggTilPeriode(vedtaksperiode);
        var revurdering = new Revurdering.Builder().endringsdato(endringsdato).gjeldendeVedtak(vedtak);
        var søknadOm10UkerMødrekvote = søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, endringsdato, endringsdato.plusWeeks(10).minusDays(1)));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .revurdering(revurdering)
                .kontoer(
                        new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(75)))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitet)))
                .søknad(søknadOm10UkerMødrekvote);

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isNotEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void skal_arve_høyest_saldo_fra_for_arbeidsforhold_med_startdato_i_uttak() {
        var fødselsdato = LocalDate.of(2020, 1, 16);
        var aktivitet1 = AktivitetIdentifikator.forFrilans();
        var aktivitet2 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var tilkommetAktivitet1 = AktivitetIdentifikator.forArbeid(new Orgnummer("123"), null);
        var opprinnligUttakAktivitet1 = new FastsattUttakPeriodeAktivitet(new Trekkdager(30), Stønadskontotype.MØDREKVOTE, aktivitet1);
        var opprinnligUttakAktivitet2 = new FastsattUttakPeriodeAktivitet(new Trekkdager(20), Stønadskontotype.MØDREKVOTE, aktivitet2);
        var vedtaksperiode = new FastsattUttakPeriode.Builder().tidsperiode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .aktiviteter(List.of(opprinnligUttakAktivitet1, opprinnligUttakAktivitet2));
        var vedtak = new Vedtak.Builder().leggTilPeriode(vedtaksperiode);
        var revurdering = new Revurdering.Builder().endringsdato(fødselsdato.plusWeeks(6)).gjeldendeVedtak(vedtak);
        var søknadOm12UkerMødrekvote = søknad(Søknadstype.FØDSEL,
                oppgittPeriode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(18).minusDays(1)));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .revurdering(revurdering)
                .kontoer(
                        new Kontoer.Builder().konto(new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(75)))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitet1))
                        .arbeidsforhold(new Arbeidsforhold(aktivitet2))
                        .arbeidsforhold(new Arbeidsforhold(tilkommetAktivitet1, fødselsdato.plusWeeks(8))))
                .søknad(søknadOm12UkerMødrekvote);

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        //Knekk pga nytt arbeidsforhold
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //aktivitet1 går tom for dager
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //aktivitet2 og tilkommet går tom for dager
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //alle arbeidsforhold tom for dager
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }
}
