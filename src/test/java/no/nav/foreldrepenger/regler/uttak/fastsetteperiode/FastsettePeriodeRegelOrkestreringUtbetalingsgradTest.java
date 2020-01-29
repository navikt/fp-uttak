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
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

public class FastsettePeriodeRegelOrkestreringUtbetalingsgradTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void tom_for_dager_skal_gi_null_utbetaling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        OppgittPeriode fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(20).minusDays(1));
        basicGrunnlag(fødselsdato).medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);

        var up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        var up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        var up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        var up3 = perioder.get(3).getUttakPeriode();
        verifiserManuellBehandlingPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(BigDecimal.ZERO);

    }

    @Test
    public void gyldig_utsettelse_gir_ingen_utbetaling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        OppgittPeriode fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        OppgittPeriode utsettelseFellesperiode = utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), UtsettelseÅrsak.FERIE);
        OppgittPeriode fellesperiode = oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1));
        basicGrunnlag(fødselsdato).medSøknad(new Søknad.Builder()
                .medMottattDato(fpff.getFom().minusWeeks(1))
                .leggTilOppgittPeriode(fpff)
                .leggTilOppgittPeriode(mødrekvote)
                .leggTilOppgittPeriode(utsettelseFellesperiode)
                .leggTilOppgittPeriode(fellesperiode)
                .medType(Søknadstype.FØDSEL));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        var up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualByComparingTo(new BigDecimal("100.00"));

        var up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualByComparingTo(new BigDecimal("100.00"));

        var up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualByComparingTo(new BigDecimal("100.00"));

        var up3 = perioder.get(3).getUttakPeriode();
        assertThat(up3.getUtsettelseÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        verifiserPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), INNVILGET, null);
        assertThat(up3.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(BigDecimal.ZERO);

        var up4 = perioder.get(4).getUttakPeriode();
        verifiserPeriode(up4, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up4.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    public void gradering_gir_redusert_utbetalingsgrad() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var aktiviteter = Set.of(ARBEIDSFORHOLD_1);
        leggPåKvoter(grunnlag);
        OppgittPeriode fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        OppgittPeriode gradertFellesperiode = gradertoppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                BigDecimal.valueOf(20), aktiviteter);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato)))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote, gradertFellesperiode))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);

        var up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualByComparingTo(new BigDecimal("100.00"));

        var up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualByComparingTo(new BigDecimal("100.00"));

        var up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    public void gradering_gir_redusert_utbetalingsgrad_avrunding() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        var aktivititeter = Set.of(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2);
        leggPåKvoter(grunnlag);
        OppgittPeriode fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        OppgittPeriode gradertFellesperiode = gradertoppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), new BigDecimal("17.55"), aktivititeter);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medBehandling(morBehandling())
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote, gradertFellesperiode))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1))
                        .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_2)))
                .medInngangsvilkår(oppfyltAlleVilkår());

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        var uttakPeriode = perioder.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("82.45"));
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(new BigDecimal("82.45"));
    }

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder) {
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                .leggTilKonto(konto(MØDREKVOTE, 50))
                .leggTilKonto(konto(FEDREKVOTE, 50))
                .leggTilKonto(konto(FELLESPERIODE, 130));
        return builder.medKontoer(kontoer);
    }
}
