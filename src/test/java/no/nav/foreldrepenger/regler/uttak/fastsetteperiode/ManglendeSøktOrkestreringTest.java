package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forArbeid;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forFrilans;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator.forSelvstendigNæringsdrivende;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;

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
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isFalse();
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isFalse();
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
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
        var søknad = søknad(FØDSEL,
                oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(100), fødselsdato.plusWeeks(100), MORS_AKTIVITET_GODKJENT));
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad)
                .kontoer(kontoer)
                .arbeid(arbeid)
                .rettOgOmsorg(bareFarRett())
                .revurdering(new Revurdering.Builder().endringsdato(fødselsdato)
                        .gjeldendeVedtak(new Vedtak.Builder().leggTilPeriode(fastsattPeriode)))
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        //UT1291
        assertThat(perioder.get(1).isManuellBehandling()).isTrue();
    }

    @Test
    void manglende_søkt_periode_før_nytt_arbeidsforhold_tilkommer() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var arbeidsforhold = forArbeid(new Orgnummer("000000001"), null);
        var tilkommetArbeidsforhold = forArbeid(new Orgnummer("000000001"), "1234");

        var startdatoNyttArbeidsforhold = fødselsdato.plusWeeks(12);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder()
                        .arbeidsforhold(new Arbeidsforhold(arbeidsforhold))
                        .arbeidsforhold(new Arbeidsforhold(tilkommetArbeidsforhold, startdatoNyttArbeidsforhold)))
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .inngangsvilkår(oppfyltAlleVilkår())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarRett())
                .søknad(new Søknad.Builder().type(FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(13), fødselsdato.plusWeeks(15).minusDays(1), MORS_AKTIVITET_GODKJENT)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(0).uttakPeriode().getAktiviteter()).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD))
                .isEqualTo(new Trekkdager(Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), startdatoNyttArbeidsforhold.minusDays(1))));
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(startdatoNyttArbeidsforhold.minusDays(1));

        assertThat(perioder.get(1).uttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(1).uttakPeriode().getAktiviteter()).hasSize(2);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(tilkommetArbeidsforhold)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(startdatoNyttArbeidsforhold);
        //minus3 for vi ikke avslutter msp i helgen
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(13).minusDays(3));

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(perioder.get(2).uttakPeriode().getAktiviteter()).hasSize(2);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(tilkommetArbeidsforhold).merEnn0()).isTrue();

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
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(3).minusDays(1));
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
        assertThat(perioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4).minusDays(1));
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(3).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(4));
        assertThat(perioder.get(3).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(3).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
        assertThat(perioder.get(3).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(3).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(3).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
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
        assertThat(perioder.get(0).uttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(0).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(0).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(3).minusDays(1));
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
        assertThat(perioder.get(1).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(1).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4).minusDays(1));
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);

        assertThat(perioder.get(3).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(4));
        assertThat(perioder.get(3).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(3).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
        assertThat(perioder.get(3).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
        assertThat(perioder.get(3).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(perioder.get(3).uttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void bfhr_eneste_periode_er_første_6_ukene_skal_innvilges() {
        var fødselsdato = LocalDate.of(2021, 10, 11);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL,
                        OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(4), null, false, fødselsdato, fødselsdato, MorsAktivitet.SYK, MORS_AKTIVITET_GODKJENT))
                )
                .kontoer(kontoer(konto(FORELDREPENGER, 100)))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void bfhr_msp_skal_ikke_avslås_pga_tom_på_konto_hvis_dager_igjen_på_minsteretten() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(7).minusDays(1), null, false, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING, MORS_AKTIVITET_GODKJENT);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(8),
                fødselsdato.plusWeeks(9).minusDays(1), null, false, fødselsdato, fødselsdato,
                null, null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp))
                .kontoer(kontoer(konto(FORELDREPENGER, 10)).minsterettDager(5))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(8).minusDays(1));
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
    }

    @Test
    void bfhr_msp_skal_avslås_pga_tom_på_konto_hvis_ikke_dager_igjen_på_fp_men_ubrukte_minsterett() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(7).minusDays(1), null, false, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING, MORS_AKTIVITET_GODKJENT);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(8),
                fødselsdato.plusWeeks(9).minusDays(1), null, false, fødselsdato, fødselsdato,
                null, null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp))
                .kontoer(kontoer(konto(FORELDREPENGER, 5)).minsterettDager(5))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(8).minusDays(1));
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void bfhr_msp_skal_avslås_pga_tom_på_konto_hvis_dager_igjen_på_minsteretten_men_brukt_alle_dager_foreldrepenger() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(10).minusDays(1), null, false, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING, MORS_AKTIVITET_GODKJENT);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(11),
                fødselsdato.plusWeeks(12).minusDays(1), null, false, fødselsdato, fødselsdato,
                null, null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp))
                .kontoer(kontoer(konto(FORELDREPENGER, 20)).minsterettDager(5))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(1));
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void bfhr_msp_skal_ikke_avslås_pga_tom_på_konto_hvis_flere_dager_igjen_på_minsteretten_enn_foreldrepenger() {
        var fødselsdato = LocalDate.of(2022, 6, 15);
        var fpFørMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(10).minusDays(1), null, false, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING, MORS_AKTIVITET_GODKJENT);
        var fpEtterMsp = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(11),
                fødselsdato.plusWeeks(12).minusDays(1), null, false, fødselsdato, fødselsdato,
                null, null);
        var grunnlag = basicGrunnlagFar(fødselsdato).søknad(søknad(FØDSEL, fpFørMsp, fpEtterMsp))
                .kontoer(kontoer(konto(FORELDREPENGER, 30)).minsterettDager(20))
                .rettOgOmsorg(bareFarRett())
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(1).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(1).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(1));
        assertThat(perioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT);
    }

    @Test
    void msp_pga_fødselshendelse_etter_termin_skal_innvilge_mødrekvote() {
        var termindato = LocalDate.of(2022, 6, 23);
        var fødselsdato = termindato.plusWeeks(1);
        var fellesperiode = oppgittPeriode(FELLESPERIODE, termindato.minusWeeks(3), termindato.minusWeeks(2).minusDays(1));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote1 = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(5).minusDays(1));
        //Hull skapt av justering ved fødselshendelsen
        var mødrekvote2 = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termindato))
                .søknad(søknad(FØDSEL, fellesperiode, fpff, mødrekvote1, mødrekvote2))
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        //msp skal innvilges som mødrekvote
        assertThat(perioder.get(3).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(5));
        assertThat(perioder.get(3).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(perioder.get(3).uttakPeriode().getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(perioder.get(3).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.MSP_INNVILGET);
        var aktivitet = grunnlag.getArbeid().getAktiviteter().stream().findFirst().orElseThrow();
        assertThat(perioder.get(3).uttakPeriode().getTrekkdager(aktivitet)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(3).uttakPeriode().getUtbetalingsgrad(aktivitet)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void msp_pga_fødselshendelse_etter_termin_skal_innvilge_mødrekvote_søker_over_uke_6() {
        var termindato = LocalDate.of(2022, 6, 16);
        var fødselsdato = LocalDate.of(2022, 6, 23);
        var fellesperiode = oppgittPeriode(FELLESPERIODE, LocalDate.of(2022, 5, 26), LocalDate.of(2022, 6, 1));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2022, 6, 2), LocalDate.of(2022, 6, 22));
        var mødrekvote1 = oppgittPeriode(MØDREKVOTE, LocalDate.of(2022, 6, 23), LocalDate.of(2022, 7, 29));
        //Hull skapt av justering ved fødselshendelsen
        var mødrekvote2 = oppgittPeriode(MØDREKVOTE, LocalDate.of(2022, 8, 25), LocalDate.of(2022, 8, 31));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termindato))
                .søknad(søknad(FØDSEL, fellesperiode, fpff, mødrekvote1, mødrekvote2))
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        //msp skal innvilges som mødrekvote
        assertThat(perioder.get(3).uttakPeriode().getFom()).isEqualTo(LocalDate.of(2022, 8, 1));
        assertThat(perioder.get(3).uttakPeriode().getTom()).isEqualTo(LocalDate.of(2022, 8, 3));
        assertThat(perioder.get(3).uttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(perioder.get(3).uttakPeriode().getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(perioder.get(3).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.MSP_INNVILGET);
        var aktivitet = grunnlag.getArbeid().getAktiviteter().stream().findFirst().orElseThrow();
        assertThat(perioder.get(3).uttakPeriode().getTrekkdager(aktivitet)).isEqualTo(new Trekkdager(3));
        assertThat(perioder.get(3).uttakPeriode().getUtbetalingsgrad(aktivitet)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void msp_første_6_ukene_som_ikke_er_opprettet_av_fødselshendelse_skal_avlås() {
        var termindato = LocalDate.of(2022, 6, 21);
        var fødselsdato = termindato.plusDays(3);
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote1 = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(3).minusDays(1));
        //hull mellom 3-4
        var mødrekvote2 = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5).minusDays(1));
        //hull mellom 5-6
        var mødrekvote3 = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7).minusDays(1));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termindato))
                .søknad(søknad(FØDSEL, fpff, mødrekvote1, mødrekvote2, mødrekvote3))
                .build();
        var perioder = fastsettPerioder(grunnlag);
        var aktivitet = grunnlag.getArbeid().getAktiviteter().stream().findFirst().orElseThrow();

        assertThat(perioder).hasSize(6);

        assertThat(perioder.get(2).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(3));
        assertThat(perioder.get(2).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4).minusDays(1));
        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(2).uttakPeriode().getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(2).uttakPeriode().getTrekkdager(aktivitet)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(2).uttakPeriode().getUtbetalingsgrad(aktivitet)).isEqualTo(Utbetalingsgrad.ZERO);

        assertThat(perioder.get(4).uttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(5));
        assertThat(perioder.get(4).uttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(4).uttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(perioder.get(4).uttakPeriode().getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(perioder.get(4).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(perioder.get(4).uttakPeriode().getTrekkdager(aktivitet)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(4).uttakPeriode().getUtbetalingsgrad(aktivitet)).isEqualTo(Utbetalingsgrad.ZERO);
    }

}
