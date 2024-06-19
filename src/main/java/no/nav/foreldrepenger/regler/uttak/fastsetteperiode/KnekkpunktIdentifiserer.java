package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterMaksgrenseForUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.BevegeligeHelligdagerUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.FarUttakRundtFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.SøknadsfristUtil;

class KnekkpunktIdentifiserer {

    private KnekkpunktIdentifiserer() {
        // hindrer instansiering
    }

    static Set<LocalDate> finnKnekkpunkter(RegelGrunnlag grunnlag) {
        var minimumsgrenseForLovligUttak = finnMinimumgrenseLovligUttak(grunnlag);
        var maksimumsgrenseForLovligeUttak = finnMaksgrenseForLovligUttak(grunnlag);
        var familiehendelseDato = grunnlag.getDatoer().getFamiliehendelse();

        Set<LocalDate> knekkpunkter = new TreeSet<>();
        knekkpunkter.add(minimumsgrenseForLovligUttak);
        knekkpunkter.addAll(knekkPunkterBaserPåFørsteLovligeUttaksdag(grunnlag));
        knekkpunkter.add(familiehendelseDato);
        knekkpunkter.add(maksimumsgrenseForLovligeUttak);
        grunnlag.getDatoer().getStartdatoNesteStønadsperiode().ifPresent(knekkpunkter::add);

        if (PrematurukerUtil.oppfyllerKravTilPrematuruker(
                grunnlag.getDatoer().getFødsel(), grunnlag.getDatoer().getTermin())) {
            knekkpunkter.add(grunnlag.getDatoer().getTermin());
        }

        if (søkersDødsdatoFinnes(grunnlag)) {
            knekkpunkter.add(grunnlag.getDatoer().getDødsdatoer().getSøkersDødsdato().plusDays(1));
        }

        if (barnsDødsdatoFinnes(grunnlag)) {
            knekkpunkter.add(
                    grunnlag.getDatoer()
                            .getDødsdatoer()
                            .getBarnsDødsdato()
                            .plusWeeks(
                                    Konfigurasjon.STANDARD.getParameter(
                                            Parametertype.UTTAK_ETTER_BARN_DØDT_UKER,
                                            familiehendelseDato)));
        }

        if (medlemskapOpphørsdatoFinnes(grunnlag)) {
            knekkpunkter.add(grunnlag.getMedlemskap().getOpphørsdato());
        }

        knekkpunkter.addAll(knekkpunkterPåArbeid(grunnlag.getArbeid()));
        leggTilKnekkpunkterForUtsettelsePgaFerie(
                grunnlag,
                minimumsgrenseForLovligUttak,
                maksimumsgrenseForLovligeUttak,
                knekkpunkter);

        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getOppgittePerioder());
        if (grunnlag.getSøknad().getType().gjelderTerminFødsel()) {
            // Før Prop 15L 21/22: Første 6 uker forbeholdt mor, unntatt flerbarn og aleneomsorg
            // Etter Prop 15L 21/22: Første 6 uker forbeholdt mor kun for kvoter. Far har opptil 10
            // dager samtidig uttak ifm fødsel
            knekkpunkter.add(
                    familiehendelseDato.minusWeeks(
                            Konfigurasjon.STANDARD.getParameter(
                                    Parametertype.SENEST_UTTAK_FØR_TERMIN_UKER,
                                    familiehendelseDato)));
            var hjemletFarUttakRundtFødsel =
                    grunnlag.getKontoer().harSpesialkonto(Spesialkontotype.FAR_RUNDT_FØDSEL)
                            && grunnlag.getKontoer()
                                            .getSpesialkontoTrekkdager(
                                                    Spesialkontotype.FAR_RUNDT_FØDSEL)
                                    > 0;
            var sakUtenKvoter =
                    grunnlag.getKontoer().harStønadskonto(Stønadskontotype.FORELDREPENGER);
            var erMor = grunnlag.getBehandling().isSøkerMor();
            if (!hjemletFarUttakRundtFødsel || !sakUtenKvoter || erMor) {
                knekkpunkter.add(
                        familiehendelseDato.plusWeeks(
                                Konfigurasjon.STANDARD.getParameter(
                                        Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER,
                                        familiehendelseDato)));
            }
            if (hjemletFarUttakRundtFødsel && !erMor) {
                knekkpunkter.addAll(
                        finnKnekkpunkterFarsPeriodeRundtFødsel(grunnlag, sakUtenKvoter));
            }
        }
        knekkpunkter.addAll(knekkBasertPåYtelser(grunnlag));

        if (grunnlag.getAnnenPart() != null) {
            knekkBasertPåAnnenPart(grunnlag, knekkpunkter);
        }

