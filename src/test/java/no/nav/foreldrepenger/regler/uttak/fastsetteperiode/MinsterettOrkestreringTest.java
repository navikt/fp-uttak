package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_IKKE_GODKJENT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITET_UKJENT_UDOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MinsterettOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE, null);
        var utsettelseFra = Virkedager.justerHelgTilMandag(fødselsdato.plusMonths(6));
        var oppgittPeriodeU1 = foreldrepengerUtsettelse(
                utsettelseFra,
                Virkedager.plusVirkedager(utsettelseFra, 19),
                MorsAktivitet.UTDANNING,
                MORS_AKTIVITET_IKKE_DOKUMENTERT);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE, null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriodeU1, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                40)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                35)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.AVSLÅTT,
                                AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT,
                                20)))
                .isTrue();
        // Søkt ufør, ikke dager igjen på minstekvote
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1)))
                .isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_flerbarn_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 2, 1));
        var kontoer = new Kontoer.Builder()
                .kontoList(List.of(konto(FORELDREPENGER, 285)))
                .flerbarnsdager(85)
                .minsterettDager(85);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE, null);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE, null);
        var oppgittPeriode3 = foreldrepenger(fødselsdato.plusYears(2), MorsAktivitet.UFØRE, null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2, oppgittPeriode3));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(harPeriode(
                        fastsattePerioder.get(0).uttakPeriode(),
                        Perioderesultattype.AVSLÅTT,
                        BARE_FAR_RETT_IKKE_SØKT,
                        5))
                .isTrue();
        assertThat(harPeriode(
                        fastsattePerioder.get(1).uttakPeriode(),
                        Perioderesultattype.INNVILGET,
                        FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                        40))
                .isTrue();
        assertThat(harPeriode(
                        fastsattePerioder.get(2).uttakPeriode(),
                        Perioderesultattype.AVSLÅTT,
                        BARE_FAR_RETT_IKKE_SØKT,
                        195))
                .isTrue();
        assertThat(harPeriode(
                        fastsattePerioder.get(4).uttakPeriode(),
                        Perioderesultattype.INNVILGET,
                        FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                        40))
                .isTrue();
        assertThat(harPeriode(
                        fastsattePerioder.get(5).uttakPeriode(),
                        Perioderesultattype.AVSLÅTT,
                        BARE_FAR_RETT_IKKE_SØKT,
                        0))
                .isTrue();
        assertThat(harPeriode(
                        fastsattePerioder.get(6).uttakPeriode(),
                        Perioderesultattype.INNVILGET,
                        FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                        5))
                .isTrue();
        assertThat(harPeriode(
                        fastsattePerioder.get(7).uttakPeriode(),
                        Perioderesultattype.AVSLÅTT,
                        IKKE_STØNADSDAGER_IGJEN,
                        -1))
                .isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_overskrider_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(
                fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(23).minusDays(1), MorsAktivitet.UFØRE, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                75)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITET_UKJENT_UDOKUMENTERT, 5)))
                .isTrue();
        // Søkt ufør, ikke dager igjen på minstekvote
    }

    @Test
    void bfhr_enkel_minsterett_vs_innvilget_med_mye_godkjent_aktivitet() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(40);
        var oppgittPeriode = foreldrepenger(
                fødselsdato.plusWeeks(7),
                fødselsdato.plusWeeks(45).minusDays(1),
                MorsAktivitet.UTDANNING,
                MORS_AKTIVITET_GODKJENT);
        var oppgittPeriode2 = foreldrepenger(
                fødselsdato.plusWeeks(45), fødselsdato.plusWeeks(47).minusDays(1), null, null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .søknad(søknad)
                .build();
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(4);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 190)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, 0)))
                .isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE, null);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusWeeks(25), MorsAktivitet.UFØRE, null);
        var oppgittPeriode3 = foreldrepenger(fødselsdato.plusWeeks(40), MorsAktivitet.UFØRE, null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2, oppgittPeriode3));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                40)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                35)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITET_UKJENT_UDOKUMENTERT, 5)))
                .isTrue();
        // Søkt ufør, ikke dager igjen på minstekvote
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1)))
                .isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_avslått_periode_med_aktivitet_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato.minusWeeks(1), MorsAktivitet.SYK, MORS_AKTIVITET_GODKJENT);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusWeeks(14), MorsAktivitet.SYK, null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 40)))
                .isTrue(); // Akt krav oppfylt
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 35)))
                .isTrue(); // Mellomliggende
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                40)))
                .isTrue(); // Akt krav ikke oppfylt - innvilget fra uføre
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_flerbarn_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder()
                .kontoList(List.of(konto(FORELDREPENGER, 285)))
                .flerbarnsdager(85)
                .utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE, null);
        var oppgittPeriode2 = foreldrepenger(
                Virkedager.justerHelgTilMandag(fødselsdato.plusWeeks(49)),
                MorsAktivitet.UFØRE,
                null); // Strekker seg utover stønadsperioden
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                40)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                35)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1)))
                .isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_overskrider_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(
                fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(23).minusDays(1), MorsAktivitet.UFØRE, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode));

        var grunnlag = new RegelGrunnlag.Builder()
                .behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(),
                                Perioderesultattype.INNVILGET,
                                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV,
                                75)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p ->
                                harPeriode(p.uttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)))
                .isTrue();
        assertThat(fastsattePerioder.stream()
                        .anyMatch(p -> harPeriode(
                                p.uttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITET_UKJENT_UDOKUMENTERT, 5)))
                .isTrue();
        // Søkt ufør, ikke dager igjen på minstekvote
    }

    @Test
    void bfhr_flerbarnsdager_skal_også_trekkes_fra_minsteretten() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder()
                .konto(konto(FORELDREPENGER, 200))
                .minsterettDager(5)
                .flerbarnsdager(5);
        // Uten mors aktivitet bruker opp minsterett
        var oppgittPeriode1 = oppgittPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(7).minusDays(1));
        var oppgittPeriode2 = oppgittPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(7),
                fødselsdato.plusWeeks(8).minusDays(1),
                true,
                null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, oppgittPeriode2));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);

        assertThat(fastsattePerioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
    }

    @Test
    void bfhr_flerbarnsdager_minsterett_skal_gå_tom_for_dager_selv_om_ikke_minsterett_brukes_opp() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder()
                .konto(konto(FORELDREPENGER, 40))
                .minsterettDager(17)
                .flerbarnsdager(10);
        var oppgittPeriode1 = OppgittPeriode.forVanligPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(14).minusDays(1),
                null,
                false,
                fødselsdato,
                fødselsdato,
                MorsAktivitet.ARBEID,
                MORS_AKTIVITET_GODKJENT);
        var oppgittPeriode2 = oppgittPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(14),
                fødselsdato.plusWeeks(17).minusDays(1),
                true,
                null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, oppgittPeriode2));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT);

        assertThat(fastsattePerioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void bfhr_flerbarnsdager_minsterett_skal_gå_tom_for_dager_når_tomt_for_flerbarnsdager() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder()
                .konto(konto(FORELDREPENGER, 40))
                .minsterettDager(10)
                .flerbarnsdager(10);
        var oppgittPeriode1 = oppgittPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1),
                true,
                null);
        var oppgittPeriode2 = oppgittPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(8),
                fødselsdato.plusWeeks(10).minusDays(1),
                true,
                null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, oppgittPeriode2));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);

        assertThat(fastsattePerioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
    }

    @Test
    void bfhr_flerbarnsdager_trekker_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder()
                .konto(konto(FORELDREPENGER, 40))
                .minsterettDager(25)
                .flerbarnsdager(20);
        var oppgittPeriode1 = oppgittPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(6),
                fødselsdato.plusWeeks(8).minusDays(1),
                true,
                null);
        var gradertPeriode = OppgittPeriode.forGradering(
                FORELDREPENGER,
                fødselsdato.plusWeeks(8),
                fødselsdato.plusWeeks(10),
                BigDecimal.TEN,
                null,
                true,
                Set.of(ARBEIDSFORHOLD),
                fødselsdato,
                fødselsdato,
                null,
                null);
        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, gradertPeriode));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);

        assertThat(fastsattePerioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);
    }

    @ParameterizedTest
    @EnumSource(UtsettelseÅrsak.class)
    void utsettelser_etter_startdato_neste_stønadsperiode_innvilges(UtsettelseÅrsak utsettelseÅrsak) {
        var fødselsdatoFørsteSak = LocalDate.of(2023, 1, 6);
        var startdatoNesteStønadsperiode = fødselsdatoFørsteSak.plusYears(2);
        var grunnlag = basicGrunnlagFar(fødselsdatoFørsteSak)
                .kontoer(defaultKontoer().etterNesteStønadsperiodeDager(10))
                .datoer(new Datoer.Builder()
                        .fødsel(fødselsdatoFørsteSak)
                        .startdatoNesteStønadsperiode(startdatoNesteStønadsperiode))
                .søknad(søknad(
                        FØDSEL,
                        utsettelsePeriode(
                                startdatoNesteStønadsperiode,
                                startdatoNesteStønadsperiode.plusWeeks(2),
                                utsettelseÅrsak,
                                null),
                        // Bruker opp minsteretten
                        oppgittPeriode(
                                FEDREKVOTE,
                                startdatoNesteStønadsperiode.plusWeeks(4),
                                startdatoNesteStønadsperiode.plusWeeks(6).minusDays(1)),
                        utsettelsePeriode(
                                startdatoNesteStønadsperiode.plusWeeks(8),
                                startdatoNesteStønadsperiode.plusWeeks(10),
                                utsettelseÅrsak,
                                null)));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);

        assertThat(perioder.get(2).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(InnvilgetÅrsak.UTSETTELSE_GYLDIG);
    }

    @ParameterizedTest
    @EnumSource(UtsettelseÅrsak.class)
    void utsettelser_etter_startdato_neste_stønadsperiode_avslås_hvis_ingen_to_tette_minsterett(
            UtsettelseÅrsak utsettelseÅrsak) {
        var fødselsdatoFørsteSak = LocalDate.of(2021, 1, 6);
        var startdatoNesteStønadsperiode = fødselsdatoFørsteSak.plusYears(2);
        var grunnlag = basicGrunnlagFar(fødselsdatoFørsteSak)
                .kontoer(defaultKontoer().etterNesteStønadsperiodeDager(0))
                .datoer(new Datoer.Builder()
                        .fødsel(fødselsdatoFørsteSak)
                        .startdatoNesteStønadsperiode(startdatoNesteStønadsperiode))
                .søknad(søknad(
                        FØDSEL,
                        utsettelsePeriode(
                                startdatoNesteStønadsperiode,
                                startdatoNesteStønadsperiode.plusWeeks(2).minusDays(1),
                                utsettelseÅrsak,
                                null),
                        oppgittPeriode(
                                FEDREKVOTE,
                                startdatoNesteStønadsperiode.plusWeeks(2),
                                startdatoNesteStønadsperiode.plusWeeks(6).minusDays(1))));

        var perioder = fastsettPerioder(grunnlag);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE);
    }

    // FAGSYSTEM-269397
    @Test
    void bfhr_minsterett_gradering_flere_arbeidsforhold() {
        var fødselsdato = LocalDate.of(2022, 9, 12);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 40 * 5)).minsterettDager(8 * 5);
        var arbeidsforhold1 = AktivitetIdentifikator.forArbeid(new Orgnummer("1"), null);
        var arbeidsforhold2 = AktivitetIdentifikator.forArbeid(new Orgnummer("2"), null);
        var gradering = OppgittPeriode.forGradering(
                FORELDREPENGER,
                LocalDate.of(2022, 10, 24),
                LocalDate.of(2023, 7, 31),
                BigDecimal.valueOf(44.7),
                null,
                false,
                Set.of(arbeidsforhold1),
                fødselsdato,
                fødselsdato,
                MorsAktivitet.UTDANNING,
                MORS_AKTIVITET_GODKJENT);
        var foreldrepenger1 = OppgittPeriode.forVanligPeriode(
                FORELDREPENGER,
                LocalDate.of(2023, 8, 1),
                LocalDate.of(2023, 9, 25),
                null,
                false,
                fødselsdato,
                fødselsdato,
                null,
                null);
        var foreldrepenger2 = OppgittPeriode.forVanligPeriode(
                FORELDREPENGER,
                LocalDate.of(2023, 9, 26),
                LocalDate.of(2023, 12, 1),
                null,
                false,
                fødselsdato,
                fødselsdato,
                MorsAktivitet.UTDANNING,
                MORS_AKTIVITET_IKKE_GODKJENT);

        var søknad = new Søknad.Builder()
                .type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(gradering, foreldrepenger1, foreldrepenger2));

        var arbeid = new Arbeid.Builder()
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold1))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold2));
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .arbeid(arbeid)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(4);

        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);

        assertThat(fastsattePerioder.get(1).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(1).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT);

        assertThat(fastsattePerioder.get(2).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(2).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);
        assertThat(fastsattePerioder.get(2).uttakPeriode().getTrekkdager(arbeidsforhold1))
                .isEqualTo(new Trekkdager(40));
        assertThat(fastsattePerioder.get(2).uttakPeriode().getTrekkdager(arbeidsforhold2))
                .isEqualTo(Trekkdager.ZERO);

        assertThat(fastsattePerioder.get(3).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(3).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT);
    }

    // FAGSYSTEM-301302
    @Test
    void bfhr_minsterett_gradering_flere_arbeidsforhold_2() {
        var fødselsdato = LocalDate.of(2023, 10, 2);
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 40 * 5)).minsterettDager(8 * 5);
        var arbeidsforhold1 = AktivitetIdentifikator.forArbeid(new Orgnummer("1"), null);
        var arbeidsforhold2 = AktivitetIdentifikator.forArbeid(new Orgnummer("2"), null);
        var gradering = OppgittPeriode.forGradering(
                FORELDREPENGER,
                fødselsdato,
                LocalDate.of(2024, 1, 22),
                BigDecimal.valueOf(50),
                null,
                false,
                Set.of(arbeidsforhold1),
                fødselsdato,
                fødselsdato,
                null,
                null);

        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(gradering));

        var arbeid = new Arbeid.Builder()
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold1))
                .arbeidsforhold(new Arbeidsforhold(arbeidsforhold2));
        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .arbeid(arbeid)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(4);

        assertThat(fastsattePerioder.get(0).uttakPeriode().erGraderingInnvilget())
                .isTrue();
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(arbeidsforhold1))
                .isEqualTo(new Trekkdager(15));
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(arbeidsforhold2))
                .isEqualTo(new Trekkdager(30));
        assertThat(fastsattePerioder.get(1).uttakPeriode().erGraderingInnvilget())
                .isTrue();
        assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(arbeidsforhold1))
                .isEqualTo(new Trekkdager(5));
        assertThat(fastsattePerioder.get(1).uttakPeriode().getTrekkdager(arbeidsforhold2))
                .isEqualTo(new Trekkdager(10));
        assertThat(fastsattePerioder.get(2).uttakPeriode().erGraderingInnvilget())
                .isTrue();
        assertThat(fastsattePerioder.get(2).uttakPeriode().getTrekkdager(arbeidsforhold1))
                .isEqualTo(new Trekkdager(20));
        assertThat(fastsattePerioder.get(2).uttakPeriode().getTrekkdager(arbeidsforhold2))
                .isEqualTo(new Trekkdager(0));
        assertThat(fastsattePerioder.get(2).uttakPeriode().getUtbetalingsgrad(arbeidsforhold2))
                .isEqualTo(Utbetalingsgrad.ZERO);
        assertThat(fastsattePerioder.get(3).uttakPeriode().getPerioderesultattype())
                .isEqualTo(Perioderesultattype.AVSLÅTT);
        // Søkt en dag for mye minsterett
        assertThat(fastsattePerioder.get(3).uttakPeriode().getTrekkdager(arbeidsforhold1))
                .isEqualTo(new Trekkdager(0.5));
        assertThat(fastsattePerioder.get(3).uttakPeriode().getTrekkdager(arbeidsforhold2))
                .isEqualTo(new Trekkdager(1));
        assertThat(fastsattePerioder.get(3).uttakPeriode().getPeriodeResultatÅrsak())
                .isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
    }

    private boolean harPeriode(UttakPeriode p, Perioderesultattype prt, PeriodeResultatÅrsak prå, int dager) {
        return p.getPerioderesultattype().equals(prt)
                && p.getPeriodeResultatÅrsak().equals(prå)
                && (dager == -1
                        || p.getAktiviteter().stream()
                                        .map(UttakPeriodeAktivitet::getTrekkdager)
                                        .mapToInt(Trekkdager::rundOpp)
                                        .sum()
                                == dager);
    }

    private OppgittPeriode foreldrepenger(
            LocalDate fødselsdato, MorsAktivitet morsAktivitet, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(
                FORELDREPENGER,
                fødselsdato.plusWeeks(7),
                fødselsdato.plusWeeks(15).minusDays(1),
                null,
                false,
                fødselsdato,
                fødselsdato,
                morsAktivitet,
                dokumentasjonVurdering);
    }

    private OppgittPeriode foreldrepenger(
            LocalDate fom, LocalDate tom, MorsAktivitet morsAktivitet, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(
                FORELDREPENGER,
                fom,
                tom,
                null,
                false,
                fom.minusWeeks(3),
                fom.minusWeeks(3),
                morsAktivitet,
                dokumentasjonVurdering);
    }

    private OppgittPeriode foreldrepengerUtsettelse(
            LocalDate fom, LocalDate tom, MorsAktivitet morsAktivitet, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forUtsettelse(
                fom, tom, UtsettelseÅrsak.FERIE, fom, fom, morsAktivitet, dokumentasjonVurdering);
    }
}
