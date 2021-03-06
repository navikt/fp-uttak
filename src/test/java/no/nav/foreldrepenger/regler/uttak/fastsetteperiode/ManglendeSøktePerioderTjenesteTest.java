package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

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

class ManglendeSøktePerioderTjenesteTest {

    private final Konfigurasjon konfigurasjon = StandardKonfigurasjon.KONFIGURASJON;
    private final int mødrekvoteDager = konfigurasjon.getParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT,
            LocalDate.of(2018, 6, 1));
    private final int fedrekvoteDager = konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT,
            LocalDate.of(2018, 6, 1));
    private final int fellesperiodDedager = konfigurasjon.getParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER,
            LocalDate.of(2018, 6, 1));
    private final int førFødselDager = konfigurasjon.getParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 6, 1));

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
                .rettOgOmsorg(aleneomsorg())
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, farFom, farFom.plusDays(10))))
                .build();
        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(1);
    }

    @Test
    void skal_opprette_msp_før_fødsel_men_ikke_mer_enn_3_øker_før_fødsel() {
        var familiehendelsesDato = LocalDate.of(2019, 12, 11);

        var fellesperiode = oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(10), familiehendelsesDato.minusWeeks(8));
        var fpff = oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, familiehendelsesDato.minusWeeks(3), familiehendelsesDato.minusWeeks(2));
        var mødrekvote = oppgittPeriode(MØDREKVOTE, familiehendelsesDato, familiehendelsesDato.plusWeeks(6).minusDays(1));
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(fellesperiode)
                        .oppgittPeriode(fpff)
                        .oppgittPeriode(mødrekvote))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        assertThat(manglendeSøktePerioder.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(manglendeSøktePerioder.get(0).getFom()).isEqualTo(familiehendelsesDato.minusWeeks(2).plusDays(1));
        assertThat(manglendeSøktePerioder.get(0).getTom()).isEqualTo(familiehendelsesDato.minusDays(1));
    }

    @Test
    void skalUtledeMspMødrekvoteITidsperiodenForbeholdtMorEtterFødsel() {
        var familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder()
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
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelsesDato.plusWeeks(6), familiehendelsesDato.plusWeeks(7))))
                .datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .behandling(new Behandling.Builder())
                .revurdering(new Revurdering.Builder().endringsdato(LocalDate.of(2019, 6, 4)))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        assertThat(manglendeSøktePerioder).isEmpty();
    }

    @Test
    void finnerHullMellomParteneITidsrommetForbeholdtMor() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var hullDato = fødselsdato.plusWeeks(3);
        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
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
    void finnerHullMellomParteneITidsrommetForbeholdtMorHvisAnnenpartHarAvslåttPeriodeUtenTrekkdagerOgUtbetaling() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var annenpartInnvilgetMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse,
                familiehendelse.plusWeeks(3).minusDays(1))
                .innvilget(true)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(),
                        MØDREKVOTE, new Trekkdager(10), Utbetalingsgrad.TEN))
                .build();
        var annenpartAvslåttMødrekvote = AnnenpartUttakPeriode.Builder.uttak(familiehendelse.plusWeeks(3),
                familiehendelse.plusWeeks(7))
                .innvilget(false)
                .uttakPeriodeAktivitet(new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), MØDREKVOTE,
                        Trekkdager.ZERO, Utbetalingsgrad.ZERO))
                .build();
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FEDREKVOTE, familiehendelse.plusWeeks(8), familiehendelse.plusWeeks(15))))
                .behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(annenpartInnvilgetMødrekvote)
                        .uttaksperiode(annenpartAvslåttMødrekvote))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(annenpartAvslåttMødrekvote.getFom());
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(6).minusDays(1));
    }

    @Test
    void overlappendePerioderMedAnnenPartUtenHull() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var grunnlag = grunnlagMedKontoer()
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10))))
                .annenPart(new AnnenPart.Builder()
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8)).build())
                        .uttaksperiode(AnnenpartUttakPeriode.Builder.uttak(fødselsdato.plusWeeks(9).plusDays(1), fødselsdato.plusWeeks(11)).build()))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).hasSize(0);
    }

    @Test
    void helgErIkkeHull() {
        var fødselsdato = LocalDate.of(2018, 6, 6);
        var mødrekvoteSlutt = LocalDate.of(2018, 7, 13);
        var annenPartStart = LocalDate.of(2018, 7, 16);
        var grunnlag = grunnlagMedKontoer()
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)))
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

        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
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
    void skalIkkeLageManglendeSøktSomGårOver6UkerEtterFødsel() {
        var familiehendelse = LocalDate.of(2018, 12, 27);

        var søknadsperiodeFom = familiehendelse.minusWeeks(3);
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, søknadsperiodeFom, familiehendelse.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelse, familiehendelse.plusWeeks(3)))
                        .oppgittPeriode(OppgittPeriode.forUtsettelse(familiehendelse.plusWeeks(6).plusDays(1), familiehendelse.plusWeeks(8),
                                PeriodeVurderingType.PERIODE_OK, UtsettelseÅrsak.SYKDOM_SKADE, null, null, null)))
                .behandling(morBehandling())
                .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(søknadsperiodeFom))
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(3).plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    void skalLageManglendeSøktFraUke7FørStpOpptjeningHvisEtterFødselNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var søknadsperiodeFom = familiehendelse.plusWeeks(9);
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
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
    void skalLageManglendeSøktFraOmsorgsovertakelseTilFørsteUttaksdagNårBareFarHarRett() {
        var familiehendelse = LocalDate.of(2018, 12, 4);

        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
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
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void manglende_fpff_skal_ikke_slutte_i_helg() {
        var fødselsdato = LocalDate.of(2021, 6, 21);

        var grunnlag = grunnlagMedKontoer().datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2021, 5, 29), LocalDate.of(2021, 6, 18)))
                        //Mor har ikke søkt første uken etter fødsel
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2021, 6, 27), LocalDate.of(2021, 8, 8))))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        //Lager manglende søkt bare for fredagen, ok ikke helgen
        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(LocalDate.of(2021, 6, 21));
        assertThat(msp.get(0).getTom()).isEqualTo(LocalDate.of(2021, 6, 25));
    }

    private List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag) {
        return ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
    }

    private RettOgOmsorg.Builder aleneomsorg() {
        return new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true).aleneomsorg(true).samtykke(false);
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

    private OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, PeriodeVurderingType.IKKE_VURDERT, null, null,
                null);
    }

    private Behandling.Builder farBehandling() {
        return new Behandling.Builder().søkerErMor(false);
    }

    private Behandling.Builder morBehandling() {
        return new Behandling.Builder().søkerErMor(true);
    }
}
