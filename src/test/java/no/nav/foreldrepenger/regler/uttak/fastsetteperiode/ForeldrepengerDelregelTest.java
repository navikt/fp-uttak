package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;

class ForeldrepengerDelregelTest {

    @Test
    void mor_starter_tidligere_enn_12_uker_før_termin() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(12).minusDays(1), familiehendelseDato);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(15))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL);
        assertThat(regelresultat.getManuellbehandlingårsak()).isNull();
    }

    @Test
    void mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato.minusWeeks(5));
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(false).aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    void utenAleneomsorg_morRett_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_morRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato.minusWeeks(5));
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true).aleneomsorg(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    void mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_ikkeBareMorRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var gradertPeriode = gradertPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato,
                AktivitetIdentifikator.forFrilans(), PeriodeVurderingType.PERIODE_OK);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(gradertPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG,
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    void mor_aleneomsorg_før6ukerEtterFødsel_disponibleDager_ikkeGradering() {
        var familiehendelseDato = LocalDate.of(2022, 10, 4);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato, familiehendelseDato.plusWeeks(6).minusDays(1));
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(false).morHarRett(false).aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    private OppgittPeriode gradertPeriode(LocalDate fom,
                                          LocalDate tom,
                                          AktivitetIdentifikator aktivitetIdentifikator,
                                          PeriodeVurderingType periodeVurderingType) {
        return gradertPeriode(fom, tom, aktivitetIdentifikator, periodeVurderingType, null, false);
    }

    private OppgittPeriode gradertPeriode(LocalDate fom,
                                          LocalDate tom,
                                          AktivitetIdentifikator aktivitetIdentifikator,
                                          PeriodeVurderingType vurderingType,
                                          SamtidigUttaksprosent samtidigUttaksprosent,
                                          boolean flerbarnsdager) {
        return OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, BigDecimal.TEN, samtidigUttaksprosent,
                flerbarnsdager, Set.of(aktivitetIdentifikator), vurderingType, null, null, null);
    }

    @Test
    void mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_bareMorRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var gradertPeriode = gradertPeriode(familiehendelseDato.minusWeeks(6), familiehendelseDato,
                AktivitetIdentifikator.forFrilans(), PeriodeVurderingType.PERIODE_OK);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(gradertPeriode))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true).morHarRett(true))
                .kontoer(foreldrepengerKonto(100))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT,
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    void mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_ikkeBareMorRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var gradertPeriode = gradertPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                AktivitetIdentifikator.forFrilans(), PeriodeVurderingType.PERIODE_OK);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(gradertPeriode))
                .kontoer(foreldrepengerKonto(100))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    void mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_morRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        var gradertPeriode = gradertPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8), aktivitetIdentifikator,
                PeriodeVurderingType.PERIODE_OK);
        var kontoer = foreldrepengerKonto(100);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(gradertPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true).morHarRett(true))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    void mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8));
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    void mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_morRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8));
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true).morHarRett(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    private void assertInnvilgetMenAvslåttGradering(FastsettePerioderRegelresultat regelresultat,
                                                    InnvilgetÅrsak innvilgetÅrsak,
                                                    GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak);
    }

    @Test
    void mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_noenDisponibleDager() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(12));
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                        .arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .kontoer(foreldrepengerKonto(10))
                .build();

        var fastsattePerioder = List.of(new FastsattUttakPeriode.Builder().periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(oppgittPeriode.getFom().minusWeeks(1), oppgittPeriode.getFom().minusDays(1))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(8), Stønadskontotype.FORELDREPENGER,
                        AktivitetIdentifikator.forFrilans())))
                .build());
        var regelresultat = kjørRegel(oppgittPeriode, grunnlag, fastsattePerioder);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1190");
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    void mor_aleneomsorg_etter6ukerEtterFødsel_utenOmsorg() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(6);
        var tom = familiehendelseDato.plusWeeks(7);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode)
                .type(Søknadstype.FØDSEL)
                .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    @Test
    void mor_utenAleneomsorg_ikkeBareMorRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(6);
        var tom = familiehendelseDato.plusWeeks(7);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode)
                .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(false).farHarRett(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    void _fødsel_bare_mor_rett_periode_før_fødsel() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.minusWeeks(3);
        var tom = familiehendelseDato.minusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(
                new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, "UT1192");
    }

    @Test
    void _fødsel_bare_mor_aleneomsorg_periode_før_fødsel() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.minusWeeks(3);
        var tom = familiehendelseDato.minusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagMor(familiehendelseDato).søknad(
                new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, "UT1197");
    }

    private void assertManuellBehandling(FastsettePerioderRegelresultat regelresultat,
                                         PeriodeResultatÅrsak periodeResultatÅrsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak) {
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
    void far_før_familiehendelse() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var oppgittPeriode = oppgittPeriode(familiehendelseDato.minusWeeks(3), familiehendelseDato.minusWeeks(2));
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL,
                Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL);
    }

    @Test
    void far_etterFamiliehendelse_aleneomsorg_utenOmsorg() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(1);
        var tom = familiehendelseDato.plusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode)
                .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    void far_etterFamiliehendelse_aleneomsorg_medOmsorg_utenDisponibledager() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(1);
        var tom = familiehendelseDato.plusWeeks(2);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(10))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                        .arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .build();

        var fastsattePerioder = List.of(new FastsattUttakPeriode.Builder().periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(fom.minusWeeks(1), fom.minusDays(1))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), Stønadskontotype.FORELDREPENGER,
                                AktivitetIdentifikator.forFrilans()),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(5), Stønadskontotype.FORELDREPENGER,
                                AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .build());
        var regelresultat = kjørRegel(oppgittPeriode, grunnlag, fastsattePerioder);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1198");
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    void far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_medGradering_førUke7() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(1);
        var tom = familiehendelseDato.plusWeeks(2);
        var aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        var gradertPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(gradertPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    void far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_utenGradering() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(4);
        var tom = familiehendelseDato.plusWeeks(5);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_utenOmsorg() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(4);
        var tom = familiehendelseDato.plusWeeks(5);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode)
                .type(Søknadstype.FØDSEL)
                .dokumentasjon(new Dokumentasjon.Builder().periodeUtenOmsorg(new PeriodeUtenOmsorg(fom, tom))))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_utenGyldigGrunn() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(4);
        var tom = familiehendelseDato.plusWeeks(5);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, null, false,
                PeriodeVurderingType.UAVKLART_PERIODE, null, null, null);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_utenGradering() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(4);
        var tom = familiehendelseDato.plusWeeks(5);
        var oppgittPeriode = oppgittPeriode(fom, tom, PeriodeVurderingType.PERIODE_OK);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, I_AKTIVITET));
        var søknad = søknad(oppgittPeriode).dokumentasjon(dokumentasjon);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad)
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false).aleneomsorg(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_utenGradering() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(9);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, I_AKTIVITET));
        var søknad = søknad(oppgittPeriode).dokumentasjon(dokumentasjon);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad)
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_medGradering_avklart() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(4);
        var tom = familiehendelseDato.plusWeeks(5);
        var gradertPeriode = gradertPeriode(fom, tom, AktivitetIdentifikator.forFrilans(), PeriodeVurderingType.PERIODE_OK);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, I_AKTIVITET));
        var søknad = søknad(gradertPeriode)
                .dokumentasjon(dokumentasjon);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad)
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_medGradering_avklart() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(9);
        var aktivitetIdentifikator = ARBEIDSFORHOLD_1;
        var gradertPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK);
        var kontoer = foreldrepengerKonto(100);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, I_AKTIVITET));
        var søknad = søknad(gradertPeriode)
                .dokumentasjon(dokumentasjon);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_utenDisponibleDagerPåAlleAktiviteter() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(9);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(fom, tom, I_AKTIVITET));
        var søknad = søknad(oppgittPeriode)
                .dokumentasjon(dokumentasjon);
        var arbeid = new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forFrilans()))
                .arbeidsforhold(new Arbeidsforhold(AktivitetIdentifikator.forSelvstendigNæringsdrivende()));
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad)
                .arbeid(arbeid)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .kontoer(foreldrepengerKonto(10))
                .build();

        var fastsattePerioder = List.of(new FastsattUttakPeriode.Builder().periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(fom.minusWeeks(1), fom.minusDays(1))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), Stønadskontotype.FORELDREPENGER,
                                AktivitetIdentifikator.forFrilans()),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(5), Stønadskontotype.FORELDREPENGER,
                                AktivitetIdentifikator.forSelvstendigNæringsdrivende())))
                .build());
        var regelresultat = kjørRegel(oppgittPeriode, grunnlag, fastsattePerioder);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1316");
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_utenFarRett() {
        var familiehendelseDato = LocalDate.of(2018, 1, 1);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(9);
        var oppgittPeriode = oppgittPeriode(fom, tom);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(foreldrepengerKonto(100))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(1);
        var tom = familiehendelseDato.plusWeeks(3);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, null, true,
                PeriodeVurderingType.IKKE_VURDERT, null, null, null);
        var kontoer = foreldrepengerOgFlerbarnsdagerKonto(40, 17);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_medDisponibleDager_medGradering_avklart() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(1);
        var tom = familiehendelseDato.plusWeeks(3);
        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var gradertPeriode = gradertPeriode(fom, tom, aktivitetIdentifikator, PeriodeVurderingType.PERIODE_OK, null, true);
        var kontoer = foreldrepengerOgFlerbarnsdagerKonto(40, 17);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(gradertPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(gradertPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_noenDisponibleDager() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(1);
        var tom = familiehendelseDato.plusWeeks(3);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, null, true,
                PeriodeVurderingType.IKKE_VURDERT, null, null, null);
        var kontoer = foreldrepengerOgFlerbarnsdagerKonto(100, 0);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.sluttpunktId()).isEqualTo("UT1269");
        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_morUføretrygdet() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(10);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, null, false,
                PeriodeVurderingType.IKKE_VURDERT, familiehendelseDato, familiehendelseDato, MorsAktivitet.UFØRE);
        var kontoer = foreldrepengerKonto(40).minsterettDager(10);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false).morUføretrygd(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, "UT1317");
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_morUføretrygdetGradert() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(10);
        var oppgittPeriode = OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, BigDecimal.TEN, SamtidigUttaksprosent.ZERO, false,
                Set.of(ARBEIDSFORHOLD_1), PeriodeVurderingType.IKKE_VURDERT, familiehendelseDato, familiehendelseDato, MorsAktivitet.UFØRE);
        var kontoer = foreldrepengerKonto(40).minsterettDager(10);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false).morUføretrygd(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, "UT1318");
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_morUføretrygdet_ikkeFrittUttak() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(10);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, null, false,
                PeriodeVurderingType.IKKE_VURDERT, familiehendelseDato, familiehendelseDato, MorsAktivitet.UFØRE);
        var kontoer = foreldrepengerKonto(40).utenAktivitetskravDager(10);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false).morUføretrygd(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, "UT1317");
    }

    @Test
    void far_etterFamiliehendelse_utenAleneomsorg_medFarRett_utenMorRett_morUføretrygdetGradert_ikkeFrittUttak() {
        var familiehendelseDato = LocalDate.now().minusMonths(2);
        var fom = familiehendelseDato.plusWeeks(8);
        var tom = familiehendelseDato.plusWeeks(10);
        var oppgittPeriode = OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, BigDecimal.TEN, SamtidigUttaksprosent.ZERO, false,
                Set.of(ARBEIDSFORHOLD_1), PeriodeVurderingType.IKKE_VURDERT, familiehendelseDato, familiehendelseDato, MorsAktivitet.UFØRE);
        var kontoer = foreldrepengerKonto(40).utenAktivitetskravDager(10);
        var grunnlag = grunnlagFar(familiehendelseDato).søknad(søknad(oppgittPeriode))
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD_1)))
                .kontoer(kontoer)
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false).morUføretrygd(true))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, "UT1318");
    }

    @Test
    void bfhr_rundt_fødsel_blir_innvilget() {
        var fødselsdato = LocalDate.of(2022, 10, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato, fødselsdato.plusWeeks(1).plusDays(1));
        var grunnlag = grunnlagFar(fødselsdato)
                .behandling(new Behandling.Builder().søkerErMor(false).kreverSammenhengendeUttak(false))
                .søknad(søknad(oppgittPeriode))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(false).farHarRett(true).aleneomsorg(false))
                .kontoer(foreldrepengerKonto(40 * 5).farUttakRundtFødselDager(10).minsterettDager(10))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
    }

    @Test
    void bfhr_rundt_fødsel_men_før_fødsel_blir_avslått() {
        var fødselsdato = LocalDate.of(2022, 10, 1);

        var oppgittPeriode = oppgittPeriode(fødselsdato.minusDays(2), fødselsdato.minusDays(1));
        var grunnlag = grunnlagFar(fødselsdato)
                .behandling(new Behandling.Builder().søkerErMor(false).kreverSammenhengendeUttak(false))
                .søknad(søknad(oppgittPeriode))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(false).farHarRett(true).aleneomsorg(false))
                .kontoer(foreldrepengerKonto(40 * 5).farUttakRundtFødselDager(10).minsterettDager(10))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
    }

    @Test
    void bfhr_rundt_termin_blir_innvilget() {
        var termindato = LocalDate.of(2022, 10, 1);

        var oppgittPeriode = oppgittPeriode(termindato.minusDays(3), termindato.plusWeeks(1).plusDays(1));
        var grunnlag = grunnlagFar(termindato)
                .behandling(new Behandling.Builder().søkerErMor(false).kreverSammenhengendeUttak(false))
                .søknad(søknad(oppgittPeriode))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(false).farHarRett(true).aleneomsorg(false))
                .datoer(new Datoer.Builder().termin(termindato))
                .kontoer(foreldrepengerKonto(40 * 5).farUttakRundtFødselDager(10).minsterettDager(10))
                .build();

        var regelresultat = kjørRegel(oppgittPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
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
                .datoer(new Datoer.Builder().fødsel(familiehendelseDato))
                .behandling(new Behandling.Builder().søkerErMor(søkerMor))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
                .inngangsvilkår(new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                        .foreldreansvarnOppfylt(true)
                        .fødselOppfylt(true)
                        .opptjeningOppfylt(true));
    }

    private Kontoer.Builder foreldrepengerKonto(int trekkdager) {
        return new Kontoer.Builder().konto(
                new Konto.Builder().type(Stønadskontotype.FORELDREPENGER).trekkdager(trekkdager));
    }

    private Kontoer.Builder foreldrepengerOgFlerbarnsdagerKonto(int foreldrepengerTrekkdager, int flerbarnsdagerTrekkdager) {
        return new Kontoer.Builder()
                .konto(new Konto.Builder().type(Stønadskontotype.FORELDREPENGER).trekkdager(foreldrepengerTrekkdager))
                .flerbarnsdager(flerbarnsdagerTrekkdager);
    }

    private Søknad.Builder søknad(OppgittPeriode uttakPeriode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom) {
        return oppgittPeriode(fom, tom, PeriodeVurderingType.IKKE_VURDERT);
    }

    private OppgittPeriode oppgittPeriode(LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return DelRegelTestUtil.oppgittPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, vurderingType);
    }
}
