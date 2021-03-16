package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class ManglendeSøktePerioderTjenesteTest {

    private final Konfigurasjon konfigurasjon = StandardKonfigurasjon.KONFIGURASJON;
    private final int mødrekvoteDager = konfigurasjon.getParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT,
            LocalDate.of(2018, 6, 1));
    private final int fedrekvoteDager = konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT,
            LocalDate.of(2018, 6, 1));
    private final int fellesperiodDedager = konfigurasjon.getParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER,
            LocalDate.of(2018, 6, 1));
    private final int førFødselDager = konfigurasjon.getParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 6, 1));

    @Test
    public void farMedAleneomsorgSkalHaUttakFraFødsel() {
        // Case: PFP-6988. Far med aleneomsorg, 80% dekningsgrad, søkt uttak 9 uker etter fødsel.
        // Det skal legges til manglende periode fra fødselen og frem til hans første uttaksdag.
        // Skjæringstidspunktet for opptjening og beregning er ved hans første uttaksdag, men vi må trekke dager fra fødselen eller omsorgsovertakelsedatoen ved adopsjon.

        var fødsel = LocalDate.of(2018, 12, 4);
        var førsteUttakSøktFom = fødsel.plusWeeks(9);

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, førsteUttakSøktFom, førsteUttakSøktFom.plusWeeks(10))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medAleneomsorg(true))
                .medDatoer(new Datoer.Builder().medFødsel(fødsel))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(fødsel);
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void farMedAleneomsorgSkalHaUttakFraAdopsjon() {
        // Case: PFP-6988. Far med aleneomsorg, 80% dekningsgrad, søkt uttak 9 uker etter adopsjon.
        // Det skal legges til manglende periode fra fødselen og frem til hans første uttaksdag.

        var adopsjonsDato = LocalDate.of(2018, 12, 4);
        var førsteUttakSøktFom = adopsjonsDato.plusWeeks(9);

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, førsteUttakSøktFom, førsteUttakSøktFom.plusWeeks(10))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medAleneomsorg(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(adopsjonsDato))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(adopsjonsDato).medStebarnsadopsjon(false))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(adopsjonsDato);
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void farMedAleneomsorgMedInnvilgetAnnetPartPerioder() {
        // Gjelder der far først har søkt om aleneomsorg.
        var morTom = LocalDate.of(2020, 12, 3);
        var farFom = morTom.plusDays(10);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder().medFødsel(LocalDate.of(2020, 11, 5)))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2020, 11, 5), morTom).build()))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(
                        new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true).medAleneomsorg(true).medSamtykke(false))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, farFom, farFom.plusDays(10))))
                .build();
        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(1);
    }

    private RegelGrunnlag.Builder grunnlagMedKontoer() {
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, førFødselDager))
                .leggTilKonto(konto(MØDREKVOTE, mødrekvoteDager))
                .leggTilKonto(konto(FELLESPERIODE, fellesperiodDedager))
                .leggTilKonto(konto(FEDREKVOTE, fedrekvoteDager));
        return RegelGrunnlagTestBuilder.create()
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(LocalDate.MIN))
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)));
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder().medType(stønadskontotype).medTrekkdager(trekkdager);
    }

    @Test
    public void skalIkkeUtledeMspIForeldrepengerFørFødselDersomSøknadstypeErAdopsjon() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var uttakPeriode = oppgittPeriode(FELLESPERIODE, familiehendelsesDato, familiehendelsesDato.plusWeeks(7));

        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelsesDato))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON).leggTilOppgittPeriode(uttakPeriode))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(LocalDate.of(2018, 6, 5)))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        var mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(mspPeriode -> FORELDREPENGER_FØR_FØDSEL.equals(mspPeriode.getStønadskontotype()))
                .findFirst();

        assertThat(mspFørFødsel).isNotPresent();
    }

    @Test
    public void skalUtledeMspIFellesperiodeFørFødsel() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4);


        var uttakPeriode = oppgittPeriode(FELLESPERIODE, startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(5),
                startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2));
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(uttakPeriode))
                .build();


        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        var mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(periode -> periode.getStønadskontotype().equals(FELLESPERIODE))
                .findFirst();

        assertThat(mspFørFødsel).isPresent();
        mspFørFødsel.ifPresent(msp -> {
            assertThat(msp.getFom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2).plusDays(1));
            assertThat(msp.getTom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusDays(3)); //-3 pga helg
        });
    }

    private OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, PeriodeVurderingType.IKKE_VURDERT, null, null);
    }

    @Test
    public void skal_ikke_overlappe_msp_fpff_og_msp_fellesperiode_før_fødsel() {
        var familiehendelsesDato = LocalDate.of(2019, 12, 11);

        var fellesperiode1 = oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(10), familiehendelsesDato.minusWeeks(8));
        var fellesperiode2 = oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(7), familiehendelsesDato.minusWeeks(5));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, familiehendelsesDato.minusWeeks(3), familiehendelsesDato.minusWeeks(2));
        var mødrekvote = oppgittPeriode(FELLESPERIODE, familiehendelsesDato, familiehendelsesDato.plusWeeks(6).minusDays(1));
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(fellesperiode1)
                        .leggTilOppgittPeriode(fellesperiode2)
                        .leggTilOppgittPeriode(fpff)
                        .leggTilOppgittPeriode(mødrekvote))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder.get(0).getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(manglendeSøktePerioder.get(0).getFom()).isEqualTo(familiehendelsesDato.minusWeeks(8).plusDays(1));
        assertThat(manglendeSøktePerioder.get(0).getTom()).isEqualTo(familiehendelsesDato.minusWeeks(7).minusDays(1));

        assertThat(manglendeSøktePerioder.get(1).getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(manglendeSøktePerioder.get(1).getFom()).isEqualTo(familiehendelsesDato.minusWeeks(5).plusDays(1));
        assertThat(manglendeSøktePerioder.get(1).getTom()).isEqualTo(familiehendelsesDato.minusWeeks(3).minusDays(1));

        assertThat(manglendeSøktePerioder.get(2).getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(manglendeSøktePerioder.get(2).getFom()).isEqualTo(familiehendelsesDato.minusWeeks(2).plusDays(1));
        assertThat(manglendeSøktePerioder.get(2).getTom()).isEqualTo(familiehendelsesDato.minusDays(1));
    }

    @Test
    public void skalUtledeMspMødrekvoteEtterFødsel() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                oppgittPeriode(MØDREKVOTE, sluttMødrekvoteEtterFødsel(familiehendelsesDato),
                        sluttMødrekvoteEtterFødsel(familiehendelsesDato).plusWeeks(1))))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        var mspEtterFødsel = manglendeSøktePerioder.stream().filter(msp -> msp.getStønadskontotype().equals(MØDREKVOTE)).findFirst();

        assertThat(mspEtterFødsel).isPresent();

        mspEtterFødsel.ifPresent(msp -> {
            assertThat(msp.getFom()).isEqualTo(familiehendelsesDato);
            assertThat(msp.getTom()).isEqualTo(sluttMødrekvoteEtterFødsel(familiehendelsesDato).minusDays(3));//-3 pga helg
        });
    }

    @Test
    public void skalIkkeUtledeMspIPerioderFørEndringsdato() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder())
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .medBehandling(new Behandling.Builder())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(LocalDate.of(2019, 6, 4)))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isEmpty();
    }

    @Test
    public void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioder() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var hullDato = fødselsdato.plusWeeks(6);
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, hullDato.minusDays(1))))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(hullDato);
        assertThat(msp.get(0).getTom()).isEqualTo(hullDato);
    }

    @Test
    public void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioderAvslåttPeriodeUtenTrekkdagerOgUtbetaling() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var annenpartAvslåttMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse.plusWeeks(6),
                familiehendelse.plusWeeks(7))
                .medInnvilget(false)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartInnvilgetMødrekvote)
                        .leggTilUttaksperiode(annenpartAvslåttMødrekvote))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartAvslåttMødrekvote.getFom());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(8).minusDays(1));
    }

    @Test
    public void finnerIkkeHullFørRevurderingEndringsdato() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var hullDato = fødselsdato.plusWeeks(6);
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, hullDato.minusDays(1))))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build()))
                .medRevurdering(new Revurdering.Builder().medEndringsdato(LocalDate.of(2019, 1, 1)))
                .medBehandling(new Behandling.Builder())
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);
        assertThat(msp).isEmpty();
    }


    @Test
    public void overlappendePerioderMedAnnenPartUtenHull() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10))))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8)).build())
                        .leggTilUttaksperiode(
                                AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(9).plusDays(1), fødselsdato.plusWeeks(11))
                                        .build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(0);
    }

    @Test
    public void helgErIkkeHull() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var mødrekvoteSlutt = LocalDate.of(2018, 7, 20);
        var annenPartStart = LocalDate.of(2018, 7, 23);
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, mødrekvoteSlutt)))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(annenPartStart, annenPartStart.plusWeeks(10)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).isEmpty();
    }

    @Test
    public void skalLageManglendeSøktFraUke7TilFørsteUttaksdagNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(7), familiehendelse.plusWeeks(8))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void skalLageManglendeSøktSomGårOver6UkerEtterFødsel() {
        var familiehendelse = LocalDate.of(2018, 12, 27);

        var søknadsperiodeFom = familiehendelse.minusWeeks(3);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, søknadsperiodeFom, familiehendelse.minusDays(1)))
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse, familiehendelse.plusWeeks(3)))
                .leggTilOppgittPeriode(
                        OppgittPeriode.forUtsettelse(familiehendelse.plusWeeks(6).plusDays(1), familiehendelse.plusWeeks(8),
                                PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.ARBEID, null, null)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(søknadsperiodeFom))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(3).plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6).minusDays(1));
        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(1).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    public void skalLageManglendeSøktFraUke7FørStpOpptjeningHvisEtterFødselNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var søknadsperiodeFom = familiehendelse.plusWeeks(9);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, søknadsperiodeFom, familiehendelse.plusWeeks(10))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(søknadsperiodeFom))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp.get(0).getFom()).isNotEqualTo(familiehendelse.plusWeeks(7));
    }

    @Test
    public void skalLageManglendeSøktFraMellomForeldreFørStpForOpptjeningAnnenpartAvslåttSistePeriode() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var annenpartInnvilgetUtsettelse = AnnenpartUttakPeriode.Builder.utsettelse(familiehendelse.plusWeeks(6),
                familiehendelse.plusWeeks(7).minusDays(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var annenpartAvslåttMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse.plusWeeks(7),
                familiehendelse.plusWeeks(7).plusDays(2))
                .medInnvilget(false)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartInnvilgetMødrekvote)
                        .leggTilUttaksperiode(annenpartInnvilgetUtsettelse)
                        .leggTilUttaksperiode(annenpartAvslåttMødrekvote))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartAvslåttMødrekvote.getFom());
    }

    @Test
    public void skalLageManglendeSøktFraMellomForeldreFørStpForOpptjeningAnnenpartInnvilgetUtsettelseSistePeriode() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var annenpartInnvilgetUtsettelse = AnnenpartUttakPeriode.Builder.utsettelse(familiehendelse.plusWeeks(6),
                familiehendelse.plusWeeks(7).minusDays(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartInnvilgetMødrekvote)
                        .leggTilUttaksperiode(annenpartInnvilgetUtsettelse))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartInnvilgetUtsettelse.getTom().plusDays(1));
    }

    @Test
    public void skalLageManglendeSøktFraMellomForeldreFørStpForOpptjening() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .medInnvilget(true)
                .medUttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(annenpartInnvilgetMødrekvote))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartInnvilgetMødrekvote.getTom().plusDays(1));
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagVedAdopsjon() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(1).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagVedAdopsjonDerAnnenpartIkkeHarUttaksperioder() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                //Annenpart finnes, men har fått avslag på alle sine perioder
                .medAnnenPart(new AnnenPart.Builder())
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(1).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    public void skalIkkeLageManglendeSøktUke7VedAdopsjonTilFørsteUttaksdagNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(7), familiehendelse.plusWeeks(8))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    public void skalLageManglendeSøktFraAnkomstNorgeDatoTilFørsteUttaksdagVedAdopsjon() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(familiehendelse.plusDays(3)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    public void skalIkkeLageManglendeSøktFrOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartHarUttakITidsrommet() {
        var omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(FEDREKVOTE, førsteUttaksdato, førsteUttaksdato.plusWeeks(2))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusDays(1)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartIkkeHarTattHelePeriodenFramTilSøkersFørsteUttaksdato() {
        var omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, førsteUttaksdato, førsteUttaksdato.plusWeeks(2))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusWeeks(1)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagAnnenpartHvisAnnenpartHarUttakMidtMellomOmsorgsovertakelseOgSøkersFørsteUttaksdag() {
        var omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, førsteUttaksdato, førsteUttaksdato.plusWeeks(2))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(omsorgsovertakelse.plusWeeks(1), førsteUttaksdato.minusWeeks(1)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(omsorgsovertakelse);
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
    }

    @Test
    public void skalLageManglendeSøktFraAnkomstNorgeDatoTilFørsteUttaksdagVedAdopsjonPlussVanligeHullMellomPerioder() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON)
                .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3)))
                .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, familiehendelse.plusWeeks(5), familiehendelse.plusWeeks(7))))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(familiehendelse.plusDays(3)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();

        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(1).getFom().minusDays(1));
        assertThat(msp.get(1).getStønadskontotype()).isNull();
    }

    @Test
    public void manglende_fpff_skal_ikke_slutte_i_helg() {
        var fødselsdato = LocalDate.of(2018, 6, 11);

        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 5, 21), LocalDate.of(2018, 6, 7)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2018, 6, 11), LocalDate.of(2018, 8, 17)))
                        .leggTilOppgittPeriode(oppgittPeriode(FELLESPERIODE, LocalDate.of(2018, 9, 10), LocalDate.of(2018, 10, 12))))
                .medAnnenPart(new AnnenPart.Builder()
                        // Annen part starter mandagen etter
                        .leggTilUttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2018, 8, 20), LocalDate.of(2018, 9, 8))
                                .medSamtidigUttak(true)
                                .build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        //Lager manglende søkt bare for fredagen, ok ikke helgen
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 8));
        assertThat(msp.get(0).getTom()).isEqualTo(LocalDate.of(2018, 6, 8));
    }

    @Test
    public void skalIkkeOppretteManglendeSøktFørSkjæringstidspunktForOpptjening() {
        var fødselsdato = LocalDate.of(2018, 6, 11);

        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(
                        new Søknad.Builder().leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6))))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skalOppretteManglendeSøktFraSkjæringstidspunktForOpptjening() {
        var fødselsdato = LocalDate.of(2018, 6, 13);

        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(2)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6))))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato.minusWeeks(1)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getOpptjening().getSkjæringstidspunkt());
        assertThat(msp.get(0).getTom()).isEqualTo(fødselsdato.minusDays(1));
    }

    @Test
    public void skalIkkeOppretteManglendeSøktBasertPåAnnenPartFørSkjæringstidspunktForOpptjening() {
        var fødselsdato = LocalDate.of(2018, 6, 13);

        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .leggTilOppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6))))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato.plusWeeks(7)))
                .medAnnenPart(new AnnenPart.Builder().leggTilUttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_manglende_søkt_før_endringsdato() {
        var fødselsdato = LocalDate.of(2018, 6, 13);

        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(22),
                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato.plusWeeks(17), null);
        var grunnlag = grunnlagMedKontoer().medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(oppgittPeriode))
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato.plusWeeks(7)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(false))
                .medRevurdering(new Revurdering.Builder().medEndringsdato(fødselsdato.plusWeeks(18)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getRevurdering().getEndringsdato());
        assertThat(msp.get(0).getTom()).isEqualTo(fødselsdato.plusWeeks(20).minusDays(1));
    }

    private LocalDate startForeldrepengerFørFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.minusWeeks(
                konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelsesDato));
    }

    private LocalDate sluttMødrekvoteEtterFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.plusWeeks(
                konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelsesDato));
    }

}
