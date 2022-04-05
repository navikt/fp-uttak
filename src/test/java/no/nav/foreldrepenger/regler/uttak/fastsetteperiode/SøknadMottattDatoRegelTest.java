package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;

class SøknadMottattDatoRegelTest {

    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    void mottattDatoFørSluttAvGraderingBlirSendtManuellBehandling() {
        var mottattDato = LocalDate.of(2018, 10, 10);
        var søknadsperiode = gradertoppgittPeriode(mottattDato.minusWeeks(1), mottattDato, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void mottattDatoEtterSluttAvGraderingBlirInnvilget() {
        var mottattDato = LocalDate.of(2018, 10, 10);
        var søknadsperiode = gradertoppgittPeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    void mottattDatoFørSluttAvFerieBlirAvslått() {
        var mottattDato = LocalDate.of(2018, 10, 10);
        var søknadsperiode = utsettelsePeriode(mottattDato.minusWeeks(1), mottattDato, UtsettelseÅrsak.FERIE, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void mottattDatoEtterSluttAvFerieBlirInnvilget() {
        var mottattDato = LocalDate.of(2018, 10, 10);
        var søknadsperiode = utsettelsePeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), UtsettelseÅrsak.FERIE,
                mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    void mottattDatoFørSluttAvArbeidBlirAvslått() {
        var mottattDato = LocalDate.of(2018, 10, 10);
        var søknadsperiode = utsettelsePeriode(mottattDato.minusWeeks(1), mottattDato, UtsettelseÅrsak.ARBEID, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void mottattDatoEtterSluttAvArbeidBlirInnvilget() {
        var mottattDato = LocalDate.of(2018, 10, 10);
        var søknadsperiode = utsettelsePeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), UtsettelseÅrsak.ARBEID,
                mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
    }

    private Søknad.Builder søknad(OppgittPeriode søknadsperiode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(søknadsperiode);
    }

    private OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak, LocalDate mottattDato) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeVurderingType.PERIODE_OK, utsettelseÅrsak, mottattDato, mottattDato,
                null);
    }

    private RegelGrunnlag.Builder basicBuilder() {
        var aktivitetIdentifikator = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        var konto = new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(50);
        var kontoer = new Kontoer.Builder().konto(konto);
        return new RegelGrunnlag.Builder().kontoer(kontoer)
                .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
                .datoer(new Datoer.Builder().fødsel(FAMILIEHENDELSE_DATO))
                .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
                .behandling(new Behandling.Builder().søkerErMor(true).kreverSammenhengendeUttak(true))
                .inngangsvilkår(new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                        .foreldreansvarnOppfylt(true)
                        .fødselOppfylt(true)
                        .opptjeningOppfylt(true));
    }

    private OppgittPeriode gradertoppgittPeriode(LocalDate fom, LocalDate tom, LocalDate mottattDato) {
        return OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, fom, tom, BigDecimal.TEN, null, false,
                Set.of(AktivitetIdentifikator.forSelvstendigNæringsdrivende()), PeriodeVurderingType.IKKE_VURDERT, mottattDato, mottattDato,
                null);
    }
}
