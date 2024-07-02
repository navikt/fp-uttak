package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_IKKE_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.ARBEID_OG_UTDANNING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.INNLAGT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.INTROPROG;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.KVALPROG;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.SYK;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.UFØRE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.UTDANNING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_INNLEGGELSE_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_INNLEGGELSE_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_SYKDOM_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_SYKDOM_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITET_UKJENT_UDOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class AvslagAktivitetskravOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {


    @ParameterizedTest(name = "Ved mors aktivitet {0} og dokumentasjonvurdering er {1}, forventes resultat {2}")
    @MethodSource("dokumentasjonOgAvslagKombinasjoner")
    void test_kombinasjoner_av_MorsAktivitet_PeriodeMedAvklartMorsAktivitetResultat_og_IkkeOppfyltÅrsak_sammenheng(MorsAktivitet morsAktivitet,
                                                                                                                   IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                                                                                                                   DokumentasjonVurdering dokumentasjonVurdering) {
        testAvslag(morsAktivitet, ikkeOppfyltÅrsak, dokumentasjonVurdering, true);
    }

    @ParameterizedTest(name = "Ved mors aktivitet {0} og dokumentasjonvurdering er {1}, forventes resultat {2}")
    @MethodSource("dokumentasjonOgAvslagKombinasjoner")
    void test_kombinasjoner_av_MorsAktivitet_PeriodeMedAvklartMorsAktivitetResultat_og_IkkeOppfyltÅrsak_fri(MorsAktivitet morsAktivitet,
                                                                                                            IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                                                                                                            DokumentasjonVurdering dokumentasjonVurdering) {
        testAvslag(morsAktivitet, ikkeOppfyltÅrsak, dokumentasjonVurdering, false);
    }

    @Test
    void mor_med_bekreftet_uføretrygd_skal_avslås() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2018, 1, 1));
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200)).minsterettDager(20);
        var oppgittPeriode = foreldrepenger(fødselsdato, UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett().morUføretrygd(true))
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
            InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void mor_med_bekreftet_ikke_uføretrygd_skal_avslås() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200)).minsterettDager(0);
        var oppgittPeriode = foreldrepenger(fødselsdato, UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett().morUføretrygd(false))
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void mor_uten_bekreftet_uføretrygd_skal_til_manuell() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, UFØRE, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett().morUføretrygd(false))
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MOR_UFØR);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isFalse();
    }

    @Test
    void mor_med_bekreftet_uføretrygd_skal_gå_til_avslås_mangler_dager_uten_aktivitetskrav() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2018, 1, 1));
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200)).utenAktivitetskravDager(20);
        var oppgittPeriode = foreldrepenger(fødselsdato, UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett().morUføretrygd(true))
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
            InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void mor_med_bekreftet_ikke_uføretrygd_skal_avslås_dager_uten_aktivitetskrav() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER).trekkdager(200)).utenAktivitetskravDager(0);
        var oppgittPeriode = foreldrepenger(fødselsdato, UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett().morUføretrygd(false))
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void ukjent_mors_aktivitet_skal_gå_til_manuell_hvis_dokumentert_ikke_minsterett() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, null, MORS_AKTIVITET_IKKE_GODKJENT);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(
            Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void ukjent_mors_aktivitet_fellesperiode_manuell_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, null, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(
            Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void ukjent_mors_aktivitet_foreldrepenger_skal_avslås_hvis_minsterett_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2022, 9, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 50)).minsterettDager(1);
        var oppgittPeriode = foreldrepenger(fødselsdato, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void ukjent_mors_aktivitet_fri_utsettelse_foreldrepenger_skal_avslås_hvis_minsterett_ikke_dokumentert() {
        var fødselsdato = LocalDate.of(2022, 9, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 75)).minsterettDager(1);
        var oppgittPeriode = foreldrepenger(fødselsdato, null);
        var utsettelse1 = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(16).minusDays(1), UtsettelseÅrsak.ARBEID,
            fødselsdato, fødselsdato, null, null);
        var utsettelse2 = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(16), fødselsdato.plusWeeks(17).minusDays(1), UtsettelseÅrsak.FRI,
            fødselsdato, fødselsdato, null, null);

        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, utsettelse1, utsettelse2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
        assertThat(fastsattePerioder.get(2).uttakPeriode().getManuellbehandlingårsak()).isEqualTo(
            Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
        assertThat(fastsattePerioder.get(3).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(BARE_FAR_RETT_IKKE_SØKT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(fastsattePerioder.get(2).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
        assertThat(fastsattePerioder.get(3).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void kun_far_har_rett() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), null, false,
            fødselsdato, fødselsdato, UTDANNING, null, MORS_AKTIVITET_IKKE_DOKUMENTERT);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

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
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void far_søker_utsettelse_sammenhengende_uttak() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), UtsettelseÅrsak.ARBEID, fødselsdato,
            fødselsdato, ARBEID, MORS_AKTIVITET_IKKE_GODKJENT);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(true))
            .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    @Test
    void far_søker_utsettelse() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 100));
        var oppgittPeriode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10), UtsettelseÅrsak.ARBEID, fødselsdato,
            fødselsdato, ARBEID, MORS_AKTIVITET_IKKE_GODKJENT);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

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
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isTrue();
    }

    private Kontoer.Builder kontoerMedFellesperiode() {
        return new Kontoer.Builder().konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(50));
    }

    private OppgittPeriode foreldrepenger(LocalDate fødselsdato, MorsAktivitet morsAktivitet) {
        return OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15).minusDays(1), null, false,
            fødselsdato, fødselsdato, morsAktivitet, null, null);
    }

    private OppgittPeriode fellesperiode(LocalDate fødselsdato, MorsAktivitet morsAktivitet, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(FELLESPERIODE, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(15).minusDays(1), null, false,
            fødselsdato, fødselsdato, morsAktivitet, null, dokumentasjonVurdering);
    }

    private static Stream<Arguments> dokumentasjonOgAvslagKombinasjoner() {
        return Stream.of(Arguments.of(ARBEID, AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(ARBEID, AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT),

            Arguments.of(SYK, AKTIVITETSKRAVET_SYKDOM_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(SYK, AKTIVITETSKRAVET_SYKDOM_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT),

            Arguments.of(INNLAGT, AKTIVITETSKRAVET_INNLEGGELSE_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(INNLAGT, AKTIVITETSKRAVET_INNLEGGELSE_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT),

            Arguments.of(UTDANNING, AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(UTDANNING, AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT),

            Arguments.of(KVALPROG, AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(KVALPROG, AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT),

            Arguments.of(INTROPROG, AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(INTROPROG, AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT),

            Arguments.of(ARBEID_OG_UTDANNING, AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_OPPFYLT, MORS_AKTIVITET_IKKE_GODKJENT),
            Arguments.of(ARBEID_OG_UTDANNING, AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_DOKUMENTERT, MORS_AKTIVITET_IKKE_DOKUMENTERT));
    }

    private void testAvslag(MorsAktivitet morsAktivitet,
                            IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                            DokumentasjonVurdering dokumentasjonVurdering,
                            boolean sammenhengendeUttak) {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var kontoer = kontoerMedFellesperiode();
        var oppgittPeriode = fellesperiode(fødselsdato, morsAktivitet, dokumentasjonVurdering);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(sammenhengendeUttak))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(beggeRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);

        assertThat(fastsattePerioder).hasSize(1);
        verifiserAvslåttPeriode(fastsattePerioder.get(0).uttakPeriode(), fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(15).minusDays(1),
            FELLESPERIODE, ikkeOppfyltÅrsak);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD).merEnn0()).isEqualTo(sammenhengendeUttak);
    }

}
