package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.AVSLÅTT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.EndringAvStilling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class OrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void skal_innvilge_to_perioder_med_med_mødrekvote_på_under_10_uker() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))
                ));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .forEach(uttakPeriode -> assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET));
    }

    @Test
    public void skal_knekke_mødrekvote_dersom_det_ikke_er_flere_dager_igjen() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 3);

        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(12).minusDays(1))
                ));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());

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
    public void mødrekvoteMedUtsattOppstartUtenGyldigGrunnSkalTrekkeDagerPåSaldo() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 8);
        LocalDate sluttGyldigUtsattPeriode = fødselsdato.plusDays(6);
        LocalDate startUgyldigPeriode = fødselsdato.plusDays(7);
        LocalDate sluttUgyldigPeriode = startUgyldigPeriode.plusDays(4);

        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, sluttUgyldigPeriode.plusDays(1), sluttUgyldigPeriode.plusWeeks(10)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(fødselsdato, sluttGyldigUtsattPeriode))));

        RegelGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(fastsettePeriodeGrunnlag);
        var uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());
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
        assertThat(gyldigUtsettelsePeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);


        var ugyldigUtsettelsePeriode = uttakPerioder.get(2);
        assertThat(ugyldigUtsettelsePeriode.getFom()).isEqualTo(sluttGyldigUtsattPeriode.plusDays(1));
        assertThat(ugyldigUtsettelsePeriode.getTom()).isEqualTo(sluttUgyldigPeriode);
        assertThat(ugyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(ugyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(ugyldigUtsettelsePeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);

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
    public void delvisUgyldigUtsattMødrekvote() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 8);
        LocalDate gyldigUtsettelseStart = fødselsdato.plusDays(5);
        LocalDate gyldigUtsettelseSlutt = fødselsdato.plusDays(10);

        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, gyldigUtsettelseSlutt.plusDays(1), fødselsdato.plusWeeks(6).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(gyldigUtsettelseStart, gyldigUtsettelseSlutt))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        var uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);

        // Første del av msp blir manuell behandling
        var ugyldigUtsattPeriode = uttakPerioder.get(1);
        assertThat(ugyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(ugyldigUtsattPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(ugyldigUtsattPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(ugyldigUtsattPeriode.getTom()).isEqualTo(fødselsdato.plusDays(4));
        assertThat(ugyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        var gyldigUtsattPeriode = uttakPerioder.get(2);
        assertThat(gyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(gyldigUtsattPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
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
    public void helePeriodenUtenforSøknadsfrist() {
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        grunnlag.medDatoer(datoer(fødselsdato, fødselsdato.plusWeeks(6)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                ));

        // Act
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        // Assert
        assertThat(resultat).hasSize(2);

        var førFødsel = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> FORELDREPENGER_FØR_FØDSEL.equals(p.getStønadskontotype())).findFirst();
        assertThat(førFødsel).isPresent();
        assertThat(førFødsel.get().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(førFødsel.get().getManuellbehandlingårsak()).isNull();
        assertThat(førFødsel.get().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);


        var mødrekvote = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> MØDREKVOTE.equals(p.getStønadskontotype())).findFirst();
        assertThat(mødrekvote).isPresent();
        assertThat(mødrekvote.get().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(mødrekvote.get().getManuellbehandlingårsak()).isNull();
        assertThat(mødrekvote.get().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato, LocalDate førsteLovligeUttaksdag) {
        return new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag);
    }

    @Test
    public void skalKnekkePeriodenVedGrenseForSøknadsfrist() {
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        LocalDate lovligeUttaksdag = fødselsdato.plusWeeks(1);
        grunnlag.medDatoer(datoer(fødselsdato, lovligeUttaksdag))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                ));

        // Act
        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);


        // Assert
        assertThat(resultat).hasSize(3);

        var førFødsel = resultat.get(0).getUttakPeriode();
        assertThat(førFødsel.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(førFødsel.getManuellbehandlingårsak()).isNull();
        assertThat(førFødsel.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);


        var mødrekvoteFørKnekk = resultat.get(1).getUttakPeriode();
        assertThat(mødrekvoteFørKnekk.getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(mødrekvoteFørKnekk.getFom()).isEqualTo(fødselsdato);
        assertThat(mødrekvoteFørKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(mødrekvoteFørKnekk.getManuellbehandlingårsak()).isNull();
        assertThat(mødrekvoteFørKnekk.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);

        var mødrekvoteEtterKnekk = resultat.get(2).getUttakPeriode();
        assertThat(mødrekvoteEtterKnekk.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(mødrekvoteEtterKnekk.getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(mødrekvoteEtterKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(mødrekvoteEtterKnekk.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void skal_ikke_innvilge_etter_eller_på_barnets_3årsdag_selv_om_det_er_nok_på_saldoen() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 10000000))
                .leggTilKonto(konto(FEDREKVOTE, 0))
                .leggTilKonto(konto(FELLESPERIODE, 0));
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create().medDatoer(datoer(fødselsdato, LocalDate.of(2017, 10, 1)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusYears(4))
                ))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer);

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
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
    public void skal_ikke_innvilge_periode_som_starter_på_barnets_3årsdag_eller_som_starter_senere() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 10000000))
                .leggTilKonto(konto(FEDREKVOTE, 15))
                .leggTilKonto(konto(FELLESPERIODE, 10000));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(datoer(fødselsdato, LocalDate.of(2017, 10, 1)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusYears(3).minusDays(1)),
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusYears(3), fødselsdato.plusYears(4).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusYears(4), fødselsdato.plusYears(5))
                ))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
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
    public void skal_avslå_første_del_pga_manglende_omsorg_og_andre_del_pga_tom_på_kvote_siden_dager_trekkes() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FEDREKVOTE, 5));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeUtenOmsorg(new PeriodeUtenOmsorg(fødselsdato, fødselsdato.plusYears(1)))))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

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
    public void hele_perioden_skal_avslås_automatisk_når_det_er_søkt_for_sent() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate periodeFom = fødselsdato.plusYears(1);
        LocalDate periodeTom = periodeFom.plusDays(20);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FEDREKVOTE, 15));
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(datoer(fødselsdato, fødselsdato.plusYears(2)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medSøknad(søknad(Søknadstype.FØDSEL, oppgittPeriode(FEDREKVOTE, periodeFom, periodeTom)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(1);

        var uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(periodeFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(periodeTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKNADSFRIST);
    }

    @Test
    public void UT1290_skal_være_ugyldige_stønadskonto_hvis_søker_på_konto_som_man_ikke_har_tilgang_til() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(2).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(resultat.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isNotZero();
        assertThat(resultat.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    public void skal_knekke_periode_og_gå_til_manuell_behandling_ved_ikke_nok_flerbarnsdager() {
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forArbeid("1234", "12345");

        LocalDate fødselsdato = LocalDate.of(2019, 3, 13);
        LocalDate tom = Virkedager.plusVirkedager(fødselsdato.plusWeeks(6), 5);
        AnnenpartUttakPeriode annenPartPeriode1 = AnnenpartUttakPeriode.Builder.uttak(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, new Trekkdager(15), BigDecimal.TEN))
                .build();
        AnnenpartUttakPeriode annenPartPeriode2 = AnnenpartUttakPeriode.Builder.uttak(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.MØDREKVOTE, new Trekkdager(30), BigDecimal.TEN))
                .build();
        AnnenpartUttakPeriode annenPartPeriode3 = AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(6), tom)
                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FELLESPERIODE,
                        new Trekkdager(Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), tom)), BigDecimal.TEN))
                .build();
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FEDREKVOTE, 50))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(FELLESPERIODE, 130))
                .leggTilKonto(konto(FLERBARNSDAGER, 5));
        RegelGrunnlag periodeGrunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(Søknadstype.FØDSEL, oppgittPeriode(Stønadskontotype.FEDREKVOTE, fødselsdato.plusWeeks(6), tom, true, null)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(annenPartPeriode1)
                        .leggTilUttaksperiode(annenPartPeriode2)
                        .leggTilUttaksperiode(annenPartPeriode3))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(2);
        var førstePeriode = resultat.get(0).getUttakPeriode();
        assertThat(førstePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(førstePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(førstePeriode.getTom()).isEqualTo(tom.minusDays(1));

        var andrePeriode = resultat.get(1).getUttakPeriode();
        assertThat(andrePeriode.getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(andrePeriode.getFom()).isEqualTo(tom);
        assertThat(andrePeriode.getTom()).isEqualTo(tom);
    }

    @Test
    public void skal_håndtere_at_både_flerbarnsdager_og_foreldrepenger_kvote_går_tom_i_samme_søknadsperiode_på_forskjellig_dato() {
        LocalDate fødselsdato = LocalDate.of(2019, 3, 13);

        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                //Går først tom for foreldrepenger, deretter tom på flerbarnsdager
                .leggTilKonto(konto(FORELDREPENGER, 75))
                .leggTilKonto(konto(FLERBARNSDAGER, 150));
        RegelGrunnlag periodeGrunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(aleneomsorg())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), false, null),
                        oppgittPeriode(Stønadskontotype.FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(100), true, null)
                ))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
    }

    @Test
    public void skalIkkeKasteExceptionVedUtsettelseFraDerSaldoGårUt() {
        LocalDate fødselsdato = LocalDate.of(2018, 8, 20);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.annenAktivitet();
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(MØDREKVOTE, 75))
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15));
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder()
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .medMottattDato(fødselsdato.minusWeeks(4))
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 7, 30), LocalDate.of(2018, 8, 19)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2018, 8, 20), LocalDate.of(2018, 12, 2)))
                        .leggTilOppgittPeriode(utsettelsePeriode(LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31), UtsettelseÅrsak.INNLAGT_SØKER))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPeriodeMedInnleggelse(new PeriodeMedInnleggelse(LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31)))))
                .medInngangsvilkår(oppfyltAlleVilkår())
                .build();

        assertThatCode(() -> fastsettPerioder(grunnlag)).doesNotThrowAnyException();
    }

    @Test
    public void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_mor() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(13).minusDays(1))
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var uttakPeriode = resultat.get(4).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_filtrere_bort_perioden_som_kun_er_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var periodeSomKunErHelg = oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6).plusDays(5), fødselsdato.plusWeeks(6).plusDays(6));
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(6).plusDays(4)),
                        periodeSomKunErHelg,
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8))
                ));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        // Søker om 5 perioder, men den ene blir filtrert bort, derav 4
        assertThat(resultat).hasSize(4);
    }


    @Test
    public void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_far() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FEDREKVOTE, 15))
                .leggTilKonto(konto(FELLESPERIODE, 50));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL, oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1)),
                        oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(10).minusDays(1)),
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1)),
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(21).minusDays(1))
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_gå_regne_ubetalingsprosent_ut_fra_samtidig_uttaksprosent_hvis_ikke_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 5 * 15));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                                false, new SamtidigUttaksprosent(50))
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var periodeMedSamtidigUttak = resultat.get(2).getUttakPeriode();

        assertThat(periodeMedSamtidigUttak.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new Utbetalingsgrad(50));
    }

    @Test
    public void skal_gå_regne_trekkdager_ut_fra_samtidig_uttaksprosent_hvis_ikke_gradering() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 5 * 15));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                                false, new SamtidigUttaksprosent(80))
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var periodeMedSamtidigUttak = resultat.get(2).getUttakPeriode();

        assertThat(periodeMedSamtidigUttak.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(8));
    }

    @Test
    public void skal_gå_til_manuell_behandling_når_søker_er_tom_for_sine_konto_men_søkt_om_overføring() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FEDREKVOTE, 15))
                .leggTilKonto(konto(MØDREKVOTE, 15));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(OppgittPeriode.forOverføring(MØDREKVOTE, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(11).minusDays(1),
                                PeriodeKilde.SØKNAD, PeriodeVurderingType.PERIODE_OK, OverføringÅrsak.SYKDOM_ELLER_SKADE))
                        .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1)))
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(11).minusDays(1)))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        var uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til manuell behandling
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_OVERFØRING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isNull();
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(10));
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.ZERO);
    }

    @Test
    public void oppholdsperiode_skal_knekke_og_bevare_årsaken() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 100))
                .leggTilKonto(konto(FEDREKVOTE, 5));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        //Går tom for fedrekvote i oppholdsperioden
                        .leggTilOppgittPeriode(OppgittPeriode.forOpphold(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8),
                                PeriodeKilde.SØKNAD, OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(2).getUttakPeriode().getOppholdÅrsak()).isEqualTo(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER);
        assertThat(resultat.get(3).getUttakPeriode().getOppholdÅrsak()).isEqualTo(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER);
    }

    @Test
    public void skal_knekke_på_riktig_dato_når_konto_går_tom_og_periode_har_samtidig_uttak() {
        LocalDate fødselsdato = LocalDate.of(2019, 3, 19);
        LocalDate sisteUttaksdato = LocalDate.of(2019, 5, 13);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 5));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2019, 2, 27), LocalDate.of(2019, 3, 18)),
                        oppgittPeriode(MØDREKVOTE, LocalDate.of(2019, 4, 19), sisteUttaksdato,
                                false, new SamtidigUttaksprosent(80))
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat.get(resultat.size() - 1).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2019, 4, 30));
        assertThat(resultat.get(resultat.size() - 1).getUttakPeriode().getTom()).isEqualTo(sisteUttaksdato);
    }

    @Test
    public void far_skal_kunne_få_overført_perioder_fra_mor_i_revurdering_med_endringsdato() {
        var fødselsdato = LocalDate.of(2019, 1, 24);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(MØDREKVOTE, 75 - 28));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .medErTapende(true))
                .medRettOgOmsorg(beggeRett())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(LocalDate.of(2019, 3, 4)))
                //far har fått overført mange dager av mor. Det er noen dager igjen til mor. Skal avslå alle mors perioder og ikke trekk dager
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 6))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.MØDREKVOTE, new Trekkdager(3), BigDecimal.valueOf(100)))
                                .build())
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 3, 7), LocalDate.of(2019, 3, 31))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.MØDREKVOTE, new Trekkdager(17), BigDecimal.valueOf(100)))
                                .build())
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 5, 3))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), Stønadskontotype.MØDREKVOTE, new Trekkdager(25), BigDecimal.valueOf(100)))
                                .build()))
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        oppgittPeriode(MØDREKVOTE, LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 15))
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1).decimalValue()).isZero();
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD_1).decimalValue()).isZero();
    }

    @Test
    public void skal_knekke_på_riktig_dato_når_flerbarnskonto_går_tom() {
        LocalDate fødselsdato = LocalDate.of(2019, 3, 19);
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 3 * 5))
                .leggTilKonto(konto(MØDREKVOTE, 20))
                .leggTilKonto(konto(FELLESPERIODE, 20))
                .leggTilKonto(konto(FLERBARNSDAGER, 5));
        grunnlag = RegelGrunnlagTestBuilder.create()
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medDatoer(datoer(fødselsdato, førsteLovligeUttaksdag(fødselsdato)))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad(
                        Søknadstype.FØDSEL,
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), LocalDate.of(2020, 5, 13), true, null)
                ));


        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        //Flerbarnsdager går tom, trekkdager satt til 5 dager som er resten av flerbansdager kontoen
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(7).minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(5));

        //15 dager fellesperiode igjen
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(7));
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(15));

        //Resten blir avslått
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2020, 5, 13));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(0));
    }

    @Test
    public void skal_håndtere_flere_knekk_i_samme_søknadsperiode_ved_flere_arbeidsforhold() {
        LocalDate fødselsdato = LocalDate.of(2019, 5, 28);
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2);
        var fastsattPeriode = new FastsattUttakPeriode.Builder()
                .medTidsperiode(fødselsdato.minusDays(1), fødselsdato.minusDays(1))
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FORELDREPENGER, arbeidsforhold1.getIdentifikator())));
        var vedtak = new Vedtak.Builder().leggTilPeriode(fastsattPeriode);
        RegelGrunnlag.Builder grunnlag = new RegelGrunnlag.Builder()
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(arbeidsforhold1)
                        .leggTilArbeidsforhold(arbeidsforhold2))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medAleneomsorg(true))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(6)))
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6).plusDays(1), fødselsdato.plusWeeks(15)))
                )
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato))
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medRevurdering(new Revurdering.Builder().medGjeldendeVedtak(vedtak))
                .medKontoer(new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 50)));

        assertThatCode(() -> fastsettPerioder(grunnlag)).doesNotThrowAnyException();
    }

    @Test
    public void søknadsfrist_ikke_trekke_dager_etter_at_konto_er_tom() {
        var fødselsdato = LocalDate.of(2018, 6, 14);
        var grunnlag = basicGrunnlag(fødselsdato)
                .medRettOgOmsorg(aleneomsorg())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(fødselsdato.plusYears(1).plusDays(1)))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusYears(1)))
                )
                .medKontoer(new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 50)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(30));
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(20));
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void søknadsfrist_ikke_trekke_dager_etter_at_konto_er_tom_manglende_søkt_periode() {
        var fødselsdato = LocalDate.of(2018, 6, 14);
        var grunnlag = basicGrunnlag(fødselsdato)
                .medRettOgOmsorg(aleneomsorg())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(fødselsdato.plusYears(1).plusDays(1)))
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusYears(1), fødselsdato.plusYears(1)))
                )
                .medKontoer(new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 50)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);
        //første 6
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(30));
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        //fram til tom på konto
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(new Trekkdager(20));
        assertThat(resultat.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
        //mellom tom på konto og første søkte
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(2).getUttakPeriode().getStønadskontotype()).isNull();
        //første søkte
        assertThat(resultat.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void utsettelse_som_går_til_manuell_behandling_skal_ikke_trekke_dager_hvis_dager_igjen_på_bare_ett_av_to_arbeidsforhold_selv_om_sluttpunkt_trekker_dager() {
        var fødselsdato = LocalDate.of(2018, 6, 14);
        var arbeidsforhold1 = new Arbeidsforhold(ARBEIDSFORHOLD_1).leggTilEndringIStilling(new EndringAvStilling(fødselsdato, BigDecimal.valueOf(50)));
        var arbeidsforhold2 = new Arbeidsforhold(ARBEIDSFORHOLD_2).leggTilEndringIStilling(new EndringAvStilling(fødselsdato, BigDecimal.valueOf(25)));
        var grunnlag = basicGrunnlag(fødselsdato)
                .medRettOgOmsorg(aleneomsorg())
                //50% prosent stilling, men søker utsettelse. Går til manuell behandling
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold1).leggTilArbeidsforhold(arbeidsforhold2))
                .medSøknad(new Søknad.Builder()
                        .medMottattDato(fødselsdato.minusWeeks(2))
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(gradertoppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7).minusDays(1),
                                BigDecimal.valueOf(50), Set.of(arbeidsforhold1.getIdentifikator())))
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(100), UtsettelseÅrsak.ARBEID))
                )
                .medKontoer(new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 35)));

        var resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(2).getUttakPeriode().getManuellbehandlingårsak()).isNotNull();
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_1)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD_2)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(2).getUttakPeriode().getStønadskontotype()).isNull();
    }

    @Test
    public void innvilge_foreldrepenger_14_uker_før_fødsel_men_ikke_12_uker_før_termin_ved_terminsøknad() {
        var termin = LocalDate.of(2020, 6, 10);
        var testGrunnlag =
                grunnlag.medDatoer(new Datoer.Builder()
                            .medFørsteLovligeUttaksdag(termin.minusYears(1))
                            .medTermin(termin)
                            .medFødsel(termin.plusWeeks(2)))
                        .medRettOgOmsorg(aleneomsorg())
                        .medKontoer(new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(1000)))
                        .medSøknad(new Søknad.Builder()
                                .medType(Søknadstype.TERMIN)
                                .medOppgittePerioder(List.of(
                                        OppgittPeriode.forVanligPeriode(FORELDREPENGER, termin.minusWeeks(15), termin.minusWeeks(3).minusDays(1),
                                                PeriodeKilde.SØKNAD, null, false, PeriodeVurderingType.IKKE_VURDERT),
                                        OppgittPeriode.forVanligPeriode(FORELDREPENGER_FØR_FØDSEL, termin.minusWeeks(3), termin.minusDays(1),
                                                PeriodeKilde.SØKNAD, null, false, PeriodeVurderingType.IKKE_VURDERT),
                                        OppgittPeriode.forVanligPeriode(FORELDREPENGER, termin, termin.plusWeeks(4),
                                                PeriodeKilde.SØKNAD, null, false, PeriodeVurderingType.IKKE_VURDERT)
                                )));

        List<FastsettePeriodeResultat> resultater = fastsettPerioder(testGrunnlag);

        assertThat(resultater.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(AVSLÅTT);
        assertThat(resultater.get(0).getUttakPeriode().getFom()).isEqualTo(termin.minusWeeks(15));
        assertThat(resultater.get(0).getUttakPeriode().getTom()).isEqualTo(termin.minusWeeks(12).minusDays(1));
        assertThat(resultater.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultater.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        assertThat(resultater.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
    }
}
