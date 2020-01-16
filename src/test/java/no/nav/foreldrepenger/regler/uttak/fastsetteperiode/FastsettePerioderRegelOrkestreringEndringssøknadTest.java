package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePerioderRegelOrkestreringEndringssøknadTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void skal_trekke_dager_for_perioder_i_vedtak() {
        var fødselsdato = LocalDate.of(2020, 1, 16);
        var endringsdato = fødselsdato.plusWeeks(6);
        var aktivitet = AktivitetIdentifikator.forFrilans();
        var vedtaksperiode = new FastsattUttakPeriode.Builder()
                .medTidsperiode(fødselsdato, endringsdato.minusDays(1))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(30), Stønadskontotype.MØDREKVOTE, aktivitet)));
        var vedtak = new Vedtak.Builder()
                .leggTilPeriode(vedtaksperiode);
        var revurdering = new Revurdering.Builder()
                .medEndringsdato(endringsdato)
                .medGjeldendeVedtak(vedtak);
        var søknadOm10UkerMødrekvote = søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.MØDREKVOTE, endringsdato, endringsdato.plusWeeks(10).minusDays(1)));
        grunnlag.medRevurdering(revurdering)
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato).medFørsteLovligeUttaksdag(LocalDate.of(2017, 1, 1)))
                .medKontoer(new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(75)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitet)))
                .medSøknad(søknadOm10UkerMødrekvote);

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isNotEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_arve_høyest_saldo_fra_for_arbeidsforhold_med_startdato_i_uttak() {
        var fødselsdato = LocalDate.of(2020, 1, 16);
        var aktivitet1 = AktivitetIdentifikator.forFrilans();
        var aktivitet2 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var tilkommetAktivitet1 = AktivitetIdentifikator.forArbeid("123", null);
        var opprinnligUttakAktivitet1 = new FastsattUttakPeriodeAktivitet(new Trekkdager(30), Stønadskontotype.MØDREKVOTE, aktivitet1);
        var opprinnligUttakAktivitet2 = new FastsattUttakPeriodeAktivitet(new Trekkdager(20), Stønadskontotype.MØDREKVOTE, aktivitet2);
        var vedtaksperiode = new FastsattUttakPeriode.Builder()
                .medTidsperiode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medAktiviteter(List.of(opprinnligUttakAktivitet1, opprinnligUttakAktivitet2));
        var vedtak = new Vedtak.Builder()
                .leggTilPeriode(vedtaksperiode);
        var revurdering = new Revurdering.Builder()
                .medEndringsdato(fødselsdato.plusWeeks(6))
                .medGjeldendeVedtak(vedtak);
        var søknadOm12UkerMødrekvote = søknad(Søknadstype.FØDSEL, søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(18).minusDays(1)));
        grunnlag.medRevurdering(revurdering)
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato).medFørsteLovligeUttaksdag(LocalDate.of(2017, 1, 1)))
                .medKontoer(new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(75)))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(aktivitet1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(aktivitet2))
                        .leggTilArbeidsforhold(new Arbeidsforhold(tilkommetAktivitet1, fødselsdato.plusWeeks(8)))
                )
                .medSøknad(søknadOm12UkerMødrekvote);

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
