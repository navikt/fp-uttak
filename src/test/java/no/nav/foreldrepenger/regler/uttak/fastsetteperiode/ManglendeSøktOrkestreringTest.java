package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forArbeid;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forFrilans;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forSelvstendigNæringsdrivende;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

class ManglendeSøktOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_avslå_og_trekke_foreldrepenger_for_bare_far_har_rett_hvis_dager_igjen() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL,
                oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(50), fødselsdato.plusWeeks(52))))
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
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
            kontoer.konto(k);
        }
        return kontoer;
    }

    @Test
    void skal_kunne_håndtere_ulikt_antall_dager_gjenværende_på_arbeidsforhold_ved_manglende_søkt_periode() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 25));
        var arbeid = new Arbeid.Builder()
                .arbeidsforhold(new Arbeidsforhold(forFrilans()))
                .arbeidsforhold(new Arbeidsforhold(forSelvstendigNæringsdrivende()));
        //En fastsatt periode for å få ulikt antall saldo
        var fastsattPeriode = new FastsattUttakPeriode.Builder()
                .tidsperiode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6))
                .aktiviteter(List.of(
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FORELDREPENGER, forFrilans()),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FORELDREPENGER, forSelvstendigNæringsdrivende())))
                .periodeResultatType(INNVILGET);
        //SKal gå tom for dager på frilans før aktiviteten med sn
        var periodeMedAvklartMorsAktivitet = aktivitetAvklart(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(100));
        var søknad = søknad(FØDSEL,
                oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(100), fødselsdato.plusWeeks(100)))
                .dokumentasjon(new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(periodeMedAvklartMorsAktivitet));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad)
                .kontoer(kontoer)
                .arbeid(arbeid)
                .rettOgOmsorg(bareFarRett())
                .revurdering(new Revurdering.Builder().endringsdato(fødselsdato)
                        .gjeldendeVedtak(new Vedtak.Builder().leggTilPeriode(fastsattPeriode)))
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        //UT1291
        assertThat(perioder.get(1).isManuellBehandling()).isTrue();
    }

    @Test
    void manglende_søkt_periode_før_nytt_arbeidsforhold_tilkommer() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var arbeidsforhold = forArbeid(new Orgnummer("000000001"), null);
        var tilkommetArbeidsforhold = forArbeid(new Orgnummer("000000001"), "1234");

        var startdatoNyttArbeidsforhold = fødselsdato.plusWeeks(12);
        var dok = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(aktivitetAvklart(fødselsdato, fødselsdato.plusWeeks(20)));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder()
                        .arbeidsforhold(new Arbeidsforhold(arbeidsforhold))
                        .arbeidsforhold(new Arbeidsforhold(tilkommetArbeidsforhold, startdatoNyttArbeidsforhold)))
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .inngangsvilkår(oppfyltAlleVilkår())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarRett())
                .søknad(new Søknad.Builder().type(FØDSEL).dokumentasjon(dok)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(15).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(0).getUttakPeriode().getAktiviteter()).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), startdatoNyttArbeidsforhold.minusDays(1))));
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(startdatoNyttArbeidsforhold.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
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

    @Test
    void skal_avslå_foreldrepenger_når_msp_innenfor_første_6_ukene_bare_mor_har_rett() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(søknad(FØDSEL,
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(8).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15).minusDays(1))
                ))
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true))
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(6);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(3).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4).minusDays(1));
        assertThat(perioder.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(4));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
        assertThat(perioder.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }


    @Test
    void skal_avslå_foreldrepenger_når_msp_innenfor_første_6_ukene_mor_aleneomsorg() {
        var fødselsdato = LocalDate.of(2019, 9, 3);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(søknad(FØDSEL,
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(3).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(8).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15).minusDays(1))
                ))
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .rettOgOmsorg(aleneomsorg())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(6);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(3).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4).minusDays(1));
        assertThat(perioder.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(4));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
        assertThat(perioder.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void bfhr_eneste_periode_er_første_6_ukene_skal_gå_til_manuell() {
        var fødselsdato = LocalDate.of(2021, 10, 11);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL,
                        oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(4)))
                        .dokumentasjon(new Dokumentasjon.Builder()
                                .gyldigGrunnPeriode(new GyldigGrunnPeriode(fødselsdato, fødselsdato.plusWeeks(4))))
                )
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isNotNull();
    }

    @Test
    void bfhr_msp_skal_ikke_avslås_pga_tom_på_konto_hvis_dager_igjen_på_minsteretten() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(7).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(8),
                fødselsdato.plusWeeks(9).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp)
                        .dokumentasjon(new Dokumentasjon.Builder()
                                .periodeMedAvklartMorsAktivitet(aktivitetAvklart(fpFørMsp.getFom(), fpFørMsp.getTom())))
                )
                .kontoer(kontoer(konto(FORELDREPENGER, 10)).minsterettDager(5))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(8).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
    }

    @Test
    void bfhr_msp_skal_avslås_pga_tom_på_konto_hvis_ikke_dager_igjen_på_fp_men_ubrukte_minsterett() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(7).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(8),
                fødselsdato.plusWeeks(9).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp)
                        .dokumentasjon(new Dokumentasjon.Builder()
                                .periodeMedAvklartMorsAktivitet(aktivitetAvklart(fpFørMsp.getFom(), fpFørMsp.getTom())))
                )
                .kontoer(kontoer(konto(FORELDREPENGER, 5)).minsterettDager(5))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(8).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void bfhr_msp_skal_avslås_pga_tom_på_konto_hvis_dager_igjen_på_minsteretten_men_brukt_alle_dager_foreldrepenger() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(10).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(11),
                fødselsdato.plusWeeks(12).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp)
                        .dokumentasjon(new Dokumentasjon.Builder()
                                .periodeMedAvklartMorsAktivitet(aktivitetAvklart(fpFørMsp.getFom(), fpFørMsp.getTom())))
                )
                .kontoer(kontoer(konto(FORELDREPENGER, 20)).minsterettDager(5))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void bfhr_msp_skal_ikke_avslås_pga_tom_på_konto_hvis_flere_dager_igjen_på_minsteretten_enn_foreldrepenger() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(10).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(11),
                fødselsdato.plusWeeks(12).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp)
                        .dokumentasjon(new Dokumentasjon.Builder()
                                .periodeMedAvklartMorsAktivitet(aktivitetAvklart(fpFørMsp.getFom(), fpFørMsp.getTom())))
                )
                .kontoer(kontoer(konto(FORELDREPENGER, 30)).minsterettDager(20))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
    }

    private PeriodeMedAvklartMorsAktivitet aktivitetAvklart(LocalDate fom, LocalDate tom) {
        return new PeriodeMedAvklartMorsAktivitet(fom, tom,
                PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET);
    }

}
