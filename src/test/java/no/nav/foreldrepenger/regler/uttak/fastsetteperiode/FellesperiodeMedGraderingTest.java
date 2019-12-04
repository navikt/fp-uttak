package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;

public class FellesperiodeMedGraderingTest {

    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = fødselsdato.minusMonths(3);

    @Test
    public void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_5_uker_igjen_på_saldo() {
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        StønadsPeriode aktuellPeriode = StønadsPeriode.medGradering(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom,
                Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, null, false);
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 5 * 5)));
        RegelGrunnlag grunnlag = basicGrunnlag()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(aktuellPeriode)
                        .medMottattDato(graderingFom.minusWeeks(1)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .build();

        Regelresultat regelresultat = kjørRegel(aktuellPeriode, grunnlag);

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }

    @Test
    public void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_4_uker_igjen_på_saldo() {
        LocalDate graderingFom = fødselsdato.plusWeeks(10);
        LocalDate graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        StønadsPeriode aktuellPeriode = StønadsPeriode.medGradering(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, graderingFom, graderingTom,
                Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(50), PeriodeVurderingType.PERIODE_OK, null, false);
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 4 * 5)));
        RegelGrunnlag grunnlag = basicGrunnlag()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(aktuellPeriode)
                        .medMottattDato(graderingFom.minusWeeks(1)))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .build();

        Regelresultat regelresultat = kjørRegel(aktuellPeriode, grunnlag);

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager);
    }

    private RegelGrunnlag.Builder basicGrunnlag() {
        return RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }
}
