package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class OrkestreringTillegg {

    private final List<ManglendeSøktPeriode> manglendeSøktPerioder;
    private final Set<LocalDate> knekkpunkter;

    public OrkestreringTillegg(List<ManglendeSøktPeriode> manglendeSøktPerioder, Set<LocalDate> knekkpunkter) {
        this.manglendeSøktPerioder = manglendeSøktPerioder;
        this.knekkpunkter = knekkpunkter;
    }

    public List<ManglendeSøktPeriode> getManglendeSøktPerioder() {
        return manglendeSøktPerioder;
    }

    public Set<LocalDate> getKnekkpunkter() {
        return knekkpunkter;
    }
}
