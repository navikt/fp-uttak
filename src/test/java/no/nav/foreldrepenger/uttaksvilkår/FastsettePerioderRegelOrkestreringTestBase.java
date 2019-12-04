package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureTogglesForTester;

public abstract class FastsettePerioderRegelOrkestreringTestBase {
    static final AktivitetIdentifikator ARBEIDSFORHOLD = ARBEIDSFORHOLD_1;

    private FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    protected RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.create()
            .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL))
            .medBehandling(morBehandling())
            .medArbeid(new Arbeid.Builder()
                    .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, defaultKontoer())))
            .medInngangsvilkår(oppfyltAlleVilkår());

    Kontoer.Builder defaultKontoer() {
        return new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(15))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130));
    }

    protected RegelGrunnlag.Builder grunnlagAdopsjon = RegelGrunnlagTestBuilder.create()
            .medSøknad(new Søknad.Builder().medType(Søknadstype.ADOPSJON))
            .medBehandling(morBehandling())
            .medArbeid(new Arbeid.Builder()
                    .leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, new Kontoer.Builder()
                            .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50))
                            .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50))
                            .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130)))))
            .medInngangsvilkår(oppfyltAlleVilkår());

    LocalDate førsteLovligeUttaksdag(LocalDate fødselsdag) {
        return fødselsdag.withDayOfMonth(1).minusMonths(3);
    }

    void verifiserPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Perioderesultattype forventetResultat, Stønadskontotype stønadskontotype) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(forventetResultat);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
    }

    void verifiserAvslåttPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Stønadskontotype stønadskontotype, IkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(periode.getÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }


    void verifiserManuellBehandlingPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Stønadskontotype stønadskontotype, IkkeOppfyltÅrsak ikkeOppfyltÅrsak, Manuellbehandlingårsak manuellbehandlingårsak) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(periode.getManuellbehandlingårsak()).isEqualTo(manuellbehandlingårsak);
        assertThat(periode.getÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }

    Søknad.Builder søknad(Søknadstype søknadstype, UttakPeriode... perioder) {
        Søknad.Builder builder = new Søknad.Builder()
                .medMottattDato(perioder[0].getFom().minusWeeks(1))
                .medType(søknadstype);
        for (UttakPeriode uttakPeriode : perioder) {
            builder.leggTilSøknadsperiode(uttakPeriode);
        }
        return builder;
    }

    UttakPeriode søknadsperiode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return søknadsperiode(stønadskontotype, fom, tom, false, PeriodeVurderingType.PERIODE_OK, null);
    }

    UttakPeriode søknadsperiode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, boolean flerbarnsdager, PeriodeVurderingType periodeVurderingType, SamtidigUttak samtidigUttak) {
        StønadsPeriode stønadsPeriode = new StønadsPeriode(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, samtidigUttak, flerbarnsdager);
        stønadsPeriode.setPeriodeVurderingType(periodeVurderingType);
        return stønadsPeriode;
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
        return grunnlag
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                        .medFødsel(fødselsdato))
                .medRettOgOmsorg(beggeRett());
    }

    Arbeid.Builder arbeid(Konto.Builder... kontoList) {
        var kontoer = new Kontoer.Builder();
        for (Konto.Builder konto : kontoList) {
            kontoer.leggTilKonto(konto);
        }
        return new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD, kontoer));
    }

    Konto.Builder konto(Stønadskontotype stønadskontotype, int antallDager) {
        return new Konto.Builder().medType(stønadskontotype).medTrekkdager(antallDager);
    }

    RettOgOmsorg.Builder beggeRett() {
        return new RettOgOmsorg.Builder()
                .medSamtykke(true)
                .medMorHarRett(true)
                .medFarHarRett(true);
    }

    UtsettelsePeriode utsettelsePeriode(LocalDate fom,
                                        LocalDate tom,
                                        Utsettelseårsaktype utsettelseårsaktype) {
        return new UtsettelsePeriode(PeriodeKilde.SØKNAD, fom, tom, utsettelseårsaktype, PeriodeVurderingType.IKKE_VURDERT);
    }

    Inngangsvilkår.Builder oppfyltAlleVilkår() {
        return new Inngangsvilkår.Builder()
                .medAdopsjonOppfylt(true)
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
}
