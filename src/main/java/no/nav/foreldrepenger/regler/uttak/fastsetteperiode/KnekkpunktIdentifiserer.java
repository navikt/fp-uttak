package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterMaksgrenseForUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.BevegeligeHelligdagerUtil;
import no.nav.foreldrepenger.regler.uttak.felles.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;

class KnekkpunktIdentifiserer {

    private KnekkpunktIdentifiserer() {
        //hindrer instansiering
    }

    static Set<LocalDate> finnKnekkpunkter(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        LocalDate familiehendelseDato = grunnlag.getDatoer().getFamiliehendelse();
        LocalDate minimumsgrenseForLovligUttak = familiehendelseDato.minusWeeks(konfigurasjon.getParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, familiehendelseDato));
        LocalDate maksimumsgrenseForLovligeUttak = finnMaksgrenseForLovligUttak(grunnlag, konfigurasjon);

        Set<LocalDate> knekkpunkter = new TreeSet<>();
        knekkpunkter.add(minimumsgrenseForLovligUttak);
        knekkpunkter.add(grunnlag.getDatoer().getFørsteLovligeUttaksdag());
        knekkpunkter.add(familiehendelseDato);
        knekkpunkter.add(maksimumsgrenseForLovligeUttak);

        if (PrematurukerUtil.oppfyllerKravTilPrematuruker(grunnlag.getDatoer().getFødsel(), grunnlag.getDatoer().getTermin(), konfigurasjon)) {
            knekkpunkter.add(grunnlag.getDatoer().getTermin());
        }

        if (søkersDødsdatoFinnes(grunnlag)) {
            knekkpunkter.add(grunnlag.getDatoer().getDødsdatoer().getSøkersDødsdato().plusDays(1));
        }

