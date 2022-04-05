package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;


import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.utsettelsePeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import org.junit.jupiter.api.Test;

class UtsettelseDelregelSammenhengendeUttakTest {


    @Test
    void UT1101_ferie_innenfor_seks_første_uker() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var periode = OppgittPeriode.forUtsettelse(fødselsdato.plusWeeks(4), fødselsdato.plusWeeks(5),
                PeriodeVurderingType.IKKE_VURDERT, UtsettelseÅrsak.FERIE,
                fødselsdato.minusWeeks(1), null, null); // innenfor seks uker etter fødsel
        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().trekkdager(100).type(Stønadskontotype.MØDREKVOTE));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(
                new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(periode))
                .behandling(morBehandling())
                .rettOgOmsorg(beggeRett())
                .datoer(new Datoer.Builder().fødsel(fødselsdato).termin(fødselsdato))
                .inngangsvilkår(oppfylt())
                .build();

        var resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1101");
        assertThat(resultat.oppfylt()).isFalse();
        assertThat(resultat.getManuellbehandlingårsak()).isNull(); // ikke satt ved automatisk behandling
        assertThat(resultat.trekkDagerFraSaldo()).isTrue();
        assertThat(resultat.skalUtbetale()).isTrue();
    }

    @Test
    void UT1124_fødsel_mer_enn_7_uker_før_termin() {
        var fom = LocalDate.of(2019, 7, 1);
        var periode = utsettelsePeriode(fom, fom, UtsettelseÅrsak.INNLAGT_BARN);
        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().trekkdager(100).type(Stønadskontotype.MØDREKVOTE));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(
                new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(periode)
                        .dokumentasjon(new Dokumentasjon.Builder().periodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom, fom))))
                .behandling(morBehandling())
                .revurdering(new Revurdering.Builder().endringsdato(fom))
                .rettOgOmsorg(beggeRett())
                .datoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .fødsel(fom).termin(fom.plusWeeks(8)))
                .inngangsvilkår(oppfylt())
                .build();

        var resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1124");
    }

    @Test
    void UT1120_fødsel_mer_enn_7_uker_før_termin_perioden_ligger_etter_termin() {
        var fom = LocalDate.of(2019, 7, 1);
        var periode = utsettelsePeriode(fom.plusWeeks(10), fom.plusWeeks(10), UtsettelseÅrsak.INNLAGT_BARN);
        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().trekkdager(100).type(Stønadskontotype.MØDREKVOTE));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(
                new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(periode)
                        .dokumentasjon(new Dokumentasjon.Builder().periodeMedBarnInnlagt(
                                new PeriodeMedBarnInnlagt(fom.plusWeeks(10), fom.plusWeeks(10)))))
                .behandling(morBehandling())
                .revurdering(new Revurdering.Builder().endringsdato(fom))
                .rettOgOmsorg(beggeRett())
                .datoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .fødsel(fom).termin(fom.plusWeeks(8)))
                .inngangsvilkår(oppfylt())
                .build();

        var resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1120");
    }

    @Test
    void UT1120_fødsel_mindre_enn_7_uker_før_termin() {
        var fom = LocalDate.of(2019, 7, 1);
        var periode = utsettelsePeriode(fom, fom, UtsettelseÅrsak.INNLAGT_BARN);
        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().trekkdager(100).type(Stønadskontotype.MØDREKVOTE));
        var grunnlag = new RegelGrunnlag.Builder().arbeid(
                new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .kontoer(kontoer)
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(periode)
                        .dokumentasjon(new Dokumentasjon.Builder().periodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fom, fom))))
                .behandling(morBehandling())
                .revurdering(new Revurdering.Builder().endringsdato(fom))
                .rettOgOmsorg(beggeRett())
                .datoer(new Datoer.Builder()
                        //Nok til å få prematuruker
                        .fødsel(fom).termin(fom))
                .inngangsvilkår(oppfylt())
                .build();

        var resultat = kjørRegel(periode, grunnlag);

        assertThat(resultat.sluttpunktId()).isEqualTo("UT1120");
    }

    private RettOgOmsorg.Builder beggeRett() {
        return new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true).samtykke(true);
    }

    private Behandling.Builder morBehandling() {
        return new Behandling.Builder().søkerErMor(true).kreverSammenhengendeUttak(true);
    }

    private Inngangsvilkår.Builder oppfylt() {
        return new Inngangsvilkår.Builder().fødselOppfylt(true)
                .adopsjonOppfylt(true)
                .foreldreansvarnOppfylt(true)
                .opptjeningOppfylt(true);
    }
}
