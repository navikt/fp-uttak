package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;

public class OrkestreringTillegg {

    private final List<OppgittPeriode> manglendeSøktPerioder;
    private final Set<LocalDate> knekkpunkter;

    public OrkestreringTillegg(List<OppgittPeriode> manglendeSøktPerioder, Set<LocalDate> knekkpunkter) {
        this.manglendeSøktPerioder = manglendeSøktPerioder;
        this.knekkpunkter = knekkpunkter;
    }

    public List<OppgittPeriode> getManglendeSøktPerioder() {
        return manglendeSøktPerioder;
    }

    public Set<LocalDate> getKnekkpunkter() {
        return knekkpunkter;
    }

    @Override
    public String toString() {
        var msp = manglendeSøktPerioder.stream()
                .map(p -> p.getFom() + " - " + p.getTom())
                .toList();
        return "OrkestreringTillegg{" + "manglendeSøktPerioder=" + msp + ", knekkpunkter=" + knekkpunkter + '}';
    }
}
