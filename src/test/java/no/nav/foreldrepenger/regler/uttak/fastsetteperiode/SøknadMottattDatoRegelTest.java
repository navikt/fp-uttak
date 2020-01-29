package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class SøknadMottattDatoRegelTest {

    private static final LocalDate FØRSTE_LOVLIGE_UTTAKSDAG = LocalDate.of(2018, 5, 5);
    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    public void søknadMottattdatoFørGradertPeriodeBlirSendtManuellBehandling() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = gradertoppgittPeriode(søknadMottattdato.minusWeeks(1), søknadMottattdato);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void søknadMottattdatoEtterGradertPeriodeBlirInnvilget() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = gradertoppgittPeriode(søknadMottattdato.plusDays(1), søknadMottattdato.plusWeeks(1));
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void søknadMottattdatoFørUtsettelseFeriePeriodeBlirAvslått() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.minusWeeks(1), søknadMottattdato, UtsettelseÅrsak.FERIE);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void søknadMottattdatoEtterUtsettelseFeriePeriodeBlirInnvilget() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.plusDays(1), søknadMottattdato.plusWeeks(1), UtsettelseÅrsak.FERIE);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void søknadMottattdatoFørUtsettelseArbeidPeriodeBlirAvslått() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.minusWeeks(1), søknadMottattdato, UtsettelseÅrsak.ARBEID);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void søknadMottattdatoEtterUtsettelseArbeidPeriodeBlirInnvilget() {
        LocalDate søknadMottattdato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(søknadMottattdato.plusDays(1), søknadMottattdato.plusWeeks(1), UtsettelseÅrsak.ARBEID);
        RegelGrunnlag grunnlag = basicBuilder()
                .medSøknad(søknad(søknadsperiode, søknadMottattdato))
                .build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
    }

    private Søknad.Builder søknad(OppgittPeriode søknadsperiode, LocalDate søknadMottattdato) {
        return new Søknad.Builder()
                .medType(Søknadstype.FØDSEL)
                .leggTilOppgittPeriode(søknadsperiode)
                .medMottattDato(søknadMottattdato);
    }

    private OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeKilde.SØKNAD, PeriodeVurderingType.PERIODE_OK, utsettelseÅrsak);
    }

    private RegelGrunnlag.Builder basicBuilder() {
        var aktivitetIdentifikator = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var konto = new Konto.Builder()
                .medType(Stønadskontotype.MØDREKVOTE)
                .medTrekkdager(50);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto);
        return new RegelGrunnlag.Builder()
                .medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
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

    private OppgittPeriode gradertoppgittPeriode(LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, fom, tom, PeriodeKilde.SØKNAD, BigDecimal.TEN,
                null, false, Set.of(AktivitetIdentifikator.forSelvstendigNæringsdrivende()), PeriodeVurderingType.IKKE_VURDERT);
    }
}
