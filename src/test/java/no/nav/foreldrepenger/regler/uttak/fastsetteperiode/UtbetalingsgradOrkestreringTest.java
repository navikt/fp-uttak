package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class UtbetalingsgradOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void tom_for_dager_skal_gi_null_utbetaling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);

        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(20).minusDays(1));
        basicGrunnlag(fødselsdato).søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);

        var up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up3 = perioder.get(3).getUttakPeriode();
        verifiserManuellBehandlingPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1), MØDREKVOTE,
                IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);

    }

    @Test
    void gyldig_utsettelse_gir_ingen_utbetaling() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        var utsettelseFellesperiode = utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1),
                UtsettelseÅrsak.FERIE);
        var fellesperiode = oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(12),
                fødselsdato.plusWeeks(14).minusDays(1));
        basicGrunnlag(fødselsdato).søknad(new Søknad.Builder().oppgittPeriode(fpff)
                .oppgittPeriode(mødrekvote)
                .oppgittPeriode(utsettelseFellesperiode)
                .oppgittPeriode(fellesperiode)
                .type(Søknadstype.FØDSEL));

        //Krever sammenhengende pga søkt om ferie som innvilges, dette er gamle regler
        var grunnlag = this.grunnlag.behandling(morBehandling().kreverSammenhengendeUttak(true));
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        var up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up3 = perioder.get(3).getUttakPeriode();
        assertThat(up3.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        verifiserPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), INNVILGET, null);
        assertThat(up3.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.ZERO);

        var up4 = perioder.get(4).getUttakPeriode();
        verifiserPeriode(up4, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up4.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(Utbetalingsgrad.HUNDRED);
    }

    @Test
    void gradering_gir_redusert_utbetalingsgrad() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var aktiviteter = Set.of(ARBEIDSFORHOLD_1);
        leggPåKvoter(grunnlag);
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var gradertFellesperiode = gradertoppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), BigDecimal.valueOf(20), aktiviteter);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .inngangsvilkår(oppfyltAlleVilkår())
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote, gradertFellesperiode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        var up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(Utbetalingsgrad.HUNDRED);

        var up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new Utbetalingsgrad(80));
    }

    @Test
    void gradering_gir_redusert_utbetalingsgrad_avrunding() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var aktivititeter = Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2);
        leggPåKvoter(grunnlag);
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var gradertFellesperiode = gradertoppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), new BigDecimal("17.55"), aktivititeter);
        grunnlag.datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .behandling(morBehandling())
                .søknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote, gradertFellesperiode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
                .inngangsvilkår(oppfyltAlleVilkår());

        var perioder = fastsettPerioder(grunnlag);

        var uttakPeriode = perioder.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new Utbetalingsgrad(82.45));
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(new Utbetalingsgrad(82.45));
    }

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .konto(konto(MØDREKVOTE, 50))
                .konto(konto(FEDREKVOTE, 50))
                .konto(konto(FELLESPERIODE, 130));
        return builder.kontoer(kontoer);
    }
}
