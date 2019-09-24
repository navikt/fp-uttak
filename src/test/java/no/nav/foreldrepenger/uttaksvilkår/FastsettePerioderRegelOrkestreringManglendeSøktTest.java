package no.nav.foreldrepenger.uttaksvilkår;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public class FastsettePerioderRegelOrkestreringManglendeSøktTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void skal_avslå_og_trekke_mødrekvote_for_mor_hvis_dager_igjen() {

        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        //manglende søkt i mellom
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9))
                ))
                .medKontoer(kontoer(konto(Stønadskontotype.MØDREKVOTE, 1000), konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotEqualByComparingTo(BigDecimal.ZERO);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
    }

    @Test
    public void skal_avslå_og_trekke_foreldrepenger_for_far_med_enerett_hvis_dager_igjen() {
        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        RegelGrunnlag grunnlag = basicGrunnlagFar(fødselsdato)
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(false)
                        .medFarHarRett(true)
                        .build())
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        //manglende søkt i mellom blir opprettet før fellesperioden
                        søknadsperiode(Stønadskontotype.FORELDREPENGER, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12))
                ))
                .medKontoer(kontoer(konto(Stønadskontotype.FORELDREPENGER, 1000)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void skal_ikke_trekke_dager_hvis_tom_på_alle_konto() {
        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        //manglende søkt i mellom
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9))
                ))
                .medKontoer(kontoer(konto(Stønadskontotype.MØDREKVOTE, 30), konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.UKJENT);
    }
}