        if (barnsDødsdatoFinnes(grunnlag)) {
            knekkpunkter.add(grunnlag.getDatoer().getDødsdatoer().getBarnsDødsdato().plusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER, familiehendelseDato)));
        }

        if (medlemskapOpphørsdatoFinnes(grunnlag)) {
            knekkpunkter.add(grunnlag.getMedlemskap().getOpphørsdato());
        }

        knekkpunkter.addAll(knekkpunkterPåArbeid(grunnlag.getArbeid()));
        leggTilKnekkpunkterForUtsettelsePgaFerie(grunnlag, minimumsgrenseForLovligUttak, maksimumsgrenseForLovligeUttak, knekkpunkter);

        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getUttaksperioder());
        if (grunnlag.getSøknad().getType() == Søknadstype.FØDSEL) {
            leggTilKnekkpunkterForFødsel(knekkpunkter, familiehendelseDato, konfigurasjon);
        }
        knekkBasertPåDokumentasjon(grunnlag, knekkpunkter);

        if (grunnlag.getAnnenPart() != null) {
            knekkBasertPåAnnenPart(grunnlag, knekkpunkter);
        }
        leggTilKnekkpunkterPåMottattDato(knekkpunkter, grunnlag);


        return knekkpunkter.stream()
                .filter(k -> !k.isBefore(minimumsgrenseForLovligUttak))
                .filter(k -> !k.isAfter(maksimumsgrenseForLovligeUttak))
                .collect(Collectors.toSet());
    }

    private static Set<LocalDate> knekkpunkterPåArbeid(Arbeid arbeid) {
        if (arbeid.getArbeidsforhold().size() == 1) {
            return Set.of();
        }
        return arbeid.getArbeidsforhold().stream()
                .map(Arbeidsforhold::getStartdato)
                .collect(Collectors.toSet());
    }

    private static boolean medlemskapOpphørsdatoFinnes(RegelGrunnlag grunnlag) {
        return grunnlag.getMedlemskap() != null && grunnlag.getMedlemskap().getOpphørsdato() != null;
    }

    private static boolean barnsDødsdatoFinnes(RegelGrunnlag grunnlag) {
        return grunnlag.getDatoer().getDødsdatoer() != null && grunnlag.getDatoer().getDødsdatoer().getBarnsDødsdato() != null;
    }

    private static boolean søkersDødsdatoFinnes(RegelGrunnlag grunnlag) {
        return grunnlag.getDatoer().getDødsdatoer() != null && grunnlag.getDatoer().getDødsdatoer().getSøkersDødsdato() != null;
    }

    private static void leggTilKnekkpunkterForUtsettelsePgaFerie(RegelGrunnlag grunnlag, LocalDate minimumsgrenseForLovligUttak, LocalDate maksimumsgrenseForLovligeUttak, Set<LocalDate> knekkpunkter) {
        List<LocalDate> bevegeligeHelligdager = finnKnekkpunktPåBevegeligeHelligdagerI(new LukketPeriode(minimumsgrenseForLovligUttak, maksimumsgrenseForLovligeUttak));
        List<UtsettelsePeriode> perioderMedFerie = perioderMedFerie(grunnlag);
        knekkpunkter.addAll(knekkpunkterForUtsettelsePgaFerie(bevegeligeHelligdager, perioderMedFerie));
    }

    private static List<UtsettelsePeriode> perioderMedFerie(RegelGrunnlag grunnlag) {
        return grunnlag.getSøknad().getUttaksperioder().stream()
                .filter(UttakPeriode::isUtsettelsePgaFerie)
                .map(u -> (UtsettelsePeriode) u)
                .collect(Collectors.toList());
    }

    private static void knekkBasertPåAnnenPart(RegelGrunnlag grunnlag, Set<LocalDate> knekkpunkter) {
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getAnnenPart().getUttaksperioder());
    }

    private static void knekkBasertPåDokumentasjon(RegelGrunnlag grunnlag, Set<LocalDate> knekkpunkter) {
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getDokumentasjon().getGyldigGrunnPerioder());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getDokumentasjon().getPerioderUtenOmsorg());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getDokumentasjon().getPerioderMedSykdomEllerSkade());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getDokumentasjon().getPerioderMedInnleggelse());
        leggTilKnekkpunkter(knekkpunkter, grunnlag.getSøknad().getDokumentasjon().getPerioderMedBarnInnlagt());
    }

    private static LocalDate finnMaksgrenseForLovligUttak(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        return SjekkOmPeriodenErEtterMaksgrenseForUttak.regnUtMaksgrenseForLovligeUttaksdag(grunnlag.getDatoer().getFamiliehendelse(), konfigurasjon);
    }

    private static void leggTilKnekkpunkterPåMottattDato(Set<LocalDate> knekkpunkter, RegelGrunnlag grunnlag) {
        leggTilKnekkpunkterVedGradering(knekkpunkter, grunnlag);
        leggTilKnekkpunkterVedUtsettelse(knekkpunkter, grunnlag);
    }

    private static void leggTilKnekkpunkterVedGradering(Set<LocalDate> knekkpunkter,
                                                        RegelGrunnlag grunnlag) {
        for (UttakPeriode uttakPeriode : grunnlag.getSøknad().getUttaksperioder()) {
            if (uttakPeriode.isGradering() && !uttakPeriode.getFom().isAfter(grunnlag.getSøknad().getMottattDato())) {
                knekkpunkter.add(grunnlag.getSøknad().getMottattDato());
            }
        }
    }

    private static void leggTilKnekkpunkterVedUtsettelse(Set<LocalDate> knekkpunkter, RegelGrunnlag grunnlag) {
        for (UttakPeriode uttakPeriode : grunnlag.getSøknad().getUttaksperioder()) {
            LocalDate mottattDato = grunnlag.getSøknad().getMottattDato();
            if (uttakPeriode instanceof UtsettelsePeriode && !uttakPeriode.getFom().isAfter(mottattDato)) {
                knekkpunkter.add(mottattDato);
            }
        }
    }

    private static List<LocalDate> finnKnekkpunktPåBevegeligeHelligdagerI(LukketPeriode uttaksperiode) {
        List<LocalDate> knekkpunkt = new ArrayList<>();
        for (LocalDate knekkpunktet : BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(uttaksperiode)) {
            knekkpunkt.add(knekkpunktet);
            knekkpunkt.add(knekkpunktet.plusDays(1));
        }

        return knekkpunkt;
    }

    private static List<LocalDate> knekkpunkterForUtsettelsePgaFerie(List<LocalDate> bevegeligeHelligdager, List<UtsettelsePeriode> utsettelsePerioder) {
        List<LocalDate> knekkpunkter = new ArrayList<>();
        for (Periode periode : utsettelsePerioder) {
            for (LocalDate helligdag : bevegeligeHelligdager) {
                if (helligdag.isAfter(periode.getFom()) && !helligdag.isAfter(periode.getTom())) {
                    knekkpunkter.add(helligdag);
                }
            }
        }
        return knekkpunkter;
    }

    private static void leggTilKnekkpunkterForFødsel(Set<LocalDate> knekkpunkter, LocalDate familiehendelseDato, Konfigurasjon konfigurasjon) {
        knekkpunkter.add(familiehendelseDato.minusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelseDato)));
        knekkpunkter.add(familiehendelseDato.plusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelseDato)));
    }

    private static void leggTilKnekkpunkter(Set<LocalDate> knekkpunkter, List<? extends Periode> perioder) {
        for (Periode periode : perioder) {
            knekkpunkter.add(periode.getFom());
            knekkpunkter.add(periode.getTom().plusDays(1));
        }
    }
}
