package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;

class EøsOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void annen_part_eøs_periode_skal_trekke_dager() {
        var fødselsdato = LocalDate.of(2025, 6, 17);
        var annenPart = annenPartMedEøsPeriode(fødselsdato.plusWeeks(21), fødselsdato.plusWeeks(22).minusDays(1), FELLESPERIODE);
        var grunnlag = bareGrunnlagBfhrMorEøs(fødselsdato, annenPart)
            .søknad(søknad(Søknadstype.FØDSEL, oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(21).minusDays(1)),
                oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(22), fødselsdato.plusWeeks(22 + 16).minusDays(1),
                    MORS_AKTIVITET_GODKJENT)));
        var resultat = fastsettPerioder(grunnlag);

        var uttaksperiode1 = resultat.getFirst().uttakPeriode();
        assertThat(uttaksperiode1.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttaksperiode1.getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(uttaksperiode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttaksperiode1.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(75));
        assertThat(uttaksperiode1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.FULL);

        var uttaksperiode2 = resultat.get(1).uttakPeriode();
        assertThat(uttaksperiode2.getFom()).isEqualTo(fødselsdato.plusWeeks(22));
        assertThat(uttaksperiode2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(uttaksperiode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttaksperiode2.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(75));
        assertThat(uttaksperiode2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.FULL);

        //Tom for fellesperiode etter 15 uker pga at mor bruker 1 uke fellesperiode i eøs
        var uttaksperiode3 = resultat.get(2).uttakPeriode();
        assertThat(uttaksperiode3.getFom()).isEqualTo(fødselsdato.plusWeeks(22 + 15));
        assertThat(uttaksperiode3.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(uttaksperiode3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(uttaksperiode3.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(uttaksperiode3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(uttaksperiode3.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void overlapp_annen_part_eøs_periode_skal_til_manuell_behandling() {
        var fødselsdato = LocalDate.of(2025, 6, 17);
        var fom = fødselsdato.plusWeeks(21);
        var annenPart = annenPartMedEøsPeriode(fom, fødselsdato.plusWeeks(23).minusDays(1), FELLESPERIODE);
        var grunnlag = bareGrunnlagBfhrMorEøs(fødselsdato, annenPart)
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FELLESPERIODE, fom, fødselsdato.plusWeeks(22).minusDays(1), MORS_AKTIVITET_GODKJENT),
                oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(22), fødselsdato.plusWeeks(23).minusDays(1))
                ));
        var resultat = fastsettPerioder(grunnlag);

        var uttaksperiode1 = resultat.getFirst().uttakPeriode();
        assertThat(uttaksperiode1.getFom()).isEqualTo(fom);
        assertThat(uttaksperiode1.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(uttaksperiode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        var uttaksperiode2 = resultat.get(1).uttakPeriode();
        assertThat(uttaksperiode2.getFom()).isEqualTo(fødselsdato.plusWeeks(22));
        assertThat(uttaksperiode2.getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(uttaksperiode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    void overlapp_annen_part_eøs_periode_skal_til_manuell_behandling_også_ved_søkt_samtidig_uttak() {
        var fødselsdato = LocalDate.of(2025, 6, 17);
        var fom = fødselsdato.plusWeeks(21);
        var annenPart = annenPartMedEøsPeriode(fom, fødselsdato.plusWeeks(23).minusDays(1), FELLESPERIODE);
        var grunnlag = bareGrunnlagBfhrMorEøs(fødselsdato, annenPart)
            .søknad(søknad(Søknadstype.FØDSEL,
                oppgittPeriode(FELLESPERIODE, fom, fødselsdato.plusWeeks(22).minusDays(1), false, new SamtidigUttaksprosent(50), MORS_AKTIVITET_GODKJENT),
                oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(22), fødselsdato.plusWeeks(23).minusDays(1), false, new SamtidigUttaksprosent(50))
            ));
        var resultat = fastsettPerioder(grunnlag);

        var uttaksperiode1 = resultat.getFirst().uttakPeriode();
        assertThat(uttaksperiode1.getFom()).isEqualTo(fom);
        assertThat(uttaksperiode1.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(uttaksperiode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        var uttaksperiode2 = resultat.get(1).uttakPeriode();
        assertThat(uttaksperiode2.getFom()).isEqualTo(fødselsdato.plusWeeks(22));
        assertThat(uttaksperiode2.getStønadskontotype()).isEqualTo(FEDREKVOTE);
        assertThat(uttaksperiode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    private static AnnenPart.Builder annenPartMedEøsPeriode(LocalDate uttakFom, LocalDate uttakTom, Stønadskontotype stønadskontotype) {
        return new AnnenPart.Builder().uttaksperiode(AnnenpartUttakPeriode.Builder.eøs(uttakFom, uttakTom, stønadskontotype,
            new Trekkdager(Virkedager.beregnAntallVirkedager(uttakFom, uttakTom))).build()).eøs(true);
    }

    private RegelGrunnlag.Builder bareGrunnlagBfhrMorEøs(LocalDate fødselsdato, AnnenPart.Builder annenPart) {
        return basicGrunnlagFar(fødselsdato).kontoer(kvoter()).rettOgOmsorg(beggeRett()).annenPart(annenPart);
    }

    private static Kontoer.Builder kvoter() {
        return new Kontoer.Builder().konto(FORELDREPENGER_FØR_FØDSEL, 15).konto(MØDREKVOTE, 75).konto(FELLESPERIODE, 80).konto(FEDREKVOTE, 75);
    }
}
