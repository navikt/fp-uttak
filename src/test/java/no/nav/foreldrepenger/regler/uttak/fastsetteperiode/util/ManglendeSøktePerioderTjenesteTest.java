package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;

class ManglendeSøktePerioderTjenesteTest {

    private final int mødrekvoteDager = 75;
    private final int fedrekvoteDager = 75;
    private final int fellesperiodDedager = 85;
    private final int førFødselDager = 15;

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
    void farMedAleneomsorgSkalIkkeHaMsp() {
        var fødsel = LocalDate.of(2022, 7, 29);
        var grunnlag = RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødsel))
            .behandling(farBehandling())
            .rettOgOmsorg(aleneomsorg())
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødsel, fødsel.plusDays(10)))
                .oppgittPeriode(oppgittPeriode(FORELDREPENGER, fødsel.plusWeeks(3), fødsel.plusWeeks(10))))
            .build();
        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).isEmpty();
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
    void skal_ikke_opprette_msp_hvis_mor_ikke_søkt() {
        var fødselsdato = LocalDate.of(2021, 9, 27);
        var grunnlag = grunnlagMedKontoer()
                .behandling(farBehandling())
                .rettOgOmsorg(beggeRett())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(4)))
                        .oppgittPeriode(oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(20))))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);
        assertThat(msp).isEmpty();
    }

    private RettOgOmsorg.Builder beggeRett() {
        return new RettOgOmsorg.Builder().aleneomsorg(false).morHarRett(true).farHarRett(true).samtykke(true);
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
        assertThat(msp).isEmpty();
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
                .rettOgOmsorg(bareFarHarRett())
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(grunnlag.getDatoer().getFamiliehendelse().plusWeeks(6));
        assertThat(msp.get(0).getTom()).isEqualTo(grunnlag.getSøknad().getOppgittePerioder().get(0).getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skalLageManglendeSøktIMellomliggendePerioderNårBareFarHarRett_fødsel() {
        var familiehendelse = LocalDate.of(2021, 9, 28);

        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.minusWeeks(3), familiehendelse.plusWeeks(2)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(4), familiehendelse.plusWeeks(5)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(12), familiehendelse.plusWeeks(15)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(16), familiehendelse.plusWeeks(18)))
                )
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarHarRett())
                .datoer(new Datoer.Builder().fødsel(familiehendelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(familiehendelse.plusWeeks(6));
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(12).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(msp.get(1).getFom()).isEqualTo(familiehendelse.plusWeeks(15).plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(familiehendelse.plusWeeks(16).minusDays(1));
        assertThat(msp.get(1).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skalLageManglendeSøktIMellomliggendePerioderNårBareFarHarRett_adopsjon() {
        var familiehendelse = LocalDate.of(2021, 9, 28);

        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.minusWeeks(3), familiehendelse.plusWeeks(2)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(4), familiehendelse.plusWeeks(5)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(12), familiehendelse.plusWeeks(15)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER, familiehendelse.plusWeeks(16), familiehendelse.plusWeeks(18)))
                )
                .behandling(farBehandling())
                .rettOgOmsorg(bareFarHarRett())
                .datoer(new Datoer.Builder().omsorgsovertakelse(familiehendelse))
                .adopsjon(new Adopsjon.Builder().ankomstNorge(familiehendelse))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(3);
        assertThat(msp.get(0).getFom()).isEqualTo(familiehendelse.plusWeeks(2).plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(familiehendelse.plusWeeks(4).minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(msp.get(1).getFom()).isEqualTo(familiehendelse.plusWeeks(5).plusDays(1));
        assertThat(msp.get(1).getTom()).isEqualTo(familiehendelse.plusWeeks(12).minusDays(1));
        assertThat(msp.get(1).getStønadskontotype()).isEqualTo(FORELDREPENGER);
        assertThat(msp.get(2).getFom()).isEqualTo(familiehendelse.plusWeeks(15).plusDays(1));
        assertThat(msp.get(2).getTom()).isEqualTo(familiehendelse.plusWeeks(16).minusDays(1));
        assertThat(msp.get(2).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skalLageManglendeSøktHvisBfhrAdopsjonIkkeStarterPåOmsorgsovertakelseDato() {
        var omsorgsovertakelse = LocalDate.of(2023, 3, 3);

        var søktPeriode = oppgittPeriode(FORELDREPENGER, LocalDate.of(2023, 4, 4), LocalDate.of(2023, 4, 10));
        var grunnlag = grunnlagMedKontoer()
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(søktPeriode)
            )
            .behandling(farBehandling())
            .rettOgOmsorg(bareFarHarRett())
            .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
            .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
            .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(omsorgsovertakelse);
        assertThat(msp.get(0).getTom()).isEqualTo(søktPeriode.getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    @Test
    void skalLageManglendeSøktHvisBfhrAdopsjonIkkeStarterPåOmsorgsovertakelseDato_endagsperiode() {
        //FAGSYSTEM-276286
        var omsorgsovertakelse = LocalDate.of(2023, 3, 3);

        var søktPeriode = oppgittPeriode(FORELDREPENGER, LocalDate.of(2023, 4, 4), LocalDate.of(2023, 4, 4));
        var grunnlag = grunnlagMedKontoer()
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON)
                .oppgittPeriode(søktPeriode)
            )
            .behandling(farBehandling())
            .rettOgOmsorg(bareFarHarRett())
            .datoer(new Datoer.Builder().omsorgsovertakelse(omsorgsovertakelse))
            .adopsjon(new Adopsjon.Builder().ankomstNorge(null))
            .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(omsorgsovertakelse);
        assertThat(msp.get(0).getTom()).isEqualTo(søktPeriode.getFom().minusDays(1));
        assertThat(msp.get(0).getStønadskontotype()).isEqualTo(FORELDREPENGER);
    }

    private static RettOgOmsorg.Builder bareFarHarRett() {
        return new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false);
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
                            UtsettelseÅrsak.SYKDOM_SKADE, null, null, null, DokumentasjonVurdering.SYKDOM_ANNEN_FORELDER_GODKJENT)))
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
                .rettOgOmsorg(bareFarHarRett())
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
                .rettOgOmsorg(bareFarHarRett())
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

    @Test
    @DisplayName("Skal lage manglende søkt periode for den ene dagen som ligger innen første 6 ukene.")
    void ikke_msp_hvis_fødsel_etter_termin_med_påfølgende_fritt_uttak() {
        //Fra prod saksnummer 152085835
        var termin = LocalDate.of(2021, 12, 15);
        var fødselsdato = LocalDate.of(2021, 12, 16);

        var grunnlag = grunnlagMedKontoer()
                .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(termin))
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, LocalDate.of(2021, 11, 24), LocalDate.of(2021, 11, 24)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2021, 11, 25), LocalDate.of(2021, 12, 15)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2021, 12, 16), LocalDate.of(2022, 1, 25)))
                        //Opprinnelig søkt fritt uttak etter uke 6
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2022, 2, 16), LocalDate.of(2022, 2, 16)))
                )
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(LocalDate.of(2022, 1, 26));
        assertThat(msp.get(0).getTom()).isEqualTo(LocalDate.of(2022, 1, 26));
    }

    @Test
    @DisplayName("FAGSYSTEM-214259 - Mor søker en dag fellesperiode lenge før fødsel. Termin på en lørdag. "
            + "Skal ikke få opprettet msp før fpff perioden")
    void skalIkkeLageManglendeSøktFørUke3FørFødsel() {
        var termindato = LocalDate.of(2022, 4, 16);

        var søknadsperiodeFom = LocalDate.of(2022, 1, 31);
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder().type(Søknadstype.TERMIN)
                        //Søker en dag
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, søknadsperiodeFom, søknadsperiodeFom))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2022, 3, 28), LocalDate.of(2022, 4, 15)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, LocalDate.of(2022, 4, 18), LocalDate.of(2022, 7, 29)))
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, LocalDate.of(2022, 8, 1), LocalDate.of(2022, 11, 17)))
                )
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(søknadsperiodeFom))
                .datoer(new Datoer.Builder().termin(termindato))
                .build();

        var msp = finnManglendeSøktePerioder(grunnlag);

        assertThat(msp).isEmpty();
    }

    @Test
    void ikke_msp_før_3_uker_før_fødsel() {
        var familiehendelsesDato = LocalDate.of(2022, 5, 25);
        var grunnlag = grunnlagMedKontoer()
                .søknad(new Søknad.Builder()
                        .oppgittPeriode(oppgittPeriode(FELLESPERIODE, familiehendelsesDato.minusWeeks(5), familiehendelsesDato.minusWeeks(4)))
                        .oppgittPeriode(oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, familiehendelsesDato.minusWeeks(2), familiehendelsesDato.minusDays(1)))
                        .oppgittPeriode(oppgittPeriode(MØDREKVOTE, familiehendelsesDato, familiehendelsesDato.plusWeeks(6).minusDays(1)))
                )
                .datoer(new Datoer.Builder().fødsel(familiehendelsesDato))
                .build();

        var manglendeSøktePerioder = finnManglendeSøktePerioder(grunnlag);

        var mspFørFødsel = manglendeSøktePerioder.stream()
                .filter(msp -> msp.getStønadskontotype().equals(FORELDREPENGER_FØR_FØDSEL))
                .toList();
        assertThat(mspFørFødsel).hasSize(1);

        //bare msp fra uke 3 til uke 2 før fødsel
        assertThat(mspFørFødsel.get(0).getFom()).isEqualTo(familiehendelsesDato.minusWeeks(3));
        assertThat(mspFørFødsel.get(0).getTom()).isEqualTo(familiehendelsesDato.minusWeeks(2).minusDays(1));
    }

    private List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag) {
        return ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag);
    }

    private RettOgOmsorg.Builder aleneomsorg() {
        return new RettOgOmsorg.Builder().farHarRett(true).morHarRett(false).aleneomsorg(true).samtykke(false);
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
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, null, null,
                null, null);
    }

    private Behandling.Builder farBehandling() {
        return new Behandling.Builder().søkerErMor(false);
    }

    private Behandling.Builder morBehandling() {
        return new Behandling.Builder().søkerErMor(true);
    }
}
