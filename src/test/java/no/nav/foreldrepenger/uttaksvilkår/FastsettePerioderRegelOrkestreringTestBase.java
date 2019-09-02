package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;

public abstract class FastsettePerioderRegelOrkestreringTestBase {
    static final AktivitetIdentifikator ARBEIDSFORHOLD = ARBEIDSFORHOLD_1;

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    protected RegelGrunnlag.Builder grunnlag = RegelGrunnlagTestBuilder.normal()
            .medSøknad(new Søknad.Builder()
                    .medType(Søknadstype.FØDSEL)
                    .build())
            .medBehandling(new Behandling.Builder()
                    .medType(Behandlingtype.FØRSTEGANGSSØKNAD)
                    .medSøkerErMor(true)
                    .build())
            .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
                    .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(15).build())
                    .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50).build())
                    .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50).build())
                    .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130).build())
                    .build())
            .medInngangsvilkår(new Inngangsvilkår.Builder()
                    .medAdopsjonOppfylt(true)
                    .medForeldreansvarnOppfylt(true)
                    .medFødselOppfylt(true)
                    .medOpptjeningOppfylt(true)
                    .build());

    protected RegelGrunnlag.Builder grunnlagAdopsjon = RegelGrunnlagTestBuilder.normal()
        .medSøknad(new Søknad.Builder()
            .medType(Søknadstype.ADOPSJON)
            .build())
        .medBehandling(new Behandling.Builder()
            .medType(Behandlingtype.FØRSTEGANGSSØKNAD)
            .medSøkerErMor(true)
            .build())
        .leggTilKontoer(ARBEIDSFORHOLD, new Kontoer.Builder()
            .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(50).build())
            .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(50).build())
            .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(130).build())
            .build())
        .medInngangsvilkår(new Inngangsvilkår.Builder()
            .medAdopsjonOppfylt(true)
            .medForeldreansvarnOppfylt(true)
            .medFødselOppfylt(true)
            .medOpptjeningOppfylt(true)
            .build());

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

    Søknad søknad(Søknadstype søknadstype, UttakPeriode... perioder) {
        Søknad.Builder builder = new Søknad.Builder()
                .medType(søknadstype);
        for (UttakPeriode uttakPeriode : perioder) {
            builder.leggTilSøknadsperiode(uttakPeriode);
        }
        return builder.build();
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
        return basicGrunnlag(fødselsdato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(true).build());
    }

    RegelGrunnlag.Builder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(false).build());
    }

    RegelGrunnlag.Builder basicGrunnlag(LocalDate fødselsdato) {
        return grunnlag
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                        .medFødsel(fødselsdato)
                        .build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medFarHarRett(true)
                        .medMorHarRett(true)
                        .medSamtykke(true)
                        .build());
    }


}
