package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Årsak;

public class Manuellbehandling {

    private Manuellbehandling() {
        // For å hindre instanser
    }

    public static FastsettePeriodeUtfall opprett(String id, Årsak årsak, Manuellbehandlingårsak manuellbehandlingårsak, boolean trekkDagerFraSaldo, boolean utbetal) {
        return opprett(id, årsak, manuellbehandlingårsak, trekkDagerFraSaldo, utbetal, Optional.empty());
    }

    public static FastsettePeriodeUtfall opprett(String id, Årsak årsak, Manuellbehandlingårsak manuellbehandlingårsak, boolean trekkDagerFraSaldo, boolean utbetal, Optional<GraderingIkkeInnvilgetÅrsak> graderingIkkeInnvilgetÅrsak) {
        FastsettePeriodeUtfall.Builder builder = FastsettePeriodeUtfall.builder()
                .manuellBehandling(årsak, manuellbehandlingårsak)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(trekkDagerFraSaldo)
                .medId(id);
        graderingIkkeInnvilgetÅrsak.ifPresent(builder::medAvslåttGradering);
        return builder.create();
    }

}
