package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.UKJENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ManglendeSøktPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class ManglendeSøktePerioderTjenesteTest {

    private final Konfigurasjon konfigurasjon = StandardKonfigurasjon.KONFIGURASJON;
    private final int mødrekvoteDager = konfigurasjon.getParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, LocalDate.of(2018, 06, 01));
    private final int fedrekvoteDager = konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, LocalDate.of(2018, 06, 01));
    private final int fellesperiodDedager = konfigurasjon.getParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, LocalDate.of(2018, 06, 01));
    private final int førFødselDager = konfigurasjon.getParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 06, 01));

    @Test
    public void farMedAleneomsorgSkalHaUttakFraFødsel() {
        // Case: PFP-6988. Far med aleneomsorg, 80% dekningsgrad, søkt uttak 9 uker etter fødsel.
        // Det skal legges til manglende periode fra fødselen og frem til hans første uttaksdag.
        // Skjæringstidspunktet for opptjening og beregning er ved hans første uttaksdag, men vi må trekke dager fra fødselen eller omsorgsovertakelsedatoen ved adopsjon.

        LocalDate fødsel = LocalDate.of(2018, 12, 4);
        LocalDate førsteUttakSøktFom = fødsel.plusWeeks(9);

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, førsteUttakSøktFom,
                                førsteUttakSøktFom.plusWeeks(10), null, false)))
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

        LocalDate adopsjonsDato = LocalDate.of(2018, 12, 4);
        LocalDate førsteUttakSøktFom = adopsjonsDato.plusWeeks(9);

        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, førsteUttakSøktFom,
                                førsteUttakSøktFom.plusWeeks(10), null, false)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medAleneomsorg(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(adopsjonsDato))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(adopsjonsDato)
                        .medStebarnsadopsjon(false))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(adopsjonsDato);
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    private RegelGrunnlag.Builder grunnlagMedKontoer() {
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, førFødselDager))
                .leggTilKonto(konto(MØDREKVOTE, mødrekvoteDager))
                .leggTilKonto(konto(FELLESPERIODE, fellesperiodDedager))
                .leggTilKonto(konto(FEDREKVOTE, fedrekvoteDager));
        return RegelGrunnlagTestBuilder.create()
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(LocalDate.MIN))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer)));
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager);
    }

    @Test
    public void skalIkkeUtledeMspIForeldrepengerFørFødselDersomSøknadstypeErAdopsjon() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                familiehendelsesDato, familiehendelsesDato.plusWeeks(7), null, false);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelsesDato))
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON).leggTilSøknadsperiode(uttakPeriode))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(LocalDate.of(2018, 6, 5)))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        Optional<ManglendeSøktPeriode> mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(mspPeriode -> mspPeriode.getStønadskontotype().equals(FORELDREPENGER_FØR_FØDSEL))
                .findFirst();

        assertThat(mspFørFødsel).isNotPresent();
    }

    @Test
    public void skalUtledeMspIFellesperiodeFørFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4);


        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(5), startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2), null, false);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(uttakPeriode))
                .build();


        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        Optional<ManglendeSøktPeriode> mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(periode -> periode.getStønadskontotype().equals(FELLESPERIODE))
                .findFirst();

        assertThat(mspFørFødsel).isPresent();
        mspFørFødsel.ifPresent(msp -> {
            assertThat(msp.getFom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2).plusDays(1));
            assertThat(msp.getTom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusDays(3)); //-3 pga helg
        });
    }

    @Test
    public void skal_ikke_overlappe_msp_fpff_og_msp_fellesperiode_før_fødsel() {
        var familiehendelsesDato = LocalDate.of(2019, 12, 11);

        var fellesperiode1 = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                familiehendelsesDato.minusWeeks(10), familiehendelsesDato.minusWeeks(8), null, false);
        var fellesperiode2 = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                familiehendelsesDato.minusWeeks(7), familiehendelsesDato.minusWeeks(5), null, false);
        var fpff = new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD,
                familiehendelsesDato.minusWeeks(3), familiehendelsesDato.minusWeeks(2), null, false);
        var mødrekvote = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                familiehendelsesDato, familiehendelsesDato.plusWeeks(6).minusDays(1), null, false);
        var grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(fellesperiode1)
                        .leggTilSøknadsperiode(fellesperiode2)
                        .leggTilSøknadsperiode(fpff)
                        .leggTilSøknadsperiode(mødrekvote))
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
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder().leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, sluttMødrekvoteEtterFødsel(familiehendelsesDato),
                        sluttMødrekvoteEtterFødsel(familiehendelsesDato).plusWeeks(1), null, false)))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        Optional<ManglendeSøktPeriode> mspEtterFødsel = manglendeSøktePerioder.stream()
                .filter(msp -> msp.getStønadskontotype().equals(MØDREKVOTE))
                .findFirst();

        assertThat(mspEtterFødsel).isPresent();

        mspEtterFødsel.ifPresent(msp -> {
            assertThat(msp.getFom()).isEqualTo(familiehendelsesDato);
            assertThat(msp.getTom()).isEqualTo(sluttMødrekvoteEtterFødsel(familiehendelsesDato).minusDays(3));//-3 pga helg
        });
    }

    @Test
    public void skalIkkeUtledeMspIPerioderFørEndringsdato() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder())
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelsesDato))
                .medBehandling(new Behandling.Builder())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(LocalDate.of(2019, 6, 4)))
                .build();

        var manglendeSøktePerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);

        assertThat(manglendeSøktePerioder).isEmpty();
    }

    @Test
    public void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioder() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate hullDato = fødselsdato.plusWeeks(6);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, hullDato.minusDays(1), null, false)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(hullDato);
        assertThat(msp.get(0).getTom()).isEqualTo(hullDato);
    }

    @Test
    public void finnerIkkeHullFørRevurderingEndringsdato() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate hullDato = fødselsdato.plusWeeks(6);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, hullDato.minusDays(1), null, false)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build()))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2019, 1, 1)))
                .medBehandling(new Behandling.Builder())
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);
        assertThat(msp).isEmpty();
    }


    @Test
    public void overlappendePerioderMedAnnenPartUtenHull() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10), null, false)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8)).build())
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(9).plusDays(1), fødselsdato.plusWeeks(11)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(0);
    }

    @Test
    public void helgErIkkeHull() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate mødrekvoteSlutt = LocalDate.of(2018, 7, 20);
        LocalDate annenPartStart = LocalDate.of(2018, 7, 23);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, mødrekvoteSlutt, null, false)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(annenPartStart, annenPartStart.plusWeeks(10)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).isEmpty();
    }

    @Test
    public void skalLageManglendeSøktFraUke7TilFørsteUttaksdagNårBareFarHarRett() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(7),
                                familiehendelse.plusWeeks(8), null, false)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .medDatoer(new Datoer.Builder().medFødsel(familiehendelse))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void skalLageManglendeSøktSomGårOver6UkerEtterFødsel() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 27);

        LocalDate søknadsperiodeFom = familiehendelse.minusWeeks(3);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, søknadsperiodeFom,
                                familiehendelse.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, familiehendelse,
                                familiehendelse.plusWeeks(3), null, false))
                        .leggTilSøknadsperiode(new UtsettelsePeriode(PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(6).plusDays(1),
                                familiehendelse.plusWeeks(8), Utsettelseårsaktype.ARBEID, PeriodeVurderingType.PERIODE_OK)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
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
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        LocalDate søknadsperiodeFom = familiehendelse.plusWeeks(9);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, søknadsperiodeFom,
                                familiehendelse.plusWeeks(10), null, false)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(søknadsperiodeFom))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp.get(0).getFom()).isNotEqualTo(familiehendelse.plusWeeks(7));
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagVedAdopsjon() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(1),
                                familiehendelse.plusWeeks(3), null, false)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(1).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void skalIkkeLageManglendeSøktUke7VedAdopsjonTilFørsteUttaksdagNårBareFarHarRett() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(7),
                                familiehendelse.plusWeeks(8), null, false)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void skalLageManglendeSøktFraAnkomstNorgeDatoTilFørsteUttaksdagVedAdopsjon() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(1),
                                familiehendelse.plusWeeks(3), null, false)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(familiehendelse.plusDays(3)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void skalIkkeLageManglendeSøktFrOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartHarUttakITidsrommet() {
        LocalDate omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, førsteUttaksdato,
                                førsteUttaksdato.plusWeeks(2), null, false)))
                .medBehandling(new Behandling.Builder().medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medDatoer(new Datoer.Builder().medOmsorgsovertakelse(omsorgsovertakelse))
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(null))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusDays(1)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartIkkeHarTattHelePeriodenFramTilSøkersFørsteUttaksdato() {
        LocalDate omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, førsteUttaksdato,
                                førsteUttaksdato.plusWeeks(2), null, false)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusWeeks(1)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagAnnenpartHvisAnnenpartHarUttakMidtMellomOmsorgsovertakelseOgSøkersFørsteUttaksdag() {
        LocalDate omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, førsteUttaksdato,
                                førsteUttaksdato.plusWeeks(2), null, false)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(omsorgsovertakelse.plusWeeks(1), førsteUttaksdato.minusWeeks(1)).build()))
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
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(1),
                                familiehendelse.plusWeeks(3), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(5),
                                familiehendelse.plusWeeks(7), null, false)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(familiehendelse))
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(familiehendelse.plusDays(3)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(UKJENT);

        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(1).getFom().minusDays(1));
        assertThat(msp.get(1).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void manglende_fpff_skal_ikke_slutte_i_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 11);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, LocalDate.of(2018, 5, 21), LocalDate.of(2018, 6, 7), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 6, 11), LocalDate.of(2018, 8, 17), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 9, 10), LocalDate.of(2018, 10, 12), null, false)))
                .medAnnenPart(new AnnenPart.Builder()
                        // Annen part starter mandagen etter
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(LocalDate.of(2018, 8, 20), LocalDate.of(2018, 9, 8))
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
        LocalDate fødselsdato = LocalDate.of(2018, 6, 11);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), null, false)))
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skalOppretteManglendeSøktFraSkjæringstidspunktForOpptjening() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 13);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(2), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), null, false)))
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato.minusWeeks(1)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getOpptjening().getSkjæringstidspunkt());
        assertThat(msp.get(0).getTom()).isEqualTo(fødselsdato.minusDays(1));
    }

    @Test
    public void skalIkkeOppretteManglendeSøktBasertPåAnnenPartFørSkjæringstidspunktForOpptjening() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 13);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), null, false)))
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato.plusWeeks(7)))
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8)).build()))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_manglende_søkt_før_endringsdato() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 13);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato))
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(22), null, false))
                        .medMottattDato(fødselsdato.plusWeeks(17)))
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato.plusWeeks(7)))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false))
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(fødselsdato.plusWeeks(18)))
                .build();

        var msp = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getRevurdering().getEndringsdato());
        assertThat(msp.get(0).getTom()).isEqualTo(fødselsdato.plusWeeks(20).minusDays(1));
    }

    private LocalDate startForeldrepengerFørFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.minusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelsesDato));
    }

    private LocalDate sluttMødrekvoteEtterFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.plusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelsesDato));
    }

}
