package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.*;
import static org.assertj.core.api.Assertions.assertThat;

abstract class FastsettePerioderRegelOrkestreringTestBase {
    static final AktivitetIdentifikator ARBEIDSFORHOLD = ARBEIDSFORHOLD_1;

    private final FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    protected final RegelGrunnlag.Builder grunnlagAdopsjon = RegelGrunnlagTestBuilder.create()
            .søknad(new Søknad.Builder().type(Søknadstype.ADOPSJON))
            .behandling(morBehandling())
            .kontoer(new Kontoer.Builder().konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(50))
                    .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(50))
                    .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(130)))
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .inngangsvilkår(oppfyltAlleVilkår());


    void verifiserPeriode(UttakPeriode periode,
                          LocalDate forventetFom,
                          LocalDate forventetTom,
                          Perioderesultattype forventetResultat,
                          Stønadskontotype stønadskontotype) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(forventetResultat);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
    }

    void verifiserAvslåttPeriode(UttakPeriode periode,
                                 LocalDate forventetFom,
                                 LocalDate forventetTom,
                                 Stønadskontotype stønadskontotype,
                                 IkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(periode.getPeriodeResultatÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }


    RegelGrunnlag.Builder basicGrunnlag() {
        return RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL))
                .behandling(morBehandling())
                .kontoer(defaultKontoer())
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
                .inngangsvilkår(oppfyltAlleVilkår());
    }

    Kontoer.Builder defaultKontoer() {
        return new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER_FØR_FØDSEL).trekkdager(15))
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(50))
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(50))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(130));
    }

    void verifiserManuellBehandlingPeriode(UttakPeriode periode,
                                           LocalDate forventetFom,
                                           LocalDate forventetTom,
                                           Stønadskontotype stønadskontotype,
                                           IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                                           Manuellbehandlingårsak manuellbehandlingårsak) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(periode.getManuellbehandlingårsak()).isEqualTo(manuellbehandlingårsak);
        assertThat(periode.getPeriodeResultatÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }

    Søknad.Builder søknad(Søknadstype søknadstype, OppgittPeriode... perioder) {
        var builder = new Søknad.Builder().type(søknadstype);
        for (var oppgittPeriode : perioder) {
            builder.oppgittPeriode(oppgittPeriode);
        }
        return builder;
    }

    OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return oppgittPeriode(stønadskontotype, fom, tom, false, null);
    }

    OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype,
                                  LocalDate fom,
                                  LocalDate tom,
                                  boolean flerbarnsdager,
                                  SamtidigUttaksprosent samtidigUttaksprosent) {
        return oppgittPeriode(stønadskontotype, fom, tom, flerbarnsdager, samtidigUttaksprosent, PeriodeVurderingType.IKKE_VURDERT);
    }

    OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype,
                                  LocalDate fom,
                                  LocalDate tom,
                                  boolean flerbarnsdager,
                                  SamtidigUttaksprosent samtidigUttaksprosent,
                                  PeriodeVurderingType vurderingType) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, samtidigUttaksprosent, flerbarnsdager, vurderingType, null,
                null, null);
    }

    OppgittPeriode gradertoppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, BigDecimal arbeidsprosent) {
        return gradertoppgittPeriode(stønadskontotype, fom, tom, arbeidsprosent, Set.of(ARBEIDSFORHOLD_1));
    }

    OppgittPeriode gradertoppgittPeriode(Stønadskontotype stønadskontotype,
                                         LocalDate fom,
                                         LocalDate tom,
                                         BigDecimal arbeidsprosent,
                                         Set<AktivitetIdentifikator> gradertAktiviteter) {
        return OppgittPeriode.forGradering(stønadskontotype, fom, tom, arbeidsprosent, null, false, gradertAktiviteter,
                PeriodeVurderingType.IKKE_VURDERT, null, null, null);
    }

    RegelGrunnlag.Builder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato).behandling(morBehandling());
    }

    Behandling.Builder morBehandling() {
        return new Behandling.Builder().søkerErMor(true);
    }

    Behandling.Builder farBehandling() {
        return new Behandling.Builder().søkerErMor(false);
    }

    RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato).behandling(farBehandling());
    }

    RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return basicGrunnlag().datoer(new Datoer.Builder().fødsel(fødselsdato)).rettOgOmsorg(beggeRett());
    }

    Konto.Builder konto(Stønadskontotype stønadskontotype, int antallDager) {
        return new Konto.Builder().type(stønadskontotype).trekkdager(antallDager);
    }

    RettOgOmsorg.Builder beggeRett() {
        return new RettOgOmsorg.Builder().samtykke(true).morHarRett(true).farHarRett(true);
    }

    RettOgOmsorg.Builder bareFarRett() {
        return new RettOgOmsorg.Builder().samtykke(true).morHarRett(false).farHarRett(true);
    }

    OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.PERIODE_OK, utsettelseÅrsak, null, null, null);
    }

    Inngangsvilkår.Builder oppfyltAlleVilkår() {
        return new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                .foreldreansvarnOppfylt(true)
                .fødselOppfylt(true)
                .opptjeningOppfylt(true);
    }

    RettOgOmsorg.Builder aleneomsorg() {
        return beggeRett().aleneomsorg(true);
    }

    List<FastsettePeriodeResultat> fastsettPerioder(RegelGrunnlag grunnlag) {
        return fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag, new FeatureTogglesForTester());
    }

    List<FastsettePeriodeResultat> fastsettPerioder(RegelGrunnlag.Builder grunnlag) {
        return fastsettPerioder(grunnlag.build());
    }

    Set<AktivitetIdentifikator> aktiviteterIPeriode(UttakPeriode uttakPeriode) {
        return uttakPeriode.getAktiviteter().stream().map(a -> a.getIdentifikator()).collect(Collectors.toSet());
    }
}
