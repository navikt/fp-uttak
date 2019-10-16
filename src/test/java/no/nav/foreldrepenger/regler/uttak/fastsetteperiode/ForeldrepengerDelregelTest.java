package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Årsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class ForeldrepengerDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON, featureToggles());

    private FeatureToggles featureToggles() {
        return new FeatureToggles() {
        };
    }

    @Test
    public void UT1185_mor_starter_tidligere_enn_12_uker_før_termin() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.minusWeeks(12).minusDays(1), familiehendelseDato);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(15))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void UT1186_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.minusWeeks(6), familiehendelseDato.minusWeeks(5));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(false)
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1211_utenAleneomsorg_morRett_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.minusWeeks(6), familiehendelseDato.minusWeeks(5));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medAleneomsorg(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    public void UT1187_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = gradertPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato, AktivitetIdentifikator.forFrilans(),
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    private StønadsPeriode gradertPeriode(LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitetIdentifikator, PeriodeVurderingType periodeVurderingType) {
        return StønadsPeriode.medGradering(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,
                Collections.singletonList(aktivitetIdentifikator), BigDecimal.TEN, periodeVurderingType);
    }

    private StønadsPeriode gradertPeriode(LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitetIdentifikator, PeriodeVurderingType periodeVurderingType, SamtidigUttak samtidigUttak, boolean flerbarnsdager) {
        return StønadsPeriode.medGradering(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,
            Collections.singletonList(aktivitetIdentifikator), BigDecimal.TEN, periodeVurderingType, samtidigUttak, flerbarnsdager);
    }

    @Test
    public void UT1212_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_bareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = gradertPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato,
                AktivitetIdentifikator.forFrilans(),
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1210_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = gradertPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                AktivitetIdentifikator.forFrilans(),
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    public void UT1213_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        AktivitetIdentifikator aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        StønadsPeriode uttakPeriode = gradertPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                aktivitetIdentifikator,
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(aktivitetIdentifikator, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    public void UT1190_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1214_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    private void assertInnvilgetMenAvslåttGradering(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak, GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak);
    }

    @Test
    public void UT1188_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_ikkeDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(12));
        ArbeidGrunnlag.Builder arbeid = new ArbeidGrunnlag.Builder()
                .medArbeidsprosenter(new Arbeidsprosenter().leggTil(AktivitetIdentifikator.forFrilans(), new ArbeidTidslinje.Builder().build())
                        .leggTil(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), new ArbeidTidslinje.Builder().build()));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medArbeid(arbeid.build())
                .medSøknad(søknad(uttakPeriode))
                .medKontoer(Map.of(AktivitetIdentifikator.forFrilans(), foreldrepengerKonto(0), AktivitetIdentifikator.forSelvstendigNæringsdrivende(), foreldrepengerKonto(10)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1188");
        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false);
    }

    @Test
    public void UT1191_mor_aleneomsorg_etter6ukerEtterFødsel_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(6);
        LocalDate tom = familiehendelseDato.plusWeeks(7);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medType(Søknadstype.FØDSEL)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1209_mor_utenAleneomsorg_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(6);
        LocalDate tom = familiehendelseDato.plusWeeks(7);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(false)
                        .medFarHarRett(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    private void assertManuellBehandling(Regelresultat regelresultat, Årsak årsak, Manuellbehandlingårsak manuellBehandlingÅrsak) {
        assertManuellBehandling(regelresultat, årsak, manuellBehandlingÅrsak, false, false);
    }

    private void assertManuellBehandling(Regelresultat regelresultat,
                                         Årsak årsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak,
                                         boolean trekkdager,
                                         boolean utbetal) {
        assertManuellBehandling(regelresultat, årsak, manuellBehandlingÅrsak, trekkdager, utbetal, Optional.empty());
    }

    private void assertManuellBehandling(Regelresultat regelresultat,
                                         Årsak årsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak,
                                         boolean trekkdager,
                                         boolean utbetal,
                                         Optional<GraderingIkkeInnvilgetÅrsak> graderingIkkeInnvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkdager);
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        if (årsak instanceof InnvilgetÅrsak) {
            assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(årsak);
        } else {
            assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(årsak);
        }
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(manuellBehandlingÅrsak);
        if (graderingIkkeInnvilgetÅrsak.isPresent()) {
            assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak.get());
        } else {
            assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isNull();
        }
    }

    @Test
    public void UT1193_far_før_familiehendelse() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        StønadsPeriode uttakPeriode = stønadsperiode(familiehendelseDato.minusWeeks(3), familiehendelseDato.minusWeeks(2));
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1194_far_etterFamiliehendelse_aleneomsorg_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1195_far_etterFamiliehendelse_aleneomsorg_medOmsorg_utenDisponibledager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        ArbeidGrunnlag.Builder arbeid = new ArbeidGrunnlag.Builder()
                .medArbeidsprosenter(new Arbeidsprosenter().leggTil(AktivitetIdentifikator.forFrilans(), new ArbeidTidslinje.Builder().build())
                        .leggTil(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), new ArbeidTidslinje.Builder().build()));
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medArbeid(arbeid.build())
                .medSøknad(søknad(uttakPeriode))
                .medKontoer(Map.of(AktivitetIdentifikator.forFrilans(), foreldrepengerKonto(0), AktivitetIdentifikator.forSelvstendigNæringsdrivende(), foreldrepengerKonto(10)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1195");
        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false);
    }

    @Test
    public void UT1196_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_medGradering_førUke7() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        AktivitetIdentifikator aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        StønadsPeriode uttakPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(aktivitetIdentifikator, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    public void UT1198_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1199_far_etterFamiliehendelse_utenAleneomsorg_farRett_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medType(Søknadstype.FØDSEL)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))
                                .build())
                        .build())
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1200_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_utenGyldigGrunn() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    @Test
    public void UT1201_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        uttakPeriode.setPeriodeVurderingType(PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .medAleneomsorg(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null,     Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1201_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1216_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        StønadsPeriode uttakPeriode = gradertPeriode(fom, tom, AktivitetIdentifikator.forFrilans(), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1216_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        AktivitetIdentifikator aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        StønadsPeriode uttakPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(aktivitetIdentifikator, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1203_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_utenDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        ArbeidGrunnlag.Builder arbeid = new ArbeidGrunnlag.Builder()
                .medArbeidsprosenter(new Arbeidsprosenter().leggTil(AktivitetIdentifikator.forFrilans(), new ArbeidTidslinje.Builder().build())
                .leggTil(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), new ArbeidTidslinje.Builder().build()));
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medArbeid(arbeid.build())
                .medSøknad(søknad(uttakPeriode))
                .medKontoer(Map.of(AktivitetIdentifikator.forFrilans(), foreldrepengerKonto(10), AktivitetIdentifikator.forSelvstendigNæringsdrivende(), foreldrepengerKonto(0)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1203");
        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false);
    }

    @Test
    public void UT1204_far_etterFamiliehendelse_utenAleneomsorg_utenFarRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        StønadsPeriode uttakPeriode = stønadsperiode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode))
                .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .build())
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    public void UT1266_far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett() {
        LocalDate familiehendelseDato = LocalDate.now().minusMonths(2);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(3);
        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, null, true);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
            .medSøknad(søknad(uttakPeriode))
            .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerOgFlerbarnsdagerKonto(40, 17))
            .medRettOgOmsorg(new RettOgOmsorg.Builder()
                .medFarHarRett(true)
                .medMorHarRett(false)
                .build())
            .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    public void UT1267_far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_medDisponibleDager_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.now().minusMonths(2);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(3);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        StønadsPeriode uttakPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK, null, true);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
            .medSøknad(søknad(uttakPeriode))
            .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerOgFlerbarnsdagerKonto(40, 17))
            .medRettOgOmsorg(new RettOgOmsorg.Builder()
                .medFarHarRett(true)
                .medMorHarRett(false)
                .build())
            .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    public void UT1269_far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_utenDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.now().minusMonths(2);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(3);
        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, null, true);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
            .medSøknad(søknad(uttakPeriode))
            .leggTilKontoer(ARBEIDSFORHOLD_1, foreldrepengerOgFlerbarnsdagerKonto(100, 0))
            .medRettOgOmsorg(new RettOgOmsorg.Builder()
                .medFarHarRett(true)
                .medMorHarRett(false)
                .build())
            .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1269");
        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private RegelGrunnlag.Builder grunnlagMor(LocalDate familiehendelseDato) {
        return grunnlag(familiehendelseDato, true);
    }

    private RegelGrunnlag.Builder grunnlagFar(LocalDate familiehendelseDato) {
        return grunnlag(familiehendelseDato, false);
    }

    private RegelGrunnlag.Builder grunnlag(LocalDate familiehendelseDato, boolean søkerMor) {
        return RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(familiehendelseDato.minusWeeks(15))
                        .medFødsel(familiehendelseDato)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(søkerMor)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .build())
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());
    }

    private Kontoer foreldrepengerKonto(int trekkdager) {
        return new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(trekkdager)
                        .build())
                .build();
    }

    private Kontoer foreldrepengerOgFlerbarnsdagerKonto(int foreldrepengerTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder()
            .leggTilKonto(new Konto.Builder()
                .medType(Stønadskontotype.FORELDREPENGER)
                .medTrekkdager(foreldrepengerTrekkdager)
                .build())
            .leggTilKonto(new Konto.Builder()
                .medType(Stønadskontotype.FLERBARNSDAGER)
                .medTrekkdager(flerbarnsdagerTrekkdager)
                .build())
            .build();
    }

    private Søknad søknad(StønadsPeriode uttakPeriode) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .medMottattDato(uttakPeriode.getFom().minusWeeks(1))
                .leggTilSøknadsperiode(uttakPeriode)
                .build();
    }

    private Regelresultat evaluer(StønadsPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode)));
    }

    private StønadsPeriode stønadsperiode(LocalDate fom, LocalDate tom) {
        return new StønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }
}
