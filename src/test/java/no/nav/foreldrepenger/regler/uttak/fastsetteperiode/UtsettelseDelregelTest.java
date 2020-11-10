package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.utsettelsePeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class UtsettelseDelregelTest {


    @Test
    public void UT1101_ferie_innenfor_seks_første_uker() {
        LocalDate fødselsdato = LocalDate.of(2019, 7, 1);
        var periode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5),
                PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.FERIE,
                fødselsdato.minusWeeks(1)); // innenfor seks uker etter fødsel
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medTrekkdager(100).medType(Stønadskontotype.MØDREKVOTE));
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(periode))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder().medFødsel(fødselsdato).medTermin(fødselsdato))
                .medInngangsvilkår(oppfylt())
                .build();

        FastsettePerioderRegelresultat resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1101");
        assertThat(resultat.oppfylt()).isFalse();
        assertThat(resultat.getManuellbehandlingårsak()).isNull(); // ikke satt ved automatisk behandling
        assertThat(resultat.trekkDagerFraSaldo()).isTrue();
        assertThat(resultat.skalUtbetale()).isTrue();
    }

    @Test
    public void UT1124_fødsel_mer_enn_7_uker_før_termin() {
        LocalDate fom = LocalDate.of(2019, 7, 1);
        var periode = utsettelsePeriode(fom, fom, UtsettelseÅrsak.INNLAGT_BARN);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medTrekkdager(100).medType(Stønadskontotype.MØDREKVOTE));
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(periode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom, fom))))
                .medBehandling(behandlingMor())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(fom))
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .medFødsel(fom).medTermin(fom.plusWeeks(8)))
                .medInngangsvilkår(oppfylt())
                .build();

        FastsettePerioderRegelresultat resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1124");
    }

    @Test
    public void UT1120_fødsel_mer_enn_7_uker_før_termin_perioden_ligger_etter_termin() {
        LocalDate fom = LocalDate.of(2019, 7, 1);
        var periode = utsettelsePeriode(fom.plusWeeks(10), fom.plusWeeks(10), UtsettelseÅrsak.INNLAGT_BARN);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medTrekkdager(100).medType(Stønadskontotype.MØDREKVOTE));
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(periode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(
                                new PeriodeMedBarnInnlagt(fom.plusWeeks(10), fom.plusWeeks(10)))))
                .medBehandling(behandlingMor())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(fom))
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .medFødsel(fom).medTermin(fom.plusWeeks(8)))
                .medInngangsvilkår(oppfylt())
                .build();

        FastsettePerioderRegelresultat resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1120");
    }

    @Test
    public void UT1120_fødsel_mindre_enn_7_uker_før_termin() {
        LocalDate fom = LocalDate.of(2019, 7, 1);
        var periode = utsettelsePeriode(fom, fom, UtsettelseÅrsak.INNLAGT_BARN);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().leggTilKonto(new Konto.Builder().medTrekkdager(100).medType(Stønadskontotype.MØDREKVOTE));
        RegelGrunnlag grunnlag = new RegelGrunnlag.Builder().medArbeid(
                new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medKontoer(kontoer)
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(periode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom, fom))))
                .medBehandling(behandlingMor())
                .medRevurdering(new Revurdering.Builder().medEndringsdato(fom))
                .medRettOgOmsorg(beggeRett())
                .medDatoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .medFødsel(fom).medTermin(fom))
                .medInngangsvilkår(oppfylt())
                .build();

        FastsettePerioderRegelresultat resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1120");
    }

    private RettOgOmsorg.Builder beggeRett() {
        return new RettOgOmsorg.Builder().medFarHarRett(true).medMorHarRett(true).medSamtykke(true);
    }

    private Behandling.Builder behandlingMor() {
        return new Behandling.Builder().medSøkerErMor(true);
    }

    private Inngangsvilkår.Builder oppfylt() {
        return new Inngangsvilkår.Builder().medFødselOppfylt(true)
                .medAdopsjonOppfylt(true)
                .medForeldreansvarnOppfylt(true)
                .medOpptjeningOppfylt(true);
    }
}
