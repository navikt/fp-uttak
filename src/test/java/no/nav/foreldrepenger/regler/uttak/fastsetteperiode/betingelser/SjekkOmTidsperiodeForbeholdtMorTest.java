package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet.INNLAGT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak.INNLAGT_SØKER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import org.junit.jupiter.api.Test;

class SjekkOmTidsperiodeForbeholdtMorTest {

    @Test
    void periodeFørTidsperiodenGirNei() {
        var familiehendelse = LocalDate.of(2021, 6, 17);
        var fom = LocalDate.of(2021, 5, 1);
        var tom = LocalDate.of(2021, 5, 25);
        var grunnlag = grunnlag(familiehendelse, fom, tom);
        var evaluation = evaluer(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    void periodeEtterTidsperiodenGirNei() {
        var familiehendelse = LocalDate.of(2021, 6, 17);
        var fom = LocalDate.of(2021, 7, 29);
        var tom = LocalDate.of(2021, 10, 1);
        var grunnlag = grunnlag(familiehendelse, fom, tom);
        var evaluation = evaluer(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    void periodeDelAvTidsperiodenFørFødselGirJa() {
        var familiehendelse = LocalDate.of(2021, 6, 17);
        var fom = LocalDate.of(2021, 6, 1);
        var tom = LocalDate.of(2021, 6, 16);
        var grunnlag = grunnlag(familiehendelse, fom, tom);
        var evaluation = evaluer(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void periodeDelAvTidsperiodenEtterFødselGirJa() {
        var familiehendelse = LocalDate.of(2021, 6, 17);
        var fom = LocalDate.of(2021, 6, 20);
        var tom = LocalDate.of(2021, 7, 1);
        var grunnlag = grunnlag(familiehendelse, fom, tom);
        var evaluation = evaluer(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void periodeLikTidsperiodenFørFødselGirJa() {
        var familiehendelse = LocalDate.of(2021, 6, 17);
        var fom = LocalDate.of(2021, 5, 26);
        var tom = LocalDate.of(2021, 6, 16);
        var grunnlag = grunnlag(familiehendelse, fom, tom);
        var evaluation = evaluer(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void periodeLikTidsperiodenEtterFødselGirJa() {
        var familiehendelse = LocalDate.of(2021, 6, 17);
        var fom = LocalDate.of(2021, 6, 17);
        var tom = LocalDate.of(2021, 7, 29);
        var grunnlag = grunnlag(familiehendelse, fom, tom);
        var evaluation = evaluer(grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    private Evaluation evaluer(FastsettePeriodeGrunnlagImpl grunnlag) {
        return sjekk().evaluate(grunnlag);
    }

    private SjekkOmTidsperiodeForbeholdtMor sjekk() {
        return new SjekkOmTidsperiodeForbeholdtMor();
    }

    private FastsettePeriodeGrunnlagImpl grunnlag(
            LocalDate familiehendelse, LocalDate periodeFom, LocalDate periodeTom) {
        var datoer = new Datoer.Builder().fødsel(familiehendelse);
        var regelGrunnlag = new RegelGrunnlag.Builder()
                .datoer(datoer)
                .søknad(new Søknad.Builder().type(FØDSEL))
                .build();
        var aktuellPeriode = OppgittPeriode.forUtsettelse(
                periodeFom,
                periodeTom,
                INNLAGT_SØKER,
                periodeFom,
                periodeFom,
                INNLAGT,
                DokumentasjonVurdering.INNLEGGELSE_SØKER_GODKJENT);
        return new FastsettePeriodeGrunnlagImpl(regelGrunnlag, null, null, aktuellPeriode);
    }
}
