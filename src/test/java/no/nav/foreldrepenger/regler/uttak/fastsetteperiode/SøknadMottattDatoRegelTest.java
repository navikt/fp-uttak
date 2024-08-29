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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;

class SøknadMottattDatoRegelTest {

    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);

    @Test
    void mottattDatoFørSluttAvGraderingBlirInnvilget() {
        var mottattDato = FAMILIEHENDELSE_DATO.plusWeeks(7);
        var søknadsperiode = gradertoppgittPeriode(mottattDato.minusWeeks(1), mottattDato, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isTrue();
        assertThat(regelresultat.trekkDagerFraSaldo()).isTrue();
    }

    @Test
    void mottattDatoFørSluttAvFerieBlirInnvilget() {
        var mottattDato = FAMILIEHENDELSE_DATO.plusWeeks(7);
        var søknadsperiode = utsettelsePeriode(mottattDato.minusWeeks(1), mottattDato, UtsettelseÅrsak.FERIE, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void mottattDatoEtterSluttAvFerieBlirInnvilget() {
        var mottattDato = FAMILIEHENDELSE_DATO.plusWeeks(7);
        var søknadsperiode = utsettelsePeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), UtsettelseÅrsak.FERIE, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void mottattDatoFørSluttAvArbeidBlirInnvilget() {
        var mottattDato = FAMILIEHENDELSE_DATO.plusWeeks(7);
        var søknadsperiode = utsettelsePeriode(mottattDato.minusWeeks(1), mottattDato, UtsettelseÅrsak.ARBEID, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    @Test
    void mottattDatoEtterSluttAvArbeidBlirInnvilget() {
        var mottattDato = FAMILIEHENDELSE_DATO.plusWeeks(7);
        var søknadsperiode = utsettelsePeriode(mottattDato.plusDays(1), mottattDato.plusWeeks(1), UtsettelseÅrsak.ARBEID, mottattDato);
        var grunnlag = basicBuilder().søknad(søknad(søknadsperiode)).build();

        var regelresultat = kjørRegel(søknadsperiode, grunnlag);

        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.skalUtbetale()).isFalse();
        assertThat(regelresultat.trekkDagerFraSaldo()).isFalse();
    }

    private Søknad.Builder søknad(OppgittPeriode søknadsperiode) {
        return new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(søknadsperiode);
    }

    private OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelseÅrsak, LocalDate mottattDato) {
        return OppgittPeriode.forUtsettelse(fom, tom, utsettelseÅrsak, mottattDato, mottattDato, null, null);
    }

    private RegelGrunnlag.Builder basicBuilder() {
        var aktivitetIdentifikator = AktivitetIdentifikator.forArbeid(new Orgnummer("123"), null);
        var konto = new Konto.Builder().type(Stønadskontotype.MØDREKVOTE).trekkdager(50);
        var kontoer = new Kontoer.Builder().konto(konto);
        return new RegelGrunnlag.Builder().kontoer(kontoer)
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(aktivitetIdentifikator)))
            .datoer(new Datoer.Builder().fødsel(FAMILIEHENDELSE_DATO))
            .rettOgOmsorg(new RettOgOmsorg.Builder().samtykke(true))
            .behandling(new Behandling.Builder().søkerErMor(true).sammenhengendeUttakTomDato(LocalDate.of(9999, 1, 1)))
            .inngangsvilkår(
                new Inngangsvilkår.Builder().adopsjonOppfylt(true).foreldreansvarnOppfylt(true).fødselOppfylt(true).opptjeningOppfylt(true));
    }

    private OppgittPeriode gradertoppgittPeriode(LocalDate fom, LocalDate tom, LocalDate mottattDato) {
        return OppgittPeriode.forGradering(Stønadskontotype.MØDREKVOTE, fom, tom, BigDecimal.TEN, null, false,
            Set.of(AktivitetIdentifikator.forSelvstendigNæringsdrivende()), mottattDato, mottattDato, null, null, null);
    }
}
