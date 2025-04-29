package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Rettighetstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

abstract class FastsettePerioderRegelOrkestreringTestBase {
    static final AktivitetIdentifikator ARBEIDSFORHOLD = ARBEIDSFORHOLD_1;

    private final FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

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
        return oppgittPeriode(stønadskontotype, fom, tom, flerbarnsdager, samtidigUttaksprosent, null);
    }

    OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype,
                                  LocalDate fom,
                                  LocalDate tom,
                                  boolean flerbarnsdager,
                                  SamtidigUttaksprosent samtidigUttaksprosent,
                                  DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, samtidigUttaksprosent, flerbarnsdager, null, null, null, null,
            dokumentasjonVurdering);
    }

    OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, null, null, null, null, dokumentasjonVurdering);
    }

    OppgittPeriode gradertoppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, BigDecimal arbeidsprosent) {
        return gradertoppgittPeriode(stønadskontotype, fom, tom, arbeidsprosent, Set.of(ARBEIDSFORHOLD_1));
    }

    OppgittPeriode gradertoppgittPeriode(Stønadskontotype stønadskontotype,
                                         LocalDate fom,
                                         LocalDate tom,
                                         BigDecimal arbeidsprosent,
                                         Set<AktivitetIdentifikator> gradertAktiviteter) {
        return OppgittPeriode.forGradering(stønadskontotype, fom, tom, arbeidsprosent, null, false, gradertAktiviteter, null, null, null, null, null);
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
        return rettogOmsorg(Rettighetstype.BEGGE_RETT);
    }

    RettOgOmsorg.Builder bareFarRett() {
        return rettogOmsorg(Rettighetstype.BARE_FAR_RETT);
    }

    RettOgOmsorg.Builder bareMorRett() {
        return rettogOmsorg(Rettighetstype.BARE_MOR_RETT);
    }

    private static RettOgOmsorg.Builder rettogOmsorg(Rettighetstype rettighetstype) {
        return new RettOgOmsorg.Builder().samtykke(true).rettighetstype(rettighetstype);
    }

    OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak, DokumentasjonVurdering dokumentasjonVurdering) {
        return utsettelsePeriode(fom, tom, utsettelseÅrsak, null, dokumentasjonVurdering);
    }

    OppgittPeriode utsettelsePeriode(LocalDate fom,
                                     LocalDate tom,
                                     UtsettelseÅrsak utsettelseÅrsak,
                                     MorsAktivitet morsAktivitet,
                                     DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forUtsettelse(fom, tom, utsettelseÅrsak, null, null, morsAktivitet, dokumentasjonVurdering);
    }

    Inngangsvilkår.Builder oppfyltAlleVilkår() {
        return new Inngangsvilkår.Builder().adopsjonOppfylt(true).foreldreansvarnOppfylt(true).fødselOppfylt(true).opptjeningOppfylt(true).medlemskapOppfylt(true);
    }

    RettOgOmsorg.Builder aleneomsorg() {
        return new RettOgOmsorg.Builder().rettighetstype(Rettighetstype.ALENEOMSORG);
    }

    List<FastsettePeriodeResultat> fastsettPerioder(RegelGrunnlag grunnlag) {
        return fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);
    }

    List<FastsettePeriodeResultat> fastsettPerioder(RegelGrunnlag.Builder grunnlag) {
        return fastsettPerioder(grunnlag.build());
    }

    Set<AktivitetIdentifikator> aktiviteterIPeriode(UttakPeriode uttakPeriode) {
        return uttakPeriode.getAktiviteter().stream().map(UttakPeriodeAktivitet::getIdentifikator).collect(Collectors.toSet());
    }
}
