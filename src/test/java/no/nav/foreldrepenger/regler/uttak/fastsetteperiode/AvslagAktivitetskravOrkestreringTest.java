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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class AvslagAktivitetskravOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {


    @ParameterizedTest(name = "Ved mors aktivitet {0} og dokumentasjonvurdering er {1}, forventes resultat {2}")
    @MethodSource("dokumentasjonOgAvslagKombinasjoner")
    void test_kombinasjoner_av_MorsAktivitet_PeriodeMedAvklartMorsAktivitetResultat_og_IkkeOppfyltÅrsak(MorsAktivitet morsAktivitet,
                                                                                                               PeriodeMedAvklartMorsAktivitet.Resultat avklartMorsAktivitetResultat,
                                                                                                               IkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        testAvslag(morsAktivitet, avklartMorsAktivitetResultat, ikkeOppfyltÅrsak);
    }

    @Test
    void mor_med_uføretrygd_skal_gå_til_manuell() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().medBehandling(farBehandling())
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad)
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MOR_UFØR);
    }

    @Test
    void ukjent_mors_aktivitet_skal_gå_til_manuell_hvis_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, null);
        var søknad = new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().medBehandling(farBehandling())
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad)
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(
                Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    @Test
    void kun_far_har_rett() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(10), null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                MorsAktivitet.UTDANNING);
        var dokumentasjon = new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(),
                        IKKE_I_AKTIVITET_IKKE_DOKUMENTERT));
        var søknad = new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode)
                .medDokumentasjon(dokumentasjon);

        var grunnlag = new RegelGrunnlag.Builder().medBehandling(farBehandling())
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato))
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(bareFarRett())
                .medSøknad(søknad)
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT);
    }

    @Test
    void far_søker_utsettelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10),
                PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.ARBEID, fødselsdato, fødselsdato, MorsAktivitet.ARBEID);
        var dokumentasjon = new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(), IKKE_I_AKTIVITET_DOKUMENTERT));
        var søknad = new Søknad.Builder().medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(oppgittPeriode)
                .medDokumentasjon(dokumentasjon);

        var grunnlag = new RegelGrunnlag.Builder().medBehandling(farBehandling())
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(fødselsdato))
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(bareFarRett())
                .medSøknad(søknad)
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);
    }

    private Kontoer.Builder kontoerMedFellesperiode() {
        return new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(50));
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
        var søknad = new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(oppgittPeriode);

        søknad.medDokumentasjon(new Dokumentasjon.Builder().leggTilPeriodeMedAvklartMorsAktivitet(
                new PeriodeMedAvklartMorsAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom(), avklaringsResultat)));


        var grunnlag = new RegelGrunnlag.Builder().medBehandling(farBehandling())
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett())
                .medSøknad(søknad)
                .medInngangsvilkår(oppfyltAlleVilkår())
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .medKontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);

        assertThat(fastsattePerioder).hasSize(1);
        verifiserAvslåttPeriode(fastsattePerioder.get(0).getUttakPeriode(), fødselsdato.plusWeeks(7),
                fødselsdato.plusWeeks(15).minusDays(1), FELLESPERIODE, ikkeOppfyltÅrsak);
    }

}
