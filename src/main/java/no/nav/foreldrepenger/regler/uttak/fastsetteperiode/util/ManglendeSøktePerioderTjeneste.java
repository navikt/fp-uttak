package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.ManglendeSøktPeriodeUtil.fjernPerioderFørEndringsdatoVedRevurdering;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.ManglendeSøktPeriodeUtil.lagManglendeSøktPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.ManglendeSøktPeriodeUtil.slåSammenUttakForBeggeParter;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.util.MspBfhrUtil.finnManglendeSøktPeriodeBareFarHarRett;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Parametertype;


public final class ManglendeSøktePerioderTjeneste {

    private ManglendeSøktePerioderTjeneste() {
        // Skal ikke instansieres
    }

    public static List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag) {
        if (grunnlag.getSøknad().getOppgittePerioder().isEmpty()) {
            throw new IllegalArgumentException("Krever minst en oppgitt periode");
        }
        final List<OppgittPeriode> msPerioder;
        if (grunnlag.getRettOgOmsorg().rettighetsType().bareFarRett()) {
            msPerioder = finnManglendeSøktPeriodeBareFarHarRett(grunnlag).stream().toList();
        } else if (grunnlag.getSøknad().gjelderAdopsjon()) {
            msPerioder = List.of();
        } else {
            msPerioder = finnManglendeSøktePerioderITidsprommetForbeholdtMor(grunnlag);
        }
        return fellesFilter(grunnlag, msPerioder);
    }

    private static List<OppgittPeriode> fellesFilter(RegelGrunnlag grunnlag, List<OppgittPeriode> msPerioder) {
        return msPerioder.stream()
            .map(p -> fjernPerioderFørEndringsdatoVedRevurdering(p, grunnlag))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ManglendeSøktPeriodeUtil::fjernHelg)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .flatMap(msp -> fjernPerioderFørSammenhengendeTom(grunnlag, msp).stream())
            .sorted(Comparator.comparing(Periode::getFom))
            .toList();
    }

    private static Optional<OppgittPeriode> fjernPerioderFørSammenhengendeTom(RegelGrunnlag grunnlag, OppgittPeriode msp) {
        var sammenhengendeUttakTomDato = grunnlag.getBehandling().getSammenhengendeUttakTomDato();
        if (msp.overlapper(sammenhengendeUttakTomDato)) {
            return Optional.of(
                lagManglendeSøktPeriode(sammenhengendeUttakTomDato.plusDays(1), msp.getTom(), msp.getStønadskontotype()));
        }
        if (msp.getFom().isAfter(sammenhengendeUttakTomDato)) {
            return Optional.of(msp);
        }
        return Optional.empty();
    }

    /**
     * Gjelder ikke bare far har rett, se egen metode
     */
    private static List<OppgittPeriode> finnManglendeSøktePerioderITidsprommetForbeholdtMor(RegelGrunnlag grunnlag) {
        // Må sjekke om annenpart. Gjelder der far først har søkt om aleneomsorg.
        var behandling = grunnlag.getBehandling();
        if (behandling.isSøkerFarMedMor() && (grunnlag.getAnnenPart() == null || grunnlag.getAnnenPart().getUttaksperioder().isEmpty())) {
            return List.of();
        }
        var familiehendelse = grunnlag.getDatoer().getFamiliehendelse();

        var fellesUttakBeggeParter = slåSammenUttakForBeggeParter(grunnlag).stream()
            .flatMap(p -> splitPåTidsperiodeForbeholdtMor(familiehendelse, p).stream())
            .collect(Collectors.toList());
        var førsteFellesUttaksdato = fellesUttakBeggeParter.get(0).getFom();
        var sisteFellesUttaksdato = fellesUttakBeggeParter.get(fellesUttakBeggeParter.size() - 1).getTom();

        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(familiehendelse);
        if (behandling.isSøkerMor()) {
            if (førsteFellesUttaksdato.isAfter(familiehendelse)) {
                //Feks mor søker ikke om uke 1-3, men fra uke 4 og utover. Legger til periode for at det skal opprettes msp
                fellesUttakBeggeParter.add(new LukketPeriode(familiehendelse.minusDays(1), familiehendelse.minusDays(1)));
            }
            if (sisteFellesUttaksdato.isBefore(tomTidsperiodeForbeholdtMor)) {
                //Feks mor søker bare om de første 4 ukene, må ha msp på resten av ukene forbeholdt mor. Legger til periode for at det skal opprettes msp
                fellesUttakBeggeParter.add(new LukketPeriode(tomTidsperiodeForbeholdtMor.plusDays(1), tomTidsperiodeForbeholdtMor.plusDays(1)));
            }
        }
        var harForeldrepengerKonto = grunnlag.getKontoer().harStønadskonto(Stønadskontotype.FORELDREPENGER);

        var stønadskontotype = harForeldrepengerKonto ? Stønadskontotype.FORELDREPENGER : Stønadskontotype.MØDREKVOTE;
        var fomTidsperiodeForbeholdtMor = fomTidsperiodeForbeholdtMor(familiehendelse);
        return finnManglendeMellomliggendePerioderEtterFamilieHendelse(fellesUttakBeggeParter, familiehendelse, stønadskontotype).stream()
            .flatMap(p -> split(tomTidsperiodeForbeholdtMor.plusDays(1), p))
            .flatMap(p -> split(fomTidsperiodeForbeholdtMor, p))
            .filter(p -> periodeLiggerITidsrommetForbeholdtMor(grunnlag, p))
            .sorted(Comparator.comparing(OppgittPeriode::getFom))
            .toList();
    }

    private static List<OppgittPeriode> finnManglendeMellomliggendePerioderEtterFamilieHendelse(List<LukketPeriode> perioder,
                                                                                                LocalDate familieHendelse,
                                                                                                Stønadskontotype stønadskontotype) {
        var sortertePerioder = perioder.stream().sorted(Comparator.comparing(LukketPeriode::getFom)).toList();

        List<OppgittPeriode> mellomliggendePerioder = new ArrayList<>();
        LocalDate mspFom = null;
        for (var lukketPeriode : sortertePerioder) {
            if (mspFom == null) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            } else if (mspFom.isBefore(lukketPeriode.getFom())) {
                var mspTom = lukketPeriode.getFom().minusDays(1);
                if (Virkedager.beregnAntallVirkedager(mspFom, mspTom) > 0) {
                    var msp = finnMsp(familieHendelse, mspFom, mspTom, stønadskontotype);
                    msp.ifPresent(mellomliggendePerioder::add);
                }
            }
            if (!lukketPeriode.getTom().isBefore(mspFom)) {
                mspFom = lukketPeriode.getTom().plusDays(1);
            }
        }
        return mellomliggendePerioder;
    }

    private static Optional<OppgittPeriode> finnMsp(LocalDate familieHendelse,
                                                    LocalDate mspFom,
                                                    LocalDate mspTom,
                                                    Stønadskontotype stønadskontotype) {
        if (new LukketPeriode(mspFom, mspTom).overlapper(familieHendelse) && !mspFom.isEqual(familieHendelse)) {
            var etterSplitt = lagManglendeSøktPeriode(familieHendelse, mspTom, stønadskontotype);
            return Optional.of(etterSplitt);

        } else if (mspFom.isBefore(familieHendelse)) {
            return Optional.empty();
        }
        return Optional.of(lagManglendeSøktPeriode(mspFom, mspTom, stønadskontotype));
    }

    private static Stream<OppgittPeriode> split(LocalDate dato, OppgittPeriode periode) {
        if (periode.getFom().isEqual(dato)) {
            return Stream.of(periode);
        }

        if (periode.overlapper(dato)) {
            return Stream.of(periode.kopiMedNyPeriode(periode.getFom(), dato.minusDays(1)), periode.kopiMedNyPeriode(dato, periode.getTom()));
        }
        return Stream.of(periode);
    }

    private static List<LukketPeriode> splitPåTidsperiodeForbeholdtMor(LocalDate familiehendelse, LukketPeriode periode) {

        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(familiehendelse);
        //Regner allerede med at perioden er splittet på familiehendelse
        if (periode.overlapper(tomTidsperiodeForbeholdtMor) && periode.getTom().isAfter(tomTidsperiodeForbeholdtMor)) {
            return List.of(new LukketPeriode(periode.getFom(), tomTidsperiodeForbeholdtMor),
                new LukketPeriode(tomTidsperiodeForbeholdtMor.plusDays(1), periode.getTom()));
        }
        var fomTidsperiodeForbeholdtMor = fomTidsperiodeForbeholdtMor(familiehendelse);
        if (periode.overlapper(fomTidsperiodeForbeholdtMor) && periode.getFom().isBefore(fomTidsperiodeForbeholdtMor)) {
            return List.of(new LukketPeriode(periode.getFom(), fomTidsperiodeForbeholdtMor.minusDays(1)),
                new LukketPeriode(fomTidsperiodeForbeholdtMor, periode.getTom()));
        }
        return List.of(periode);
    }

    private static boolean periodeLiggerITidsrommetForbeholdtMor(RegelGrunnlag grunnlag, LukketPeriode periode) {
        var familiehendelse = grunnlag.getDatoer().getFamiliehendelse();
        var fomTidsperiodeForbeholdtMor = fomTidsperiodeForbeholdtMor(familiehendelse);
        var tomTidsperiodeForbeholdtMor = tomTidsperiodeForbeholdtMor(familiehendelse);
        //Regner med at periodene som kommer inne aldri overlapper med fom og tom forbeholdt mor. Altså at splittingen allerede er gjort
        return periode.overlapper(new LukketPeriode(fomTidsperiodeForbeholdtMor, tomTidsperiodeForbeholdtMor));
    }

    static LocalDate tomTidsperiodeForbeholdtMor(LocalDate familiehendelse) {
        return familiehendelse.plusWeeks(Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, familiehendelse))
            .minusDays(1);
    }

    private static LocalDate fomTidsperiodeForbeholdtMor(LocalDate familiehendelse) {
        return Virkedager.justerHelgTilMandag(
            familiehendelse.minusWeeks(Konfigurasjon.STANDARD.getParameter(Parametertype.SENEST_UTTAK_FØR_TERMIN_UKER, familiehendelse)));
    }
}
