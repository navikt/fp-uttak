package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
                .medArbeid(arbeid(konto(Stønadskontotype.MØDREKVOTE, 1000), konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

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
                        .medFarHarRett(true))
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        //manglende søkt i mellom blir opprettet før fellesperioden
                        søknadsperiode(Stønadskontotype.FORELDREPENGER, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12))
                ))
                .medArbeid(arbeid(konto(Stønadskontotype.FORELDREPENGER, 1000)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(0).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER);
    }

    @Test
    public void skal_avslå_og_ikke_trekke_dager_når_alle_kontoer_går_tom() {
        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        //manglende søkt i mellom
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(9))
                ))
                .medArbeid(arbeid(konto(Stønadskontotype.MØDREKVOTE, 30), konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        assertThat(perioder.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.UKJENT);
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    public void skal_avslå_og_ikke_trekke_dager_når_alle_kontoer_går_tom_midt_i_en_manglede_søkt_periode() {
        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        //manglende søkt i mellom
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12))
                ))
                .medArbeid(arbeid(konto(Stønadskontotype.MØDREKVOTE, 32), konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(5);
        assertThat(perioder.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(3).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.UKJENT);
        assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isZero();
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    public void skal_avslå_og_ikke_trekke_dager_når_alle_kontoer_går_tom_midt_i_en_manglede_søkt_periode_flere_knekk() {
        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        RegelGrunnlag grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        //manglende søkt i mellom
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12))
                ))
                .medArbeid(arbeid(
                        konto(Stønadskontotype.MØDREKVOTE, 32),
                        konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15),
                        konto(Stønadskontotype.FELLESPERIODE, 5)))
                .build();
        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        //Går tom for mødrekvote først, deretter fellesperiode
        assertThat(perioder).hasSize(6);
        assertThat(perioder.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(3).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(3).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isNotZero();
        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(4).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(4).getUttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isZero();
        assertThat(perioder.get(4).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.UKJENT);
        assertThat(perioder.get(4).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD).decimalValue()).isZero();
        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    public void skal_kunne_håndtere_ulikt_antall_dager_gjenværende_på_arbeidsforhold_ved_manglende_søkt_periode() {
        LocalDate fødselsdato = LocalDate.of(2019, 9, 3);
        var arbeidsforhold1 = new Arbeidsforhold(AktivitetIdentifikator.forFrilans(), new Kontoer.Builder()
                .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 75))
                .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 1)));
        var arbeidsforhold2 = new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), new Kontoer.Builder()
                .leggTilKonto(konto(Stønadskontotype.MØDREKVOTE, 75))
                .leggTilKonto(konto(Stønadskontotype.FELLESPERIODE, 0)));
        var arbeid = new Arbeid.Builder()
                .leggTilArbeidsforhold(arbeidsforhold1)
                .leggTilArbeidsforhold(arbeidsforhold2);
        var grunnlag = basicGrunnlagMor(fødselsdato)
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1)),
                        //Har igjen 1 dag på fellesperiode på ett arbeidsforhold når manglende søkt skal behandles
                        søknadsperiode(Stønadskontotype.MØDREKVOTE, fødselsdato.plusWeeks(16), fødselsdato.plusWeeks(17).minusDays(1))
                ))
                .medArbeid(arbeid)
                .build();
        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(4);
        //UT1291
        assertThat(perioder.get(3).isManuellBehandling()).isTrue();
    }

    @Test
    public void manglende_søkt_periode_før_nytt_arbeidsforhold_tilkommer() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

        var arbeidsforhold = AktivitetIdentifikator.forArbeid("000000001", null);
        var arbeidsforholdMedId = AktivitetIdentifikator.forArbeid("000000001", "1234");

        RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
                .medBehandling(morBehandling())
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforhold, defaultKontoer()))
                        .leggTilArbeidsforhold(new Arbeidsforhold(arbeidsforholdMedId, defaultKontoer(), fødselsdato.plusWeeks(12)))
                )
                .medInngangsvilkår(oppfyltAlleVilkår());

        grunnlag.medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3)))
                .medBehandling(morBehandling())
                .medRettOgOmsorg(beggeRett())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(11), fødselsdato.plusWeeks(15).minusDays(1), null, false)));

        List<FastsettePeriodeResultat> perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(6);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(3*5));
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(6*5));
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(4*5));
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));


        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(3).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(11).minusDays(3)); //tar ikke med helg

        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(4).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(4).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(5));
        assertThat(perioder.get(4).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(perioder.get(4).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));

        assertThat(perioder.get(5).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(5).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FELLESPERIODE);
        assertThat(perioder.get(5).getUttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualTo(new Trekkdager(3*5));
        assertThat(perioder.get(5).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(12));
        assertThat(perioder.get(5).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));
    }

}
