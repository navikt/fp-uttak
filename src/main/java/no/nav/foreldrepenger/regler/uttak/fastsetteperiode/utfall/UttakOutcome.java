package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.util.Optional;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;

public class UttakOutcome implements RuleReasonRef {

    private final UtfallType utfallType;
    private PeriodeResultatÅrsak periodeÅrsak;
    private GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak;
    private Manuellbehandlingårsak manuellbehandlingårsak;
    private boolean skalUtbetale;
    private boolean trekkDagerFraSaldo;

    private UttakOutcome(UtfallType utfallType) {
        this.utfallType = utfallType;
    }

    public static UttakOutcome oppfylt(InnvilgetÅrsak årsak) {
        return new UttakOutcome(UtfallType.INNVILGET).medPeriodeResultatÅrsak(årsak);
    }

    public static UttakOutcome manuell(
            PeriodeResultatÅrsak årsak, Manuellbehandlingårsak manuellårsak) {
        return new UttakOutcome(UtfallType.MANUELL_BEHANDLING)
                .medPeriodeResultatÅrsak(årsak)
                .medManuellBehandlingårsak(manuellårsak);
    }

    public static UttakOutcome ikkeOppfylt(IkkeOppfyltÅrsak årsak) {
        return new UttakOutcome(UtfallType.AVSLÅTT).medPeriodeResultatÅrsak(årsak);
    }

    public boolean skalUtbetale() {
        return skalUtbetale;
    }

    public boolean trekkDagerFraSaldo() {
        return trekkDagerFraSaldo;
    }

    public GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return graderingIkkeInnvilgetÅrsak;
    }

    public PeriodeResultatÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }

    public Manuellbehandlingårsak getManuellbehandlingårsak() {
        return manuellbehandlingårsak;
    }

    public UtfallType getUtfallType() {
        return utfallType;
    }

    public UttakOutcome medPeriodeResultatÅrsak(PeriodeResultatÅrsak årsak) {
        this.periodeÅrsak = årsak;
        return this;
    }

    public UttakOutcome medManuellBehandlingårsak(Manuellbehandlingårsak manuellbehandlingårsak) {
        this.manuellbehandlingårsak = manuellbehandlingårsak;
        return this;
    }

    public UttakOutcome medGraderingIkkeInnvilgetÅrsak(
            GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        this.graderingIkkeInnvilgetÅrsak = graderingIkkeInnvilgetÅrsak;
        return this;
    }

    public UttakOutcome medSkalUtbetale(boolean skalUtbetale) {
        this.skalUtbetale = skalUtbetale;
        return this;
    }

    public UttakOutcome medTrekkDagerFraSaldo(boolean trekkDagerFraSaldo) {
        this.trekkDagerFraSaldo = trekkDagerFraSaldo;
        return this;
    }

    @Override
    public String getReasonTextTemplate() {
        return Optional.ofNullable(periodeÅrsak)
                .map(PeriodeResultatÅrsak::getBeskrivelse)
                .orElse("");
    }

    @Override
    public String getReasonCode() {
        return Optional.ofNullable(periodeÅrsak)
                .map(PeriodeResultatÅrsak::getId)
                .map(String::valueOf)
                .orElse("");
    }
}
