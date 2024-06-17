package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.ARBEID;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;

public class DelvisArbeidOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {
    @Test
    void far_søker_foreldrepenger_bare_far_rett_med_aktivitetskrav_og_mor_arbeider_under_75_prosent() {
        var fødselsdato = LocalDate.of(2024, 6, 17);
        var kontoer = new Kontoer.Builder()
            .konto(FORELDREPENGER, 100);
        var oppgittPeriode = OppgittPeriode.forVanligPeriode(FORELDREPENGER, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(12).minusDays(1), null,
            false, fødselsdato, fødselsdato, ARBEID, new MorsStillingsprosent(BigDecimal.valueOf(40)),
            DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT);
        var søknad = new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(oppgittPeriode);

        var grunnlag = new RegelGrunnlag.Builder().behandling(farBehandling())
            .opptjening(new Opptjening.Builder().skjæringstidspunkt(fødselsdato))
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .rettOgOmsorg(bareFarRett())
            .søknad(søknad)
            .inngangsvilkår(oppfyltAlleVilkår())
            .arbeid(new Arbeid.Builder().arbeidsforhold(new Arbeidsforhold(ARBEIDSFORHOLD)))
            .kontoer(kontoer);
        var fastsattePerioder = fastsettPerioder(grunnlag);
        assertThat(fastsattePerioder).hasSize(1);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(fastsattePerioder.get(0).uttakPeriode().getUtbetalingsgrad(ARBEIDSFORHOLD)).isEqualTo(new Utbetalingsgrad(40));
        assertThat(fastsattePerioder.get(0).uttakPeriode().getTrekkdager(ARBEIDSFORHOLD)).isEqualByComparingTo(new Trekkdager(30));
    }
}
