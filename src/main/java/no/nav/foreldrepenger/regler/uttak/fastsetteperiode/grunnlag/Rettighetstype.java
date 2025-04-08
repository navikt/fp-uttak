package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Set;

public enum Rettighetstype {

    ALENEOMSORG,
    BEGGE_RETT,
    BARE_SØKER_RETT,
    BARE_FAR_RETT_MOR_UFØR;

    public boolean bareSøkerRett() {
        return Set.of(Rettighetstype.BARE_SØKER_RETT, Rettighetstype.BARE_FAR_RETT_MOR_UFØR).contains(this);
    }
}
