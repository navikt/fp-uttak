package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class SøknadMottattDatoRegelTest {

    private static final LocalDate FØRSTE_LOVLIGE_UTTAKSDAG = LocalDate.of(2018, 5, 5);
    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);
    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void søknadMottattdatoFørGradertPeriodeBlirSendtManuellBehandling() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        UttakPeriode søknadsperiode = gradertSøknadsperiode(søknadMottattdato.minusWeeks(1), søknadMottattdato);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        Regelresultat regelresultat = kjørRegler(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void søknadMottattdatoEtterGradertPeriodeBlirInnvilget() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        UttakPeriode søknadsperiode = gradertSøknadsperiode(søknadMottattdato.plusDays(1), søknadMottattdato.plusWeeks(1));
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        Regelresultat regelresultat = kjørRegler(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void søknadMottattdatoFørUtsettelseFeriePeriodeBlirAvslått() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        UttakPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.minusWeeks(1), søknadMottattdato, Utsettelseårsaktype.FERIE);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        Regelresultat regelresultat = kjørRegler(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void søknadMottattdatoEtterUtsettelseFeriePeriodeBlirInnvilget() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        UttakPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.plusDays(1), søknadMottattdato.plusWeeks(1), Utsettelseårsaktype.FERIE);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        Regelresultat regelresultat = kjørRegler(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void søknadMottattdatoFørUtsettelseArbeidPeriodeBlirAvslått() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        UttakPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.minusWeeks(1), søknadMottattdato, Utsettelseårsaktype.ARBEID);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        Regelresultat regelresultat = kjørRegler(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void søknadMottattdatoEtterUtsettelseArbeidPeriodeBlirInnvilget() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        UttakPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.plusDays(1), søknadMottattdato.plusWeeks(1), Utsettelseårsaktype.ARBEID);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        Regelresultat regelresultat = kjørRegler(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
    }

    private Søknad.Builder søknad(UttakPeriode søknadsperiode, LocalDate søknadMottattdato) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .leggTilSøknadsperiode(søknadsperiode)
                .medMottattDato(søknadMottattdato);
    }

    private Regelresultat kjørRegler(UttakPeriode søknadsperiode, RegelGrunnlag grunnlag) {
        return new Regelresultat(regel.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag,
                Trekkdagertilstand.ny(grunnlag, Collections.singletonList(søknadsperiode)), søknadsperiode)));
    }

    private UttakPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, Utsettelseårsaktype utsettelseårsaktype) {
        return new UtsettelsePeriode(PeriodeKilde.SØKNAD, fom, tom, utsettelseårsaktype, PeriodeVurderingType.PERIODE_OK);
    }

    private RegelGrunnlag.Builder basicBuilder() {
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        return new RegelGrunnlag.Builder()
                .leggTilKontoer(aktivitetIdentifikator, new Kontoer.Builder()
                        .leggTilKonto(new Konto.Builder()
                                .medType(Stønadskontotype.MØDREKVOTE)
                                .medTrekkdager(50)))
                .medArbeid(new ArbeidGrunnlag.Builder()
                        .medArbeidsprosenter(new Arbeidsprosenter().leggTil(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())))
                .medDatoer(new Datoer.Builder()
                        .medFødsel(FAMILIEHENDELSE_DATO)
                        .medFørsteLovligeUttaksdag(FØRSTE_LOVLIGE_UTTAKSDAG))
                .medRettOgOmsorg(new RettOgOmsorg.Builder()
                        .medSamtykke(true))
                .medBehandling(new Behandling.Builder()
                        .medSøkerErMor(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private UttakPeriode gradertSøknadsperiode(LocalDate fom, LocalDate tom) {
        return StønadsPeriode.medGradering(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fom, tom,
                Collections.singletonList(AktivitetIdentifikator.forSelvstendigNæringsdrivende()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK);
    }
}
