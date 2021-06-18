package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forArbeid;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forFrilans;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forSelvstendigNæringsdrivende;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

class ManglendeSøktOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_avslå_og_trekke_foreldrepenger_for_bare_far_har_rett_hvis_dager_igjen() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag = basicGrunnlagFar(fødselsdato).medSøknad(søknad(FØDSEL,
                oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(50), fødselsdato.plusWeeks(52))))
                .medKontoer(kontoer(konto(FORELDREPENGER, 100)))
                .medRettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        //TODO fritt uttak: Manuell behandling for alle manglende søkt
        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isFalse();
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isFalse();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    private Kontoer.Builder kontoer(Konto.Builder... konto) {
        var kontoer = new Kontoer.Builder();
        for (var k : konto) {
            kontoer.leggTilKonto(k);
        }
        return kontoer;
    }

    @Test
    void skal_kunne_håndtere_ulikt_antall_dager_gjenværende_på_arbeidsforhold_ved_manglende_søkt_periode() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 25));
        var arbeid = new Arbeid.Builder()
                .leggTilArbeidsforhold(new Arbeidsforhold(forFrilans()))
                .leggTilArbeidsforhold(new Arbeidsforhold(forSelvstendigNæringsdrivende()));
        //En fastsatt periode for å få ulikt antall saldo
        var fastsattPeriode = new FastsattUttakPeriode.Builder()
                .medTidsperiode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6))
                .medAktiviteter(List.of(
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FORELDREPENGER, forFrilans()),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FORELDREPENGER, forSelvstendigNæringsdrivende())))
                .medPeriodeResultatType(INNVILGET);
        //SKal gå tom for dager på frilans før aktiviteten med sn
        var periodeMedAvklartMorsAktivitet = new PeriodeMedAvklartMorsAktivitet(fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(100), PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET);
        var søknad = søknad(FØDSEL,
                oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(100), fødselsdato.plusWeeks(100)))
                .medDokumentasjon(new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(periodeMedAvklartMorsAktivitet));
        var grunnlag = basicGrunnlagFar(fødselsdato).medSøknad(søknad)
                .medKontoer(kontoer)
                .medArbeid(arbeid)
                .medRettOgOmsorg(bareFarRett())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(fødselsdato)
                        .medGjeldendeVedtak(new Vedtak.Builder().leggTilPeriode(fastsattPeriode)))
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        //UT1291
        assertThat(perioder.get(1).isManuellBehandling()).isTrue();
    }

    @Test
    void manglende_søkt_periode_før_nytt_arbeidsforhold_tilkommer() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var arbeidsforhold = forArbeid(new Orgnummer("000000001"), null);
        var tilkommetArbeidsforhold = forArbeid(new Orgnummer("000000001"), "1234");

        var startdatoNyttArbeidsforhold = fødselsdato.plusWeeks(12);
        var dok = new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fødselsdato, fødselsdato.plusWeeks(20), PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold))
                        .leggTilArbeidsforhold(new Arbeidsforhold(tilkommetArbeidsforhold, startdatoNyttArbeidsforhold)))
                .medKontoer(kontoer(konto(FORELDREPENGER, 100)))
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(bareFarRett())
                .medSøknad(new Søknad.Builder().medType(FØDSEL).medDokumentasjon(dok)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(15).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(0).getUttakPeriode().getAktiviteter()).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), startdatoNyttArbeidsforhold.minusDays(1))));
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(startdatoNyttArbeidsforhold.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(1).getUttakPeriode().getAktiviteter()).hasSize(2);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(tilkommetArbeidsforhold)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(startdatoNyttArbeidsforhold);
        //minus3 for vi ikke avslutter msp i helgen
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(13).minusDays(3));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(2).getUttakPeriode().getAktiviteter()).hasSize(2);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(tilkommetArbeidsforhold).merEnn0()).isTrue();

    }

}
