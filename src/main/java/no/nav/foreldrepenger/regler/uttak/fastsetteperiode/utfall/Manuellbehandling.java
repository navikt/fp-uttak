package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.util.Optional;

public class Manuellbehandling {

    private Manuellbehandling() {
        // For å hindre instanser
    }

    public static FastsettePeriodeUtfall opprett(
            String id,
            PeriodeResultatÅrsak periodeResultatÅrsak,
            Manuellbehandlingårsak manuellbehandlingårsak,
            boolean trekkDagerFraSaldo,
            boolean utbetal) {
        return opprett(id, periodeResultatÅrsak, manuellbehandlingårsak, trekkDagerFraSaldo, utbetal, Optional.empty());
    }

    public static FastsettePeriodeUtfall opprett(
            String id,
            PeriodeResultatÅrsak periodeResultatÅrsak,
            Manuellbehandlingårsak manuellbehandlingårsak,
            boolean trekkDagerFraSaldo,
            boolean utbetal,
            Optional<GraderingIkkeInnvilgetÅrsak> graderingIkkeInnvilgetÅrsak) {
        var builder = FastsettePeriodeUtfall.builder()
                .manuellBehandling(periodeResultatÅrsak, manuellbehandlingårsak)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(trekkDagerFraSaldo)
                .medId(id);
        graderingIkkeInnvilgetÅrsak.ifPresent(builder::medAvslåttGradering);
        return builder.create();
    }
}
