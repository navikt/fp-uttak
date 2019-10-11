package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype.MANGLENDE_SØKT_PERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.UKJENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
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
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class OppholdPeriodeTjenesteTest {

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
                                førsteUttakSøktFom.plusWeeks(10), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødsel)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(fødsel);
        assertThat(hull.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
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
                                førsteUttakSøktFom.plusWeeks(10), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medAleneomsorg(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(adopsjonsDato)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(adopsjonsDato)
                        .medStebarnsadopsjon(false)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(adopsjonsDato);
        assertThat(hull.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    public void skalUtledeOppholdIForeldrepengerFørFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelsesDato)
                        .build())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL).build())
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdFørFødsel = oppholdPerioder.stream()
                .filter(oppholdPeriode -> oppholdPeriode.getStønadskontotype().equals(FORELDREPENGER_FØR_FØDSEL))
                .findFirst();

        assertThat(oppholdFørFødsel).isPresent();
        oppholdFørFødsel.ifPresent(opphold -> {
            assertThat(opphold.getOppholdårsaktype()).isEqualTo(MANGLENDE_SØKT_PERIODE);
            assertThat(opphold.getFom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato));
            assertThat(opphold.getTom()).isEqualTo(sluttForeldrepengerFørFødsel(familiehendelsesDato));
        });
    }

    private RegelGrunnlag.Builder grunnlagMedKontoer() {
        return RegelGrunnlagTestBuilder.create()
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(LocalDate.MIN).build())
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(FORELDREPENGER_FØR_FØDSEL, førFødselDager))
                        .leggTilKonto(konto(MØDREKVOTE, mødrekvoteDager))
                        .leggTilKonto(konto(FELLESPERIODE, fellesperiodDedager))
                        .leggTilKonto(konto(FEDREKVOTE, fedrekvoteDager))
                        .build());
    }

    private Konto konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager)
                .build();
    }

    @Test
    public void skalIkkeUtledeOppholdIForeldrepengerFørFødselDersomSøknadstypeErAdopsjon() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                familiehendelsesDato, familiehendelsesDato.plusWeeks(7), null, false);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(familiehendelsesDato)
                        .build())
                .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON).leggTilSøknadsperiode(uttakPeriode).build())
                .medAdopsjon(new Adopsjon.Builder().medAnkomstNorge(LocalDate.of(2018, 6, 5)).build())
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdFørFødsel = oppholdPerioder.stream()
                .filter(oppholdPeriode -> oppholdPeriode.getStønadskontotype().equals(FORELDREPENGER_FØR_FØDSEL))
                .findFirst();

        assertThat(oppholdFørFødsel).isNotPresent();
    }

    @Test
    public void skalUtledeOppholdIFellesperiodeFørFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4);


        StønadsPeriode uttakPeriode = new StønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(5), startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2), null, false);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelsesDato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(uttakPeriode)
                        .build())
                .build();


        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdFørFødsel = oppholdPerioder.stream()
                .filter(oppholdPeriode -> oppholdPeriode.getStønadskontotype().equals(FELLESPERIODE))
                .findFirst();

        assertThat(oppholdFørFødsel).isPresent();
        oppholdFørFødsel.ifPresent(opphold -> {
            assertThat(opphold.getOppholdårsaktype()).isEqualTo(MANGLENDE_SØKT_PERIODE);
            assertThat(opphold.getFom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2).plusDays(1));
            assertThat(opphold.getTom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusDays(3)); //-3 pga helg
        });
    }

    @Test
    public void skalUtledeOppholdMødrekvoteEtterFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder().build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelsesDato)
                        .build())
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdEtterFødsel = oppholdPerioder.stream()
                .filter(opphold -> opphold.getStønadskontotype().equals(MØDREKVOTE))
                .findFirst();

        assertThat(oppholdEtterFødsel).isPresent();

        oppholdEtterFødsel.ifPresent(opphold -> {
            assertThat(opphold.getOppholdårsaktype()).isEqualTo(MANGLENDE_SØKT_PERIODE);
            assertThat(opphold.getFom()).isEqualTo(startMødrekvoteEtterFødsel(familiehendelsesDato));
            assertThat(opphold.getTom()).isEqualTo(sluttMødrekvoteEtterFødsel(familiehendelsesDato).minusDays(3));//-3 pga helg
        });
    }

    @Test
    public void skalIkkeUtledeOppholdIPerioderFørEndringsdato() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder().build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelsesDato)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2019, 6, 4))
                        .build())
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isEmpty();
    }

    @Test
    public void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioder() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate hullDato = fødselsdato.plusWeeks(6);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, hullDato.minusDays(1), null, false))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(hullDato);
        assertThat(hull.get(0).getTom()).isEqualTo(hullDato);
    }

    @Test
    public void finnerIkkeHullFørRevurderingEndringsdato() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate hullDato = fødselsdato.plusWeeks(6);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, hullDato.minusDays(1), null, false))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build())
                        .build())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(LocalDate.of(2019, 1, 1))
                        .build())
                .medBehandling(new Behandling.Builder().build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon);
        assertThat(hull).isEmpty();
    }


    @Test
    public void overlappendePerioderMedAnnenPartUtenHull() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10), null, false))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8)).build())
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(9).plusDays(1), fødselsdato.plusWeeks(11)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(hull).hasSize(0);
    }

    @Test
    public void helgErIkkeHull() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate mødrekvoteSlutt = LocalDate.of(2018, 7, 20);
        LocalDate annenPartStart = LocalDate.of(2018, 7, 23);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, mødrekvoteSlutt, null, false))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(annenPartStart, annenPartStart.plusWeeks(10)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(hull).isEmpty();
    }

    @Test
    public void manglende_fpff_skal_ikke_starte_i_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 9);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, LocalDate.of(2018, 5, 22), LocalDate.of(2018, 6, 8), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 6, 11), LocalDate.of(2018, 8, 17), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 9, 10), LocalDate.of(2018, 10, 12), null, false))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        // Annen part starter mandagen etter
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(LocalDate.of(2018, 8, 20), LocalDate.of(2018, 9, 8))
                                .medSamtidigUttak(true)
                                .build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        //Sjekk at det bare opprettes manglende søkt periode for mandag 21. mai 2018. Helg skal ikke tas med.
        assertThat(hull).hasSize(1);
        OppholdPeriode hull0 = hull.get(0);
        assertThat(hull0.getOppholdårsaktype()).isEqualTo(Oppholdårsaktype.MANGLENDE_SØKT_PERIODE);
        assertThat(hull0.getFom()).isEqualTo(LocalDate.of(2018, 5, 21));
        assertThat(hull0.getTom()).isEqualTo(LocalDate.of(2018, 5, 21));
    }

    @Test
    public void skalLageManglendeSøktFraUke7TilFørsteUttaksdagNårBareFarHarRett() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(7),
                                familiehendelse.plusWeeks(8), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelse)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(hull.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
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
                                familiehendelse.plusWeeks(8), Utsettelseårsaktype.ARBEID, PeriodeVurderingType.PERIODE_OK, null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(søknadsperiodeFom)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelse)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(3).plusDays(1));
        assertThat(hull.get(0).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6).minusDays(1));
        assertThat(hull.get(1).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(hull.get(1).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    public void skalLageManglendeSøktFraUke7FørStpOpptjeningHvisEtterFødselNårBareFarHarRett() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        LocalDate søknadsperiodeFom = familiehendelse.plusWeeks(9);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, søknadsperiodeFom,
                                familiehendelse.plusWeeks(10), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(søknadsperiodeFom)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(familiehendelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull.get(0).getFom()).isNotEqualTo(familiehendelse.plusWeeks(7));
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagVedAdopsjon() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(1),
                                familiehendelse.plusWeeks(3), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(familiehendelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(hull.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(1).minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void skalIkkeLageManglendeSøktUke7VedAdopsjonTilFørsteUttaksdagNårBareFarHarRett() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(7),
                                familiehendelse.plusWeeks(8), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(false)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(familiehendelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(hull.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void skalLageManglendeSøktFraAnkomstNorgeDatoTilFørsteUttaksdagVedAdopsjon() {
        LocalDate familiehendelse = LocalDate.of(2018, 12, 4);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, familiehendelse.plusWeeks(1),
                                familiehendelse.plusWeeks(3), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(familiehendelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(familiehendelse.plusDays(3))
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(hull.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void skalIkkeLageManglendeSøktFrOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartHarUttakITidsrommet() {
        LocalDate omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, førsteUttaksdato,
                                førsteUttaksdato.plusWeeks(2), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(false)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusDays(1)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).isEmpty();
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartIkkeHarTattHelePeriodenFramTilSøkersFørsteUttaksdato() {
        LocalDate omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, førsteUttaksdato,
                                førsteUttaksdato.plusWeeks(2), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusWeeks(1)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(hull.get(0).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
    }

    @Test
    public void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagAnnenpartHvisAnnenpartHarUttakMidtMellomOmsorgsovertakelseOgSøkersFørsteUttaksdag() {
        LocalDate omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.ADOPSJON)
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, førsteUttaksdato,
                                førsteUttaksdato.plusWeeks(2), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(omsorgsovertakelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(null)
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(omsorgsovertakelse.plusWeeks(1), førsteUttaksdato.minusWeeks(1)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(omsorgsovertakelse);
        assertThat(hull.get(0).getTom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(hull.get(1).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(hull.get(1).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
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
                                familiehendelse.plusWeeks(7), null, false))
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .build())
                .medDatoer(new Datoer.Builder()
                        .medOmsorgsovertakelse(familiehendelse)
                        .build())
                .medAdopsjon(new Adopsjon.Builder()
                        .medAnkomstNorge(familiehendelse.plusDays(3))
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(hull.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(hull.get(0).getStønadskontotype()).isEqualTo(UKJENT);

        assertThat(hull.get(1).getFom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(hull.get(1).getTom()).isEqualTo(grunnlag.getSøknad().getUttaksperioder().get(1).getFom().minusDays(1));
        assertThat(hull.get(1).getStønadskontotype()).isEqualTo(UKJENT);
    }

    @Test
    public void manglende_fpff_skal_ikke_slutte_i_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 11);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, LocalDate.of(2018, 5, 21), LocalDate.of(2018, 6, 7), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 6, 11), LocalDate.of(2018, 8, 17), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 9, 10), LocalDate.of(2018, 10, 12), null, false))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        // Annen part starter mandagen etter
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(LocalDate.of(2018, 8, 20), LocalDate.of(2018, 9, 8))
                                .medSamtidigUttak(true)
                                .build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        //Lager manglende søkt bare for fredagen, ok ikke helgen
        assertThat(hull).hasSize(1);
        OppholdPeriode hull0 = hull.get(0);
        assertThat(hull0.getOppholdårsaktype()).isEqualTo(Oppholdårsaktype.MANGLENDE_SØKT_PERIODE);
        assertThat(hull0.getFom()).isEqualTo(LocalDate.of(2018, 6, 8));
        assertThat(hull0.getTom()).isEqualTo(LocalDate.of(2018, 6, 8));
    }

    @Test
    public void skalIkkeOppretteManglendeSøktFørSkjæringstidspunktForOpptjening() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 11);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), null, false))
                        .build())
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato)
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).isEmpty();
    }

    @Test
    public void skalOppretteManglendeSøktFraSkjæringstidspunktForOpptjening() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 13);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), null, false))
                        .build())
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato.minusWeeks(1))
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        OppholdPeriode hull0 = hull.get(0);
        assertThat(hull0.getOppholdårsaktype()).isEqualTo(Oppholdårsaktype.MANGLENDE_SØKT_PERIODE);
        assertThat(hull0.getFom()).isEqualTo(grunnlag.getOpptjening().getSkjæringstidspunkt());
        assertThat(hull0.getTom()).isEqualTo(fødselsdato.minusDays(1));
    }

    @Test
    public void skalIkkeOppretteManglendeSøktBasertPåAnnenPartFørSkjæringstidspunktForOpptjening() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 13);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .build())
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), null, false))
                        .leggTilSøknadsperiode(new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6), null, false))
                        .build())
                .medOpptjening(new Opptjening.Builder()
                        .medSkjæringstidspunkt(fødselsdato.plusWeeks(7))
                        .build())
                .medAnnenPart(new AnnenPart.Builder()
                        .leggTilUttaksperiode(AnnenpartUttaksperiode.Builder.uttak(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8)).build())
                        .build())
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_manglende_søkt_før_endringsdato() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 13);

        RegelGrunnlag grunnlag = grunnlagMedKontoer()
            .medDatoer(new Datoer.Builder()
                .medFødsel(fødselsdato)
                .build())
            .medSøknad(new Søknad.Builder()
                .leggTilSøknadsperiode(new StønadsPeriode(FORELDREPENGER, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(22), null, false))
                    .medMottattDato(fødselsdato.plusWeeks(17))
                .build())
            .medOpptjening(new Opptjening.Builder()
                .medSkjæringstidspunkt(fødselsdato.plusWeeks(7))
                .build())
            .medBehandling(new Behandling.Builder()
                .medSøkerErMor(false)
                .build())
            .medRettOgOmsorg(new RettOgOmsorg.Builder()
                .medFarHarRett(true)
                .medMorHarRett(false)
                .build())
            .medRevurdering(new Revurdering.Builder()
                .medEndringsdato(fødselsdato.plusWeeks(18))
                .build()
            )
            .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(hull).hasSize(1);
        OppholdPeriode hull0 = hull.get(0);
        assertThat(hull0.getOppholdårsaktype()).isEqualTo(Oppholdårsaktype.MANGLENDE_SØKT_PERIODE);
        assertThat(hull0.getFom()).isEqualTo(grunnlag.getRevurdering().getEndringsdato());
        assertThat(hull0.getTom()).isEqualTo(fødselsdato.plusWeeks(20).minusDays(1));
    }

    /* Hjelpemetoder */
    private LocalDate startForeldrepengerFørFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.minusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelsesDato));
    }

    private LocalDate sluttForeldrepengerFørFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.minusDays(3);//-3 pga helg
    }

    private LocalDate startMødrekvoteEtterFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato;
    }

    private LocalDate sluttMødrekvoteEtterFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.plusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelsesDato));
    }

}
