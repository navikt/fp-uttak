package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Datoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;

class SporingOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void fastsette_perioder_regel_skal_produsere_sporing_i_json_format() throws JsonProcessingException {
        var fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medDatoer(new Datoer.Builder().medFødsel(fødselsdato))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medSamtykke(true))
                .medSøknad(søknad(Søknadstype.FØDSEL,
                        oppgittPeriode(FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1)),
                        oppgittPeriode(MØDREKVOTE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))));

        var resultatListe = fastsettPerioder(grunnlag);

        assertThat(resultatListe).hasSize(3);
        for (var resultat : resultatListe) {
            assertThat(new ObjectMapper().readValue(resultat.getInnsendtGrunnlag(), HashMap.class)).isNotNull().isNotEmpty();
            assertThat(new ObjectMapper().readValue(resultat.getEvalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();
        }
    }
}
