package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.fpsak.nare.evaluation.Evaluation;

class FastsettePerioderRegelresultat extends Regelresultat {

    private final boolean skalUtbetale;
    private final boolean trekkDagerFraSaldo;
    private final GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak;
    private final PeriodeResultatÅrsak avklaringÅrsak;
    private final Manuellbehandlingårsak manuellbehandlingårsak;
    private final UtfallType utfallType;

    FastsettePerioderRegelresultat(Evaluation evaluation) {
        super(evaluation);
        skalUtbetale = getProperty(FastsettePeriodePropertyType.UTBETAL, Boolean.class) != null && getProperty(
                FastsettePeriodePropertyType.UTBETAL, Boolean.class);
        trekkDagerFraSaldo = getProperty(FastsettePeriodePropertyType.TREKK_DAGER_FRA_SALDO, Boolean.class) != null && getProperty(
                FastsettePeriodePropertyType.TREKK_DAGER_FRA_SALDO, Boolean.class);
        graderingIkkeInnvilgetÅrsak = getProperty(FastsettePeriodePropertyType.GRADERING_IKKE_OPPFYLT_ÅRSAK,
                GraderingIkkeInnvilgetÅrsak.class);
        avklaringÅrsak = getProperty(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, PeriodeResultatÅrsak.class);
        manuellbehandlingårsak = getProperty(FastsettePeriodePropertyType.MANUELL_BEHANDLING_ÅRSAK, Manuellbehandlingårsak.class);
        utfallType = getProperty(FastsettePeriodePropertyType.UTFALL, UtfallType.class);
    }

    public FastsettePerioderRegelresultat(Evaluation evaluation,
                                          boolean skalUtbetale,
                                          boolean trekkDagerFraSaldo,
                                          GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                                          PeriodeResultatÅrsak avklaringÅrsak,
                                          Manuellbehandlingårsak manuellbehandlingårsak,
                                          UtfallType utfallType) {
        super(evaluation);
        this.skalUtbetale = skalUtbetale;
        this.trekkDagerFraSaldo = trekkDagerFraSaldo;
        this.graderingIkkeInnvilgetÅrsak = graderingIkkeInnvilgetÅrsak;
        this.avklaringÅrsak = avklaringÅrsak;
        this.manuellbehandlingårsak = manuellbehandlingårsak;
        this.utfallType = utfallType;
    }

    UtfallType getUtfallType() {
        return utfallType;
    }

    Manuellbehandlingårsak getManuellbehandlingårsak() {
        return manuellbehandlingårsak;
    }

    PeriodeResultatÅrsak getAvklaringÅrsak() {
        return avklaringÅrsak;
    }

    GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return graderingIkkeInnvilgetÅrsak;
    }

    boolean trekkDagerFraSaldo() {
        return trekkDagerFraSaldo;
    }

    boolean skalUtbetale() {
        return skalUtbetale;
    }
}