        return knekkpunkter.stream()
                .filter(k -> !k.isBefore(minimumsgrenseForLovligUttak))
                .filter(k -> !k.isAfter(maksimumsgrenseForLovligeUttak))
                .collect(Collectors.toSet());
    }

    private static Set<LocalDate> knekkBasertPåYtelser(RegelGrunnlag grunnlag) {
        var ytelser = grunnlag.getYtelser();
        var pleiepenger = ytelser.pleiepenger();
        return pleiepenger
                .map(
                        p ->
                                p.perioder().stream()
                                        .flatMap(
                                                per ->
                                                        Stream.of(
                                                                per.getFom(),
                                                                per.getTom().plusDays(1)))
                                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    private static Set<LocalDate> knekkPunkterBaserPåFørsteLovligeUttaksdag(
            RegelGrunnlag grunnlag) {
        return grunnlag.getSøknad().getOppgittePerioder().stream()
                .filter(p -> p.getTidligstMottattDato().isPresent())
                .filter(
                        p ->
                                p.overlapper(
                                        SøknadsfristUtil.finnFørsteLoveligeUttaksdag(
                                                p.getTidligstMottattDato().get())))
                .map(
                        p ->
                                SøknadsfristUtil.finnFørsteLoveligeUttaksdag(
                                        p.getTidligstMottattDato().get()))
                .collect(Collectors.toSet());
    }

    private static LocalDate finnMinimumgrenseLovligUttak(RegelGrunnlag grunnlag) {
        if (grunnlag.getSøknad().getType() == Søknadstype.TERMIN) {
            var termin = grunnlag.getDatoer().getTermin();
            return termin.minusWeeks(
                    Konfigurasjon.STANDARD.getParameter(
                            Parametertype.TIDLIGST_UTTAK_FØR_TERMIN_UKER, termin));
        }
        var familiehendelseDato = grunnlag.getDatoer().getFamiliehendelse();
        return familiehendelseDato.minusWeeks(
                Konfigurasjon.STANDARD.getParameter(
                        Parametertype.TIDLIGST_UTTAK_FØR_TERMIN_UKER, familiehendelseDato));
    }

    private static Set<LocalDate> knekkpunkterPåArbeid(Arbeid arbeid) {
        if (arbeid.getArbeidsforhold().size() == 1) {
            return Set.of();
        }
        return arbeid.getArbeidsforhold().stream()
                .map(a -> a.startdato())
                .collect(Collectors.toSet());
    }

    private static boolean medlemskapOpphørsdatoFinnes(RegelGrunnlag grunnlag) {
        return grunnlag.getMedlemskap() != null
                && grunnlag.getMedlemskap().getOpphørsdato() != null;
    }

    private static boolean barnsDødsdatoFinnes(RegelGrunnlag grunnlag) {
        return grunnlag.getDatoer().getDødsdatoer() != null
                && grunnlag.getDatoer().getDødsdatoer().getBarnsDødsdato() != null;
    }

    private static boolean søkersDødsdatoFinnes(RegelGrunnlag grunnlag) {
        return grunnlag.getDatoer().getDødsdatoer() != null
                && grunnlag.getDatoer().getDødsdatoer().getSøkersDødsdato() != null;
    }

    private static void leggTilKnekkpunkterForUtsettelsePgaFerie(
            RegelGrunnlag grunnlag,
            LocalDate minimumsgrenseForLovligUttak,
            LocalDate maksimumsgrenseForLovligeUttak,
            Set<LocalDate> knekkpunkter) {
        var bevegeligeHelligdager =
                finnKnekkpunktPåBevegeligeHelligdagerI(
                        new LukketPeriode(
                                minimumsgrenseForLovligUttak, maksimumsgrenseForLovligeUttak));
        var perioderMedFerie = perioderMedFerie(grunnlag);
        knekkpunkter.addAll(
                knekkpunkterForUtsettelsePgaFerie(bevegeligeHelligdager, perioderMedFerie));
    }

    private static List<OppgittPeriode> perioderMedFerie(RegelGrunnlag grunnlag) {
        return grunnlag.getSøknad().getOppgittePerioder().stream()
                .filter(p -> p.isUtsettelsePga(UtsettelseÅrsak.FERIE))
                .toList();
    }

    private static void knekkBasertPåAnnenPart(
            RegelGrunnlag grunnlag, Set<LocalDate> knekkpunkter) {
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getAnnenPart().getUttaksperioder());
    }

    private static LocalDate finnMaksgrenseForLovligUttak(RegelGrunnlag grunnlag) {
        return SjekkOmPeriodenErEtterMaksgrenseForUttak.regnUtMaksgrenseForLovligeUttaksdag(
                grunnlag.getDatoer().getFamiliehendelse());
    }

    private static List<LocalDate> finnKnekkpunktPåBevegeligeHelligdagerI(
            LukketPeriode uttaksperiode) {
        List<LocalDate> knekkpunkt = new ArrayList<>();
        for (var knekkpunktet :
                BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(uttaksperiode)) {
            knekkpunkt.add(knekkpunktet);
            knekkpunkt.add(knekkpunktet.plusDays(1));
        }

        return knekkpunkt;
    }

    private static List<LocalDate> knekkpunkterForUtsettelsePgaFerie(
            List<LocalDate> bevegeligeHelligdager, List<OppgittPeriode> utsettelsePerioder) {
        List<LocalDate> knekkpunkter = new ArrayList<>();
        for (Periode periode : utsettelsePerioder) {
            for (var helligdag : bevegeligeHelligdager) {
                if (helligdag.isAfter(periode.getFom()) && !helligdag.isAfter(periode.getTom())) {
                    knekkpunkter.add(helligdag);
                }
            }
        }
        return knekkpunkter;
    }

    private static Set<LocalDate> finnKnekkpunkterFarsPeriodeRundtFødsel(
            RegelGrunnlag grunnlag, boolean medTom) {
        return FarUttakRundtFødsel.utledFarsPeriodeRundtFødsel(grunnlag)
                .map(p -> medTom ? Set.of(p.getFom(), p.getTom().plusDays(1)) : Set.of(p.getFom()))
                .orElse(Set.of());
    }

    private static void leggTilKnekkpunkter(
            Set<LocalDate> knekkpunkter, List<? extends Periode> perioder) {
        for (Periode periode : perioder) {
            knekkpunkter.add(periode.getFom());
            knekkpunkter.add(periode.getTom().plusDays(1));
        }
    }
}
