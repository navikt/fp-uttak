package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

public class RegelResultatBehandlerResultat {

    private final UttakPeriode periode;
    private final UttakPeriode etterKnekk;

    private RegelResultatBehandlerResultat(UttakPeriode periode, UttakPeriode etterKnekk) {
        this.periode = periode;
        this.etterKnekk = etterKnekk;
    }

    public static RegelResultatBehandlerResultat utenKnekk(UttakPeriode utenKnekk) {
        return new RegelResultatBehandlerResultat(utenKnekk, null);
    }

    public static RegelResultatBehandlerResultat medKnekk(UttakPeriode førKnekk, UttakPeriode etterKnekk) {
        return new RegelResultatBehandlerResultat(førKnekk, etterKnekk);
    }

    public UttakPeriode getEtterKnekkPeriode() {
        return etterKnekk;
    }

    public UttakPeriode getPeriode() {
        return periode;
    }
}
