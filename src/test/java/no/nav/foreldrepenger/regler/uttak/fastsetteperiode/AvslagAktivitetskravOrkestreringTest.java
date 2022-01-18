package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

class AvslagAktivitetskravOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {


    @ParameterizedTest(name = "Ved mors aktivitet {0} og dokumentasjonvurdering er {1}, forventes resultat {2}")
    @MethodSource("dokumentasjonOgAvslagKombinasjoner")
    void test_kombinasjoner_av_MorsAktivitet_PeriodeMedAvklartMorsAktivitetResultat_og_IkkeOppfyltÅrsak(MorsAktivitet morsAktivitet,
                                                                                                               PeriodeMedAvklartMorsAktivitet.Resultat avklartMorsAktivitetResultat,
                                                                                                               IkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        testAvslag(morsAktivitet, avklartMorsAktivitetResultat, ikkeOppfyltÅrsak);
    }

    @Test
    void mor_med_bekreftet_uføretrygd_skal_gå_til_manuell() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2018, 1, 1));
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200)).minsterettDager(20);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(false).farHarRett(true).morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR);
        assertThat(fastsattePerioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MOR_UFØR);
    }

    @Test
    void mor_med_bekreftet_ikke_uføretrygd_skal_avslås() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200)).minsterettDager(0);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(false).farHarRett(true).morUføretrygd(false))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR);
    }

    @Test
    void mor_uten_bekreftet_uføretrygd_skal_til_manuell() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true).morUføretrygd(false))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MOR_UFØR);
    }

    @Test
    void ukjent_mors_aktivitet_skal_gå_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(
                Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    @Test
    void kun_far_har_rett() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(10), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(),
                        IKKE_I_AKTIVITET_IKKE_DOKUMENTERT));
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode)
                .dokumentasjon(dokumentasjon);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT);
    }

    @Test
    void far_søker_utsettelse_sammenhengende_uttak() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10),
                PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.ARBEID, fødselsdato, fødselsdato, MorsAktivitet.ARBEID);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(), IKKE_I_AKTIVITET_DOKUMENTERT));
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode)
                .dokumentasjon(dokumentasjon);

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(true))
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);
    }

    @Test
    void far_søker_utsettelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10),
                PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.ARBEID, fødselsdato, fødselsdato, MorsAktivitet.ARBEID);
        var dokumentasjon = new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(), IKKE_I_AKTIVITET_DOKUMENTERT));
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittPeriode(oppgittPeriode)
                .dokumentasjon(dokumentasjon);

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling())
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);
    }

    private Kontoer.Builder kontoerMedFellesperiode() {
        return new Kontoer.Builder().konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(50));
    }

    private OppgittPeriode foreldrepenger(LocalDate fødselsdato, MorsAktivitet utdanning) {
        return OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15).minusDays(1), null,
                false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato, utdanning);
    }

    private OppgittPeriode fellesperiode(LocalDate fødselsdato, MorsAktivitet utdanning) {
        return OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(15).minusDays(1), null,
                false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato, utdanning);
    }

    private static Stream<Arguments> dokumentasjonOgAvslagKombinasjoner() {
        return Stream.of(Arguments.of(MorsAktivitet.ARBEID, IKKE_I_AKTIVITET_DOKUMENTERT,
                IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.ARBEID, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT),

                Arguments.of(MorsAktivitet.SYK, IKKE_I_AKTIVITET_DOKUMENTERT, IkkeOppfyltÅrsak.AKTIVITETSKRAVET_SYKDOM_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.SYK, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_SYKDOM_IKKE_DOKUMENTERT),

                Arguments.of(MorsAktivitet.INNLAGT, IKKE_I_AKTIVITET_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_INNLEGGELSE_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.INNLAGT, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_INNLEGGELSE_IKKE_DOKUMENTERT),

                Arguments.of(MorsAktivitet.UTDANNING, IKKE_I_AKTIVITET_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.UTDANNING, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT),

                Arguments.of(MorsAktivitet.KVALPROG, IKKE_I_AKTIVITET_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.KVALPROG, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_DOKUMENTERT),

                Arguments.of(MorsAktivitet.INTROPROG, IKKE_I_AKTIVITET_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.INTROPROG, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT),

                Arguments.of(MorsAktivitet.ARBEID_OG_UTDANNING, IKKE_I_AKTIVITET_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_OPPFYLT),
                Arguments.of(MorsAktivitet.ARBEID_OG_UTDANNING, IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
                        IkkeOppfyltÅrsak.AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_DOKUMENTERT));
    }

    private void testAvslag(MorsAktivitet morsAktivitet,
                            PeriodeMedAvklartMorsAktivitet.Resultat avklaringsResultat,
                            IkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, morsAktivitet);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        søknad.dokumentasjon(new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(), avklaringsResultat)));


        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);

        assertThat(fastsattePerioder).hasSize(1);
        verifiserAvslåttPeriode(fastsattePerioder.get(0).getUttakPeriode(), fødselsdato.plusWeeks(7),
                fødselsdato.plusWeeks(15).minusDays(1), FELLESPERIODE, ikkeOppfyltÅrsak);
    }

}
