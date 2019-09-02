package no.nav.foreldrepenger.uttaksvilkår;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

final class FinnOppholdUtil {
    private FinnOppholdUtil() {

    }

    static List<OppholdPeriode> finnOppholdIPeriode(List<LukketPeriode> perioder, LukketPeriode periode) {
        Objects.requireNonNull(periode, "periode");
        List<LukketPeriode> sortertePerioder = perioder.stream()
                .filter(p -> !p.getTom().isBefore(periode.getFom()) && !p.getFom().isAfter(periode.getTom()))
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());

        List<OppholdPeriode> oppholdPerioder = new ArrayList<>();
        LocalDate hullFom = periode.getFom();
        for (LukketPeriode lukketPeriode : sortertePerioder) {
            if (hullFom.isBefore(lukketPeriode.getFom())) {
                LocalDate hullTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                    oppholdPerioder.add(lagOppholdPeriode(hullFom, hullTom));
                }
            }
            LocalDate nesteDatoFom = lukketPeriode.getTom().plusDays(1);
            if (nesteDatoFom.isAfter(hullFom)) {
                hullFom = nesteDatoFom;
            }
        }
        if (!hullFom.isAfter(periode.getTom())) {
            LocalDate hullTom = periode.getTom();
            if (Virkedager.beregnAntallVirkedager(hullFom, hullTom) > 0) {
                oppholdPerioder.add(lagOppholdPeriode(hullFom, hullTom));
            }

        }
        return oppholdPerioder;
    }

    static OppholdPeriode lagOppholdPeriode(LocalDate hullFom, LocalDate hullTom) {
        return lagOppholdPeriode(hullFom, hullTom, Stønadskontotype.UKJENT);
    }

    static OppholdPeriode lagOppholdPeriode(LocalDate hullFom, LocalDate hullTom, Stønadskontotype type) {
        return new OppholdPeriode(type, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, hullFom, hullTom,
                null, false);
    }

}
