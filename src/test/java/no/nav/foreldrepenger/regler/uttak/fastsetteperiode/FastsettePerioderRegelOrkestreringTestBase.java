package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public abstract class FastsettePerioderRegelOrkestreringTestBase {
    static final AktivitetIdentifikator ARBEIDSFORHOLD = ARBEIDSFORHOLD_1;

    private FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    protected RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
            .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
            .medBehandling(morBehandling())
            .medKontoer(defaultKontoer())
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .medInngangsvilkår(oppfyltAlleVilkår());

    Kontoer.Builder defaultKontoer() {
        return new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(15))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130));
    }

    protected RegelGrunnlag.Builder grunnlagAdopsjon = RegelGrunnlagTestBuilder.create()
            .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON))
            .medBehandling(morBehandling())
            .medKontoer(new Kontoer.Builder().leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50))
                    .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50))
                    .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130)))
            .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .medInngangsvilkår(oppfyltAlleVilkår());


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
        Søknad.Builder builder = new Søknad.Builder().medType(søknadstype);
        for (OppgittPeriode oppgittPeriode : perioder) {
            builder.leggTilOppgittPeriode(oppgittPeriode);
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
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, samtidigUttaksprosent, flerbarnsdager, vurderingType, null);
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
                PeriodeVurderingType.IKKE_VURDERT, null);
    }

    RegelGrunnlag.Builder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato).medBehandling(morBehandling());
    }

    Behandling.Builder morBehandling() {
        return new Behandling.Builder().medSøkerErMor(true);
    }

    Behandling.Builder farBehandling() {
        return new Behandling.Builder().medSøkerErMor(false);
    }

    RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato).medBehandling(farBehandling());
    }

    RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato)).medRettOgOmsorg(beggeRett());
    }

    Konto.Builder konto(Stønadskontotype stønadskontotype, int antallDager) {
        return new Konto.Builder().medType(stønadskontotype).medTrekkdager(antallDager);
    }

    RettOgOmsorg.Builder beggeRett() {
        return new RettOgOmsorg.Builder().medSamtykke(true).medMorHarRett(true).medFarHarRett(true);
    }

    OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.PERIODE_OK, utsettelseÅrsak, null);
    }

    Inngangsvilkår.Builder oppfyltAlleVilkår() {
        return new Inngangsvilkår.Builder().medAdopsjonOppfylt(true)
                .medForeldreansvarnOppfylt(true)
                .medFødselOppfylt(true)
                .medOpptjeningOppfylt(true);
    }

    RettOgOmsorg.Builder aleneomsorg() {
        return beggeRett().medAleneomsorg(true);
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
