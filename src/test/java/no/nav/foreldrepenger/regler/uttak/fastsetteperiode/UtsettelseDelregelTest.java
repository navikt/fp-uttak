package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class UtsettelseDelregelTest {


    @Test
    public void UT1101_ferie_innenfor_seks_første_uker() {
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        UtsettelsePeriode periode = utsettelsePeriode(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5), Utsettelseårsaktype.FERIE); // innenfor seks uker etter fødsel
        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        kontoer.put(aktivitetIdentifikator, new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medTrekkdager(100)
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .build())
                .build());
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder()
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())).build())
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(periode)
                        .build())
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true)
                        .build())
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        .medFødsel(fødselsdato)
                        .medTermin(fødselsdato)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 1, 1))
                        .build())
                .medInngangsvilkår(oppfylt())
                .build();

        Regelresultat resultat = evaluer(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1101");
        assertThat(resultat.oppfylt()).isFalse();
        assertThat(resultat.getManuellbehandlingårsak()).isNull(); // ikke satt ved automatisk behandling
        assertThat(resultat.trekkDagerFraSaldo()).isTrue();
        assertThat(resultat.skalUtbetale()).isTrue();
    }

    @Test
    public void UT1124_fødsel_mer_enn_7_uker_før_termin() {
        LocalDate fom = LocalDate.of(2019, 7, 1);
        UtsettelsePeriode periode = utsettelsePeriode(fom, fom, Utsettelseårsaktype.INNLAGT_BARN);
        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        kontoer.put(aktivitetIdentifikator, new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medTrekkdager(100)
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .build())
                .build());
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder()
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())).build())
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(periode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom, fom))
                                .build())
                        .build())
                .medBehandling(behandlingMor())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(fom)
                        .build())
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .medFødsel(fom)
                        .medTermin(fom.plusWeeks(8))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 1, 1))
                        .build())
                .medInngangsvilkår(oppfylt())
                .build();

        Regelresultat resultat = evaluer(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1124");
    }

    @Test
    public void UT1120_fødsel_mer_enn_7_uker_før_termin_perioden_ligger_etter_termin() {
        LocalDate fom = LocalDate.of(2019, 7, 1);
        UtsettelsePeriode periode = utsettelsePeriode(fom.plusWeeks(10), fom.plusWeeks(10), Utsettelseårsaktype.INNLAGT_BARN);
        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        kontoer.put(aktivitetIdentifikator, new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medTrekkdager(100)
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .build())
                .build());
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())).build())
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(periode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom.plusWeeks(10), fom.plusWeeks(10)))
                                .build())
                        .build())
                .medBehandling(behandlingMor())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(fom)
                        .build())
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .medFødsel(fom)
                        .medTermin(fom.plusWeeks(8))
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 1, 1))
                        .build())
                .medInngangsvilkår(oppfylt())
                .build();

        Regelresultat resultat = evaluer(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1120");
    }

    @Test
    public void UT1120_fødsel_mindre_enn_7_uker_før_termin() {
        LocalDate fom = LocalDate.of(2019, 7, 1);
        UtsettelsePeriode periode = utsettelsePeriode(fom, fom, Utsettelseårsaktype.INNLAGT_BARN);
        HashMap<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        kontoer.put(aktivitetIdentifikator, new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder()
                        .medTrekkdager(100)
                        .medType(Stønadskontotype.MØDREKVOTE)
                        .build())
                .build());
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder()
                .medArbeid(new ArbeidGrunnlag.Builder().medArbeidsprosenter(new Arbeidsprosenter().leggTil(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())).build())
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(periode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggPerioderMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom, fom))
                                .build())
                        .build())
                .medBehandling(behandlingMor())
                .medRevurdering(new Revurdering.Builder()
                        .medEndringsdato(fom)
                        .build())
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .medFødsel(fom)
                        .medTermin(fom)
                        .medFørsteLovligeUttaksdag(LocalDate.of(2017, 1, 1))
                        .build())
                .medInngangsvilkår(oppfylt())
                .build();

        Regelresultat resultat = evaluer(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1120");
    }

    private RettOgOmsorg beggeRett() {
        return new RettOgOmsorg.Builder()
                .medFarHarRett(true)
                .medMorHarRett(true)
                .medSamtykke(true)
                .build();
    }

    private Behandling behandlingMor() {
        return new Behandling.Builder()
                .medSøkerErMor(true)
                .build();
    }

    private Inngangsvilkår oppfylt() {
        return new Inngangsvilkår.Builder()
                .medFødselOppfylt(true)
                .medAdopsjonOppfylt(true)
                .medForeldreansvarnOppfylt(true)
                .medOpptjeningOppfylt(true)
                .build();
    }

    private Regelresultat evaluer(UtsettelsePeriode uttakPeriode, RegelGrunnlag grunnlag) {
        FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);
        Evaluation evaluer = regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode));
        return new Regelresultat(evaluer);
    }

    private UtsettelsePeriode utsettelsePeriode(LocalDate fom, LocalDate tom, Utsettelseårsaktype utsettelsesÅrsak) {
        return new UtsettelsePeriode(PeriodeKilde.SØKNAD, fom, tom, utsettelsesÅrsak, PeriodeVurderingType.IKKE_VURDERT, null, false);
    }

}
