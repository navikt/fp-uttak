package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.ManglendeSøktPeriodeUtil.lagManglendeSøktPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.ManglendeSøktPeriodeUtil.utledSenesteLovligeStartdatoVedAdopsjon;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.ManglendeSøktePerioderTjeneste.tomTidsperiodeForbeholdtMor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

final class MspBfhrUtil {

    private MspBfhrUtil() {
    }

    static List<OppgittPeriode> finnManglendeSøktPeriodeBareFarHarRett(RegelGrunnlag grunnlag) {
        if (grunnlag.getSøknad().gjelderAdopsjon()) {
            return forAdopsjon(grunnlag);
        }
        return forTerminFødsel(grunnlag);
    }

    private static List<OppgittPeriode> forAdopsjon(RegelGrunnlag grunnlag) {
        var senesteLovligeStartdatoVedAdopsjon = utledSenesteLovligeStartdatoVedAdopsjon(grunnlag);
        var førsteSøkteDag = grunnlag.getSøknad().getOppgittePerioder().get(0).getFom();
        return fraDato(grunnlag, senesteLovligeStartdatoVedAdopsjon, førsteSøkteDag);
    }

    private static List<OppgittPeriode> forTerminFødsel(RegelGrunnlag grunnlag) {
        var familiehendelse = grunnlag.getDatoer().getFamiliehendelse();
        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(familiehendelse);
        var bareFarSomHarRettMåHaStartdato = Virkedager.plusVirkedager(tomTidsperiodeForbeholdtMor, 1);
        return fraDato(grunnlag, bareFarSomHarRettMåHaStartdato, tomTidsperiodeForbeholdtMor);
    }

    private static List<OppgittPeriode> fraDato(RegelGrunnlag grunnlag,
                                                LocalDate påkrevdOppstartsdato,
                                                LocalDate ikkeLagHullFørDato) {
        List<OppgittPeriode> msp = new ArrayList<>();
        var søktePerioderSomSkalLageHull = grunnlag.getSøknad()
                .getOppgittePerioder()
                .stream()
                .filter(p -> p.getTom().isAfter(ikkeLagHullFørDato))
                .map(p -> new LukketPeriode(p.getFom(), p.getTom()))
                .toList();
        if (søktePerioderSomSkalLageHull.isEmpty()) {
            return List.of();
        }
        var førstePeriodeFom = søktePerioderSomSkalLageHull.get(0).getFom();
        if (førstePeriodeFom.isAfter(påkrevdOppstartsdato)) {
            var mspFraPåkrevdOppstart = lagManglendeSøktPeriode(påkrevdOppstartsdato, førstePeriodeFom.minusDays(1),
                    Stønadskontotype.FORELDREPENGER);
            msp.add(mspFraPåkrevdOppstart);
        }
        var manglendeMellomliggendePerioder = finnMellomliggende(søktePerioderSomSkalLageHull);
        msp.addAll(manglendeMellomliggendePerioder);
        return msp;
    }

    private static List<OppgittPeriode> finnMellomliggende(List<LukketPeriode> perioder) {
        var sortertePerioder = perioder.stream().sorted(Comparator.comparing(LukketPeriode::getFom)).toList();

        List<OppgittPeriode> mellomliggendePerioder = new ArrayList<>();
        LocalDate mspFom = null;
        for (var lukketPeriode : sortertePerioder) {
            if (mspFom == null) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            } else if (mspFom.isBefore(lukketPeriode.getFom())) {
                var mspTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(mspFom, mspTom) > 0) {
                    mellomliggendePerioder.add(lagManglendeSøktPeriode(mspFom, mspTom, Stønadskontotype.FORELDREPENGER));
                }
            }
            if (!lukketPeriode.getTom().isBefore(mspFom)) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            }
        }
        return mellomliggendePerioder;
    }
}
