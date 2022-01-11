package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.UTSETTELSE_GYLDIG_BFR_AKT_KRAV_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;

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
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(beggeRett())
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MOR_UFØR);
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_skal_godkjennes() {
        var fødselsdato = LocalDate.of(2022, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var oppgittPeriodeU1 =  foreldrepengerUtsettelse(fødselsdato.plusMonths(6), fødselsdato.plusMonths(7).minusDays(1), MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriodeU1, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(false).farHarRett(true).morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, UTSETTELSE_GYLDIG_BFR_AKT_KRAV_OPPFYLT, 0))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, -1))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> p.getUttakPeriode().isManglendeSøktPeriode() && harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> !p.getUttakPeriode().isManglendeSøktPeriode() && harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_flerbarn_skal_godkjennes() {
        var fødselsdato = LocalDate.of(2022, 1, 1);
        var kontoer = new Kontoer.Builder().kontoList(List.of(konto(FORELDREPENGER, 285), konto(FLERBARNSDAGER, 85))).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true).morHarRett(false).farHarRett(true).morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, -1))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> p.getUttakPeriode().isManglendeSøktPeriode() && harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> !p.getUttakPeriode().isManglendeSøktPeriode() && harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    private boolean harPeriode(UttakPeriode p, Perioderesultattype prt, PeriodeResultatÅrsak prå, int dager) {
        return p.getPerioderesultattype().equals(prt) && p.getPeriodeResultatÅrsak().equals(prå) && (dager == -1 ||
                p.getAktiviteter().stream().map(UttakPeriodeAktivitet::getTrekkdager).mapToInt(Trekkdager::rundOpp).sum() == dager);
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

    private OppgittPeriode fellesperiode(LocalDate fødselsdato, MorsAktivitet utdanning) {
        return OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(15).minusDays(1), null,
                false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato, utdanning);
    }

    private OppgittPeriode foreldrepenger(LocalDate fødselsdato, MorsAktivitet utdanning) {
        return OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(15).minusDays(1), null,
                false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato, utdanning);
    }

    private OppgittPeriode foreldrepengerUtsettelse(LocalDate fom, LocalDate tom, MorsAktivitet utdanning) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.FRI, fom, fom, utdanning);
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
