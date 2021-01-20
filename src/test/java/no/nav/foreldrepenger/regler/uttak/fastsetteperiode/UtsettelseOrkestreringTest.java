package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedHV;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedTiltakIRegiAvNav;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

public class UtsettelseOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void periode_med_gyldig_utsettelse_pga_barn_innlagt_i_helseinstitusjon_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        UtsettelseÅrsak.INNLAGT_BARN))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(
                        new PeriodeMedBarnInnlagt(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.INNLAGT_BARN);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_søker_innlagt_i_helseinstitusjon_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        UtsettelseÅrsak.INNLAGT_SØKER))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedInnleggelse(
                        new PeriodeMedInnleggelse(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.INNLAGT_SØKER);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_søkers_sykdom_eller_skade_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        UtsettelseÅrsak.SYKDOM_SKADE))
                .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedSykdomEllerSkade(
                        new PeriodeMedSykdomEllerSkade(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.SYKDOM_SKADE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_arbeid_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate utsettelseFom = fødselsdato.plusWeeks(10);
        LocalDate utsettelseTom = fødselsdato.plusWeeks(12).minusDays(1);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 75))
                .leggTilKonto(konto(FEDREKVOTE, 75))
                .leggTilKonto(konto(FELLESPERIODE, 80));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD).leggTilEndringIStilling(
                new EndringAvStilling(fødselsdato, BigDecimal.valueOf(100)));
        basicUtsettelseGrunnlag(fødselsdato).medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .medKontoer(kontoer)
                .medSøknad(fødselSøknad().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, utsettelseFom.minusDays(1)))
                        .leggTilOppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, UtsettelseÅrsak.ARBEID)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.ARBEID);
        assertThat(uttakPeriode.getFom()).isEqualTo(utsettelseFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(utsettelseTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(arbeidsforhold.getIdentifikator())).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void UT1110_periode_med_utsettelse_pga_arbeid_med_50_prosent_stilling_skal_manuell_behandles() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        LocalDate utsettelseFom = fødselsdato.plusWeeks(10);
        LocalDate utsettelseTom = fødselsdato.plusWeeks(12).minusDays(1);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 75))
                .leggTilKonto(konto(FELLESPERIODE, 80))
                .leggTilKonto(konto(FEDREKVOTE, 75));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD).leggTilEndringIStilling(
                new EndringAvStilling(utsettelseFom, BigDecimal.valueOf(50)));
        basicUtsettelseGrunnlag(fødselsdato).medKontoer(kontoer)
                .medSøknad(fødselSøknad().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, utsettelseFom.minusDays(1)))
                        .leggTilOppgittPeriode(utsettelsePeriode(utsettelseFom, utsettelseTom, UtsettelseÅrsak.ARBEID)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.ARBEID);
        assertThat(uttakPeriode.getFom()).isEqualTo(utsettelseFom);
        assertThat(uttakPeriode.getTom()).isEqualTo(utsettelseTom);
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_HELTIDSARBEID);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    public void periode_med_utsettelse_pga_ferie_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void skal_ikke_utlede_stønadskontotype_ved_innvilgelse_av_utsettelse() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getStønadskontotype()).isNull();
    }

    @Test
    public void periode_med_utsettelse_pga_ferie_skal_til_manuell_behandling_grunnet_bevegelige_helligdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 15);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(8);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        // 26.03 - 28.03 er en periode grunnet helligdag den 29.03
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(2));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        //2 neste uker med ugyldig utsettelse grunnet bevegelig helligdag

        // 29.03 - 29.03 er en periode fordi 29.mars er skjærtorsdag
        uttakPeriode = resultat.get(4).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 30.03 - 30.03 er en periode fordi 30.mars er en helligdag
        uttakPeriode = resultat.get(5).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 02.04 - 02.04 er en periode fordi 02.april er 2.påskedag
        uttakPeriode = resultat.get(6).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 03.04 - 08.04 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(7).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(1));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(6));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void flere_perioder_med_utsettelse_pga_ferie_skal_til_manuell_behandling_grunnet_bevegelige_helligdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 15);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), UtsettelseÅrsak.FERIE))
                .leggTilOppgittPeriode(
                        oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(17).minusDays(1)))
                .leggTilOppgittPeriode(
                        utsettelsePeriode(fødselsdato.plusWeeks(17), fødselsdato.plusWeeks(18).minusDays(1), UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(12);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        // 26.03 - 28.03 er en periode grunnet helligdag den 29.03
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(2));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        //2 neste uker med ugyldig utsettelse grunnet bevegelig helligdag

        // 29.03 - 29.03 er en periode fordi 29.mars er skjærtorsdag
        uttakPeriode = resultat.get(4).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 30.03 - 30.03 er en periode fordi 30.mars er en helligdag (langfredag)
        uttakPeriode = resultat.get(5).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 02.04 - 02.04 er en periode fordi 02.april er 2.påskedag
        uttakPeriode = resultat.get(6).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 03.04 - 08.04 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(7).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(1));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        // 09.04 - 13.05 er en periode fordi det er søkt om vanlig fellesperiode, ingen utsettelse
        uttakPeriode = resultat.get(8).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(12));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(25));

        // 14.05 - 16.05 er en periode grunnet helligdag den 17.05
        uttakPeriode = resultat.get(9).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(2));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);

        // 17.05 - 17.05 er en periode pga 17.mai
        uttakPeriode = resultat.get(10).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(3));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        assertThat(uttakPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));

        // 18.05 - 20.05 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(11).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(4));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(18).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void periode_med_ugyldig_utsettelse_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        UtsettelseÅrsak.INNLAGT_BARN)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med forsøkt utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.INNLAGT_BARN);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    public void utsettelse_uten_stønadskonto_på_helligdag_skal_gi_ugyldig_opphold() {
        LocalDate fødselsdato = LocalDate.of(2018, 11, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, LocalDate.of(2018, 12, 24)))
                .leggTilOppgittPeriode(
                        utsettelsePeriode(LocalDate.of(2018, 12, 25), LocalDate.of(2018, 12, 25), UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        //2 neste uker med gyldig utsettelse
        var utsettelse = resultat.get(3).getUttakPeriode();
        assertThat(utsettelse.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(utsettelse.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE);
        assertThat(utsettelse.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG);
        //Utsettelse periode med UKJENT stønadskontotype er settes til neste tilgjengelige stønadskontotype
        assertThat(utsettelse.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(utsettelse.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(1));
    }

    @Test
    public void utsettelse_periode_med_ukjent_kontotype_må_settes_til_neste_tilgjengelig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(fødselSøknad().leggTilOppgittPeriode(
                oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                        UtsettelseÅrsak.SYKDOM_SKADE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med forsøkt utsettelse
        var uttakPeriode = resultat.get(3).getUttakPeriode();
        assertThat(uttakPeriode.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.SYKDOM_SKADE);
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(uttakPeriode.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(10));
    }

    @Test
    public void pleiepenger_utsettelse_skal_trekke_fra_fellesperiode() {
        //Over 7 uker for tidlig, får pleiepenger. Utsettelsen skal avlås og det skal trekkes dager
        LocalDate termindato = LocalDate.of(2019, 9, 1);
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        basicUtsettelseGrunnlag(fødselsdato).medDatoer(new Datoer.Builder().medTermin(termindato).medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(
                        new PeriodeMedBarnInnlagt(fødselsdato, termindato.minusWeeks(2).minusDays(1))))
                        //Starter med pleiepenger
                        .leggTilOppgittPeriode(
                                utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), UtsettelseÅrsak.INNLAGT_BARN))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), termindato)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.INNLAGT_BARN);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FELLESPERIODE);
    }

    @Test
    public void pleiepenger_utsettelse_skal_trekke_fra_foreldrepenger() {
        //Over 7 uker for tidlig, får pleiepenger. Utsettelsen skal avlås og det skal trekkes dager
        LocalDate termindato = LocalDate.of(2019, 9, 1);
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(100));
        basicUtsettelseGrunnlag(fødselsdato).medKontoer(kontoer)
                .medDatoer(new Datoer.Builder().medTermin(termindato).medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(
                        new PeriodeMedBarnInnlagt(fødselsdato, termindato.minusWeeks(2).minusDays(1))))
                        //Starter med pleiepenger
                        .leggTilOppgittPeriode(
                                utsettelsePeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), UtsettelseÅrsak.INNLAGT_BARN))
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), termindato)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(0).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.INNLAGT_BARN);
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void skal_trekke_fra_mødrekvote_ved_avslag_ferie_innenfor_første_seks_uker() {
        //Søkt om ferieutsettelse innenfor seks uker etter fødsel. Utsettelsen skal avlås og det skal trekkes dager fra mødrekvote
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        basicUtsettelseGrunnlag(fødselsdato).medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4)))
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4).plusDays(1), fødselsdato.plusWeeks(8),
                                UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isNotEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    public void skal_trekke_fra_foreldrepengekvote_ved_avslag_ferie_innenfor_første_seks_uker_ved_aleneomsorg_far() {
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        aleneomsorgUtsettelseGrunnlag(fødselsdato, farBehandling()).medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(4)))
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4).plusDays(1), fødselsdato.plusWeeks(8),
                                UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isNotEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void skal_trekke_fra_foreldrepengekvote_ved_avslag_ferie_innenfor_første_seks_uker_ved_aleneomsorg_mor() {
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        aleneomsorgUtsettelseGrunnlag(fødselsdato, morBehandling()).medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(fødselSøknad().leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, fødselsdato, fødselsdato.plusWeeks(4)))
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(4).plusDays(1), fødselsdato.plusWeeks(8),
                                UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(resultat.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isNotEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void utsettelse_før_søknad_mottatt_dato_skal_gå_til_manuell() {
        //Søkt om ferieutsettelse innenfor seks uker etter fødsel. Utsettelsen skal avlås og det skal trekkes dager fra mødrekvote
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        var utsettelse = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10),
                PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.FERIE, fødselsdato.plusWeeks(8), null);
        basicUtsettelseGrunnlag(fødselsdato).medSøknad(
                fødselSøknad().leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)))
                        .leggTilOppgittPeriode(utsettelse));

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(grunnlag);

        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(resultat.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void utsettelse_ved_tomme_dager_skal_også_avslås() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        RegelGrunnlag uttakAvsluttetMedUtsettelse = basicUtsettelseGrunnlag(fødselsdato).medSøknad(
                fødselSøknad().leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1)))
                        .leggTilOppgittPeriode(
                                oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(36).minusDays(1)))
                        .leggTilOppgittPeriode(utsettelsePeriode(fødselsdato.plusWeeks(36), fødselsdato.plusWeeks(37).minusDays(1),
                                UtsettelseÅrsak.FERIE))
                        .leggTilOppgittPeriode(
                                utsettelsePeriode(fødselsdato.plusWeeks(37), fødselsdato.plusWeeks(100), UtsettelseÅrsak.ARBEID)))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettPerioder(uttakAvsluttetMedUtsettelse);

        assertThat(resultat).hasSize(5);
        assertThat(resultat.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE);

        assertThat(resultat.get(4).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(resultat.get(4).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    public void skal_knekke_riktig_på_helligdager_i_jula_ved_utsettelse_pga_ferie() {
        LocalDate fødselsdato = LocalDate.of(2019, 10, 10);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, BigDecimal.ZERO))
                                .build()))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(
                                utsettelsePeriode(fødselsdato.plusWeeks(6), LocalDate.of(2020, 1, 1), UtsettelseÅrsak.FERIE)));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);
        //Før jul ok
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //25
        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        //26
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        //Romjul
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        //1
        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    public void utsettelse_pga_heimevernet_skal_gå_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var hvFom = fødselsdato.plusWeeks(6);
        var hvTom = fødselsdato.plusWeeks(7).minusDays(1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, hvFom.minusDays(1))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, BigDecimal.ZERO))
                                .build()))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(utsettelsePeriode(hvFom, hvTom, UtsettelseÅrsak.HV_OVELSE)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
    }

    @Test
    public void utsettelse_pga_heimevernet_skal_innvilges_hvis_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var hvFom = fødselsdato.plusWeeks(6);
        var hvTom = fødselsdato.plusWeeks(7).minusDays(1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, hvFom.minusDays(1))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, BigDecimal.ZERO))
                                .build()))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(utsettelsePeriode(hvFom, hvTom, UtsettelseÅrsak.HV_OVELSE))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggTilPeriodeMedHV(new PeriodeMedHV(hvFom, hvTom))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void utsettelse_pga_tiltak_i_regi_av_nav_skal_gå_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var tiltakFom = fødselsdato.plusWeeks(6);
        var tiltakTom = fødselsdato.plusWeeks(7).minusDays(1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, tiltakFom.minusDays(1))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, BigDecimal.ZERO))
                                .build()))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(utsettelsePeriode(tiltakFom, tiltakTom, UtsettelseÅrsak.NAV_TILTAK)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
    }

    @Test
    public void utsettelse_pga_tiltak_i_regi_av_nav_skal_innvilges_hvis_dokumentert() {
        var fødselsdato = LocalDate.of(2019, 10, 10);
        var tiltakFom = fødselsdato.plusWeeks(6);
        var tiltakTom = fødselsdato.plusWeeks(7).minusDays(1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(farBehandling())
                .medRettOgOmsorg(beggeRett())
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato, tiltakFom.minusDays(1))
                                .medInnvilget(true)
                                .medUttakPeriodeAktivitet(
                                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                                                Trekkdager.ZERO, BigDecimal.ZERO))
                                .build()))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(utsettelsePeriode(tiltakFom, tiltakTom, UtsettelseÅrsak.NAV_TILTAK))
                        .medDokumentasjon(new Dokumentasjon.Builder().leggTilPeriodeMedTiltakViaNav(
                                new PeriodeMedTiltakIRegiAvNav(tiltakFom, tiltakTom))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    @Test
    public void avslag_utsettelse_med_trekkdager_skal_knekkes_når_saldo_går_tom() {
        var fødselsdato = LocalDate.of(2021, 1, 20);
        //Skal få avslag pga mor ikke er i aktivitet
        var fom = fødselsdato.plusWeeks(6);
        var tom = fødselsdato.plusWeeks(9);
        var dokumentasjon = new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_DOKUMENTERT));
        //Skal gå tom for dager
        var utsettelse = OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.PERIODE_OK,
                UtsettelseÅrsak.ARBEID, fødselsdato, MorsAktivitet.ARBEID);
        basicGrunnlagFar(fødselsdato)
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(bareFarRett())
                .medKontoer(new Kontoer.Builder().leggTilKonto(new Konto.Builder().medTrekkdager(10).medType(FORELDREPENGER)))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(utsettelse)
                .medDokumentasjon(dokumentasjon));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fom);
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fom.plusWeeks(2).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fom.plusWeeks(2));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(tom);
        assertThat(perioder.get(1).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
    }

    private void assertDeTreFørstePeriodene(List<FastsettePeriodeResultat> resultat, LocalDate fødselsdato) {
        //3 uker før fødsel - innvilges
        var uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    private Datoer.Builder datoer(LocalDate fødselsdato) {
        return new Datoer.Builder().medFødsel(fødselsdato);
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato) {
        return basicUtsettelseGrunnlag(fødselsdato, morBehandling());
    }

    private RegelGrunnlag.Builder aleneomsorgUtsettelseGrunnlag(LocalDate fødselsdato, Behandling.Builder behandling) {
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER).medTrekkdager(130));
        return grunnlag.medDatoer(datoer(fødselsdato)).medBehandling(behandling).medKontoer(kontoer).medRettOgOmsorg(aleneomsorg());
    }

    private RegelGrunnlag.Builder basicUtsettelseGrunnlag(LocalDate fødselsdato, Behandling.Builder behandling) {
        return grunnlag.medDatoer(datoer(fødselsdato)).medBehandling(behandling).medRettOgOmsorg(beggeRett());
    }

    private Søknad.Builder fødselSøknad() {
        return new Søknad.Builder().medType(Søknadstype.FØDSEL);
    }
}
