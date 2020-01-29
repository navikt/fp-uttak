package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

public class FastsettePeriodeResultat {
    private final UttakPeriode uttakPeriode;
    private final String evalueringResultat;
    private final String innsendtGrunnlag;
    private final OppgittPeriode periodeEtterKnekk;

    public FastsettePeriodeResultat(UttakPeriode uttakPeriode,
                                    String evalueringResultat,
                                    String innsendtGrunnlag,
                                    OppgittPeriode periodeEtterKnekk) {
        this.periodeEtterKnekk = periodeEtterKnekk;
        Objects.requireNonNull(uttakPeriode);
        this.uttakPeriode = uttakPeriode;
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
    }

    public UttakPeriode getUttakPeriode() {
        return uttakPeriode;
    }

    public String getEvalueringResultat() {
        return evalueringResultat;
    }

    public String getInnsendtGrunnlag() {
        return innsendtGrunnlag;
    }

    public boolean isManuellBehandling() {
        return Perioderesultattype.MANUELL_BEHANDLING.equals(getUttakPeriode().getPerioderesultattype());
    }

    boolean harFørtTilKnekk() {
        return periodeEtterKnekk != null;
    }

    OppgittPeriode getPeriodeEtterKnekk() {
        return periodeEtterKnekk;
    }

    @Override
    public String toString() {
        return "FastsettePeriodeResultat{" +
            "uttakPeriode=" + uttakPeriode +
            '}';
    }
}
