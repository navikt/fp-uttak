package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.UKJENT;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePeriodeRegelOrkestreringUtbetalingsgradTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void tom_for_dager_skal_gi_null_utbetaling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(30), Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100)))
                .build();

        UttakPeriode fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        UttakPeriode mødrekvote = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(20).minusDays(1));
        basicGrunnlag(fødselsdato)
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter().leggTil(ARBEIDSFORHOLD, arbeidTidslinje))
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote));

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(4);

        UttakPeriode up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up3 = perioder.get(3).getUttakPeriode();
        verifiserManuellBehandlingPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1), MØDREKVOTE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(BigDecimal.ZERO);

    }

    @Test
    public void gyldig_utsettelse_gir_ingen_utbetaling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(30), Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100)))
                .build();
        UttakPeriode fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        UttakPeriode mødrekvote = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        UttakPeriode utsettelseFellesperiode = utsettelsePeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.FERIE);
        UttakPeriode fellesperiode = søknadsperiode(FELLESPERIODE, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1));
        basicGrunnlag(fødselsdato)
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter().leggTil(ARBEIDSFORHOLD, arbeidTidslinje))
                        .build())
                .medSøknad(new Søknad.Builder()
                        .medMottattDato(fpff.getFom().minusWeeks(1))
                        .leggTilSøknadsperiode(fpff)
                        .leggTilSøknadsperiode(mødrekvote)
                        .leggTilSøknadsperiode(utsettelseFellesperiode)
                        .leggTilSøknadsperiode(fellesperiode)
                        .medType(Søknadstype.FØDSEL)
                        .build());

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(5);

        UttakPeriode up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up3 = perioder.get(3).getUttakPeriode();
        assertThat(up3).isInstanceOf(UtsettelsePeriode.class);
        verifiserPeriode(up3, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), INNVILGET, UKJENT);
        assertThat(up3.getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(Trekkdager.ZERO);
        assertThat(up3.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(BigDecimal.ZERO);

        UttakPeriode up4 = perioder.get(4).getUttakPeriode();
        verifiserPeriode(up4, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(14).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up4.getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void gradering_gir_redusert_utbetalingsgrad() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), BigDecimal.valueOf(20));
        List<AktivitetIdentifikator> aktiviteter = Collections.singletonList(ARBEIDSFORHOLD_1);
        leggPåKvoter(grunnlag, aktiviteter);
        UttakPeriode fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        UttakPeriode mødrekvote = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        StønadsPeriode gradertFellesperiode = StønadsPeriode.medGradering(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                aktiviteter, BigDecimal.valueOf(20), PeriodeVurderingType.PERIODE_OK);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .build())
                .medRettOgOmsorg(beggeRett())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote, gradertFellesperiode));

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        assertThat(perioder).hasSize(3);

        UttakPeriode up0 = perioder.get(0).getUttakPeriode();
        verifiserPeriode(up0, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        assertThat(up0.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up1 = perioder.get(1).getUttakPeriode();
        verifiserPeriode(up1, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        assertThat(up1.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("100.00"));

        UttakPeriode up2 = perioder.get(2).getUttakPeriode();
        verifiserPeriode(up2, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), INNVILGET, FELLESPERIODE);
        assertThat(up2.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    public void gradering_gir_redusert_utbetalingsgrad_avrunding() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        List<AktivitetIdentifikator> aktivititeter = Arrays.asList(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2);
        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.enGraderingsperiodeMedFlereAktiviteter(fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), new BigDecimal("17.55"), aktivititeter);
        leggPåKvoter(grunnlag, aktivititeter);
        UttakPeriode fpff = søknadsperiode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        UttakPeriode mødrekvote = søknadsperiode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        UttakPeriode gradertFellesperiode = StønadsPeriode.medGradering(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1), aktivititeter, BigDecimal.valueOf(20), PeriodeVurderingType.PERIODE_OK);
        grunnlag.medDatoer(new Datoer.Builder()
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFødsel(fødselsdato)
                .build())
                .medRettOgOmsorg(beggeRett())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL, fpff, mødrekvote, gradertFellesperiode))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build(), new FeatureTogglesForTester());

        UttakPeriode uttakPeriode = perioder.get(2).getUttakPeriode();
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_1)).isEqualTo(new BigDecimal("82.45"));
        assertThat(uttakPeriode.getUtbetalingsgrad(ARBEIDSFORHOLD_2)).isEqualTo(new BigDecimal("82.45"));
    }

    private RegelGrunnlag.Builder leggPåKvoter(RegelGrunnlag.Builder builder, List<AktivitetIdentifikator> aktivitetIdentifikatorer) {
        for (AktivitetIdentifikator aktivitetIdentifikator : aktivitetIdentifikatorer) {
            builder.leggTilKontoer(aktivitetIdentifikator, new Kontoer.Builder()
                    .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, 15))
                    .leggTilKonto(konto(MØDREKVOTE, 50))
                    .leggTilKonto(konto(FEDREKVOTE, 50))
                    .leggTilKonto(konto(FELLESPERIODE, 130))
                    .build());
        }
        return builder;
    }

    private UttakPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, Utsettelseårsaktype årsak) {
        return new UtsettelsePeriode(PeriodeKilde.SØKNAD, fom, tom, årsak, PeriodeVurderingType.PERIODE_OK, null, false);
    }
}
