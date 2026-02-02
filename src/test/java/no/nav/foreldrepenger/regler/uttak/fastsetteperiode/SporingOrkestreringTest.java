package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.fpsak.nare.json.JsonOutput;

class SporingOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void fastsette_perioder_regel_skal_produsere_sporing_i_json_format() {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        var grunnlag = basicGrunnlagMor(fødselsdato)
            .søknad(søknad(Søknadstype.FØDSEL, oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))));

        var resultatListe = fastsettPerioder(grunnlag);

        assertThat(resultatListe).hasSize(3);
        for (var resultat : resultatListe) {
            assertThat(JsonOutput.fromJson(resultat.innsendtGrunnlag(), HashMap.class)).isNotNull().isNotEmpty();
            assertThat(JsonOutput.fromJson(resultat.evalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();
        }
    }
}
