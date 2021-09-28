package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.bareFarRett;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.fjernHelg;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.fjernPerioderFørEndringsdatoVedRevurdering;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.lagManglendeSøktPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.slåSammenUttakForBeggeParter;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.ManglendeSøktPeriodeUtil.utledSenesteLovligeStartdatoVedAdopsjon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

final class ManglendeSøktePerioderTjeneste {

    private ManglendeSøktePerioderTjeneste() {
        // Skal ikke instansieres
    }

    static List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        if (grunnlag.getSøknad().getOppgittePerioder().isEmpty()) {
            throw new IllegalArgumentException("Krever minst en oppgitt periode");
        }
        final List<OppgittPeriode> msPerioder;
        if (bareFarRett(grunnlag)) {
            msPerioder = finnManglendeSøktPeriodeBareFarHarRett(grunnlag, konfigurasjon).stream().toList();
        } else if (grunnlag.getSøknad().gjelderAdopsjon()) {
            msPerioder = List.of();
        } else {
            msPerioder = finnManglendeSøktePerioderITidsprommetForbeholdtMor(grunnlag, konfigurasjon);
        }
        return fellesFilter(grunnlag, msPerioder);
    }

    private static List<OppgittPeriode> fellesFilter(RegelGrunnlag grunnlag, List<OppgittPeriode> msPerioder) {
        return msPerioder.stream()
                .map(p -> fjernPerioderFørEndringsdatoVedRevurdering(p, grunnlag))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(p -> fjernHelg(p))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
    }

    /**
     * Gjelder ikke bare far har rett, se egen metode
     */
    private static List<OppgittPeriode> finnManglendeSøktePerioderITidsprommetForbeholdtMor(RegelGrunnlag grunnlag,
                                                                                            Konfigurasjon konfigurasjon) {
        // Må sjekke om annenpart. Gjelder der far først har søkt om aleneomsorg.
        if (grunnlag.getBehandling().isSøkerFarMedMor() && grunnlag.getAnnenPart() == null) {
            return List.of();
        }
        var familiehendelse = grunnlag.getDatoer().getFamiliehendelse();

        var fellesUttakBeggeParter = slåSammenUttakForBeggeParter(grunnlag)
                .stream()
                .flatMap(p -> splitPåTidsperiodeForbeholdtMor(familiehendelse, p, konfigurasjon).stream())
                .collect(Collectors.toList());
        var førsteFellesUttaksdato = fellesUttakBeggeParter.get(0).getFom();
        var sisteFellesUttaksdato = fellesUttakBeggeParter.get(fellesUttakBeggeParter.size() - 1).getTom();

        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(konfigurasjon, familiehendelse);
        if (grunnlag.getBehandling().isSøkerMor()) {
            if (førsteFellesUttaksdato.isAfter(familiehendelse)) {
                //Feks mor søker ikke om uke 1-3, men fra uke 4 og utover. Legger til periode for at det skal opprettes msp
                fellesUttakBeggeParter.add(new LukketPeriode(familiehendelse.minusDays(1), familiehendelse.minusDays(1)));
            }
            if (sisteFellesUttaksdato.isBefore(tomTidsperiodeForbeholdtMor)) {
                //Feks mor søker bare om de første 4 ukene, må ha msp på resten av ukene forbeholdt mor. Legger til periode for at det skal opprettes msp
                fellesUttakBeggeParter.add(new LukketPeriode(tomTidsperiodeForbeholdtMor.plusDays(1), tomTidsperiodeForbeholdtMor.plusDays(1)));
            }
        }
        return finnManglendeMellomliggendePerioder(fellesUttakBeggeParter, familiehendelse).stream()
                .flatMap(p -> split(tomTidsperiodeForbeholdtMor, p))
                .filter(p -> periodeLiggerITidsrommetForbeholdtMor(grunnlag, konfigurasjon, p))
                .sorted(Comparator.comparing(OppgittPeriode::getFom))
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriode> finnManglendeMellomliggendePerioder(List<LukketPeriode> perioder, LocalDate familieHendelse) {
        var sortertePerioder = perioder.stream()
                .sorted(Comparator.comparing(LukketPeriode::getFom))
                .collect(Collectors.toList());

        List<OppgittPeriode> mellomliggendePerioder = new ArrayList<>();
        LocalDate mspFom = null;
        for (var lukketPeriode : sortertePerioder) {
            if (mspFom == null) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            } else if (mspFom.isBefore(lukketPeriode.getFom())) {
                var mspTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(mspFom, mspTom) > 0) {
                    mellomliggendePerioder.addAll(finnMsp(familieHendelse, mspFom, mspTom));
                }
            }
            if (!lukketPeriode.getTom().isBefore(mspFom)) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            }
        }
        return mellomliggendePerioder;
    }

    private static List<OppgittPeriode> finnMsp(LocalDate familieHendelse, LocalDate mspFom, LocalDate mspTom) {
        if (new LukketPeriode(mspFom, mspTom).overlapper(familieHendelse) && !mspFom.isEqual(familieHendelse)) {
            //Splitter opp msp for å få riktig konto før og etter familiehendelse
            var førSplitt = lagManglendeSøktPeriode(mspFom, familieHendelse.minusDays(1), Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
            var etterSplitt = lagManglendeSøktPeriode(familieHendelse, mspTom, Stønadskontotype.MØDREKVOTE);
            return List.of(førSplitt, etterSplitt);

        } else if (mspFom.isBefore(familieHendelse)) {
            return List.of(lagManglendeSøktPeriode(mspFom, mspTom, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL));
        }
        return List.of(lagManglendeSøktPeriode(mspFom, mspTom, Stønadskontotype.MØDREKVOTE));
    }

    private static Stream<OppgittPeriode> split(LocalDate dato, OppgittPeriode periode) {
        if (periode.overlapper(dato) && !periode.getFom().isEqual(dato) && !periode.getTom().isEqual(dato)) {
            return Stream.of(periode.kopiMedNyPeriode(periode.getFom(), dato),
                    periode.kopiMedNyPeriode(dato.plusDays(1), periode.getTom()));
        }
        return Stream.of(periode);
    }

    private static List<OppgittPeriode> finnManglendeSøktPeriodeBareFarHarRett(RegelGrunnlag grunnlag,
                                                                                   Konfigurasjon konfigurasjon) {
        //Kommer sortert
        var søktFom = grunnlag.getSøknad().getOppgittePerioder().get(0).getFom();
        if (grunnlag.getSøknad().gjelderAdopsjon()) {
            return finnManglendeSøktPeriodeBareFarHarRettAdopsjon(grunnlag, søktFom);
        }
        return finnManglendeSøktPeriodeBareFarHarRettFødselTermin(grunnlag, konfigurasjon, søktFom);
    }

    private static List<OppgittPeriode> finnManglendeSøktPeriodeBareFarHarRettAdopsjon(RegelGrunnlag grunnlag, LocalDate søktFom) {
        var senesteLovligeStartdatoVedAdopsjon = utledSenesteLovligeStartdatoVedAdopsjon(grunnlag);
        return finnManglendeSøktPeriodeBareFarHarRettFraDato(grunnlag, søktFom, senesteLovligeStartdatoVedAdopsjon);
    }

    private static List<OppgittPeriode> finnManglendeSøktPeriodeBareFarHarRettFødselTermin(RegelGrunnlag grunnlag,
                                                                                           Konfigurasjon konfigurasjon,
                                                                                           LocalDate søktFom) {
        var familiehendelse = grunnlag.getDatoer().getFamiliehendelse();
        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(konfigurasjon, familiehendelse);
        var bareFarSomHarRettMåHaStartdato = Virkedager.plusVirkedager(tomTidsperiodeForbeholdtMor, 1);
        return finnManglendeSøktPeriodeBareFarHarRettFraDato(grunnlag, søktFom, bareFarSomHarRettMåHaStartdato);
    }

    private static List<OppgittPeriode> finnManglendeSøktPeriodeBareFarHarRettFraDato(RegelGrunnlag grunnlag,
                                                                                      LocalDate søktFom,
                                                                                      LocalDate påkrevdOppstartsdato) {
        List<OppgittPeriode> msp = new ArrayList<>();
        if (søktFom.isAfter(påkrevdOppstartsdato)) {
            var mspFraPåkrevdOppstart = lagManglendeSøktPeriode(påkrevdOppstartsdato, søktFom.minusDays(1),
                    Stønadskontotype.FORELDREPENGER);
            msp.add(mspFraPåkrevdOppstart);
        }
        var søktePerioder = grunnlag.getSøknad()
                .getOppgittePerioder()
                .stream()
                .map(p -> new LukketPeriode(p.getFom(), p.getTom()))
                .toList();
        var manglendeMellomliggendePerioder = finnManglendeMellomliggendePerioder(søktePerioder, påkrevdOppstartsdato);
        msp.addAll(manglendeMellomliggendePerioder);
        return msp;
    }

    private static List<LukketPeriode> splitPåTidsperiodeForbeholdtMor(LocalDate familiehendelse,
                                                                       LukketPeriode periode,
                                                                       Konfigurasjon konfigurasjon) {

        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(konfigurasjon, familiehendelse);
        //Regner allerede med at perioden er splittet på familiehendelse
        if (periode.overlapper(tomTidsperiodeForbeholdtMor) && periode.getTom().isAfter(tomTidsperiodeForbeholdtMor)) {
            return List.of(new LukketPeriode(periode.getFom(), tomTidsperiodeForbeholdtMor),
                    new LukketPeriode(tomTidsperiodeForbeholdtMor.plusDays(1), periode.getTom()));
        }
        var fomTidsperiodeForbeholdtMor = fomTidsperiodeForbeholdtMor(konfigurasjon, familiehendelse);
        if (periode.overlapper(fomTidsperiodeForbeholdtMor) && periode.getFom().isBefore(fomTidsperiodeForbeholdtMor)) {
            return List.of(new LukketPeriode(periode.getFom(), fomTidsperiodeForbeholdtMor.minusDays(1)),
                    new LukketPeriode(fomTidsperiodeForbeholdtMor, periode.getTom()));
        }
        return List.of(periode);
    }

    private static boolean periodeLiggerITidsrommetForbeholdtMor(RegelGrunnlag grunnlag,
                                                                 Konfigurasjon konfigurasjon,
                                                                 LukketPeriode periode) {
        var familiehendelse = grunnlag.getDatoer().getFamiliehendelse();
        var fomTidsperiodeForbeholdtMor = fomTidsperiodeForbeholdtMor(konfigurasjon, familiehendelse);
        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(konfigurasjon, familiehendelse);
        //Regner med at periodene som kommer inne aldri overlapper med fom og tom forbeholdt mor. Altså at splittingen allerede er gjort
        return periode.overlapper(new LukketPeriode(fomTidsperiodeForbeholdtMor, tomTidsperiodeForbeholdtMor));
    }

    private static LocalDate tomTidsperiodeForbeholdtMor(Konfigurasjon konfigurasjon, LocalDate familiehendelse) {
        return familiehendelse.plusWeeks(
                konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelse))
                .minusDays(1);
    }

    private static LocalDate fomTidsperiodeForbeholdtMor(Konfigurasjon konfigurasjon, LocalDate familiehendelse) {
        return familiehendelse.minusWeeks(
                konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelse));
    }
}
