package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Set;

public enum DokumentasjonVurdering {
    SYKDOM_SØKER_GODKJENT,
    SYKDOM_ANNEN_FORELDER_GODKJENT,
    INNLEGGELSE_SØKER_GODKJENT,
    INNLEGGELSE_ANNEN_FORELDER_GODKJENT,
    INNLEGGELSE_BARN_GODKJENT,
    HV_OVELSE_GODKJENT,
    NAV_TILTAK_GODKJENT,
    MORS_AKTIVITET_GODKJENT,
    MORS_AKTIVITET_IKKE_GODKJENT,
    MORS_AKTIVITET_IKKE_DOKUMENTERT,
    ER_ALENEOMSORG_GODKJENT,
    ER_BARE_SØKER_RETT_GODKJENT,
    TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT;

    public boolean erGyldigGrunnForTidligOppstart() {
        return Set.of(
                        SYKDOM_ANNEN_FORELDER_GODKJENT,
                        INNLEGGELSE_ANNEN_FORELDER_GODKJENT,
                        MORS_AKTIVITET_GODKJENT,
                        ER_ALENEOMSORG_GODKJENT,
                        ER_BARE_SØKER_RETT_GODKJENT,
                        TIDLIG_OPPSTART_FEDREKVOTE_GODKJENT)
                .contains(this);
    }
}
