package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.uttaksvilkår.FinnOppholdUtil.finnOppholdIPeriode;
import static no.nav.foreldrepenger.uttaksvilkår.FinnOppholdUtil.lagOppholdPeriode;

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

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class OppholdPeriodeTjeneste {

    private OppholdPeriodeTjeneste() {
        // Skal ikke instansieres
    }

    static List<OppholdPeriode> finnOppholdsperioder(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        List<OppholdPeriode> ikkeSøktePerioderTilUkerForbeholdtMor = finnOppholdIUkerForbeholdtMor(grunnlag, konfigurasjon);
        List<OppholdPeriode> ikkeSøktePerioderTilFarMedEneomsorg = finnIkkeSøktePerioderTilFarMedAleneomsorg(grunnlag);
        List<OppholdPeriode> oppholdPerioder = finnOppholdISøktePerioderEtterUkerForbeholdtMorOgFastsattePerioderTilAnnenPart(grunnlag, ikkeSøktePerioderTilUkerForbeholdtMor);
        oppholdPerioder.addAll(ikkeSøktePerioderTilFarMedEneomsorg);
        oppholdPerioder.addAll(finnOppholdISøktePerioderVedAdopsjon(grunnlag));

        List<OppholdPeriode> samlet = new ArrayList<>(ikkeSøktePerioderTilUkerForbeholdtMor);
        for (OppholdPeriode oppholdPeriode : oppholdPerioder) {
            if (!contains(samlet, oppholdPeriode.getFom(), oppholdPeriode.getTom())) {
                samlet.add(oppholdPeriode);
            }
        }

        return samlet.stream()
                .map(OppholdPeriodeTjeneste::fjernHelgFraBegynnelseOgSlutt).filter(Optional::isPresent).map(Optional::get)
                .map(p -> fjernPerioderFørSkjæringstidspunktOpptjening(p, grunnlag)).filter(Optional::isPresent).map(Optional::get)
                .map(p -> fjernPerioderFørEndringsdatoVedRevurdering(p, grunnlag)).filter(Optional::isPresent).map(Optional::get)
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
    }

    private static boolean contains(List<OppholdPeriode> list, LocalDate fom, LocalDate tom) {
        return list.stream().anyMatch(item -> item.getFom().isEqual(fom) && item.getTom().isEqual(tom));
    }

    private static List<OppholdPeriode> finnOppholdISøktePerioderVedAdopsjon(RegelGrunnlag grunnlag) {
        if (Søknadstype.ADOPSJON.equals(grunnlag.getSøknad().getType())) {
            Optional<OppholdPeriode> oppholdAdopsjon = utledOppholdVedAdopsjon(grunnlag);
            if (oppholdAdopsjon.isPresent()) {
                return Collections.singletonList(oppholdAdopsjon.get());
            }
        }
        return Collections.emptyList();
    }

    private static List<OppholdPeriode> finnOppholdIUkerForbeholdtMor(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        if (grunnlag.getBehandling().getSøkerErMor() && Søknadstype.FØDSEL.equals(grunnlag.getSøknad().getType())) {
            return utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);
        }
        if (farSøkerFødselEllerTerminOgBareFarHarRett(grunnlag) && !grunnlag.getRettOgOmsorg().getAleneomsorg()) {
            Optional<OppholdPeriode> oppholdFar = utledOppholdForFar(grunnlag.getDatoer().getFamiliehendelse(), grunnlag.getSøknad().getUttaksperioder(), konfigurasjon);
            if (oppholdFar.isPresent()) {
                return Collections.singletonList(oppholdFar.get());
            }
        }
        return Collections.emptyList();
    }

    private static List<OppholdPeriode> finnIkkeSøktePerioderTilFarMedAleneomsorg(RegelGrunnlag grunnlag) {
        if (!grunnlag.getBehandling().getSøkerErMor() && grunnlag.getRettOgOmsorg().getAleneomsorg()) {
            UttakPeriode førstePeriode = grunnlag.getSøknad().getUttaksperioder().get(0);
            if (grunnlag.getDatoer().getFamiliehendelse().isBefore(førstePeriode.getFom())) {
                OppholdPeriode nyPeriode = lagOppholdPeriode(grunnlag.getDatoer().getFamiliehendelse(), førstePeriode.getFom().minusDays(1), Stønadskontotype.FORELDREPENGER);
                return Collections.singletonList(nyPeriode);
            }
        }
        return Collections.emptyList();
    }

    private static Optional<OppholdPeriode> utledOppholdVedAdopsjon(RegelGrunnlag grunnlag) {
        LocalDate senesteLovligeStartdatoVedAdopsjon = utledSenesteLovligeStartdatoVedAdopsjon(grunnlag);

        var førsteUttaksdatoSøknad = førsteUttaksdatoSøknad(grunnlag);
        Optional<LocalDate> førsteUttaksdatoAnnenpart = førsteUttaksdatoAnnenpart(grunnlag);
        if (førsteUttaksdatoSøknad.isAfter(senesteLovligeStartdatoVedAdopsjon)
                && (førsteUttaksdatoAnnenpart.isEmpty() || førsteUttaksdatoAnnenpart.get().isAfter(senesteLovligeStartdatoVedAdopsjon))) {
            LocalDate tom;
            if (førsteUttaksdatoAnnenpart.isEmpty() || førsteUttaksdatoSøknad.isBefore(førsteUttaksdatoAnnenpart.get())) {
                tom = førsteUttaksdatoSøknad.minusDays(1);
            } else {
                tom = førsteUttaksdatoAnnenpart.get().minusDays(1);
            }
            return Optional.of(lagOppholdPeriode(senesteLovligeStartdatoVedAdopsjon, tom));
        }
        return Optional.empty();
    }

    private static Optional<LocalDate> førsteUttaksdatoAnnenpart(RegelGrunnlag grunnlag) {
        return grunnlag.getAnnenPart() == null ? Optional.empty() : Optional.of(førsteFom(grunnlag.getAnnenPart().getUttaksperioder()));
    }

    private static LocalDate førsteUttaksdatoSøknad(RegelGrunnlag grunnlag) {
        return førsteFom(grunnlag.getSøknad().getUttaksperioder());
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

    private static Optional<OppholdPeriode> utledOppholdForFar(LocalDate familiehendelse, List<UttakPeriode> uttaksperioder, Konfigurasjon konfigurasjon) {
        UttakPeriode førstePeriode = uttaksperioder.get(0);
        int ukerForbeholdtMor = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelse);
        if (familiehendelse.plusWeeks(ukerForbeholdtMor).isBefore(førstePeriode.getFom())) {
            OppholdPeriode nyPeriode = lagOppholdPeriode(familiehendelse.plusWeeks(ukerForbeholdtMor), førstePeriode.getFom().minusDays(1), Stønadskontotype.FORELDREPENGER);
            return Optional.of(nyPeriode);
        }
        return Optional.empty();
    }

    private static boolean farSøkerFødselEllerTermin(RegelGrunnlag grunnlag) {
        return !grunnlag.getBehandling().getSøkerErMor() && erFødselEllerTermin(grunnlag);
    }

    private static boolean erFødselEllerTermin(RegelGrunnlag grunnlag) {
        return Søknadstype.FØDSEL.equals(grunnlag.getSøknad().getType());
    }

    private static boolean bareFarRett(RegelGrunnlag grunnlag) {
        return grunnlag.getRettOgOmsorg().getFarHarRett() && !grunnlag.getRettOgOmsorg().getMorHarRett();
    }

    private static List<OppholdPeriode> utledOppholdForMorFraOppgittePerioder(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        LocalDate familiehendelseDato = grunnlag.getDatoer().getFamiliehendelse();
        List<LukketPeriode> uttakPerioder = slåSammenUttakForBeggeParter(grunnlag).stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());
        List<OppholdPeriode> oppholdFørFødsel = finnOppholdFørFødsel(uttakPerioder, familiehendelseDato, grunnlag.getSøknad().getType(),
                grunnlag.getGyldigeStønadskontotyper(), konfigurasjon);
        List<OppholdPeriode> oppholdEtterFødsel = finnOppholdEtterFødsel(uttakPerioder, familiehendelseDato, grunnlag.getGyldigeStønadskontotyper(), konfigurasjon);

        return Stream.of(oppholdFørFødsel, oppholdEtterFødsel)
                .flatMap(Collection::stream)
                .map(OppholdPeriodeTjeneste::fjernHelgFraBegynnelseOgSlutt).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<OppholdPeriode> fjernPerioderFørEndringsdatoVedRevurdering(OppholdPeriode oppholdsperiode, RegelGrunnlag grunnlag) {
        if (!grunnlag.erRevurdering()) {
            return Optional.of(oppholdsperiode);
        }
        LocalDate endringsdato = grunnlag.getRevurdering().getEndringsdato();
        return fjernPerioderFørDato(oppholdsperiode, endringsdato);
    }

    private static Optional<OppholdPeriode> fjernPerioderFørSkjæringstidspunktOpptjening(OppholdPeriode oppholdsperiode, RegelGrunnlag grunnlag) {
        LocalDate skjæringstidspunkt = grunnlag.getOpptjening().getSkjæringstidspunkt();
        // Skal ikke fjerne periode før skjæringstidspunkt for far med aleneomsorg eller enerett (fødsel eller adopsjon)
        if ((farSøkerFødselEllerTerminOgBareFarHarRett(grunnlag) || (!grunnlag.getBehandling().getSøkerErMor() && (grunnlag.getRettOgOmsorg().getAleneomsorg()))
            && skjæringstidspunkt.isAfter(grunnlag.getDatoer().getFamiliehendelse()))) {
            return Optional.of(oppholdsperiode);
        }
        return fjernPerioderFørDato(oppholdsperiode, skjæringstidspunkt);
    }

    private static Optional<OppholdPeriode> fjernPerioderFørDato(OppholdPeriode oppholdsperiode, LocalDate dato) {
        if (oppholdsperiode.getTom().isBefore(dato)) {
            return Optional.empty();
        }
        if (oppholdsperiode.overlapper(dato)) {
            return Optional.of(oppholdsperiode.kopiMedNyPeriode(dato, oppholdsperiode.getTom()));
        }
        return Optional.of(oppholdsperiode);
    }

    private static boolean farSøkerFødselEllerTerminOgBareFarHarRett(RegelGrunnlag grunnlag) {
        return farSøkerFødselEllerTermin(grunnlag) && bareFarRett(grunnlag);
    }

    private static List<OppholdPeriode> finnOppholdISøktePerioderEtterUkerForbeholdtMorOgFastsattePerioderTilAnnenPart(RegelGrunnlag grunnlag,
                                                                                                                       List<OppholdPeriode> ikkeSøktePerioder) {
        List<LukketPeriode> allePerioder = slåSammenUttakForBeggeParter(grunnlag);
        //legge inn ikke søkte perioder til uker som er Forbeholdt til Mor etter fødsel
        allePerioder.addAll(ikkeSøktePerioder);
        List<OppholdPeriode> oppholdPerioder = finnOppholdIPerioderEtterUkerForbeholdtMorEtterFødsel(allePerioder);
        return oppholdPerioder.stream()
                .map(OppholdPeriodeTjeneste::fjernHelgFraBegynnelseOgSlutt).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private static List<OppholdPeriode> finnOppholdIPerioderEtterUkerForbeholdtMorEtterFødsel(List<LukketPeriode> perioder) {
        List<LukketPeriode> sortertePerioder = perioder.stream()
                .sorted(Comparator.comparing(LukketPeriode::getFom)).collect(Collectors.toList());

        List<OppholdPeriode> oppholdPerioder = new ArrayList<>();
        LocalDate oppholdFom = null;
        for (LukketPeriode lukketPeriode : sortertePerioder) {
            if (oppholdFom == null) {
                oppholdFom = lukketPeriode.getTom().plusDays(1);
            } else if (oppholdFom.isBefore(lukketPeriode.getFom())) {
                LocalDate oppholdTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(oppholdFom, oppholdTom) > 0) {
                    oppholdPerioder.add(lagOppholdPeriode(oppholdFom, oppholdTom));
                }
            }
            if (!lukketPeriode.getTom().isBefore(oppholdFom)) {
                oppholdFom = lukketPeriode.getTom().plusDays(1);
            }
        }
        return oppholdPerioder;
    }

    private static List<LukketPeriode> slåSammenUttakForBeggeParter(RegelGrunnlag grunnlag) {
        List<LukketPeriode> allePerioder = new ArrayList<>();
        if (grunnlag.getAnnenPart() != null) {
            allePerioder.addAll(grunnlag.getAnnenPart().getUttaksperioder());
        }
        allePerioder.addAll(grunnlag.getSøknad().getUttaksperioder());
        return allePerioder;
    }


    private static List<OppholdPeriode> finnOppholdFørFødsel(List<LukketPeriode> søktePerioder,
                                                             LocalDate familiehendelseDato,
                                                             Søknadstype søknadstype,
                                                             Set<Stønadskontotype> gyldigeStønadskontotyper,
                                                             Konfigurasjon konfigurasjon) {
        if (Søknadstype.ADOPSJON.equals(søknadstype)) {
            return new ArrayList<>();
        }
        int fellesperiodeFørFødselUker = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelseDato);
        LukketPeriode betingetPeriodeFørFødsel = new LukketPeriode(familiehendelseDato.minusWeeks(fellesperiodeFørFødselUker), familiehendelseDato.minusDays(1));

        List<OppholdPeriode> oppholdFørFødsel = finnOppholdIPeriode(søktePerioder, betingetPeriodeFørFødsel).stream()
                .map(opphold -> lagOppholdPeriode(opphold.getFom(), opphold.getTom(), Stønadskontotype.FORELDREPENGER_FØR_FØDSEL))
                .collect(Collectors.toList());

        List<OppholdPeriode> oppholdForFellesperiodeFørFødsel = new ArrayList<>();

        if (!søktePerioder.isEmpty()) {
            søktePerioder.sort((p1, p2) -> p2.getFom().isAfter(p1.getFom()) ? -1 : 1);

            if (søktePerioder.get(0).getFom().isBefore(familiehendelseDato.minusWeeks(fellesperiodeFørFødselUker))) {

                LukketPeriode betingetFellesperiodeFørFødsel = new LukketPeriode(søktePerioder.get(0).getFom(), familiehendelseDato.minusWeeks(fellesperiodeFørFødselUker).minusDays(1));
                oppholdForFellesperiodeFørFødsel = finnOppholdIPeriode(søktePerioder, betingetFellesperiodeFørFødsel).stream()
                        .map(opphold -> {
                            Stønadskontotype type = gyldigeStønadskontotyper.contains(Stønadskontotype.FELLESPERIODE) ? Stønadskontotype.FELLESPERIODE : Stønadskontotype.FORELDREPENGER;
                            return lagOppholdPeriode(opphold.getFom(), opphold.getTom(), type);
                        })
                        .collect(Collectors.toList());
            }
        }
        return Stream.of(oppholdForFellesperiodeFørFødsel, oppholdFørFødsel)
                .flatMap(Collection::stream)
                .filter(p -> p.virkedager() > 0)
                .collect(Collectors.toList());
    }


    private static List<OppholdPeriode> finnOppholdEtterFødsel(List<LukketPeriode> søktePerioder,
                                                               LocalDate familiehendelseDato,
                                                               Set<Stønadskontotype> gyldigeStønadskontotyper,
                                                               Konfigurasjon konfigurasjon) {
        int mødrekvoteEtterFødselUker = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelseDato);
        LukketPeriode betingetPeriodeEtterFødsel = new LukketPeriode(familiehendelseDato, familiehendelseDato.plusWeeks(mødrekvoteEtterFødselUker).minusDays(1));
        Stønadskontotype stønadskontotype = gyldigeStønadskontotyper.contains(Stønadskontotype.MØDREKVOTE) ? Stønadskontotype.MØDREKVOTE : Stønadskontotype.FORELDREPENGER;
        return finnOppholdIPeriode(søktePerioder, betingetPeriodeEtterFødsel).stream()
                .map(opphold -> lagOppholdPeriode(opphold.getFom(), opphold.getTom(), stønadskontotype))
                .collect(Collectors.toList());
    }


    /**
     * Fjern helgedager i begynnelse og slutt av oppholdsperiode.
     *
     * @param oppholdPeriode perioder som skal strippes.
     *
     * @return periode uten helg i begynnelsen og slutten. Optional.empty() dersom perioden bare besto av helgedager.
     */
    private static Optional<OppholdPeriode> fjernHelgFraBegynnelseOgSlutt(OppholdPeriode oppholdPeriode) {
        Predicate<LocalDate> sjekkOmHelg = dato -> dato.getDayOfWeek().equals(DayOfWeek.SATURDAY) || dato.getDayOfWeek().equals(DayOfWeek.SUNDAY);

        LocalDate fom = oppholdPeriode.getFom();
        LocalDate tom = oppholdPeriode.getTom();

        while(sjekkOmHelg.test(fom)) {
            fom = fom.plusDays(1);
        }
        while(sjekkOmHelg.test(tom)) {
            tom = tom.minusDays(1);
        }

        if (fom.isAfter(tom)) {
            return Optional.empty();
        }
        return Optional.of(oppholdPeriode.kopiMedNyPeriode(fom, tom));
    }

}
