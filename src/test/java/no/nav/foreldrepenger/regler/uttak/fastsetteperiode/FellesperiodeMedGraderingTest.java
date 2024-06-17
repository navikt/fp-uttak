package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.DelRegelTestUtil.kjørRegel;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;

class FellesperiodeMedGraderingTest {

    private final LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    @Test
    void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_5_uker_igjen_på_saldo() {
        var graderingFom = fødselsdato.plusWeeks(10);
        var graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        var aktuellPeriode = OppgittPeriode.forGradering(Stønadskontotype.FELLESPERIODE, graderingFom, graderingTom, BigDecimal.valueOf(50), null,
            false, Set.of(ARBEIDSFORHOLD_1), null, null, null, null);
        var kontoer = new Kontoer.Builder().konto(konto(Stønadskontotype.FELLESPERIODE, 5 * 5));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var grunnlag = basicGrunnlag().kontoer(kontoer)
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(aktuellPeriode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold))
            .build();

        var regelresultat = kjørRegel(aktuellPeriode, grunnlag);

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }

    @Test
    void mor_graderer_med_50_prosent_arbeid_i_10_uker_med_4_uker_igjen_på_saldo() {
        var graderingFom = fødselsdato.plusWeeks(10);
        var graderingTom = fødselsdato.plusWeeks(20).minusDays(1);
        var aktuellPeriode = OppgittPeriode.forGradering(Stønadskontotype.FELLESPERIODE, graderingFom, graderingTom, BigDecimal.valueOf(50), null,
            false, Set.of(ARBEIDSFORHOLD_1), null, null, null, null);
        var kontoer = new Kontoer.Builder().konto(konto(Stønadskontotype.FELLESPERIODE, 4 * 5));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        var grunnlag = basicGrunnlag().kontoer(kontoer)
            .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(aktuellPeriode))
            .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold))
            .build();

        var regelresultat = kjørRegel(aktuellPeriode, grunnlag);

        assertThat(regelresultat.getUtfallType()).isEqualTo(UtfallType.INNVILGET);
    }

    private Konto.Builder konto(Stønadskontotype stønadskontotype, int trekkdager) {
        return new Konto.Builder().type(stønadskontotype).trekkdager(trekkdager);
    }

    private RegelGrunnlag.Builder basicGrunnlag() {
        return RegelGrunnlagTestBuilder.create()
            .datoer(new Datoer.Builder().fødsel(fødselsdato))
            .behandling(new Behandling.Builder().søkerErMor(true))
            .rettOgOmsorg(new RettOgOmsorg.Builder().farHarRett(true).morHarRett(true).samtykke(true))
            .inngangsvilkår(
                new Inngangsvilkår.Builder().adopsjonOppfylt(true).foreldreansvarnOppfylt(true).fødselOppfylt(true).opptjeningOppfylt(true));
    }
}
