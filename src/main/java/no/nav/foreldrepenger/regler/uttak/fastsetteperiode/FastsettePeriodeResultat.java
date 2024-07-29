package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

public record FastsettePeriodeResultat(UttakPeriode uttakPeriode, String evalueringResultat, String innsendtGrunnlag,
                                       OppgittPeriode periodeEtterKnekk, String versjon) {

    public FastsettePeriodeResultat {
        Objects.requireNonNull(uttakPeriode);
    }

    public FastsettePeriodeResultat(UttakPeriode uttakPeriode, String evalueringResultat, String innsendtGrunnlag, OppgittPeriode periodeEtterKnekk) {
        this(uttakPeriode, evalueringResultat, innsendtGrunnlag, periodeEtterKnekk, UttakVersion.UTTAK_VERSION.nameAndVersion());
    }

    public boolean isManuellBehandling() {
        return Perioderesultattype.MANUELL_BEHANDLING.equals(uttakPeriode().getPerioderesultattype());
    }

    boolean harFørtTilKnekk() {
        return periodeEtterKnekk != null;
    }

    @Override
    public String toString() {
        return "FastsettePeriodeResultat{" + "uttakPeriode=" + uttakPeriode + '}';
    }
}
