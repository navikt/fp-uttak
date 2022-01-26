package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
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
        var oppgittPeriodeU1 =  foreldrepengerUtsettelse(utsettelseFra, Virkedager.plusVirkedager(utsettelseFra, 19), MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriodeU1, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harManuellPeriode(p.getUttakPeriode(), Manuellbehandlingårsak.MOR_UFØR, 20))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_flerbarn_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().kontoList(List.of(konto(FORELDREPENGER, 285), konto(FLERBARNSDAGER, 85))).minsterettDager(75);
        var oppgittPeriode = foreldrepenger(fødselsdato, MorsAktivitet.UFØRE);
        var oppgittPeriode2 = foreldrepenger(fødselsdato.plusYears(1), MorsAktivitet.UFØRE);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittePerioder(List.of(oppgittPeriode, oppgittPeriode2));

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling().kreverSammenhengendeUttak(false))
                .datoer(new Datoer.Builder().fødsel(fødselsdato))
                .rettOgOmsorg(bareFarRett().morUføretrygd(true))
                .søknad(søknad)
                .inngangsvilkår(oppfyltAlleVilkår())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
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
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 75))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harManuellPeriode(p.getUttakPeriode(), Manuellbehandlingårsak.MOR_UFØR, 5))).isTrue();
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
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 35))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harManuellPeriode(p.getUttakPeriode(), Manuellbehandlingårsak.MOR_UFØR, 5))).isTrue(); // Søkt ufør, ikke dager igjen på minstekvote
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, IKKE_STØNADSDAGER_IGJEN, -1))).isTrue();
    }

    @Test
    void bfhr_mor_med_bekreftet_uføretrygd_uten_aktivitetskrav_flerbarn_skal_godkjennes() {
        var fødselsdato = Virkedager.justerHelgTilMandag(LocalDate.of(2022, 1, 1));
        var kontoer = new Kontoer.Builder().kontoList(List.of(konto(FORELDREPENGER, 285), konto(FLERBARNSDAGER, 85))).utenAktivitetskravDager(75);
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
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 40))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 35))).isTrue();
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
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.INNVILGET, FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_UFØR, 75))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harPeriode(p.getUttakPeriode(), Perioderesultattype.AVSLÅTT, BARE_FAR_RETT_IKKE_SØKT, 5))).isTrue();
        assertThat(fastsattePerioder.stream().anyMatch(p -> harManuellPeriode(p.getUttakPeriode(), Manuellbehandlingårsak.MOR_UFØR, 5))).isTrue();
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

    private OppgittPeriode foreldrepengerUtsettelse(LocalDate fom, LocalDate tom, MorsAktivitet utdanning) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.FERIE, fom, fom, utdanning);
    }



}
