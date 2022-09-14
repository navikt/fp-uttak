package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITET_UKJENT_UDOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

class MinsterettOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {


    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var utsettelseFra = Virkedager.justerHelgTilMandag(fødselsdato.plusMonths(6));
        var oppgittPeriodeU1 =  foreldrepengerUtsettelse(utsettelseFra, Virkedager.plusVirkedager(utsettelseFra, 19), MorsAktivitet.UTDANNING);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE);
        var dokumentasjon = new Dokumentasjon.Builder()
                .periodeMedAvklartMorsAktivitet(new PeriodeMedAvklartMorsAktivitet(oppgittPeriodeU1.getFom(), oppgittPeriodeU1.getTom(), IKKE_I_AKTIVITET_IKKE_DOKUMENTERT));
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).dokumentasjon(dokumentasjon)
                .oppgittePerioder(List.of(oppgittPeriode, oppgittPeriodeU1, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT, 20))).isTrue();; // Søkt ufør, ikke dager igjen på minstekvote
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_flerbarn_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 2, 1));
        var kontoer = new Kontoer.Builder().kontoList(List.of(konto(FORELDREPENGER, 285))).flerbarnsdager(85).minsterettDager(85);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE);
        var oppgittPeriode3 = foreldrepenger(fødselsdato.plusYears(2), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2, oppgittPeriode3));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(harPeriode(fastsattePerioder.get(0).getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5)).isTrue();
        assertThat(harPeriode(fastsattePerioder.get(1).getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 40)).isTrue();
        assertThat(harPeriode(fastsattePerioder.get(2).getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 195)).isTrue();
        assertThat(harPeriode(fastsattePerioder.get(4).getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 40)).isTrue();
        assertThat(harPeriode(fastsattePerioder.get(5).getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 0)).isTrue();
        assertThat(harPeriode(fastsattePerioder.get(6).getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 5)).isTrue();
        assertThat(harPeriode(fastsattePerioder.get(7).getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1)).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_overskrider_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(23).minusDays(1), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 75))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITET_UKJENT_UDOKUMENTERT, 5))).isTrue();; // Søkt ufør, ikke dager igjen på minstekvote
    }

    @Test
    void bfhr_enkel_minsterett_vs_innvilget_med_mye_godkjent_aktivitet() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(40);
        var oppgittPeriode = foreldrepenger(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(45).minusDays(1), MorsAktivitet.UTDANNING);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusWeeks(45), fødselsdato.plusWeeks(47).minusDays(1), null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad.dokumentasjon(new Dokumentasjon.Builder()
                        .periodeMedAvklartMorsAktivitet(new PeriodeMedAvklartMorsAktivitet(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(45).minusDays(1), I_AKTIVITET))))
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer)
                .build();
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(4);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 190))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, 0))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusWeeks(25), MorsAktivitet.UFØRE);
        var oppgittPeriode3 = foreldrepenger(fødselsdato.plusWeeks(40), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2, oppgittPeriode3));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITET_UKJENT_UDOKUMENTERT, 5))).isTrue();; // Søkt ufør, ikke dager igjen på minstekvote
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_avslått_periode_med_aktivitet_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato.minusWeeks(1), MorsAktivitet.SYK);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusWeeks(14), MorsAktivitet.SYK);
        var dokSyk = new Dokumentasjon.Builder()
                .periodeMedAvklartMorsAktivitet(new PeriodeMedAvklartMorsAktivitet(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(14).minusDays(1), I_AKTIVITET));
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2))
                .dokumentasjon(dokSyk);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT, 40))).isTrue(); // Akt krav oppfylt
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 35))).isTrue(); // Mellomliggende
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 40))).isTrue(); // Akt krav ikke oppfylt - innvilget fra uføre
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_flerbarn_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().kontoList(List.of(konto(FORELDREPENGER, 285)))
                .flerbarnsdager(85).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(Virkedager.justerHelgTilMandag(fødselsdato.plusWeeks(49)), MorsAktivitet.UFØRE); // Strekker seg utover stønadsperioden
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_overskrider_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).utenAktivitetskravDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(23).minusDays(1), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET,
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, 75))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, AKTIVITET_UKJENT_UDOKUMENTERT, 5))).isTrue();; // Søkt ufør, ikke dager igjen på minstekvote
    }

    @Test
    void bfhr_flerbarnsdager_skal_også_trekkes_fra_minsteretten() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 200)).minsterettDager(5).flerbarnsdager(5);
        //Uten mors aktivitet bruker opp minsterett
        var oppgittPeriode1 = oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(7).minusDays(1));
        var oppgittPeriode2 = oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8).minusDays(1),
                true, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(
                List.of(oppgittPeriode1, oppgittPeriode2));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);

        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
    }

    @Test
    void bfhr_flerbarnsdager_minsterett_skal_gå_tom_for_dager_selv_om_ikke_minsterett_brukes_opp() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 40)).minsterettDager(17).flerbarnsdager(10);
        var oppgittPeriode1 = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(14).minusDays(1),
                null, false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato, MorsAktivitet.ARBEID);
        var oppgittPeriode2 = oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(14), fødselsdato.plusWeeks(17).minusDays(1),
                true, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, oppgittPeriode2))
                .dokumentasjon(new Dokumentasjon.Builder().periodeMedAvklartMorsAktivitet(new PeriodeMedAvklartMorsAktivitet(oppgittPeriode1.getFom(),
                        oppgittPeriode1.getTom(), I_AKTIVITET)));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                FORELDREPENGER_KUN_FAR_HAR_RETT);

        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void bfhr_flerbarnsdager_minsterett_skal_gå_tom_for_dager_når_tomt_for_flerbarnsdager() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 40)).minsterettDager(10).flerbarnsdager(10);
        var oppgittPeriode1 = oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                true, null);
        var oppgittPeriode2 = oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(10).minusDays(1),
                true, null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, oppgittPeriode2));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);

        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(AKTIVITET_UKJENT_UDOKUMENTERT);
    }

    @Test
    void bfhr_flerbarnsdager_trekker_minsterett() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().konto(konto(FORELDREPENGER, 40)).minsterettDager(25).flerbarnsdager(20);
        var oppgittPeriode1 = oppgittPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1),
                true, null);
        var gradertPeriode = OppgittPeriode.forGradering(FORELDREPENGER, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(10),
                BigDecimal.TEN, null, true, Set.of(ARBEIDSFORHOLD), PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato,
                null);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL)
                .oppgittePerioder(List.of(oppgittPeriode1, gradertPeriode));

        var grunnlag = basicGrunnlagFar(fødselsdato)
                .rettOgOmsorg(bareFarRett())
                .søknad(søknad)
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(2);

        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);

        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV);
    }

    private boolean harPeriode(UttakPeriode p, Perioderesultattype prt, PeriodeResultatÅrsak prå, int dager) {
        return p.getPerioderesultattype().equals(prt) && p.getPeriodeResultatÅrsak().equals(prå) &&
                (dager == -1 || p.getAktiviteter().stream().map(UttakPeriodeAktivitet::getTrekkdager).mapToInt(Trekkdager::rundOpp).sum() == dager);
    }

    private boolean harManuellPeriode(UttakPeriode p, Manuellbehandlingårsak årsak, int dager) {
        return p.getPerioderesultattype().equals(Perioderesultattype.MANUELL_BEHANDLING) && p.getManuellbehandlingårsak().equals(årsak) &&
                (dager == -1 || p.getAktiviteter().stream().map(UttakPeriodeAktivitet::getTrekkdager).mapToInt(Trekkdager::rundOpp).sum() == dager);
    }

    private OppgittPeriode foreldrepenger(LocalDate fødselsdato, MorsAktivitet utdanning) {
        return OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(15).minusDays(1), null,
                false, PeriodeVurderingType.IKKE_VURDERT, fødselsdato, fødselsdato, utdanning);
    }

    private OppgittPeriode foreldrepenger(LocalDate fom, LocalDate tom, MorsAktivitet utdanning) {
        return OppgittPeriode.forVanligPeriode(FORELDREPENGER, fom, tom, null,
                false, PeriodeVurderingType.IKKE_VURDERT, fom.minusWeeks(3), fom.minusWeeks(3), utdanning);
    }

    private OppgittPeriode foreldrepengerUtsettelse(LocalDate fom, LocalDate tom, MorsAktivitet morsAktivitet) {
        return OppgittPeriode.forUtsettelse(fom, tom, UtsettelseÅrsak.FERIE, fom, fom, morsAktivitet);
    }



}
