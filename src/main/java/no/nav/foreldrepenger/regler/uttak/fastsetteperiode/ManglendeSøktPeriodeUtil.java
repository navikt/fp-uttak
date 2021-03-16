package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

final class ManglendeSøktPeriodeUtil {
    private ManglendeSøktPeriodeUtil() {

    }

    static List<OppgittPeriode> finnManglendeSøktePerioder(List<LukketPeriode> perioder, LukketPeriode periode) {
        Objects.requireNonNull(periode, "periode");
        var sortertePerioder = perioder.stream()
                .filter(p -> !p.getTom().isBefore(periode.getFom()) && !p.getFom().isAfter(periode.getTom()))
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());

        List<OppgittPeriode> msp = new ArrayList<>();
        var hullFom = periode.getFom();
        for (var lukketPeriode : sortertePerioder) {
            if (hullFom.isBefore(lukketPeriode.getFom())) {
                var hullTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                    msp.add(lagManglendeSøktPeriode(hullFom, hullTom));
                }
            }
            var nesteDatoFom = lukketPeriode.getTom().plusDays(1);
            if (nesteDatoFom.isAfter(hullFom)) {
                hullFom = nesteDatoFom;
            }
        }
        if (!hullFom.isAfter(periode.getTom())) {
            var hullTom = periode.getTom();
            if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                msp.add(lagManglendeSøktPeriode(hullFom, hullTom));
            }

        }
        return msp;
    }

    static OppgittPeriode lagManglendeSøktPeriode(LocalDate hullFom, LocalDate hullTom) {
        return lagManglendeSøktPeriode(hullFom, hullTom, null);
    }

    static OppgittPeriode lagManglendeSøktPeriode(LocalDate hullFom, LocalDate hullTom, Stønadskontotype type) {
        return OppgittPeriode.forManglendeSøkt(type, hullFom, hullTom);
    }

}
