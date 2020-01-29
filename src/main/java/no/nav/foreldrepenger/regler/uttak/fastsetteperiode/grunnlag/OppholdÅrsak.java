package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public enum OppholdÅrsak {

    FELLESPERIODE_ANNEN_FORELDER,
    MØDREKVOTE_ANNEN_FORELDER,
    FEDREKVOTE_ANNEN_FORELDER,
    FORELDREPENGER_ANNEN_FORELDER
    ;

    public static Stønadskontotype map(OppholdÅrsak oppholdÅrsak) {
        if (FELLESPERIODE_ANNEN_FORELDER.equals(oppholdÅrsak)) {
            return Stønadskontotype.FELLESPERIODE;
        }
        if (FEDREKVOTE_ANNEN_FORELDER.equals(oppholdÅrsak)) {
            return Stønadskontotype.FEDREKVOTE;
        }
        if (MØDREKVOTE_ANNEN_FORELDER.equals(oppholdÅrsak)) {
            return Stønadskontotype.MØDREKVOTE;
        }
        if (FORELDREPENGER_ANNEN_FORELDER.equals(oppholdÅrsak)) {
            return Stønadskontotype.FORELDREPENGER;
        }
        throw new IllegalArgumentException("Ukjent oppholdsårsaktype " + oppholdÅrsak);
    }
}
