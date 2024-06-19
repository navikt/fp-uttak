package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

public class Oppfylt {

    private Oppfylt() {
        // For å hindre instanser
    }

    /**
     * Opprette endenode for oppfylt periode.
     *
     * @param id sluttnode id.
     * @param innvilgetÅrsak innvilget årsak
     * @param utbetal skal det utbetales for denne perioden.
     * @return periode utfall.
     */
    public static FastsettePeriodeUtfall opprett(String id, InnvilgetÅrsak innvilgetÅrsak, boolean utbetal) {
        return opprett(id, innvilgetÅrsak, true, utbetal);
    }

    /**
     * Opprette endenode for oppfylt periode.
     *
     * @param id sluttnode id.
     * @param innvilgetÅrsak innvilget årsak
     * @param trekkDager skal det trekkes dager for denne perioder.
     * @param utbetal skal det utbetales for denne perioden.
     * @return periode utfall.
     */
    public static FastsettePeriodeUtfall opprett(
            String id, InnvilgetÅrsak innvilgetÅrsak, boolean trekkDager, boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .oppfylt(innvilgetÅrsak)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(trekkDager)
                .medId(id)
                .create();
    }

    public static FastsettePeriodeUtfall opprettMedAvslåttGradering(
            String id,
            InnvilgetÅrsak innvilgetÅrsak,
            GraderingIkkeInnvilgetÅrsak graderingAvslagÅrsak,
            boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .oppfylt(innvilgetÅrsak)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(true)
                .medAvslåttGradering(graderingAvslagÅrsak)
                .medId(id)
                .create();
    }

    /**
     * Opprette endenode for oppfylt oppholds periode
     *
     * @param id sluttnode id.
     * @return periode utfall.
     */
    public static FastsettePeriodeUtfall opprettForOppholds(String id, boolean trekkDagerFraSaldo, boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .oppfylt(null)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(trekkDagerFraSaldo)
                .medId(id)
                .create();
    }
}
