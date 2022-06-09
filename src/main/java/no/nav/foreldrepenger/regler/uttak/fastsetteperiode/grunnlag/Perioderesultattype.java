package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;

public enum Perioderesultattype {
    INNVILGET,
    AVSLÅTT,
    MANUELL_BEHANDLING;

    public static Perioderesultattype fra(UtfallType utfallType) {
        return switch (utfallType) {
            case INNVILGET -> INNVILGET;
            case AVSLÅTT -> AVSLÅTT;
            case MANUELL_BEHANDLING -> MANUELL_BEHANDLING;
        };
    }
}
