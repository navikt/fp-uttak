package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktørId;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dødsdatoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.EndringAvStilling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

class OrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void skal_innvilge_to_perioder_med_med_mødrekvote_på_under_10_uker() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .forEach(uttakPeriode -> assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET));
    }

    @Test
    void skal_knekke_mødrekvote_dersom_det_ikke_er_flere_dager_igjen() {
        var fødselsdato = LocalDate.of(2018, 1, 3);

        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(12).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        var uttakPerioder = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode).collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);
        /* Innvilget foreldrepenger før fødsel*/
        assertThat(uttakPerioder.get(0).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(0).getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPerioder.get(0).getTom()).isEqualTo(fødselsdato.minusDays(1));

        /* Innvilget mødrekvote etter fødsel frem til og med uke 6*/
        assertThat(uttakPerioder.get(1).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(1).getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPerioder.get(1).getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        /* Innvilget mødrekvote etter fødsel, etter uke 6 */
        assertThat(uttakPerioder.get(2).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(2).getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPerioder.get(2).getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        /* Avslått mødrekvote, ikke nok dager */
        assertThat(uttakPerioder.get(3).getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(uttakPerioder.get(3).getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(uttakPerioder.get(3).getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPerioder.get(3).getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    void mødrekvoteMedUtsattOppstartUtenGyldigGrunnSkalTrekkeDagerPåSaldo() {
        var fødselsdato = LocalDate.of(2018, 1, 8);
        var sluttGyldigUtsattPeriode = fødselsdato.plusDays(6);
        var startUgyldigPeriode = fødselsdato.plusDays(7);
        var sluttUgyldigPeriode = startUgyldigPeriode.plusDays(4);

        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(
                                oppgittPeriode(MØDREKVOTE, sluttUgyldigPeriode.plusDays(1), sluttUgyldigPeriode.plusWeeks(10)))
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(fødselsdato, sluttGyldigUtsattPeriode))));

        var fastsettePeriodeGrunnlag = grunnlag.build();
        var resultat = fastsettPerioder(fastsettePeriodeGrunnlag);
        var uttakPerioder = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode).collect(Collectors.toList());
        assertThat(uttakPerioder).hasSize(6);

        /* FPFF blir innvilget. */
        var foreldrepengerFørFødselPeriode = uttakPerioder.get(0);
        assertThat(foreldrepengerFørFødselPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(foreldrepengerFørFødselPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(foreldrepengerFørFødselPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(foreldrepengerFørFødselPeriode.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);


        /* Første del av opphold-perioden er gyldig utsettelse, men skal likevel behandles manuelt. */
        var gyldigUtsettelsePeriode = uttakPerioder.get(1);
        assertThat(gyldigUtsettelsePeriode.getTom()).isEqualTo(sluttGyldigUtsattPeriode);
        assertThat(gyldigUtsettelsePeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(gyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(gyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(gyldigUtsettelsePeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);


        var ugyldigUtsettelsePeriode = uttakPerioder.get(2);
        assertThat(ugyldigUtsettelsePeriode.getFom()).isEqualTo(sluttGyldigUtsattPeriode.plusDays(1));
        assertThat(ugyldigUtsettelsePeriode.getTom()).isEqualTo(sluttUgyldigPeriode);
        assertThat(ugyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(ugyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(ugyldigUtsettelsePeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);

        /* Splittes ved knekkpunkt ved 6 uker pga regelflyt */
        var uttakPeriode1 = uttakPerioder.get(3);
        assertThat(uttakPeriode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode1.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode1.getFom()).isEqualTo(sluttUgyldigPeriode.plusDays(1));
        assertThat(uttakPeriode1.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode1.getManuellbehandlingårsak()).isNull();

        var uttakPeriode2 = uttakPerioder.get(4);
        assertThat(uttakPeriode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode2.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode2.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode2.getTom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(8).plusDays(2));
        assertThat(uttakPeriode2.getManuellbehandlingårsak()).isNull();

        //Det er tom for konto for siste del siden allerede trekk fra saldo for forrige perioder
        // (gyldigutsett + ugyldigutsett) som gikk til manuell behandling
        var uttakPeriode3 = uttakPerioder.get(5);
        assertThat(uttakPeriode3.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode3.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode3.getFom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(8).plusDays(3));
        assertThat(uttakPeriode3.getTom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(10));
        assertThat(uttakPeriode3.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    void delvisUgyldigUtsattMødrekvote() {
        var fødselsdato = LocalDate.of(2018, 1, 8);
        var gyldigUtsettelseStart = fødselsdato.plusDays(5);
        var gyldigUtsettelseSlutt = fødselsdato.plusDays(10);

        var søknad = new Søknad.Builder()
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, gyldigUtsettelseSlutt.plusDays(1), fødselsdato.plusWeeks(6).minusDays(1)))
                .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(new GyldigGrunnPeriode(gyldigUtsettelseStart, gyldigUtsettelseSlutt)));
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(søknad);

        var resultat = fastsettPerioder(grunnlag);
        var uttakPerioder = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode).collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);

        // Første del av msp blir manuell behandling
        var ugyldigUtsattPeriode = uttakPerioder.get(1);
        assertThat(ugyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(ugyldigUtsattPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(ugyldigUtsattPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(ugyldigUtsattPeriode.getTom()).isEqualTo(fødselsdato.plusDays(4));
        assertThat(ugyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        var gyldigUtsattPeriode = uttakPerioder.get(2);
        assertThat(gyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(gyldigUtsattPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL);
        assertThat(gyldigUtsattPeriode.getFom()).isEqualTo(gyldigUtsettelseStart);
        assertThat(gyldigUtsattPeriode.getTom()).isEqualTo(gyldigUtsettelseSlutt);
        assertThat(gyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        var innvilgetUttakPeriode = uttakPerioder.get(3);
        assertThat(innvilgetUttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(innvilgetUttakPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(innvilgetUttakPeriode.getFom()).isEqualTo(gyldigUtsettelseSlutt.plusDays(1));
        assertThat(innvilgetUttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(innvilgetUttakPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    void helePeriodenUtenforSøknadsfrist() {
        var fødselsdato = LocalDate.of(2017, 11, 1);
        var sisteUttaksdag = fødselsdato.plusWeeks(6).minusDays(1);
        var søknadsfrist = sisteUttaksdag.plusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
        var fpff = OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                null, false, PeriodeVurderingType.IKKE_VURDERT, søknadsfrist.plusWeeks(1), søknadsfrist.plusWeeks(1), null);
        var mødrekvote = OppgittPeriode.forVanligPeriode(MØDREKVOTE, fødselsdato, sisteUttaksdag, null, false,
                PeriodeVurderingType.IKKE_VURDERT, søknadsfrist.plusWeeks(1), søknadsfrist.plusWeeks(1), null);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        // Act
        var resultat = fastsettPerioder(grunnlag);

        // Assert
        assertThat(resultat).hasSize(2);

        var førFødsel = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> FORELDREPENGER_FØR_FØDSEL.equals(p.getStønadskontotype()))
                .findFirst();
        assertThat(førFødsel).isPresent();
        assertThat(førFødsel.get().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(førFødsel.get().getManuellbehandlingårsak()).isNull();
        assertThat(førFødsel.get().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);


        var avslåttMødrekvote = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> MØDREKVOTE.equals(p.getStønadskontotype()))
                .findFirst();
        assertThat(avslåttMødrekvote).isPresent();
        assertThat(avslåttMødrekvote.get().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(avslåttMødrekvote.get().getManuellbehandlingårsak()).isNull();
        assertThat(avslåttMødrekvote.get().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().fødsel(fødselsdato);
    }

    @Test
    void skalKnekkePeriodenVedGrenseForSøknadsfrist() {
        var fødselsdato = LocalDate.of(2017, 11, 1);
        var mottattDato = fødselsdato.plusMonths(4).plusWeeks(1);
        var fpff = OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                null, false, PeriodeVurderingType.IKKE_VURDERT, mottattDato, mottattDato, null);
        //Mødrekvote skal knekkes på for at første delen skal avlås pga søknadsfrist
        var mødrekvote = OppgittPeriode.forVanligPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), null, false,
                PeriodeVurderingType.IKKE_VURDERT, mottattDato, mottattDato, null);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        // Act
        var resultat = fastsettPerioder(grunnlag);


        // Assert
        assertThat(resultat).hasSize(3);

        var førFødsel = resultat.get(0).getUttakPeriode();
        assertThat(førFødsel.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(førFødsel.getManuellbehandlingårsak()).isNull();
        assertThat(førFødsel.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);


        var mødrekvoteFørKnekk = resultat.get(1).getUttakPeriode();
        assertThat(mødrekvoteFørKnekk.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(mødrekvoteFørKnekk.getFom()).isEqualTo(fødselsdato);
        assertThat(mødrekvoteFørKnekk.getTom()).isEqualTo(LocalDate.of(2017, 11, 30));
        assertThat(mødrekvoteFørKnekk.getManuellbehandlingårsak()).isNull();
        assertThat(mødrekvoteFørKnekk.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);

        var mødrekvoteEtterKnekk = resultat.get(2).getUttakPeriode();
        assertThat(mødrekvoteEtterKnekk.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(mødrekvoteEtterKnekk.getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(mødrekvoteEtterKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(mødrekvoteEtterKnekk.getManuellbehandlingårsak()).isNull();
    }

    @Test
    void skal_ikke_innvilge_etter_eller_på_barnets_3årsdag_selv_om_det_er_nok_på_saldoen() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 10000000))
                .konto(konto(FEDREKVOTE, 0))
                .konto(konto(FELLESPERIODE, 0));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusYears(4))))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer);

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode frem til 3-årsdag (eksklusiv) innvilges
        //periode knekkes alltid knekt ved 6 uker pga regelflyt
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(3).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //resten av søknadsperide avslås
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(3));
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(4));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    void skal_ikke_innvilge_periode_som_starter_på_barnets_3årsdag_eller_som_starter_senere() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 10000000))
                .konto(konto(FEDREKVOTE, 15))
                .konto(konto(FELLESPERIODE, 10000));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusYears(3).minusDays(1)),
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusYears(3), fødselsdato.plusYears(4).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusYears(4), fødselsdato.plusYears(5))))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode frem til 3-årsdag (eksklusiv) innvilges
        //periode knekkes alltid knekt ved 6 uker pga regelflyt
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(3).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode som starter på 3årsdag avslås
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(3));
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(4).minusDays(1));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);

        //periode som starter etter 3årsdag avslås
        assertThat(resultat.get(4).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(4));
        assertThat(resultat.get(4).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(5));
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }


    @Test
    void skal_avslå_første_del_pga_manglende_omsorg_og_andre_del_pga_tom_på_kvote_siden_dager_trekkes() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(
                                new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusYears(1)))))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .build();

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);

        var uttakPeriode1 = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode1.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode1.getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(1));
        assertThat(uttakPeriode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode1.getManuellbehandlingårsak()).isNull();
        assertThat(uttakPeriode1.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);

        var uttakPeriode2 = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode2.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode2.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode2.getManuellbehandlingårsak()).isNull();
        assertThat(uttakPeriode2.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void hele_perioden_skal_avslås_automatisk_når_det_er_søkt_for_sent() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var periodeFom = fødselsdato.plusYears(1);
        var periodeTom = periodeFom.plusDays(20);
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 15));
        var fedrekvote = OppgittPeriode.forVanligPeriode(FEDREKVOTE, periodeFom, periodeTom, null, false,
                PeriodeVurderingType.IKKE_VURDERT, periodeTom.plusYears(2), periodeTom.plusYears(2), null);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
                .søknad(søknad(Søknadstype.FØDSEL, fedrekvote))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .build();

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(1);

        var uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(periodeFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(periodeTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    @Test
    void UT1290_skal_være_ugyldige_stønadskonto_hvis_søker_på_konto_som_man_ikke_har_tilgang_til() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(2).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(
                Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(resultat.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
        assertThat(resultat.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    void skal_knekke_periode_og_gå_til_manuell_behandling_ved_ikke_nok_flerbarnsdager() {
        var aktivitetIdentifikator = AktivitetIdentifikator.forArbeid(new Orgnummer("1234"), "12345");

        var fødselsdato = LocalDate.of(2019, 3, 13);
        var tom = Virkedager.plusVirkedager(fødselsdato.plusWeeks(6), 5);
        var annenPartPeriode1 = AnnenpartUttakPeriode.Builder.uttak(fødselsdato.minusWeeks(3),
                fødselsdato.minusDays(1))
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL,
                                new Trekkdager(15), Utbetalingsgrad.TEN))
                .build();
        var annenPartPeriode2 = AnnenpartUttakPeriode.Builder.uttak(fødselsdato,
                fødselsdato.plusWeeks(6).minusDays(1))
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.MØDREKVOTE, new Trekkdager(30),
                                Utbetalingsgrad.TEN))
                .build();
        var annenPartPeriode3 = AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(6), tom)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FELLESPERIODE,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), tom)), Utbetalingsgrad.TEN))
                .build();
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 50))
                .konto(konto(MØDREKVOTE, 50))
                .konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(FELLESPERIODE, 130))
                .flerbarnsdager(5);
        var periodeGrunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6), tom, true, null)))
                .annenPart(new AnnenPart.Builder().uttaksperiode(annenPartPeriode1)
                        .uttaksperiode(annenPartPeriode2)
                        .uttaksperiode(annenPartPeriode3))
                .build();

        var resultat = fastsettPerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(2);
        var førstePeriode = resultat.get(0).getUttakPeriode();
        assertThat(førstePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(førstePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(førstePeriode.getTom()).isEqualTo(tom.minusDays(1));

        var andrePeriode = resultat.get(1).getUttakPeriode();
        assertThat(andrePeriode.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(andrePeriode.getFom()).isEqualTo(tom);
        assertThat(andrePeriode.getTom()).isEqualTo(tom);
    }

    @Test
    void skal_håndtere_at_både_flerbarnsdager_og_foreldrepenger_kvote_går_tom_i_samme_søknadsperiode_på_forskjellig_dato() {
        var fødselsdato = LocalDate.of(2019, 3, 13);

        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                //Går først tom for foreldrepenger, deretter tom på flerbarnsdager
                .konto(konto(FORELDREPENGER, 75)).flerbarnsdager(150);
        var periodeGrunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(aleneomsorg())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                                false, null),
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(100), true, null)))
                .build();

        var resultat = fastsettPerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void mor_aleneomsorg_knekkes_riktig() {
        var fødselsdato = LocalDate.of(2022, 10, 4);

        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(FORELDREPENGER, 75)).farUttakRundtFødselDager(10);
        var periodeGrunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(false).farHarRett(false).aleneomsorg(true))
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                                false, null),
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(100), false, null)))
                .build();

        var resultat = fastsettPerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    void begge_rett_med_termin_fff_knekkes_riktig() {
        var fødselsdato = LocalDate.of(2022, 10, 4);

        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 75)).konto(konto(FEDREKVOTE, 75)).konto(konto(FELLESPERIODE, 80))
                .farUttakRundtFødselDager(10);
        var periodeGrunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato).termin(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1),
                                false, null),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1), false, null)))
                .build();
        var resultat = fastsettPerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    void skalIkkeKasteExceptionVedUtsettelseFraDerSaldoGårUt() {
        var fødselsdato = LocalDate.of(2018, 8, 20);
        var aktivitetIdentifikator = AktivitetIdentifikator.annenAktivitet();
        var kontoer = new Kontoer.Builder().konto(konto(MØDREKVOTE, 75)).konto(konto(FORELDREPENGER_FØR_FØDSEL, 15));
        var grunnlag = new RegelGrunnlag.Builder().datoer(datoer(fødselsdato))
                .behandling(morBehandling().kreverSammenhengendeUttak(true))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(
                                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 7, 30), LocalDate.of(2018, 8, 19)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2018, 8, 20), LocalDate.of(2018, 12, 2)))
                        .oppgittPeriode(utsettelsePeriode(LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31),
                                UtsettelseÅrsak.INNLAGT_SØKER))
                        .dokumentasjon(new Dokumentasjon.Builder().periodeMedInnleggelse(
                                new PeriodeMedInnleggelse(LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31)))))
                .inngangsvilkår(oppfyltAlleVilkår())
                .build();

        assertThatCode(() -> fastsettPerioder(grunnlag)).doesNotThrowAnyException();
    }

    @Test
    void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_mor() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15)).konto(konto(MØDREKVOTE, 50));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(13).minusDays(1))));


        var resultat = fastsettPerioder(grunnlag);

        var uttakPeriode = resultat.get(4).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void skal_filtrere_bort_perioden_som_kun_er_helg() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var periodeSomKunErHelg = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6).plusDays(5),
                fødselsdato.plusWeeks(6).plusDays(6));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15)).konto(konto(MØDREKVOTE, 50));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6).plusDays(4)),
                        periodeSomKunErHelg, oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8))));

        var resultat = fastsettPerioder(grunnlag);

        // Søker om 5 perioder, men den ene blir filtrert bort, derav 4
        assertThat(resultat).hasSize(4);
    }


    @Test
    void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_far() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 15)).konto(konto(FELLESPERIODE, 50));
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(21).minusDays(1), I_AKTIVITET));
        var søknad = søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1)),
                oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(10).minusDays(1)),
                oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1)),
                oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(21).minusDays(1)))
                .dokumentasjon(dokumentasjon);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad);


        var resultat = fastsettPerioder(grunnlag);

        var uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void skal_gå_regne_ubetalingsprosent_ut_fra_samtidig_uttaksprosent_hvis_ikke_gradering() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .konto(konto(MØDREKVOTE, 5 * 15));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), false,
                                new SamtidigUttaksprosent(50))));


        var resultat = fastsettPerioder(grunnlag);

        var periodeMedSamtidigUttak = resultat.get(2).getUttakPeriode();

        assertThat(periodeMedSamtidigUttak.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new Utbetalingsgrad(50));
    }

    @Test
    void skal_gå_regne_trekkdager_ut_fra_samtidig_uttaksprosent_hvis_ikke_gradering() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .konto(konto(MØDREKVOTE, 5 * 15));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), false,
                                new SamtidigUttaksprosent(80))));


        var resultat = fastsettPerioder(grunnlag);

        var periodeMedSamtidigUttak = resultat.get(2).getUttakPeriode();

        assertThat(periodeMedSamtidigUttak.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(8));
    }

    @Test
    void oppholdsperiode_skal_knekke_og_bevare_årsaken() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 100))
                .konto(konto(FEDREKVOTE, 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        //Går tom for fedrekvote i oppholdsperioden
                        .oppgittPeriode(OppgittPeriode.forOpphold(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8),
                                OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER, null, null)));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(2).getUttakPeriode().getOppholdÅrsak()).isEqualTo(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER);
        assertThat(resultat.get(3).getUttakPeriode().getOppholdÅrsak()).isEqualTo(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER);
    }

    @Test
    void skal_knekke_på_riktig_dato_når_konto_går_tom_og_periode_har_samtidig_uttak() {
        var fødselsdato = LocalDate.of(2019, 3, 19);
        var sisteUttaksdato = LocalDate.of(2019, 5, 13);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5)).konto(konto(MØDREKVOTE, 5));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2019, 2, 27), LocalDate.of(2019, 3, 18)),
                        oppgittPeriode(MØDREKVOTE, LocalDate.of(2019, 4, 19), sisteUttaksdato, false, new SamtidigUttaksprosent(80))));


        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat.get(resultat.size() - 1).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2019, 4, 30));
        assertThat(resultat.get(resultat.size() - 1).getUttakPeriode().getTom()).isEqualTo(sisteUttaksdato);
    }

    @Test
    void far_skal_kunne_få_overført_perioder_fra_mor_i_revurdering_med_endringsdato() {
        var fødselsdato = LocalDate.of(2019, 1, 24);
        var kontoer = new Kontoer.Builder().konto(konto(MØDREKVOTE, 75 - 28));
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(new Behandling.Builder().søkerErMor(true).berørtBehandling(true))
                .rettOgOmsorg(beggeRett())
                .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2019, 3, 4)))
                //far har fått overført mange dager av mor. Det er noen dager igjen til mor. Skal avslå alle mors perioder og ikke trekk dager
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 6))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                                        Stønadskontotype.MØDREKVOTE, new Trekkdager(3), Utbetalingsgrad.FULL))
                                .build())
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 3, 7), LocalDate.of(2019, 3, 31))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                                        Stønadskontotype.MØDREKVOTE, new Trekkdager(17), Utbetalingsgrad.FULL))
                                .build())
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 5, 3))
                                .innvilget(true)
                                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                                        Stønadskontotype.MØDREKVOTE, new Trekkdager(25), Utbetalingsgrad.FULL))
                                .build()))
                .søknad(
                        søknad(Søknadstype.FØDSEL, oppgittPeriode(MØDREKVOTE, LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 15))));


        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1).decimalValue()).isZero();
    }

    @Test
    void skal_knekke_på_riktig_dato_når_flerbarnskonto_går_tom() {
        var fødselsdato = LocalDate.of(2019, 3, 19);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .konto(konto(MØDREKVOTE, 20))
                .konto(konto(FELLESPERIODE, 20))
                .flerbarnsdager(5);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .datoer(datoer(fødselsdato))
                .behandling(farBehandling())
                .rettOgOmsorg(beggeRett())
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), LocalDate.of(2020, 5, 13), true, null)));


        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        //Flerbarnsdager går tom, trekkdager satt til 5 dager som er resten av flerbansdager kontoen
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(7).minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));

        //15 dager fellesperiode igjen, men ingen flerbarnsdager
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2020, 5, 13));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
    }

    @Test
    void skal_håndtere_flere_knekk_i_samme_søknadsperiode_ved_flere_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2019, 5, 28);
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2);
        var fastsattPeriode = new FastsattUttakPeriode.Builder().tidsperiode(fødselsdato.minusDays(1), fødselsdato.minusDays(1))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FORELDREPENGER,
                        arbeidsforhold1.getIdentifikator())));
        var vedtak = new Vedtak.Builder().leggTilPeriode(fastsattPeriode);
        var grunnlag = new RegelGrunnlag.Builder().arbeid(
                new Arbeid.Builder().arbeidsforhold(arbeidsforhold1).arbeidsforhold(arbeidsforhold2))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).aleneomsorg(true))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(6)))
                        .oppgittPeriode(
                                oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6).plusDays(1), fødselsdato.plusWeeks(15))))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
                .inngangsvilkår(oppfyltAlleVilkår())
                .revurdering(new Revurdering.Builder().gjeldendeVedtak(vedtak))
                .kontoer(new Kontoer.Builder().konto(konto(FORELDREPENGER, 50)));

        assertThatCode(() -> fastsettPerioder(grunnlag)).doesNotThrowAnyException();
    }

    @Test
    void søknadsfrist_ikke_trekke_dager_etter_at_konto_er_tom() {
        var fødselsdato = LocalDate.of(2018, 6, 14);
        var grunnlag = basicGrunnlag(fødselsdato).rettOgOmsorg(aleneomsorg())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusYears(1))))
                .kontoer(new Kontoer.Builder().konto(konto(FORELDREPENGER, 50)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(30));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(20));
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    void søknadsfrist_ikke_trekke_dager_etter_at_konto_er_tom_manglende_søkt_periode() {
        var fødselsdato = LocalDate.of(2018, 6, 14);
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusYears(1), fødselsdato.plusYears(1))))
                .kontoer(new Kontoer.Builder().konto(konto(FORELDREPENGER, 50)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        //fram til tom på konto
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(50));
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        //mellom tom på konto og første søkte
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        //første søkte
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void utsettelse_som_går_til_manuell_behandling_skal_ikke_trekke_dager_hvis_dager_igjen_på_bare_ett_av_to_arbeidsforhold_selv_om_sluttpunkt_trekker_dager() {
        var fødselsdato = LocalDate.of(2018, 6, 14);
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1).leggTilEndringIStilling(
                new EndringAvStilling(fødselsdato, BigDecimal.valueOf(50)));
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2).leggTilEndringIStilling(
                new EndringAvStilling(fødselsdato, BigDecimal.valueOf(25)));
        var grunnlag = basicGrunnlag(fødselsdato)
                .behandling(morBehandling().kreverSammenhengendeUttak(true))
                .rettOgOmsorg(aleneomsorg())
                //50% prosent stilling, men søker utsettelse. Går til manuell behandling
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold1).arbeidsforhold(arbeidsforhold2))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .oppgittPeriode(
                                gradertoppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7).minusDays(1),
                                        BigDecimal.valueOf(50), Set.of(arbeidsforhold1.getIdentifikator())))
                        .oppgittPeriode(
                                utsettelsePeriode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(100), UtsettelseÅrsak.ARBEID)))
                .kontoer(new Kontoer.Builder().konto(konto(FORELDREPENGER, 35)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(2).getUttakPeriode().getManuellbehandlingårsak()).isNotNull();
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(2).getUttakPeriode().getStønadskontotype()).isNull();
    }

    @Test
    void innvilge_foreldrepenger_14_uker_før_fødsel_men_ikke_12_uker_før_termin_ved_terminsøknad() {
        var termin = LocalDate.of(2020, 6, 10);
        var testGrunnlag = basicGrunnlag()
                .datoer(new Datoer.Builder().termin(termin).fødsel(termin.plusWeeks(2)))
                .rettOgOmsorg(aleneomsorg())
                .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(1000)))
                .søknad(new Søknad.Builder().type(Søknadstype.TERMIN)
                        .oppgittePerioder(List.of(OppgittPeriode.forVanligPeriode(FORELDREPENGER, termin.minusWeeks(15),
                                termin.minusWeeks(3).minusDays(1), null, false, PeriodeVurderingType.IKKE_VURDERT, null, null, null),
                                OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, termin.minusWeeks(3), termin.minusDays(1),
                                        null, false, PeriodeVurderingType.IKKE_VURDERT, null, null, null),
                                OppgittPeriode.forVanligPeriode(FORELDREPENGER, termin, termin.plusWeeks(4), null, false,
                                        PeriodeVurderingType.IKKE_VURDERT, null, null, null))));

        var resultater = fastsettPerioder(testGrunnlag);

        assertThat(resultater.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultater.get(0).getUttakPeriode().getFom()).isEqualTo(termin.minusWeeks(15));
        assertThat(resultater.get(0).getUttakPeriode().getTom()).isEqualTo(termin.minusWeeks(12).minusDays(1));
        assertThat(resultater.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultater.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(resultater.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }

    @Test
    public void skal_innvilge_med_privatperson_som_arbeidsgiver() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var aktivitetIdentifikator = AktivitetIdentifikator.forArbeid(new AktørId("123"), "345");
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        gradertoppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), BigDecimal.TEN,
                                Set.of(aktivitetIdentifikator))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat.stream()).allMatch(p -> p.getUttakPeriode().getPerioderesultattype().equals(Perioderesultattype.INNVILGET));
    }

    @Test
    public void skal_delvis_innvilge_med_periode_før_etter_start_ny_stønadsperiode() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2021, 1, 1));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
            .rettOgOmsorg(beggeRett())
            .behandling(farBehandling())
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FEDREKVOTE, nesteStønadsperiode.minusWeeks(5), nesteStønadsperiode.plusWeeks(5).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode).map(UttakPeriode::getPerioderesultattype).filter(INNVILGET::equals).count()).isEqualTo(1);
        var avslått = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode).filter(up -> AVSLÅTT.equals(up.getPerioderesultattype())).findFirst().orElseThrow();
        assertThat(avslått.getFom()).isEqualTo(nesteStønadsperiode);
        assertThat(avslått.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);
    }

    @Test
    public void skal_innvilge_med_periode_før_start_ny_stønadsperiode() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2021, 1, 1));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
            .rettOgOmsorg(beggeRett())
            .behandling(farBehandling())
            .kontoer(defaultKontoer())
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FEDREKVOTE, nesteStønadsperiode.minusWeeks(18), nesteStønadsperiode.minusWeeks(8).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void skal_avslå_med_periode_etter_start_ny_stønadsperiode() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2021, 1, 1));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
            .rettOgOmsorg(beggeRett())
            .behandling(farBehandling())
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FEDREKVOTE, nesteStønadsperiode.plusWeeks(1), nesteStønadsperiode.plusWeeks(11).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);
    }

    @Test
    public void to_tette_skal_innvilge_med_periode_etter_start_ny_stønadsperiode_gjenstående_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 8, 2));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2023, 4, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(15*5))
                        .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(16*5)).etterNesteStønadsperiodeDager(22 * 5))
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1)),
                        oppgittPeriode(FELLESPERIODE, nesteStønadsperiode.minusWeeks(4), nesteStønadsperiode.plusWeeks(12).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(5);
        assertThat(resultat.stream().filter(r -> Perioderesultattype.AVSLÅTT.equals(r.getUttakPeriode().getPerioderesultattype())).count()).isEqualTo(1);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(nesteStønadsperiode);
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(nesteStønadsperiode.plusWeeks(3).minusDays(1));
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(4).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);
    }

    @Test
    public void to_tette_skal_avslå_med_periode_etter_start_ny_stønadsperiode_oppbrukt_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 8, 2));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2023, 4, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
                .rettOgOmsorg(beggeRett())
                .behandling(farBehandling())
                .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(15*5))
                        .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(16*5)).minsterettDager(8 * 5).etterNesteStønadsperiodeDager(8 * 5))
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FEDREKVOTE, nesteStønadsperiode.minusWeeks(12), nesteStønadsperiode.minusWeeks(2).minusDays(1)),
                        oppgittPeriode(FEDREKVOTE, nesteStønadsperiode, nesteStønadsperiode.plusWeeks(5).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.stream().filter(r -> Perioderesultattype.AVSLÅTT.equals(r.getUttakPeriode().getPerioderesultattype())).count()).isEqualTo(1);
        // Periode 0 er 8 uker FK innenfor minsteretten. Det var søkt om 10 uker men perioden blir knekt etter 8 uker pga minsterett
        // Periode 1 er 2 uker FK utenfor minsteretten. Det var søkt om 10 uker og dette er restperioden på 2 uker
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(nesteStønadsperiode.minusWeeks(4));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(nesteStønadsperiode.minusWeeks(2).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);
    }

    @Test
    public void to_tette_bfhr_redusert_minsterett_oppbrukt_før_start_ny_sak() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 8, 2));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2023, 4, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
                .rettOgOmsorg(bareFarRett())
                .behandling(farBehandling())
                .kontoer(new Kontoer.Builder()
                        .konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(40*5)).minsterettDager(15 * 5).etterNesteStønadsperiodeDager(8 * 5))
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER, nesteStønadsperiode.minusWeeks(12), nesteStønadsperiode.minusWeeks(2).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, nesteStønadsperiode, nesteStønadsperiode.plusWeeks(5).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(resultat.stream().filter(r -> IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT.equals(r.getUttakPeriode().getPeriodeResultatÅrsak())).count()).isEqualTo(2); //MSP
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(nesteStønadsperiode.minusWeeks(12));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(nesteStønadsperiode.minusWeeks(2).minusDays(1));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);
    }

    @Test
    public void to_tette_bfhr_redusert_minsterett_oppbrukt_etter_start_ny_sak() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 8, 2));
        var nesteStønadsperiode = Virkedager.justerHelgTilMandag(LocalDate.of(2023, 4, 1));
        var grunnlag = basicGrunnlag().datoer(datoer(fødselsdato).startdatoNesteStønadsperiode(nesteStønadsperiode))
                .rettOgOmsorg(bareFarRett())
                .behandling(farBehandling())
                .kontoer(new Kontoer.Builder()
                        .konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(40*5)).minsterettDager(15 * 5).etterNesteStønadsperiodeDager(8 * 5))
                .søknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER, nesteStønadsperiode.minusWeeks(12), nesteStønadsperiode.minusWeeks(6).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, nesteStønadsperiode.plusWeeks(8), nesteStønadsperiode.plusWeeks(9).minusDays(1)),
                        oppgittPeriode(FORELDREPENGER, nesteStønadsperiode.plusWeeks(15), nesteStønadsperiode.plusWeeks(18).minusDays(1))));

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(8);
        assertThat(resultat.stream().filter(r -> IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT.equals(r.getUttakPeriode().getPeriodeResultatÅrsak())).count()).isEqualTo(2); //MSP
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(nesteStønadsperiode.minusWeeks(12));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(nesteStønadsperiode.minusWeeks(6).minusDays(1));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE); // MSP
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(4).getUttakPeriode().getFom()).isEqualTo(nesteStønadsperiode.plusWeeks(8));
        assertThat(resultat.get(4).getUttakPeriode().getTom()).isEqualTo(nesteStønadsperiode.plusWeeks(9).minusDays(1));
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(5).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);  // MSP
        assertThat(resultat.get(6).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(6).getUttakPeriode().getFom()).isEqualTo(nesteStønadsperiode.plusWeeks(15));
        assertThat(resultat.get(6).getUttakPeriode().getTom()).isEqualTo(nesteStønadsperiode.plusWeeks(16).minusDays(1));
        assertThat(resultat.get(7).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(7).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE); // Vanlig avslag
    }

    @Test
    void bare_far_har_rett_skal_innvilge_to_uker_rundt_fødsel_deretter_minsterett() {
        //Søkt samme dag, men mor har søkt etter far
        var fødselsdato = LocalDate.of(2022, 10, 6);
        var termindato = LocalDate.of(2022, 10, 5);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato).termin(termindato))
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder()
                        .konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(5*40))
                        .minsterettDager(5*8)
                        .farUttakRundtFødselDager(10))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, termindato.minusDays(2), fødselsdato.plusWeeks(2).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(3).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(50), fødselsdato.plusWeeks(58).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                );

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(7);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Før fødsel P1
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(3));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Etter Fødsel P1
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(10));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Etter Fødsel P2
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT); // MSP
        assertThat(resultat.get(4).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT); // MSP
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Minsterett
        assertThat(resultat.get(5).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(22)); // Minsterett
        assertThat(resultat.get(6).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN); // Oppbrukt minsterett
    }

    @Test
    void bare_far_har_rett_skal_innvilge_minsterett_men_ikke_etter_barns_død_selv_om_dager_igjen() {
        //Søkt samme dag, men mor har søkt etter far
        var fødselsdato = LocalDate.of(2022, 10, 6);
        var dødsdato = fødselsdato.plusWeeks(10);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato).termin(fødselsdato).dødsdatoer(new Dødsdatoer.Builder().barnsDødsdato(dødsdato).alleBarnDøde(true)))
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder()
                        .konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(5*40))
                        .minsterettDager(5*8)
                        .farUttakRundtFødselDager(10))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(4).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(17).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(25), fødselsdato.plusWeeks(29).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                );

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(6);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Før fødsel P1
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(10));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT); // MSP fom uke 6
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager((15-6)*5));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Etter Fødsel P2
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING); // Fom dødsdato + 6 uker
        assertThat(resultat.get(3).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.DØDSFALL);
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.BARN_DØD);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(resultat.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
    }

    @Test
    void bare_far_har_rett_skal_innvilge_minsterett_men_ikke_etter_ny_stønad_selv_om_dager_igjen() {
        //Søkt samme dag, men mor har søkt etter far
        var fødselsdato = LocalDate.of(2022, 10, 6);
        var nestesakStartDato = fødselsdato.plusWeeks(41);  // TOTETTE <= 45 uker (stønadsperiode begynner 3 uker før fødsel). OBS neste sak kan begyynne langt senere
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(datoer(fødselsdato).termin(fødselsdato).startdatoNesteStønadsperiode(nestesakStartDato))
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarRett())
                .kontoer(new Kontoer.Builder()
                        .konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(5*40))
                        .minsterettDager(5*15).etterNesteStønadsperiodeDager(8*5)
                        .farUttakRundtFødselDager(10))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(24).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                        .oppgittPeriode(OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(40), fødselsdato.plusWeeks(43).minusDays(1),
                                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                                null))
                );

        var resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(5);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT); // MSP
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager((15-6)*5));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Uttak 9 uker
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager((24-15)*5));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT); // MSP
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager((40-24)*5));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET); // Fom ny stønad - en uke mellom fom og ny stønad
        assertThat(resultat.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(4).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
    }

}
