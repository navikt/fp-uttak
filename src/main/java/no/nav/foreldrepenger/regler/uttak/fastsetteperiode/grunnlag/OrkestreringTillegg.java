package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class OrkestreringTillegg {

    private final List<OppholdPeriode> oppholdsperioder;
    private final Set<LocalDate> knekkpunkter;

    public OrkestreringTillegg(List<OppholdPeriode> oppholdsperioder, Set<LocalDate> knekkpunkter) {
        this.oppholdsperioder = oppholdsperioder;
        this.knekkpunkter = knekkpunkter;
    }

    public List<OppholdPeriode> getOppholdsperioder() {
        return oppholdsperioder;
    }

    public Set<LocalDate> getKnekkpunkter() {
        return knekkpunkter;
    }
}
