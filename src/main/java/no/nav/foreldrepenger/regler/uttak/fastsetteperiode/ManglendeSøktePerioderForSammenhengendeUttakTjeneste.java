package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.bareFarRett;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.fjernPerioderFørDato;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.fjernPerioderFørEndringsdatoVedRevurdering;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.lagManglendeSøktPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.slåSammenUttakForBeggeParter;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.utledSenesteLovligeStartdatoVedAdopsjon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

final class ManglendeSøktePerioderForSammenhengendeUttakTjeneste {

    private ManglendeSøktePerioderForSammenhengendeUttakTjeneste() {
        // Skal ikke instansieres
    }

    static List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag) {
        var manglendePerioderIUkerForbeholdtMor = finnManglendeSøktIUkerForbeholdtMor(grunnlag);
        var manglendePerioderIUkerFarMedAleneomsorg = finnManglendeSøktePerioderTilFarMedAleneomsorg(grunnlag);
        List<OppgittPeriode> manglendePerioder = new ArrayList<>();
        manglendePerioder.addAll(manglendePerioderIUkerFarMedAleneomsorg);
        manglendePerioder.addAll(manglendePerioderIUkerForbeholdtMor);

        var manglendeSøktePerioder = finnManglendeMellomliggendePerioder(
                grunnlag, manglendePerioder);
        manglendeSøktePerioder.addAll(manglendePerioderIUkerFarMedAleneomsorg);
        manglendeSøktePerioder.addAll(finnPerioderVedAdopsjon(grunnlag));
        manglendeSøktePerioder.addAll(manglendePerioderIUkerForbeholdtMor);

        var trimmedePerioder = trimPerioder(grunnlag, manglendeSøktePerioder);
        List<OppgittPeriode> samlet = new ArrayList<>();
        for (var msp : trimmedePerioder) {
            if (!contains(samlet, msp.getFom(), msp.getTom())) {
                samlet.add(msp);
            }
        }

        return samlet;
    }

    private static List<OppgittPeriode> trimPerioder(RegelGrunnlag grunnlag, List<OppgittPeriode> manglendeSøktePerioder) {
        return manglendeSøktePerioder.stream()
                .map(ManglendeSøktPeriodeUtil::fjernHelg)
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
        if (grunnlag.getSøknad().gjelderAdopsjon()) {
            var mspAdopsjon = utledManglendeSøktVedAdopsjon(grunnlag);
            if (mspAdopsjon.isPresent()) {
                return List.of(mspAdopsjon.get());
            }
        }
        return Collections.emptyList();
    }

    private static List<OppgittPeriode> finnManglendeSøktIUkerForbeholdtMor(RegelGrunnlag grunnlag) {
        if (grunnlag.getBehandling().isSøkerMor() && grunnlag.getSøknad().getType().gjelderTerminFødsel()) {
            return utledManglendeForMorFraOppgittePerioder(grunnlag);
        }
        if (farSøkerFødselEllerTerminOgBareFarHarRett(grunnlag) && !grunnlag.getRettOgOmsorg().getAleneomsorg()) {
            var oppholdFar = utledManglendeSøktForFar(grunnlag.getDatoer().getFamiliehendelse(),
                    grunnlag.getSøknad().getOppgittePerioder());
            if (oppholdFar.isPresent()) {
                return List.of(oppholdFar.get());
            }
        }
        return Collections.emptyList();
    }

    private static List<OppgittPeriode> finnManglendeSøktePerioderTilFarMedAleneomsorg(RegelGrunnlag grunnlag) {
        if (isFarMedAleneomsorg(grunnlag)) {
            var førstePeriode = grunnlag.getSøknad().getOppgittePerioder().get(0);
            if (grunnlag.getDatoer().getFamiliehendelse().isBefore(førstePeriode.getFom())) {
                var nyPeriode = lagManglendeSøktPeriode(grunnlag.getDatoer().getFamiliehendelse(),
                        førstePeriode.getFom().minusDays(1), Stønadskontotype.FORELDREPENGER);
                return List.of(nyPeriode);
            }
        }
        return Collections.emptyList();
    }

    private static Optional<OppgittPeriode> utledManglendeSøktVedAdopsjon(RegelGrunnlag grunnlag) {
        var senesteLovligeStartdatoVedAdopsjon = utledSenesteLovligeStartdatoVedAdopsjon(grunnlag);

        var førsteUttaksdatoSøknad = førsteUttaksdatoSøknad(grunnlag);
        var førsteUttaksdatoAnnenpart = førsteUttaksdatoAnnenpart(grunnlag);
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

    private static Optional<OppgittPeriode> utledManglendeSøktForFar(LocalDate familiehendelse, List<OppgittPeriode> uttaksperioder) {
        var førstePeriode = uttaksperioder.get(0);
        var ukerForbeholdtMor = Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, familiehendelse);
        if (familiehendelse.plusWeeks(ukerForbeholdtMor).isBefore(førstePeriode.getFom())) {
            var nyPeriode = lagManglendeSøktPeriode(familiehendelse.plusWeeks(ukerForbeholdtMor), førstePeriode.getFom().minusDays(1),
                    Stønadskontotype.FORELDREPENGER);
            return Optional.of(nyPeriode);
        }
        return Optional.empty();
    }

    private static boolean farSøkerFødselEllerTermin(RegelGrunnlag grunnlag) {
        return grunnlag.getBehandling().isSøkerFarMedMor() && erFødselEllerTermin(grunnlag);
    }

    private static boolean erFødselEllerTermin(RegelGrunnlag grunnlag) {
        return grunnlag.getSøknad().getType().gjelderTerminFødsel();
    }

    private static List<OppgittPeriode> utledManglendeForMorFraOppgittePerioder(RegelGrunnlag grunnlag) {
        var familiehendelseDato = grunnlag.getDatoer().getFamiliehendelse();
        var uttakPerioder = slåSammenUttakForBeggeParter(grunnlag).stream()
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
        var mspFørFødsel = finnManglendeSøktFørFødsel(uttakPerioder, familiehendelseDato,
                grunnlag.getSøknad().getType(), grunnlag.getGyldigeStønadskontotyper());
        var mspEtterFødsel = finnPerioderEtterFødsel(uttakPerioder, familiehendelseDato,
                grunnlag.getGyldigeStønadskontotyper());

        return Stream.of(mspFørFødsel, mspEtterFødsel)
                .flatMap(Collection::stream)
                .map(ManglendeSøktPeriodeUtil::fjernHelg)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<OppgittPeriode> fjernPerioderFørSkjæringstidspunktOpptjening(OppgittPeriode msp, RegelGrunnlag grunnlag) {
        var skjæringstidspunkt = grunnlag.getOpptjening().getSkjæringstidspunkt();
        // Skal ikke fjerne periode før skjæringstidspunkt for far med aleneomsorg eller enerett (fødsel eller adopsjon)
        if ((farSøkerFødselEllerTerminOgBareFarHarRett(grunnlag)
                || isFarMedAleneomsorg(grunnlag)
                && skjæringstidspunkt.isAfter(grunnlag.getDatoer().getFamiliehendelse()))) {
            return Optional.of(msp);
        }
        if (mspFyllerHullMellomForeldrene(msp, grunnlag)) {
            return Optional.of(msp);
        }
        return fjernPerioderFørDato(msp, skjæringstidspunkt);
    }

    private static boolean isFarMedAleneomsorg(RegelGrunnlag grunnlag) {
        return grunnlag.getBehandling().isSøkerFarMedMor() && grunnlag.getRettOgOmsorg().getAleneomsorg();
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

    private static boolean farSøkerFødselEllerTerminOgBareFarHarRett(RegelGrunnlag grunnlag) {
        return farSøkerFødselEllerTermin(grunnlag) && bareFarRett(grunnlag);
    }

    private static List<OppgittPeriode> finnManglendeMellomliggendePerioder(RegelGrunnlag grunnlag,
                                                                            List<OppgittPeriode> ekskludertePerioder) {
        var allePerioder = slåSammenUttakForBeggeParter(grunnlag);
        //legge inn ikke søkte perioder til uker som er Forbeholdt til Mor etter fødsel
        allePerioder.addAll(ekskludertePerioder);
        return finnManglendeMellomliggendePerioder(allePerioder).stream()
                .map(ManglendeSøktPeriodeUtil::fjernHelg)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }


    private static List<OppgittPeriode> finnManglendeSøktFørFødsel(List<LukketPeriode> søktePerioder,
                                                                   LocalDate familiehendelseDato,
                                                                   Søknadstype søknadstype,
                                                                   Set<Stønadskontotype> gyldigeStønadskontotyper) {
        if (Søknadstype.ADOPSJON.equals(søknadstype)) {
            return List.of();
        }
        var antallUkerFpffFørFødsel = Konfigurasjon.STANDARD.getParameter(Parametertype.SENEST_UTTAK_FØR_TERMIN_UKER,
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
            var type = gyldigeStønadskontotyper.contains(
                    Stønadskontotype.FELLESPERIODE) ? Stønadskontotype.FELLESPERIODE : Stønadskontotype.FORELDREPENGER;
            return lagManglendeSøktPeriode(msp.getFom(), msp.getTom(), type);
        }).collect(Collectors.toList());
        return Stream.of(mspForFellesperiodeFørFødsel, mspFørFødsel)
                .flatMap(Collection::stream)
                .filter(p -> p.virkedager() > 0)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriode> finnManglendeMellomliggendePerioder(List<LukketPeriode> perioder) {
        var sortertePerioder = perioder.stream()
                .sorted(Comparator.comparing(LukketPeriode::getFom)).toList();

        List<OppgittPeriode> mellomliggendePerioder = new ArrayList<>();
        LocalDate mspFom = null;
        for (var lukketPeriode : sortertePerioder) {
            if (mspFom == null) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            } else if (mspFom.isBefore(lukketPeriode.getFom())) {
                var mspTom = lukketPeriode.getFom().minusDays(1);
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

    private static List<OppgittPeriode> finnPerioderEtterFødsel(List<LukketPeriode> søktePerioder,
                                                                LocalDate familiehendelseDato,
                                                                Set<Stønadskontotype> gyldigeStønadskontotyper) {
        var mødrekvoteEtterFødselUker = Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
                familiehendelseDato);
        var betingetPeriodeEtterFødsel = new LukketPeriode(familiehendelseDato,
                familiehendelseDato.plusWeeks(mødrekvoteEtterFødselUker).minusDays(1));
        var stønadskontotype = gyldigeStønadskontotyper.contains(
                Stønadskontotype.MØDREKVOTE) ? Stønadskontotype.MØDREKVOTE : Stønadskontotype.FORELDREPENGER;
        return ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(søktePerioder, betingetPeriodeEtterFødsel)
                .stream()
                .map(msp -> lagManglendeSøktPeriode(msp.getFom(), msp.getTom(), stønadskontotype))
                .collect(Collectors.toList());
    }
}
