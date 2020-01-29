package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class ForeldrepengerDelregelTest {

    @Test
    public void UT1185_mor_starter_tidligere_enn_12_uker_før_termin() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(12).minusDays(1), familiehendelseDato);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(15))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void UT1186_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato.minusWeeks(5));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(false)
                        .medAleneomsorg(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1211_utenAleneomsorg_morRett_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato.minusWeeks(5));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true)
                        .medAleneomsorg(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    public void UT1187_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var gradertPeriode = gradertPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato, AktivitetIdentifikator.forFrilans(),
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    private OppgittPeriode gradertPeriode(LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitetIdentifikator, PeriodeVurderingType periodeVurderingType) {
        return gradertPeriode(fom, tom, aktivitetIdentifikator, periodeVurderingType, null, false);
    }

    private OppgittPeriode gradertPeriode(LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitetIdentifikator, PeriodeVurderingType vurderingType,
                                          BigDecimal samtidigUttaksprosent, boolean flerbarnsdager) {
        return OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, PeriodeKilde.SØKNAD, BigDecimal.TEN,
                samtidigUttaksprosent, flerbarnsdager, Set.of(aktivitetIdentifikator), vurderingType);
    }

    @Test
    public void UT1212_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_bareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var gradertPeriode = gradertPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato,
                AktivitetIdentifikator.forFrilans(),
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(true))
                .medKontoer(foreldrepengerKonto(100))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1210_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var gradertPeriode = gradertPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                AktivitetIdentifikator.forFrilans(),
                PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    public void UT1213_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        AktivitetIdentifikator aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        var gradertPeriode = gradertPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                aktivitetIdentifikator,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = foreldrepengerKonto(100);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    public void UT1190_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1214_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .medMorHarRett(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    private void assertInnvilgetMenAvslåttGradering(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak, GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak);
    }

    @Test
    public void UT1190_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_noenDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(12));
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medAleneomsorg(true))
                .medKontoer(foreldrepengerKonto(10))
                .build();

        var fastsattePerioder = List.of(new FastsattUttakPeriode.Builder()
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(oppgittPeriode.getFom().minusWeeks(1), oppgittPeriode.getFom().minusDays(1))
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(8), Stønadskontotype.FORELDREPENGER, AktivitetIdentifikator.forFrilans())))
                .build());
        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag, fastsattePerioder);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1190");
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1191_mor_aleneomsorg_etter6ukerEtterFødsel_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(6);
        LocalDate tom = familiehendelseDato.plusWeeks(7);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(oppgittPeriode)
                        .medType(Søknadstype.FØDSEL)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

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
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(false)
                        .medFarHarRett(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    public void UT1192__fødsel_bare_mor_rett_periode_før_fødsel() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.minusWeeks(3);
        LocalDate tom = familiehendelseDato.minusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, "UT1192");
    }

    @Test
    public void UT1197__fødsel_bare_mor_aleneomsorg_periode_før_fødsel() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.minusWeeks(3);
        LocalDate tom = familiehendelseDato.minusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, "UT1197");
    }

    private void assertManuellBehandling(FastsettePerioderRegelresultat regelresultat, PeriodeResultatÅrsak periodeResultatÅrsak, Manuellbehandlingårsak manuellBehandlingÅrsak) {
        assertManuellBehandling(regelresultat, periodeResultatÅrsak, manuellBehandlingÅrsak, false, false);
    }

    private void assertManuellBehandling(FastsettePerioderRegelresultat regelresultat,
                                         PeriodeResultatÅrsak periodeResultatÅrsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak,
                                         boolean trekkdager,
                                         boolean utbetal) {
        assertManuellBehandling(regelresultat, periodeResultatÅrsak, manuellBehandlingÅrsak, trekkdager, utbetal, Optional.empty());
    }

    private void assertManuellBehandling(FastsettePerioderRegelresultat regelresultat,
                                         PeriodeResultatÅrsak periodeResultatÅrsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak,
                                         boolean trekkdager,
                                         boolean utbetal,
                                         Optional<GraderingIkkeInnvilgetÅrsak> graderingIkkeInnvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkdager);
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(periodeResultatÅrsak);
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
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(3), familiehendelseDato.minusWeeks(2));
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1194_far_etterFamiliehendelse_aleneomsorg_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

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
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(10))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true))
                .build();

        var fastsattePerioder = List.of(new FastsattUttakPeriode.Builder()
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(fom.minusWeeks(1), fom.minusDays(1))
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), Stønadskontotype.FORELDREPENGER, AktivitetIdentifikator.forFrilans()),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(5), Stønadskontotype.FORELDREPENGER, AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .build());
        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag, fastsattePerioder);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1198");
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1196_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_medGradering_førUke7() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        AktivitetIdentifikator aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        var gradertPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    public void UT1198_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medAleneomsorg(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1199_far_etterFamiliehendelse_utenAleneomsorg_farRett_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(oppgittPeriode)
                        .medType(Søknadstype.FØDSEL)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPerioderUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

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
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom,
                PeriodeKilde.SØKNAD, null, false, PeriodeVurderingType.UAVKLART_PERIODE);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

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
        var oppgittPeriode = oppgittPeriode(fom, tom, PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .medAleneomsorg(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1201_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1216_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        var gradertPeriode = gradertPeriode(fom, tom, AktivitetIdentifikator.forFrilans(), PeriodeVurderingType.PERIODE_OK);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1216_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        AktivitetIdentifikator aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        var gradertPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK);
        var kontoer = foreldrepengerKonto(100);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1203_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_utenDisponibleDagerPåAlleAktiviteter() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medArbeid(new Arbeid.Builder()
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                        .leggTilArbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .medKontoer(foreldrepengerKonto(10))
                .build();

        var fastsattePerioder = List.of(new FastsattUttakPeriode.Builder()
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(fom.minusWeeks(1), fom.minusDays(1))
                .medAktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), Stønadskontotype.FORELDREPENGER, AktivitetIdentifikator.forFrilans()),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(5), Stønadskontotype.FORELDREPENGER, AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .build());
        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag, fastsattePerioder);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1201");
        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false);
    }

    @Test
    public void UT1204_far_etterFamiliehendelse_utenAleneomsorg_utenFarRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(foreldrepengerKonto(100))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medMorHarRett(true))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    public void UT1266_far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett() {
        LocalDate familiehendelseDato = LocalDate.now().minusMonths(2);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(3);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, PeriodeKilde.SØKNAD,
                null, true, PeriodeVurderingType.IKKE_VURDERT);
        var kontoer = foreldrepengerOgFlerbarnsdagerKonto(40, 17);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    public void UT1267_far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_medDisponibleDager_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.now().minusMonths(2);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(3);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var gradertPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK, null, true);
        var kontoer = foreldrepengerOgFlerbarnsdagerKonto(40, 17);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(gradertPeriode))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medKontoer(kontoer)
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    public void UT1269_far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_noenDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.now().minusMonths(2);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(3);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, PeriodeKilde.SØKNAD,
                null, true, PeriodeVurderingType.IKKE_VURDERT);
        var kontoer = foreldrepengerOgFlerbarnsdagerKonto(100, 0);
        RegelGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medSøknad(søknad(oppgittPeriode))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1266");
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private void assertInnvilget(FastsettePerioderRegelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak, String sluttpunktId) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.sluttpunktId()).isEqualTo((sluttpunktId));
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
                        .medFødsel(familiehendelseDato))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(søkerMor))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private Kontoer.Builder foreldrepengerKonto(int trekkdager) {
        return new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(trekkdager));
    }

    private Kontoer.Builder foreldrepengerOgFlerbarnsdagerKonto(int foreldrepengerTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FORELDREPENGER)
                        .medTrekkdager(foreldrepengerTrekkdager))
                .leggTilKonto(new Konto.Builder()
                        .medType(Stønadskontotype.FLERBARNSDAGER)
                        .medTrekkdager(flerbarnsdagerTrekkdager));
    }

    private Søknad.Builder søknad(OppgittPeriode uttakPeriode) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .medMottattDato(uttakPeriode.getFom().minusWeeks(1))
                .leggTilOppgittPeriode(uttakPeriode);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom) {
        return oppgittPeriode(fom, tom, PeriodeVurderingType.IKKE_VURDERT);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return DelRegelTestUtil.oppgittPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, vurderingType);
    }
}
