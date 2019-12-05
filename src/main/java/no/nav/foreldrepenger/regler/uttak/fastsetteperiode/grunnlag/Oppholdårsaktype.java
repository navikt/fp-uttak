package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public enum Oppholdårsaktype {

    FELLESPERIODE_ANNEN_FORELDER,
    MØDREKVOTE_ANNEN_FORELDER,
    FEDREKVOTE_ANNEN_FORELDER,
    FORELDREPENGER_ANNEN_FORELDER
    ;

    public static Stønadskontotype map(Oppholdårsaktype oppholdårsaktype) {
        if (FELLESPERIODE_ANNEN_FORELDER.equals(oppholdårsaktype)) {
            return Stønadskontotype.FELLESPERIODE;
        }
        if (FEDREKVOTE_ANNEN_FORELDER.equals(oppholdårsaktype)) {
            return Stønadskontotype.FEDREKVOTE;
        }
        if (MØDREKVOTE_ANNEN_FORELDER.equals(oppholdårsaktype)) {
            return Stønadskontotype.MØDREKVOTE;
        }
        if (FORELDREPENGER_ANNEN_FORELDER.equals(oppholdårsaktype)) {
            return Stønadskontotype.FORELDREPENGER;
        }
        throw new IllegalArgumentException("Ukjent oppholdsårsaktype " + oppholdårsaktype);
    }
}
