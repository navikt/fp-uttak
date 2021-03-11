package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class SøknadMottattDatoRegelTest {

    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    public void mottattDatoFørSluttAvGraderingBlirSendtManuellBehandling() {
        LocalDate mottattDato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = gradertoppgittPeriode(mottattDato.minusWeeks(1), mottattDato, mottattDato);
        RegelGrunnlag grunnlag = basicBuilder().medSøknad(søknad(søknadsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void mottattDatoEtterSluttAvGraderingBlirInnvilget() {
        LocalDate mottattDato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = gradertoppgittPeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), mottattDato);
        RegelGrunnlag grunnlag = basicBuilder().medSøknad(søknad(søknadsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void mottattDatoFørSluttAvFerieBlirAvslått() {
        LocalDate mottattDato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(mottattDato.minusWeeks(1), mottattDato, UtsettelseÅrsak.FERIE, mottattDato);
        RegelGrunnlag grunnlag = basicBuilder().medSøknad(søknad(søknadsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void mottattDatoEtterSluttAvFerieBlirInnvilget() {
        LocalDate mottattDato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), UtsettelseÅrsak.FERIE,
                mottattDato);
        RegelGrunnlag grunnlag = basicBuilder().medSøknad(søknad(søknadsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void mottattDatoFørSluttAvArbeidBlirAvslått() {
        LocalDate mottattDato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(mottattDato.minusWeeks(1), mottattDato, UtsettelseÅrsak.ARBEID, mottattDato);
        RegelGrunnlag grunnlag = basicBuilder().medSøknad(søknad(søknadsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void mottattDatoEtterSluttAvArbeidBlirInnvilget() {
        LocalDate mottattDato = LocalDate.of(2018, 10, 10);
        OppgittPeriode søknadsperiode = utsettelsePeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), UtsettelseÅrsak.ARBEID,
                mottattDato);
        RegelGrunnlag grunnlag = basicBuilder().medSøknad(søknad(søknadsperiode)).build();

        FastsettePerioderRegelresultat regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
    }

    private Søknad.Builder søknad(OppgittPeriode søknadsperiode) {
        return new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(søknadsperiode);
    }

    private OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak, LocalDate mottattDato) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.PERIODE_OK, utsettelseÅrsak, mottattDato, null);
    }

    private RegelGrunnlag.Builder basicBuilder() {
        var aktivitetIdentifikator = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var konto = new Konto.Builder().medType(Stønadskontotype.MØDREKVOTE).medTrekkdager(50);
        var kontoer = new Kontoer.Builder().leggTilKonto(konto);
        return new RegelGrunnlag.Builder().medKontoer(kontoer)
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .medDatoer(new Datoer.Builder().medFødsel(FAMILIEHENDELSE_DATO))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medInngangsvilkår(new Inngangsvilkår.Builder().medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

    private OppgittPeriode gradertoppgittPeriode(LocalDate fom, LocalDate tom, LocalDate mottattDato) {
        return OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, fom, tom, BigDecimal.TEN, null, false,
                Set.of(AktivitetIdentifikator.forSelvstendigNæringsdrivende()), PeriodeVurderingType.IKKE_VURDERT, mottattDato, null);
    }
}
