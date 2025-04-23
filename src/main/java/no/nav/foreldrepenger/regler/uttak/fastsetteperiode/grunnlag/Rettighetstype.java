package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Set;

public enum Rettighetstype {

    ALENEOMSORG,
    BEGGE_RETT,
    BARE_FAR_RETT,
    BARE_MOR_RETT,
    BARE_FAR_RETT_MOR_UFØR;

    public boolean bareFarRett() {
        return Set.of(BARE_FAR_RETT, BARE_FAR_RETT_MOR_UFØR).contains(this);
    }
}
