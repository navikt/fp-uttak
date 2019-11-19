package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Årsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class ForeldrepengerFørFødselDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void UT1070_mor_utenFor3UkerFørFødsel() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.plusWeeks(8), familiehendelseDato.plusWeeks(9));
        RegelGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode, familiehendelseDato.minusWeeks(1)))
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuell(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false);
    }

    @Test
    public void UT1071_mor_innenFor3UkerFørFødsel_ikkeManglendeSøktPeriode_ikkeGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2), familiehendelseDato.minusWeeks(1));
        RegelGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode, familiehendelseDato.minusWeeks(1)))
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
    }

    @Test
    public void UT1072_mor_innenFor3UkerFørFødsel_ikkeManglendeSøktPeriode_gradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        UttakPeriode uttakPeriode = gradertPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2), familiehendelseDato.minusWeeks(1));
        RegelGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode, familiehendelseDato.minusWeeks(4)))
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1073_mor_innenFor3UkerFørFødsel_manglendeSøktPeriode() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        UttakPeriode uttakPeriode = oppholdPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                familiehendelseDato.minusWeeks(2), familiehendelseDato.minusWeeks(1));
        RegelGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medSøknad(søknad(uttakPeriode, familiehendelseDato.minusWeeks(1)))
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, kontoer(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
    }

    @Test
    public void UT1076_far_søker_fpff() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        UttakPeriode uttakPeriode = uttakPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, familiehendelseDato.minusWeeks(2), familiehendelseDato.minusWeeks(1));
        RegelGrunnlag grunnlag = basicGrunnlag(familiehendelseDato)
                .medBehandling(søkerErFarBehandling())
                .medSøknad(søknad(uttakPeriode, familiehendelseDato.minusWeeks(1)))
                .leggTilKontoer(RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1, new Kontoer.Builder()
                        .leggTilKonto(konto(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100))
                        //Må ha ellers å faller vi ut på FP_VK 10.5.1 - SjekkOmTomForAlleSineKontoer
                        .leggTilKonto(konto(Stønadskontotype.FEDREKVOTE, 100)))
                .build();

        Regelresultat regelresultat = evaluer(uttakPeriode, grunnlag);

        assertManuell(regelresultat, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false);
    }

    private Regelresultat evaluer(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag,
                Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode)));
    }

    private Kontoer.Builder kontoer(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Kontoer.Builder().leggTilKonto(konto(stønadskontotype, trekkdager));
    }

    private UttakPeriode gradertPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return StønadsPeriode.medGradering(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, Collections.singletonList(AktivitetIdentifikator.forFrilans()),
                BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK);
    }

    private UttakPeriode oppholdPeriode(Stønadskontotype stønadskontotype, Oppholdårsaktype oppholdårsaktype, LocalDate fom, LocalDate tom) {
        OppholdPeriode opphold = new OppholdPeriode(stønadskontotype, PeriodeKilde.SØKNAD, oppholdårsaktype, fom, tom, null, false);
        opphold.setPeriodeVurderingType(PeriodeVurderingType.PERIODE_OK);
        return opphold;
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder()
                .medType(stønadskontotype)
                .medTrekkdager(trekkdager);
    }

    private Søknad.Builder søknad(UttakPeriode uttakPeriode, LocalDate mottattDato) {
        return new Søknad.Builder()
                .leggTilSøknadsperiode(uttakPeriode)
                .medType(Søknadstype.FØDSEL)
                .medMottattDato(mottattDato);
    }

    private UttakPeriode uttakPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        StønadsPeriode stønadsPeriode = new StønadsPeriode(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, null, false);
        stønadsPeriode.setPeriodeVurderingType(PeriodeVurderingType.PERIODE_OK);
        return stønadsPeriode;
    }

    private Behandling.Builder søkerErFarBehandling() {
        return new Behandling.Builder().medSøkerErMor(false);
    }

    private void assertInnvilget(Regelresultat regelresultat, Årsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private void assertManuell(Regelresultat regelresultat, Årsak årsak, Manuellbehandlingårsak manuellbehandlingårsak, boolean trekkDager, boolean utbetal) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isEqualTo(utbetal);
        assertThat(regelresultat.trekkDagerFraSaldo()).isEqualTo(trekkDager);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(årsak);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(manuellbehandlingårsak);
    }

    private RegelGrunnlag.Builder basicGrunnlag(LocalDate familiehendelseDato) {
        return RegelGrunnlagTestBuilder.create()
                .medDatoer(new Datoer.Builder()
                        .medFørsteLovligeUttaksdag(familiehendelseDato.minusWeeks(15))
                        .medFødsel(familiehendelseDato))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true)
                        .medFarHarRett(true)
                        .medMorHarRett(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private RegelGrunnlag.Builder basicGrunnlagMor(LocalDate familiehendelseDato) {
        return basicGrunnlag(familiehendelseDato)
                .medBehandling(new Behandling.Builder().medSøkerErMor(true));
    }
}
