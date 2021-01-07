package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.lagManglendeSøktPeriode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class ManglendeSøktePerioderTjeneste {

    private ManglendeSøktePerioderTjeneste() {
        // Skal ikke instansieres
    }

    static List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        List<OppgittPeriode> manglendePerioderIUkerForbeholdtMor = finnManglendeSøktIUkerForbeholdtMor(grunnlag, konfigurasjon);
        List<OppgittPeriode> manglendePerioderIUkerFarMedAleneomsorg = finnManglendeSøktePerioderTilFarMedAleneomsorg(grunnlag);
        List<OppgittPeriode> manglendePerioder = new ArrayList<>();
        manglendePerioder.addAll(manglendePerioderIUkerFarMedAleneomsorg);
        manglendePerioder.addAll(manglendePerioderIUkerForbeholdtMor);

        List<OppgittPeriode> manglendeSøktePerioder = finnManglendeMellomliggendePerioder(
                grunnlag, manglendePerioder);
        manglendeSøktePerioder.addAll(manglendePerioderIUkerFarMedAleneomsorg);
        manglendeSøktePerioder.addAll(finnPerioderVedAdopsjon(grunnlag));
        manglendeSøktePerioder.addAll(manglendePerioderIUkerForbeholdtMor);

        var trimmedePerioder = trimPerioder(grunnlag, manglendeSøktePerioder);
        List<OppgittPeriode> samlet = new ArrayList<>();
        for (OppgittPeriode msp : trimmedePerioder) {
            if (!contains(samlet, msp.getFom(), msp.getTom())) {
                samlet.add(msp);
            }
        }

        return samlet;
    }

    private static List<OppgittPeriode> trimPerioder(RegelGrunnlag grunnlag, List<OppgittPeriode> manglendeSøktePerioder) {
        return manglendeSøktePerioder.stream()
                .map(ManglendeSøktePerioderTjeneste::fjernHelgFraBegynnelseOgSlutt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(p -> fjernPerioderFørSkjæringstidspunktOpptjening(p, grunnlag))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(p -> fjernPerioderFørEndringsdatoVedRevurdering(p, grunnlag))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
    }

    private static boolean contains(List<OppgittPeriode> list, LocalDate fom, LocalDate tom) {
        return list.stream().anyMatch(item -> item.getFom().isEqual(fom) && item.getTom().isEqual(tom));
    }

    private static List<OppgittPeriode> finnPerioderVedAdopsjon(RegelGrunnlag grunnlag) {
        if (Søknadstype.ADOPSJON.equals(grunnlag.getSøknad().getType())) {
            Optional<OppgittPeriode> mspAdopsjon = utledManglendeSøktVedAdopsjon(grunnlag);
            if (mspAdopsjon.isPresent()) {
                return List.of(mspAdopsjon.get());
            }
        }
        return Collections.emptyList();
    }

    private static List<OppgittPeriode> finnManglendeSøktIUkerForbeholdtMor(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        if (grunnlag.getBehandling().isSøkerMor() && grunnlag.getSøknad().getType().gjelderTerminFødsel()) {
            return utledManglendeForMorFraOppgittePerioder(grunnlag, konfigurasjon);
        }
        if (farSøkerFødselEllerTerminOgBareFarHarRett(grunnlag) && !grunnlag.getRettOgOmsorg().getAleneomsorg()) {
            Optional<OppgittPeriode> oppholdFar = utledManglendeSøktForFar(grunnlag.getDatoer().getFamiliehendelse(),
                    grunnlag.getSøknad().getOppgittePerioder(), konfigurasjon);
            if (oppholdFar.isPresent()) {
                return List.of(oppholdFar.get());
            }
        }
        return Collections.emptyList();
    }

    private static List<OppgittPeriode> finnManglendeSøktePerioderTilFarMedAleneomsorg(RegelGrunnlag grunnlag) {
        if (!grunnlag.getBehandling().isSøkerMor() && grunnlag.getRettOgOmsorg().getAleneomsorg()) {
            OppgittPeriode førstePeriode = grunnlag.getSøknad().getOppgittePerioder().get(0);
            if (grunnlag.getDatoer().getFamiliehendelse().isBefore(førstePeriode.getFom())) {
                OppgittPeriode nyPeriode = lagManglendeSøktPeriode(grunnlag.getDatoer().getFamiliehendelse(),
                        førstePeriode.getFom().minusDays(1), Stønadskontotype.FORELDREPENGER);
                return List.of(nyPeriode);
            }
        }
        return Collections.emptyList();
    }

    private static Optional<OppgittPeriode> utledManglendeSøktVedAdopsjon(RegelGrunnlag grunnlag) {
        LocalDate senesteLovligeStartdatoVedAdopsjon = utledSenesteLovligeStartdatoVedAdopsjon(grunnlag);

        var førsteUttaksdatoSøknad = førsteUttaksdatoSøknad(grunnlag);
        Optional<LocalDate> førsteUttaksdatoAnnenpart = førsteUttaksdatoAnnenpart(grunnlag);
        if (førsteUttaksdatoSøknad.isAfter(senesteLovligeStartdatoVedAdopsjon) && (førsteUttaksdatoAnnenpart.isEmpty()
                || førsteUttaksdatoAnnenpart.get().isAfter(senesteLovligeStartdatoVedAdopsjon))) {
            LocalDate tom;
            if (førsteUttaksdatoAnnenpart.isEmpty() || førsteUttaksdatoSøknad.isBefore(førsteUttaksdatoAnnenpart.get())) {
                tom = førsteUttaksdatoSøknad.minusDays(1);
            } else {
                tom = førsteUttaksdatoAnnenpart.get().minusDays(1);
            }
            return Optional.of(lagManglendeSøktPeriode(senesteLovligeStartdatoVedAdopsjon, tom));
        }
        return Optional.empty();
    }

    private static Optional<LocalDate> førsteUttaksdatoAnnenpart(RegelGrunnlag grunnlag) {
        if (grunnlag.getAnnenPart() == null || grunnlag.getAnnenPart().getUttaksperioder().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(førsteFom(grunnlag.getAnnenPart().getUttaksperioder()));
    }

    private static LocalDate førsteUttaksdatoSøknad(RegelGrunnlag grunnlag) {
        return førsteFom(grunnlag.getSøknad().getOppgittePerioder());
    }

    private static LocalDate førsteFom(List<? extends LukketPeriode> perioder) {
        return perioder.stream().min(Comparator.comparing(LukketPeriode::getFom)).orElseThrow().getFom();
    }

    private static LocalDate utledSenesteLovligeStartdatoVedAdopsjon(RegelGrunnlag grunnlag) {
        LocalDate omsorgsovertakelseDato = grunnlag.getDatoer().getFamiliehendelse();
        LocalDate ankomstNorgeDato = grunnlag.getAdopsjon().getAnkomstNorgeDato();

        if (ankomstNorgeDato != null) {
            return ankomstNorgeDato;
        }
        return omsorgsovertakelseDato;
    }

    private static Optional<OppgittPeriode> utledManglendeSøktForFar(LocalDate familiehendelse,
                                                                     List<OppgittPeriode> uttaksperioder,
                                                                     Konfigurasjon konfigurasjon) {
        var førstePeriode = uttaksperioder.get(0);
        var ukerForbeholdtMor = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelse);
        if (familiehendelse.plusWeeks(ukerForbeholdtMor).isBefore(førstePeriode.getFom())) {
            var nyPeriode = lagManglendeSøktPeriode(familiehendelse.plusWeeks(ukerForbeholdtMor), førstePeriode.getFom().minusDays(1),
                    Stønadskontotype.FORELDREPENGER);
            return Optional.of(nyPeriode);
        }
        return Optional.empty();
    }

    private static boolean farSøkerFødselEllerTermin(RegelGrunnlag grunnlag) {
        return !grunnlag.getBehandling().isSøkerMor() && erFødselEllerTermin(grunnlag);
    }

    private static boolean erFødselEllerTermin(RegelGrunnlag grunnlag) {
        return grunnlag.getSøknad().getType().gjelderTerminFødsel();
    }

    private static boolean bareFarRett(RegelGrunnlag grunnlag) {
        return grunnlag.getRettOgOmsorg().getFarHarRett() && !grunnlag.getRettOgOmsorg().getMorHarRett();
    }

    private static List<OppgittPeriode> utledManglendeForMorFraOppgittePerioder(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        LocalDate familiehendelseDato = grunnlag.getDatoer().getFamiliehendelse();
        List<LukketPeriode> uttakPerioder = slåSammenUttakForBeggeParter(grunnlag).stream()
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
        List<OppgittPeriode> mspFørFødsel = finnManglendeSøktFørFødsel(uttakPerioder, familiehendelseDato,
                grunnlag.getSøknad().getType(), grunnlag.getGyldigeStønadskontotyper(), konfigurasjon);
        List<OppgittPeriode> mspEtterFødsel = finnPerioderEtterFødsel(uttakPerioder, familiehendelseDato,
                grunnlag.getGyldigeStønadskontotyper(), konfigurasjon);

        return Stream.of(mspFørFødsel, mspEtterFødsel)
                .flatMap(Collection::stream)
                .map(ManglendeSøktePerioderTjeneste::fjernHelgFraBegynnelseOgSlutt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<OppgittPeriode> fjernPerioderFørEndringsdatoVedRevurdering(OppgittPeriode msp, RegelGrunnlag grunnlag) {
        if (!grunnlag.erRevurdering()) {
            return Optional.of(msp);
        }
        LocalDate endringsdato = grunnlag.getRevurdering().getEndringsdato();
        return fjernPerioderFørDato(msp, endringsdato);
    }

    private static Optional<OppgittPeriode> fjernPerioderFørSkjæringstidspunktOpptjening(OppgittPeriode msp, RegelGrunnlag grunnlag) {
        LocalDate skjæringstidspunkt = grunnlag.getOpptjening().getSkjæringstidspunkt();
        // Skal ikke fjerne periode før skjæringstidspunkt for far med aleneomsorg eller enerett (fødsel eller adopsjon)
        if ((farSøkerFødselEllerTerminOgBareFarHarRett(grunnlag)
                || (!grunnlag.getBehandling().isSøkerMor() && (grunnlag.getRettOgOmsorg().getAleneomsorg()))
                && skjæringstidspunkt.isAfter(grunnlag.getDatoer().getFamiliehendelse()))) {
            return Optional.of(msp);
        }
        if (mspFyllerHullMellomForeldrene(msp, grunnlag)) {
            return Optional.of(msp);
        }
        return fjernPerioderFørDato(msp, skjæringstidspunkt);
    }

    private static boolean mspFyllerHullMellomForeldrene(OppgittPeriode msp, RegelGrunnlag grunnlag) {
        if (grunnlag.getAnnenPart() == null) {
            return false;
        }
        var sisteDagAnnenpart = grunnlag.getAnnenPart().sisteUttaksdag();
        var førsteDagSøknad = grunnlag.getSøknad().getOppgittePerioder().get(0).getFom();
        if (sisteDagAnnenpart.isPresent() && sisteDagAnnenpart.get().isBefore(førsteDagSøknad)) {
            return msp.overlapper(new LukketPeriode(sisteDagAnnenpart.get(), førsteDagSøknad));
        }
        return false;
    }

    private static Optional<OppgittPeriode> fjernPerioderFørDato(OppgittPeriode msp, LocalDate dato) {
        if (msp.getTom().isBefore(dato)) {
            return Optional.empty();
        }
        if (msp.overlapper(dato)) {
            return Optional.of(msp.kopiMedNyPeriode(dato, msp.getTom()));
        }
        return Optional.of(msp);
    }

    private static boolean farSøkerFødselEllerTerminOgBareFarHarRett(RegelGrunnlag grunnlag) {
        return farSøkerFødselEllerTermin(grunnlag) && bareFarRett(grunnlag);
    }

    private static List<OppgittPeriode> finnManglendeMellomliggendePerioder(RegelGrunnlag grunnlag,
                                                                            List<OppgittPeriode> ekskludertePerioder) {
        List<LukketPeriode> allePerioder = slåSammenUttakForBeggeParter(grunnlag);
        //legge inn ikke søkte perioder til uker som er Forbeholdt til Mor etter fødsel
        allePerioder.addAll(ekskludertePerioder);
        return finnManglendeMellomliggendePerioder(allePerioder).stream()
                .map(ManglendeSøktePerioderTjeneste::fjernHelgFraBegynnelseOgSlutt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriode> finnManglendeMellomliggendePerioder(List<LukketPeriode> perioder) {
        List<LukketPeriode> sortertePerioder = perioder.stream()
                .sorted(Comparator.comparing(LukketPeriode::getFom))
                .collect(Collectors.toList());

        List<OppgittPeriode> mellomliggendePerioder = new ArrayList<>();
        LocalDate mspFom = null;
        for (LukketPeriode lukketPeriode : sortertePerioder) {
            if (mspFom == null) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            } else if (mspFom.isBefore(lukketPeriode.getFom())) {
                LocalDate mspTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(mspFom, mspTom) > 0) {
                    mellomliggendePerioder.add(lagManglendeSøktPeriode(mspFom, mspTom));
                }
            }
            if (!lukketPeriode.getTom().isBefore(mspFom)) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            }
        }
        return mellomliggendePerioder;
    }

    private static List<LukketPeriode> slåSammenUttakForBeggeParter(RegelGrunnlag grunnlag) {
        List<LukketPeriode> allePerioder = new ArrayList<>();
        if (grunnlag.getAnnenPart() != null) {
            allePerioder.addAll(grunnlag.getAnnenPart()
                    .getUttaksperioder()
                    .stream()
                    .filter(p -> p.isInnvilget() || p.harTrekkdager() || p.harUtbetaling())
                    .collect(Collectors.toList()));
        }
        allePerioder.addAll(grunnlag.getSøknad().getOppgittePerioder());
        return allePerioder;
    }


    private static List<OppgittPeriode> finnManglendeSøktFørFødsel(List<LukketPeriode> søktePerioder,
                                                                   LocalDate familiehendelseDato,
                                                                   Søknadstype søknadstype,
                                                                   Set<Stønadskontotype> gyldigeStønadskontotyper,
                                                                   Konfigurasjon konfigurasjon) {
        if (Søknadstype.ADOPSJON.equals(søknadstype)) {
            return List.of();
        }
        var antallUkerFpffFørFødsel = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER,
                familiehendelseDato);
        var sorterteSøktePerioder = søktePerioder.stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());

        var førsteUttaksdagSøknad = sorterteSøktePerioder.get(0).getFom();

        if (!førsteUttaksdagSøknad.isBefore(familiehendelseDato.minusWeeks(antallUkerFpffFørFødsel))) {
            return List.of();
        }
        var startdatoBegingetFpff = førsteUttaksdagSøknad.isBefore(
                familiehendelseDato.minusWeeks(antallUkerFpffFørFødsel)) ? familiehendelseDato.minusWeeks(
                antallUkerFpffFørFødsel) : førsteUttaksdagSøknad;
        var betingetFpffPeriode = new LukketPeriode(startdatoBegingetFpff, familiehendelseDato.minusDays(1));
        var mspFørFødsel = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(sorterteSøktePerioder, betingetFpffPeriode)
                .stream()
                .map(msp -> lagManglendeSøktPeriode(msp.getFom(), msp.getTom(), Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))
                .collect(Collectors.toList());
        var betingetFellesperiode = new LukketPeriode(førsteUttaksdagSøknad,
                familiehendelseDato.minusWeeks(antallUkerFpffFørFødsel).minusDays(1));
        var mspForFellesperiodeFørFødsel = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(sorterteSøktePerioder,
                betingetFellesperiode).stream().map(msp -> {
            Stønadskontotype type = gyldigeStønadskontotyper.contains(
                    Stønadskontotype.FELLESPERIODE) ? Stønadskontotype.FELLESPERIODE : Stønadskontotype.FORELDREPENGER;
            return lagManglendeSøktPeriode(msp.getFom(), msp.getTom(), type);
        }).collect(Collectors.toList());
        return Stream.of(mspForFellesperiodeFørFødsel, mspFørFødsel)
                .flatMap(Collection::stream)
                .filter(p -> p.virkedager() > 0)
                .collect(Collectors.toList());
    }


    private static List<OppgittPeriode> finnPerioderEtterFødsel(List<LukketPeriode> søktePerioder,
                                                                LocalDate familiehendelseDato,
                                                                Set<Stønadskontotype> gyldigeStønadskontotyper,
                                                                Konfigurasjon konfigurasjon) {
        int mødrekvoteEtterFødselUker = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER,
                familiehendelseDato);
        LukketPeriode betingetPeriodeEtterFødsel = new LukketPeriode(familiehendelseDato,
                familiehendelseDato.plusWeeks(mødrekvoteEtterFødselUker).minusDays(1));
        Stønadskontotype stønadskontotype = gyldigeStønadskontotyper.contains(
                Stønadskontotype.MØDREKVOTE) ? Stønadskontotype.MØDREKVOTE : Stønadskontotype.FORELDREPENGER;
        return ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(søktePerioder, betingetPeriodeEtterFødsel)
                .stream()
                .map(msp -> lagManglendeSøktPeriode(msp.getFom(), msp.getTom(), stønadskontotype))
                .collect(Collectors.toList());
    }


    /**
     * Fjern helgedager i begynnelse og slutt av msp.
     *
     * @param msp perioder som skal strippes.
     * @return periode uten helg i begynnelsen og slutten. Optional.empty() dersom perioden bare besto av helgedager.
     */
    private static Optional<OppgittPeriode> fjernHelgFraBegynnelseOgSlutt(OppgittPeriode msp) {
        Predicate<LocalDate> sjekkOmHelg = dato -> dato.getDayOfWeek().equals(DayOfWeek.SATURDAY) || dato.getDayOfWeek()
                .equals(DayOfWeek.SUNDAY);

        LocalDate fom = msp.getFom();
        LocalDate tom = msp.getTom();

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

}
