package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class FellesperiodeMedGraderingTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = fødselsdato.minusMonths(3);

    @Test
    public void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_5_uker_igjen_på_saldo() {
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        StønadsPeriode aktuellPeriode = StønadsPeriode.medGradering(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom,
                Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, null, false);
        RegelGrunnlag grunnlag = basicGrunnlag(graderingFom, graderingTom)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(aktuellPeriode)
                        .build())
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 5 * 5))
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(aktuellPeriode, grunnlag);

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }

    @Test
    public void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_4_uker_igjen_på_saldo() {
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        StønadsPeriode aktuellPeriode = StønadsPeriode.medGradering(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom,
                Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, null, false);
        RegelGrunnlag grunnlag = basicGrunnlag(graderingFom, graderingTom)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(aktuellPeriode)
                        .build())
                .leggTilKontoer(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 4 * 5))
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(aktuellPeriode, grunnlag);

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }

    private Konto konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager)
                .build();
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate graderingFom, LocalDate graderingTom) {
        return RegelGrunnlagTestBuilder.enGraderingsperiode(graderingFom, graderingTom, BigDecimal.valueOf(50))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());
    }

    private Regelresultat evaluer(UttakPeriode aktuellPeriode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.singletonList(aktuellPeriode)),
                aktuellPeriode)));
    }

}
