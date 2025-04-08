package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

final class ManglendeSøktPeriodeUtil {
    private ManglendeSøktPeriodeUtil() {

    }

    static List<OppgittPeriode> finnManglendeSøktePerioder(List<LukketPeriode> perioder, LukketPeriode periode) {
        Objects.requireNonNull(periode, "periode");
        var sortertePerioder = perioder.stream()
            .filter(p -> !p.getTom().isBefore(periode.getFom()) && !p.getFom().isAfter(periode.getTom()))
            .sorted(Comparator.comparing(Periode::getFom))
            .toList();

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

    /**
     * Fjern helgedager i begynnelse og slutt av msp.
     *
     * @param msp perioder som skal strippes.
     * @return periode uten helg i begynnelsen og slutten. Optional.empty() dersom perioden bare besto av helgedager.
     */
    static Optional<OppgittPeriode> fjernHelg(OppgittPeriode msp) {
        Predicate<LocalDate> sjekkOmHelg = dato -> dato.getDayOfWeek().equals(DayOfWeek.SATURDAY) || dato.getDayOfWeek().equals(DayOfWeek.SUNDAY);

        var fom = msp.getFom();
        var tom = msp.getTom();

        while (sjekkOmHelg.test(fom)) {
            fom = fom.plusDays(1);
        }
        while (sjekkOmHelg.test(tom)) {
            tom = tom.minusDays(1);
        }

        if (fom.isAfter(tom)) {
            return Optional.empty();
        }
        return Optional.of(msp.kopiMedNyPeriode(fom, tom));
    }

    static OppgittPeriode lagManglendeSøktPeriode(LocalDate hullFom, LocalDate hullTom) {
        return lagManglendeSøktPeriode(hullFom, hullTom, null);
    }

    static OppgittPeriode lagManglendeSøktPeriode(LocalDate hullFom, LocalDate hullTom, Stønadskontotype type) {
        return OppgittPeriode.forManglendeSøkt(type, hullFom, hullTom);
    }

    static Optional<OppgittPeriode> fjernPerioderFørEndringsdatoVedRevurdering(OppgittPeriode msp, RegelGrunnlag grunnlag) {
        if (!grunnlag.erRevurdering()) {
            return Optional.of(msp);
        }
        var endringsdato = grunnlag.getRevurdering().getEndringsdato();
        return fjernPerioderFørDato(msp, endringsdato);
    }

    static Optional<OppgittPeriode> fjernPerioderFørDato(OppgittPeriode msp, LocalDate dato) {
        if (msp.getTom().isBefore(dato)) {
            return Optional.empty();
        }
        if (msp.overlapper(dato)) {
            return Optional.of(msp.kopiMedNyPeriode(dato, msp.getTom()));
        }
        return Optional.of(msp);
    }

    static boolean bareFarRett(RegelGrunnlag grunnlag) {
        return grunnlag.getBehandling().isSøkerFarMedMor() && grunnlag.getRettOgOmsorg().rettighetsType().bareSøkerRett();
    }

    static List<LukketPeriode> slåSammenUttakForBeggeParter(RegelGrunnlag grunnlag) {
        List<LukketPeriode> allePerioder = new ArrayList<>();
        if (grunnlag.getAnnenPart() != null) {
            allePerioder.addAll(
                grunnlag.getAnnenPart().getUttaksperioder().stream().filter(p -> p.isInnvilget() || p.harTrekkdager() || p.harUtbetaling()).toList());
        }
        allePerioder.addAll(grunnlag.getSøknad().getOppgittePerioder());
        return allePerioder;
    }

    static LocalDate utledSenesteLovligeStartdatoVedAdopsjon(RegelGrunnlag grunnlag) {
        var omsorgsovertakelseDato = grunnlag.getDatoer().getFamiliehendelse();
        var ankomstNorgeDato = grunnlag.getAdopsjon().getAnkomstNorgeDato();

        if (ankomstNorgeDato != null) {
            return ankomstNorgeDato;
        }
        return omsorgsovertakelseDato;
    }

}
