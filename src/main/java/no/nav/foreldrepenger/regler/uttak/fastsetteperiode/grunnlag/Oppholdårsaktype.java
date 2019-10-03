package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public enum Oppholdårsaktype {
    //TODO MANGLENDE_SØKT_PERIODE hører ikke hjemme. Er et annen type "opphold"
    MANGLENDE_SØKT_PERIODE,
    KVOTE_FELLESPERIODE_ANNEN_FORELDER,
    KVOTE_ANNEN_FORELDER,
    ;

    public static Stønadskontotype map(Oppholdårsaktype oppholdårsaktype, boolean erMor) {
        if (KVOTE_FELLESPERIODE_ANNEN_FORELDER.equals(oppholdårsaktype)) {
            return Stønadskontotype.FELLESPERIODE;
        }
        if (KVOTE_ANNEN_FORELDER.equals(oppholdårsaktype)) {
            return erMor ? Stønadskontotype.FEDREKVOTE : Stønadskontotype.MØDREKVOTE;
        }
        throw new IllegalArgumentException("Ukjent oppholdsårsaktype " + oppholdårsaktype);
    }
}
