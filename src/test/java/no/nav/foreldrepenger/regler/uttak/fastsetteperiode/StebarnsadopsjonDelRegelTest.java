package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class StebarnsadopsjonDelRegelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON, featureToggles());

    private FeatureToggles featureToggles() {
        return new FeatureToggles() {
        };
    }

    @Test
    public void UT1240_stebarnsadopsjon_far_ikke_omsorg() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        StønadsPeriode uttakPeriode = stønadsperiode(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2));

        RegelGrunnlag grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(100)))
                                .build())
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();

    }

    @Test
    public void UT1241_stebarnsadopsjon_far_omsorg_disponible_dager_og_ingen_gradering() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        StønadsPeriode uttakPeriode = stønadsperiode(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2));

        RegelGrunnlag grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode)

                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    public void UT1242_stebarnsadopsjon_far_omsorg_disponible_dager_gradering_og_avklart_periode() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        StønadsPeriode uttakPeriode = StønadsPeriode.medGradering(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2),
                Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK);

        RegelGrunnlag grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode)
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STEBARNSADOPSJON);
    }

    @Test
    public void UT1244_stebarnsadopsjon_far_omsorg_ikke_disponible_stønadsdager() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        StønadsPeriode uttakPeriode = stønadsperiode(omsorgsovertakelseDato, omsorgsovertakelseDato.plusWeeks(2));

        RegelGrunnlag grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode)
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50).build())
                        .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(0).build())
                        .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130).build())
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    public void UT1285_stebarnsadopsjon_uttak_før_omsorgsovertakelse() {
        LocalDate omsorgsovertakelseDato = LocalDate.of(2019, 1, 8);
        StønadsPeriode uttakPeriode = stønadsperiode(omsorgsovertakelseDato.minusDays(3), omsorgsovertakelseDato.plusWeeks(2));

        RegelGrunnlag grunnlag = grunnlagFar(omsorgsovertakelseDato, uttakPeriode)
                .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50).build())
                        .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(0).build())
                        .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130).build())
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE);
    }

    private RegelGrunnlag.Builder grunnlagFar(LocalDate familiehendelseDato, UttakPeriode uttakPeriode) {
        return RegelGrunnlagTestBuilder.create()
            .medSøknad(new Søknad.Builder()
                .medType(Søknadstype.ADOPSJON)
                .leggTilSøknadsperiode(uttakPeriode)
                    .medMottattDato(uttakPeriode.getFom().minusWeeks(1))
                .build())
            .medDatoer(new Datoer.Builder()
                .medFørsteLovligeUttaksdag(familiehendelseDato.minusWeeks(15))
                .medOmsorgsovertakelse(familiehendelseDato)
                .build())
            .leggTilKontoer(ARBEIDSFORHOLD_1, new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50).build())
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50).build())
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130).build())
                .build())
            .medBehandling(new Behandling.Builder()
                .medSøkerErMor(false)
                .build())
            .medRettOgOmsorg(new RettOgOmsorg.Builder()
                .medFarHarRett(true)
                .medMorHarRett(true)
                .medSamtykke(true)
                .build())
            .medAdopsjon(new Adopsjon.Builder()
                .medAnkomstNorge(null)
                .medStebarnsadopsjon(true)
                .build())
            .medInngangsvilkår(new Inngangsvilkår.Builder()
                .medAdopsjonOppfylt(true)
                .medForeldreansvarnOppfylt(true)
                .medFødselOppfylt(true)
                .medOpptjeningOppfylt(true)
                .build());
    }

    private StønadsPeriode stønadsperiode(LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    private Regelresultat evaluer(StønadsPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode)));
    }

}
