package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktePerioderForSammenhengendeUttakTjeneste.finnManglendeSøktePerioder;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class ManglendeSøktePerioderForSammenhengendeUttakTjenesteTest {

    private final int mødrekvoteDager = 75;
    private final int fedrekvoteDager = 75;
    private final int fellesperiodDedager = 85;
    private final int førFødselDager = 15;

    @Test
    void farMedAleneomsorgSkalHaUttakFraFødsel() {
        // Case: PFP-6988. Far med aleneomsorg, 80% dekningsgrad, søkt uttak 9 uker etter fødsel.
        // Det skal legges til manglende periode fra fødselen og frem til hans første uttaksdag.
        // Skjæringstidspunktet for opptjening og beregning er ved hans første uttaksdag, men vi må trekke dager fra fødselen eller omsorgsovertakelsedatoen ved adopsjon.

        var fødsel = LocalDate.of(2018, 12, 4);
        var førsteUttakSøktFom = fødsel.plusWeeks(9);

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, førsteUttakSøktFom, førsteUttakSøktFom.plusWeeks(10))))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .datoer(new Datoer.Builder().fødsel(fødsel))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(fødsel);
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    private Behandling.Builder farBehandling() {
        return new Behandling.Builder().søkerErMor(false).kreverSammenhengendeUttak(true);
    }

    @Test
    void farMedAleneomsorgSkalHaUttakFraAdopsjon() {
        // Case: PFP-6988. Far med aleneomsorg, 80% dekningsgrad, søkt uttak 9 uker etter adopsjon.
        // Det skal legges til manglende periode fra fødselen og frem til hans første uttaksdag.

        var adopsjonsDato = LocalDate.of(2018, 12, 4);
        var førsteUttakSøktFom = adopsjonsDato.plusWeeks(9);

        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, førsteUttakSøktFom, førsteUttakSøktFom.plusWeeks(10))))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().aleneomsorg(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(adopsjonsDato))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(adopsjonsDato).stebarnsadopsjon(false))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(adopsjonsDato);
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttakSøktFom.minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void farMedAleneomsorgMedInnvilgetAnnetPartPerioder() {
        // Gjelder der far først har søkt om aleneomsorg.
        var morTom = LocalDate.of(2020, 12, 3);
        var farFom = morTom.plusDays(10);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .datoer(new Datoer.Builder().fødsel(LocalDate.of(2020, 11, 5)))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2020, 11, 5), morTom).build()))
                .behandling(farBehandling())
                .rettOgOmsorg(
                        new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true).aleneomsorg(true).samtykke(false))
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, farFom, farFom.plusDays(10))))
                .build();
        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(1);
    }

    private RegelGrunnlag.Builder grunnlagMedKontoer() {
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER_FØR_FØDSEL, førFødselDager))
                .konto(konto(MØDREKVOTE, mødrekvoteDager))
                .konto(konto(FELLESPERIODE, fellesperiodDedager))
                .konto(konto(FEDREKVOTE, fedrekvoteDager));
        return RegelGrunnlagTestBuilder.create()
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(LocalDate.MIN))
                .kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1)));
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder().type(stønadskontotype).trekkdager(trekkdager);
    }

    @Test
    void skalIkkeUtledeMspIForeldrepengerFørFødselDersomSøknadstypeErAdopsjon() {
        var omsorgsovertakelse = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var uttakPeriode = oppgittPeriode(FELLESPERIODE, omsorgsovertakelse, omsorgsovertakelse.plusWeeks(7));

        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON).oppgittPeriode(uttakPeriode))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(LocalDate.of(2018, 6, 5)))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        //Skal utlede msp mellom ankomst norge og første søkte dag
        assertThat(manglendeSøktePerioder).isNotEmpty();

        var mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(mspPeriode -> FORELDREPENGER_FØR_FØDSEL.equals(mspPeriode.getStønadskontotype()))
                .findFirst();

        assertThat(mspFørFødsel).isEmpty();
    }

    @Test
    void skalUtledeMspIFellesperiodeFørFødsel() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4);


        var uttakPeriode = oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(8),
                familiehendelsesDato.minusWeeks(5));
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .søknad(new Søknad.Builder().oppgittPeriode(uttakPeriode))
                .build();


        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        assertThat(manglendeSøktePerioder).isNotEmpty();

        var mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(periode -> periode.getStønadskontotype().equals(FELLESPERIODE))
                .findFirst();

        assertThat(mspFørFødsel).isPresent();
        mspFørFødsel.ifPresent(msp -> {
            assertThat(msp.getFom()).isEqualTo(familiehendelsesDato.minusWeeks(5).plusDays(1));
            assertThat(msp.getTom()).isEqualTo(familiehendelsesDato.minusWeeks(3).minusDays(3)); //-3 pga helg
        });
    }

    private OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, null, null,
                null, null);
    }

    @Test
    void skal_ikke_overlappe_msp_fpff_og_msp_fellesperiode_før_fødsel() {
        var familiehendelsesDato = LocalDate.of(2019, 12, 11);

        var fellesperiode1 = oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(10), familiehendelsesDato.minusWeeks(8));
        var fellesperiode2 = oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(7), familiehendelsesDato.minusWeeks(5));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, familiehendelsesDato.minusWeeks(3), familiehendelsesDato.minusWeeks(2));
        var fellesperiode3 = oppgittPeriode(FELLESPERIODE, familiehendelsesDato, familiehendelsesDato.plusWeeks(6).minusDays(1));
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(fellesperiode1)
                        .oppgittPeriode(fellesperiode2)
                        .oppgittPeriode(fpff)
                        .oppgittPeriode(fellesperiode3))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

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
    void skalUtledeMspMødrekvoteITidsperiodenForbeholdtMorEtterFødsel() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder()
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelsesDato.plusWeeks(6), familiehendelsesDato.plusWeeks(7))))
                .datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        var mspEtterFødsel = manglendeSøktePerioder.stream().filter(msp -> msp.getStønadskontotype().equals(MØDREKVOTE)).findFirst();

        assertThat(mspEtterFødsel).isPresent();

        mspEtterFødsel.ifPresent(msp -> {
            assertThat(msp.getFom()).isEqualTo(familiehendelsesDato);
            assertThat(msp.getTom()).isEqualTo(familiehendelsesDato.plusWeeks(6).minusDays(3));//-3 pga helg
        });
    }

    @Test
    void skalIkkeUtledeMspIPerioderFørEndringsdato() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder())
                .datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .behandling(new Behandling.Builder())
                .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2019, 6, 4)))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        assertThat(manglendeSøktePerioder).isEmpty();
    }

    @Test
    void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioder() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var hullDato = fødselsdato.plusWeeks(6);
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, hullDato.minusDays(1))))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(hullDato);
        assertThat(msp.get(0).getTom()).isEqualTo(hullDato);
    }

    @Test
    void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioderAvslåttPeriodeUtenTrekkdagerOgUtbetaling() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var annenpartAvslåttMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse.plusWeeks(6),
                familiehendelse.plusWeeks(7))
                .innvilget(false)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .annenPart(new AnnenPart.Builder().uttaksperiode(annenpartInnvilgetMødrekvote)
                        .uttaksperiode(annenpartAvslåttMødrekvote))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartAvslåttMødrekvote.getFom());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(8).minusDays(1));
    }

    @Test
    void finnerIkkeHullFørRevurderingEndringsdato() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var hullDato = fødselsdato.plusWeeks(6);
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, hullDato.minusDays(1))))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(hullDato.plusDays(1), fødselsdato.plusWeeks(10)).build()))
                .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2019, 1, 1)))
                .behandling(new Behandling.Builder())
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).isEmpty();
    }


    @Test
    void overlappendePerioderMedAnnenPartUtenHull() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10))))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8)).build())
                        .uttaksperiode(
                                AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(9).plusDays(1), fødselsdato.plusWeeks(11))
                                        .build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(0);
    }

    @Test
    void helgErIkkeHull() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var mødrekvoteSlutt = LocalDate.of(2018, 7, 20);
        var annenPartStart = LocalDate.of(2018, 7, 23);
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, mødrekvoteSlutt)))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(annenPartStart, annenPartStart.plusWeeks(10)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).isEmpty();
    }

    @Test
    void skalLageManglendeSøktFraUke7TilFørsteUttaksdagNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(7), familiehendelse.plusWeeks(8))))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skalLageManglendeSøktSomGårOver6UkerEtterFødsel() {
        var familiehendelse = LocalDate.of(2018, 12, 27);

        var søknadsperiodeFom = familiehendelse.minusWeeks(3);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, søknadsperiodeFom, familiehendelse.minusDays(1)))
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse, familiehendelse.plusWeeks(3)))
                .oppgittPeriode(
                        OppgittPeriode.forUtsettelse(familiehendelse.plusWeeks(6).plusDays(1), familiehendelse.plusWeeks(8),
                            UtsettelseÅrsak.ARBEID, null, null, null, null)))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(søknadsperiodeFom))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(3).plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6).minusDays(1));
        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(1).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    void skalLageManglendeSøktFraUke7FørStpOpptjeningHvisEtterFødselNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var søknadsperiodeFom = familiehendelse.plusWeeks(9);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER, søknadsperiodeFom, familiehendelse.plusWeeks(10))))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(søknadsperiodeFom))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp.get(0).getFom()).isNotEqualTo(familiehendelse.plusWeeks(7));
    }

    @Test
    void skalLageManglendeSøktFraMellomForeldreFørStpForOpptjeningAnnenpartAvslåttSistePeriode() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var annenpartInnvilgetUtsettelse = AnnenpartUttakPeriode.Builder.utsettelse(familiehendelse.plusWeeks(6),
                familiehendelse.plusWeeks(7).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var annenpartAvslåttMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse.plusWeeks(7),
                familiehendelse.plusWeeks(7).plusDays(2))
                .innvilget(false)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .annenPart(new AnnenPart.Builder().uttaksperiode(annenpartInnvilgetMødrekvote)
                        .uttaksperiode(annenpartInnvilgetUtsettelse)
                        .uttaksperiode(annenpartAvslåttMødrekvote))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartAvslåttMødrekvote.getFom());
    }

    @Test
    void skalLageManglendeSøktFraMellomForeldreFørStpForOpptjeningAnnenpartInnvilgetUtsettelseSistePeriode() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var annenpartInnvilgetUtsettelse = AnnenpartUttakPeriode.Builder.utsettelse(familiehendelse.plusWeeks(6),
                familiehendelse.plusWeeks(7).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, Trekkdager.ZERO,
                                Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .annenPart(new AnnenPart.Builder().uttaksperiode(annenpartInnvilgetMødrekvote)
                        .uttaksperiode(annenpartInnvilgetUtsettelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartInnvilgetUtsettelse.getTom().plusDays(1));
    }

    @Test
    void skalLageManglendeSøktFraMellomForeldreFørStpForOpptjening() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(6).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(
                        new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE, new Trekkdager(10),
                                Utbetalingsgrad.TEN))
                .build();
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(familiehendelse.plusWeeks(10)))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .annenPart(new AnnenPart.Builder().uttaksperiode(annenpartInnvilgetMødrekvote))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartInnvilgetMødrekvote.getTom().plusDays(1));
    }

    @Test
    void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagVedAdopsjon() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3))))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(1).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    private Behandling.Builder morBehandling() {
        return new Behandling.Builder().søkerErMor(true).kreverSammenhengendeUttak(true);
    }

    @Test
    void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagVedAdopsjonDerAnnenpartIkkeHarUttaksperioder() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3))))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                //Annenpart finnes, men har fått avslag på alle sine perioder
                .annenPart(new AnnenPart.Builder())
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(1).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(7), familiehendelse.plusWeeks(8))))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    void skalLageManglendeSøktFraAnkomstNorgeDatoTilFørsteUttaksdag() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3))))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(familiehendelse.plusDays(3)))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();
    }

    @Test
    void skalIkkeLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartHarUttakITidsrommet() {
        var omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(FEDREKVOTE, førsteUttaksdato, førsteUttaksdato.plusWeeks(2))))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusDays(1)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).isEmpty();
    }

    @Test
    void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagHvisAnnenpartIkkeHarTattHelePeriodenFramTilSøkersFørsteUttaksdato() {
        var omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, førsteUttaksdato, førsteUttaksdato.plusWeeks(2))))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(omsorgsovertakelse, førsteUttaksdato.minusWeeks(1)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
    }

    @Test
    void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagAnnenpartHvisAnnenpartHarUttakMidtMellomOmsorgsovertakelseOgSøkersFørsteUttaksdag() {
        var omsorgsovertakelse = LocalDate.of(2018, 12, 4);

        var førsteUttaksdato = omsorgsovertakelse.plusWeeks(5);
        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, førsteUttaksdato, førsteUttaksdato.plusWeeks(2))))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(omsorgsovertakelse.plusWeeks(1), førsteUttaksdato.minusWeeks(1)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(omsorgsovertakelse);
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getAnnenPart().getUttaksperioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(førsteUttaksdato.minusDays(1));
    }

    @Test
    void skalLageManglendeSøktFraAnkomstNorgeDatoTilFørsteUttaksdagPlussVanligeHullMellomPerioder() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer().søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse.plusWeeks(1), familiehendelse.plusWeeks(3)))
                .oppgittPeriode(oppgittPeriode(FELLESPERIODE, familiehendelse.plusWeeks(5), familiehendelse.plusWeeks(7))))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(familiehendelse.plusDays(3)))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getAdopsjon().getAnkomstNorgeDato());
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isNull();

        assertThat(msp.get(1).getFom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getTom().plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(1).getFom().minusDays(1));
        assertThat(msp.get(1).getStønadskontotype()).isNull();
    }

    @Test
    void manglende_fpff_skal_ikke_slutte_i_helg() {
        var fødselsdato = LocalDate.of(2018, 6, 11);

        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 5, 21), LocalDate.of(2018, 6, 7)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2018, 6, 11), LocalDate.of(2018, 8, 17)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, LocalDate.of(2018, 9, 10), LocalDate.of(2018, 10, 12))))
                .annenPart(new AnnenPart.Builder()
                        // Annen part starter mandagen etter
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(LocalDate.of(2018, 8, 20), LocalDate.of(2018, 9, 8))
                                .samtidigUttak(true)
                                .build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        //Lager manglende søkt bare for fredagen, ok ikke helgen
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(LocalDate.of(2018, 6, 8));
        assertThat(msp.get(0).getTom()).isEqualTo(LocalDate.of(2018, 6, 8));
    }

    @Test
    void skalIkkeOppretteManglendeSøktFørSkjæringstidspunktForOpptjening() {
        var fødselsdato = LocalDate.of(2018, 6, 11);

        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(
                        new Søknad.Builder().oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6))))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).isEmpty();
    }

    @Test
    void skalOppretteManglendeSøktFraSkjæringstidspunktForOpptjening() {
        var fødselsdato = LocalDate.of(2018, 6, 13);

        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(2)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6))))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato.minusWeeks(1)))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getOpptjening().getSkjæringstidspunkt());
        assertThat(msp.get(0).getTom()).isEqualTo(fødselsdato.minusDays(1));
    }

    @Test
    void skalIkkeOppretteManglendeSøktBasertPåAnnenPartFørSkjæringstidspunktForOpptjening() {
        var fødselsdato = LocalDate.of(2018, 6, 13);

        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6))))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato.plusWeeks(7)))
                .annenPart(new AnnenPart.Builder().uttaksperiode(
                        AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).isEmpty();
    }

    @Test
    void skal_ikke_opprette_manglende_søkt_før_endringsdato() {
        var fødselsdato = LocalDate.of(2018, 6, 13);

        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(22),
                null, false, fødselsdato.plusWeeks(17), null, null, null);
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder().oppgittPeriode(oppgittPeriode))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato.plusWeeks(7)))
                .behandling(farBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false))
                .revurdering(new Revurdering.Builder().endringsdato(fødselsdato.plusWeeks(18)))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getRevurdering().getEndringsdato());
        assertThat(msp.get(0).getTom()).isEqualTo(fødselsdato.plusWeeks(20).minusDays(1));
    }

}
