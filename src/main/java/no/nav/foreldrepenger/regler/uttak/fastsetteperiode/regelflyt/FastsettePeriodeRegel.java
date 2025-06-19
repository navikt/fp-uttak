package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import static no.nav.fpsak.nare.specification.NotSpecification.ikke;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAdopsjonsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAkseptertSamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAlleBarnErDøde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBerørtBehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDetErAdopsjonAvStebarn;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmEtterNesteStønadsperiodeHarDisponibleDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFarHarDagerRundtFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmForeldreansvarsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFødselsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmHvisOverlapperSåSamtykkeMellomParter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmKontoErOpprettet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmManglendeSøktPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmMedlemskapssvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOpphørsdatoTrefferPerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOpptjeningsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverlapperMedAnnenPartEøs;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFedrekvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFellesperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErMødrekvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterMaksgrenseForUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterNesteStønadsperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErFørGyldigDato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenKreverSammenhengendeUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPleiepenger;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderTerminEllerFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktGradering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktGraderingHundreProsentEllerMer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktOmOverføringAvKvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTapendePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTidsperiodeForbeholdtMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaBarnetsInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaksperiodenEtterSøkersDødsdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Regeltjeneste som fastsetter uttaksperioder som er søkt om for foreldrepenger.
 */
@RuleDocumentation(value = FastsettePeriodeRegel.ID)
public class FastsettePeriodeRegel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 14";
    private static final String GJELDER_FPFF_PERIODE_FØDSEL = "Gjelder foreldrepenger før fødsel periode fødsel?";
    private static final String ER_PERIODEN_FPFF = "Er det søkt om uttak av foreldrepenger før fødsel?";

    private static final Ruleset<FastsettePeriodeGrunnlag> RS = new Ruleset<>();
    private static final Specification<FastsettePeriodeGrunnlag> MSP_DELREGEL = new ManglendeSøktPeriodeDelregel().getSpecification();
    private static final Specification<FastsettePeriodeGrunnlag> REGEL = lagRegelSpec();
    public static final String ELLER = " eller";

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmTidsperiodeMorBruddSøknadsfrist;
    private static Specification<FastsettePeriodeGrunnlag> fomUttaksperiodenEtterSøkersDødsdato;
    private static Specification<FastsettePeriodeGrunnlag> fomOpphørsdatoTrefferPerioden;
    private static Specification<FastsettePeriodeGrunnlag> fomSamtykke;
    private static Specification<FastsettePeriodeGrunnlag> sjekkOmOverlapperMedAnnenPartEøs;
    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse;
    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad;
    private static Specification<FastsettePeriodeGrunnlag> sjekkPeriodeInnenforMaksgrense;
    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErUtsettelse;
    private static Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering;
    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErStebarnsadopsjon;

    public FastsettePeriodeRegel() {
        // For dokumentasjonsgenerering
    }

    @Override
    public Evaluation evaluer(FastsettePeriodeGrunnlag grunnlag) {
        return getSpecification().evaluate(grunnlag);
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return REGEL;
    }

    private static Specification<FastsettePeriodeGrunnlag> lagRegelSpec() {
        return RS.hvisRegel(SjekkOmPeriodeErUtsettelse.ID, SjekkOmPeriodeErUtsettelse.BESKRIVELSE)
            .hvis(new SjekkOmPeriodeErUtsettelse(), sjekkPeriodeInnenforMaksgrense())
            .ellers(sjekkOmPeriodenErFørGyldigDato());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenErFørGyldigDato() {
        return RS.hvisRegel(SjekkOmPeriodenErFørGyldigDato.ID, "Er uttaksperiode før \"gyldig dato\"?")
            .hvis(new SjekkOmPeriodenErFørGyldigDato(), sjekkOmManglendePeriode())
            .ellers(sjekkPeriodeInnenforMaksgrense());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmUttaksperiodenEtterSøkersDødsdato() {
        if (fomUttaksperiodenEtterSøkersDødsdato == null) {
            fomUttaksperiodenEtterSøkersDødsdato = RS.hvisRegel(SjekkOmUttaksperiodenEtterSøkersDødsdato.ID,
                    "Er uttaksperioden etter søkers dødsdato?")
                .hvis(new SjekkOmUttaksperiodenEtterSøkersDødsdato(),
                    Manuellbehandling.opprett("UT1275", IkkeOppfyltÅrsak.SØKER_DØD, Manuellbehandlingårsak.DØDSFALL, false, false))
                .ellers(sjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato());
        }
        return fomUttaksperiodenEtterSøkersDødsdato;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato() {
        return RS.hvisRegel(SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato.ID, "Er uttaksperioden etter senere enn 6 uker etter barns dødsdato?")
            .hvis(new SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato(), sjekkOmAlleBarnErDøde())
            .ellers(sjekkOmOpphørsdatoTrefferPerioden());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAlleBarnErDøde() {
        return RS.hvisRegel(SjekkOmAlleBarnErDøde.ID, "Er alle barn døde?")
            .hvis(new SjekkOmAlleBarnErDøde(),
                Manuellbehandling.opprett("UT1289", IkkeOppfyltÅrsak.BARN_DØD, Manuellbehandlingårsak.DØDSFALL, false, false))
            .ellers(sjekkOmOpphørsdatoTrefferPerioden());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmOpphørsdatoTrefferPerioden() {
        if (fomOpphørsdatoTrefferPerioden == null) {
            fomOpphørsdatoTrefferPerioden = RS.hvisRegel(SjekkOmOpphørsdatoTrefferPerioden.ID, "Inneholder perioden opphørsdato for medlemskap")
                .hvis(new SjekkOmOpphørsdatoTrefferPerioden(), IkkeOppfylt.opprett("UT1250", IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM, false, false))
                .ellers(sjekkOmFødselsvilkåretErOppfylt());
        }
        return fomOpphørsdatoTrefferPerioden;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse() {
        if (sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse == null) {
            sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse = RS.hvisRegel(SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse.ID,
                    "Sammenfaller uttaksperioden med en periode hos den andre parten som er en innvilget utsettelse?")
                .hvis(new SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse(), sjekkOmBehandlingKreverSammenhengendeUttak())
                .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
        }
        return sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmBehandlingKreverSammenhengendeUttak() {
        return RS.hvisRegel(SjekkOmPeriodenKreverSammenhengendeUttak.ID, SjekkOmPeriodenKreverSammenhengendeUttak.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenKreverSammenhengendeUttak(), ikkeOppfyltUT1166())
            .ellers(sjekkOmFødselTermin());
    }

    private static FastsettePeriodeUtfall ikkeOppfyltUT1166() {
        return IkkeOppfylt.opprett("UT1166", IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE, false, false);
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmFødselTermin() {
        return RS.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmTidsperiodeForbeholdtMor())
            .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmTidsperiodeForbeholdtMor() {
        return RS.hvisRegel(SjekkOmTidsperiodeForbeholdtMor.ID, SjekkOmTidsperiodeForbeholdtMor.BESKRIVELSE)
            .hvis(new SjekkOmTidsperiodeForbeholdtMor().og(ikke(new SjekkOmFarHarDagerRundtFødsel())), ikkeOppfyltUT1166())
            .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        if (sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad == null) {
            sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad = RS.hvisRegel(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID,
                    "Sammenfaller uttaksperioden med en periode hos den andre parten som har utbetaling > 0?")
                .hvis(new SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad(), sjekkOmSamtidigUttak())
                .ellers(sjekkOmPeriodeErUtsettelse());
        }
        return sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAkseptertSamtidigUttak() {
        return RS.hvisRegel(SjekkOmAkseptertSamtidigUttak.ID, "Samtidig uttak er akseptert?")
            .hvis(new SjekkOmAkseptertSamtidigUttak(), sjekkOmPeriodeErUtsettelse())
            .ellers(Manuellbehandling.opprett("UT1164", null, Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK, true, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmSamtidigUttak() {
        return RS.hvisRegel(SjekkOmSamtidigUttak.ID, "Har en av foreldrene huket av for samtidig uttak?")
            .hvis(new SjekkOmSamtidigUttak(), sjekkOmAkseptertSamtidigUttak())
            .ellers(IkkeOppfylt.opprett("UT1162", IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmManglendePeriode() {
        return RS.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
            .hvis(new SjekkOmManglendeSøktPeriode(), MSP_DELREGEL)
            .ellers(sjekkOmSøknadGjelderTerminFødsel());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmSøknadGjelderTerminFødsel() {
        return RS.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmPeriodeErForTidlig())
            .ellers(sjekkOmAdopsjonPeriodeErForTidlig());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAdopsjonPeriodeErForTidlig() {
        return RS.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, SjekkOmPeriodenStarterFørFamiliehendelse.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false))
            .ellers(sjekkOmTidsperiodeMorBruddSøknadsfrist());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForTidlig() {
        return RS.hvisRegel(SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.ID, SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin(),
                IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false))
            .ellers(sjekkOmTidsperiodeMorBruddSøknadsfrist());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmTidsperiodeMorBruddSøknadsfrist() {
        if (sjekkOmTidsperiodeMorBruddSøknadsfrist == null) {
            var sjekkOmTomForAlleSineKontoer = RS.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                .hvis(new SjekkOmTomForAlleSineKontoer(), IkkeOppfylt.opprett("UT1081", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false))
                .ellers(IkkeOppfylt.opprett("UT1082", IkkeOppfyltÅrsak.SØKNADSFRIST, true, false));
            var avslagUtenTrekkdagerUT1080 = IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false);
            var sjekkOmKreverSammenhengendeUttak = RS.hvisRegel(SjekkOmPeriodenKreverSammenhengendeUttak.ID + ELLER + SjekkOmBareFarHarRett.ID,
                    SjekkOmPeriodenKreverSammenhengendeUttak.BESKRIVELSE + ELLER + SjekkOmBareFarHarRett.BESKRIVELSE)
                .hvis(new SjekkOmPeriodenKreverSammenhengendeUttak().eller(new SjekkOmBareFarHarRett()), sjekkOmTomForAlleSineKontoer)
                .ellers(avslagUtenTrekkdagerUT1080);
            var sjekkOmMor = RS.hvisRegel(SjekkOmSøkerErMor.ID, SjekkOmSøkerErMor.BESKRIVELSE)
                .hvis(new SjekkOmSøkerErMor(), sjekkOmTomForAlleSineKontoer)
                .ellers(avslagUtenTrekkdagerUT1080);
            sjekkOmTidsperiodeMorBruddSøknadsfrist = RS.hvisRegel(SjekkOmTidsperiodeForbeholdtMor.ID, SjekkOmTidsperiodeForbeholdtMor.BESKRIVELSE)
                .hvis(new SjekkOmTidsperiodeForbeholdtMor(), sjekkOmMor)
                .ellers(sjekkOmKreverSammenhengendeUttak);
        }
        return sjekkOmTidsperiodeMorBruddSøknadsfrist;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkPeriodeInnenforMaksgrense() {
        if (sjekkPeriodeInnenforMaksgrense == null) {
            sjekkPeriodeInnenforMaksgrense = RS.hvisRegel(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID, SjekkOmPeriodenErEtterMaksgrenseForUttak.BESKRIVELSE)
                .hvis(new SjekkOmPeriodenErEtterMaksgrenseForUttak(),
                    IkkeOppfylt.opprett("UT1085", IkkeOppfyltÅrsak.UTTAK_ETTER_3_ÅRSGRENSE, false, false))
                .ellers(sjekkOmPeriodeEtterNesteStønadsperiode());
        }
        return sjekkPeriodeInnenforMaksgrense;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeEtterNesteStønadsperiode() {
        return RS.hvisRegel(SjekkOmPeriodenErEtterNesteStønadsperiode.ID, SjekkOmPeriodenErEtterNesteStønadsperiode.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenErEtterNesteStønadsperiode(), sjekkOmGjenståendeDagerEtterNesteStønadsperiode())
            .ellers(sjekkOmUttaksperiodenEtterSøkersDødsdato());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmGjenståendeDagerEtterNesteStønadsperiode() {
        return RS.hvisRegel(SjekkOmEtterNesteStønadsperiodeHarDisponibleDager.ID, SjekkOmEtterNesteStønadsperiodeHarDisponibleDager.BESKRIVELSE)
            .hvis(new SjekkOmEtterNesteStønadsperiodeHarDisponibleDager(), sjekkOmUttaksperiodenEtterSøkersDødsdato())
            .ellers(IkkeOppfylt.opprett("UT1086", IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE, false, false));
    }


    private static Specification<FastsettePeriodeGrunnlag> sjekkOmFødselsvilkåretErOppfylt() {
        return RS.hvisRegel(SjekkOmFødselsvilkåretErOppfylt.ID, "Er fødselsvilkåret oppfylt?")
            .hvis(new SjekkOmFødselsvilkåretErOppfylt(), sjekkOmAdopsjonsvilkåretErOppfylt())
            .ellers(IkkeOppfylt.opprett("UT1251", IkkeOppfyltÅrsak.FØDSELSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmAdopsjonsvilkåretErOppfylt() {
        return RS.hvisRegel(SjekkOmAdopsjonsvilkåretErOppfylt.ID, "Er adopsjonsvilkåret oppfylt?")
            .hvis(new SjekkOmAdopsjonsvilkåretErOppfylt(), sjekkOmForeldreansvarsvilkåretErOppfylt())
            .ellers(IkkeOppfylt.opprett("UT1252", IkkeOppfyltÅrsak.ADOPSJONSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmForeldreansvarsvilkåretErOppfylt() {
        return RS.hvisRegel(SjekkOmForeldreansvarsvilkåretErOppfylt.ID, "Er foreldreansvarsvilkåret oppfylt?")
            .hvis(new SjekkOmForeldreansvarsvilkåretErOppfylt(), sjekkOmMedlemskapsvilkåretErOppfylt())
            .ellers(IkkeOppfylt.opprett("UT1253", IkkeOppfyltÅrsak.FORELDREANSVARSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmMedlemskapsvilkåretErOppfylt() {
        return RS.hvisRegel(SjekkOmMedlemskapssvilkåretErOppfylt.ID, "Er medlemskapsvilkåret oppfylt?")
            .hvis(new SjekkOmMedlemskapssvilkåretErOppfylt(), sjekkOmOpptjeningsvilkåretErOppfylt())
            .ellers(IkkeOppfylt.opprett("UT1259", IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmOpptjeningsvilkåretErOppfylt() {
        return RS.hvisRegel(SjekkOmOpptjeningsvilkåretErOppfylt.ID, "Er opptjeningsvilkåret oppfylt?")
            .hvis(new SjekkOmOpptjeningsvilkåretErOppfylt(), sjekkOmPleiepenger())
            .ellers(IkkeOppfylt.opprett("UT1254", IkkeOppfyltÅrsak.OPPTJENINGSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPleiepenger() {
        return RS.hvisRegel(SjekkOmPleiepenger.ID, SjekkOmPleiepenger.BESKRIVELSE)
            .hvis(new SjekkOmPleiepenger(), sjekkOmBarnetsInnleggelse())
            .ellers(sjekkOmOverlappMedAnnenPartEøs());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmBarnetsInnleggelse() {
        return RS.hvisRegel(SjekkOmUtsettelsePgaBarnetsInnleggelse.ID, SjekkOmUtsettelsePgaBarnetsInnleggelse.BESKRIVELSE)
            .hvis(new SjekkOmUtsettelsePgaBarnetsInnleggelse(), sjekkOmOverlappMedAnnenPartEøs())
            .ellers(sjekkOmBarnInnlagt());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmBarnInnlagt() {
        return RS.hvisRegel(SjekkOmBarnInnlagt.ID, SjekkOmBarnInnlagt.BESKRIVELSE)
            .hvis(new SjekkOmBarnInnlagt(),
                Manuellbehandling.opprett("UT1320", null, Manuellbehandlingårsak.OVERLAPPENDE_PLEIEPENGER_MED_INNLEGGELSE, false, false))
            .ellers(Manuellbehandling.opprett("UT1321", null, Manuellbehandlingårsak.OVERLAPPENDE_PLEIEPENGER_UTEN_INNLEGGELSE, false, false));
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmOverlappMedAnnenPartEøs() {
        if (sjekkOmOverlapperMedAnnenPartEøs == null) {
            sjekkOmOverlapperMedAnnenPartEøs = RS.hvisRegel(SjekkOmOverlapperMedAnnenPartEøs.ID, SjekkOmOverlapperMedAnnenPartEøs.BESKRIVELSE)
                .hvis(new SjekkOmOverlapperMedAnnenPartEøs(),
                    Manuellbehandling.opprett("UT1332", IkkeOppfyltÅrsak.ANNEN_FORELDER_UTTAK_EØS, Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK,
                        false, false))
                .ellers(sjekkOmSamtykke());
        }
        return sjekkOmOverlapperMedAnnenPartEøs;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmSamtykke() {
        if (fomSamtykke == null) {
            fomSamtykke = RS.hvisRegel(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID, "Er det samtykke og overlappende periode?")
                .hvis(new SjekkOmHvisOverlapperSåSamtykkeMellomParter(), sjekkOmBerørtBehandling())
                .ellers(Manuellbehandling.opprett("UT1063", IkkeOppfyltÅrsak.IKKE_SAMTYKKE, Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK, false, false));
        }
        return fomSamtykke;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmBerørtBehandling() {
        return RS.hvisRegel(SjekkOmBerørtBehandling.ID, SjekkOmBerørtBehandling.BESKRIVELSE)
            .hvis(new SjekkOmBerørtBehandling(), sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse())
            .ellers(sjekkOmTapendePeriode());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmTapendePeriode() {
        return RS.hvisRegel(SjekkOmTapendePeriode.ID, SjekkOmTapendePeriode.BESKRIVELSE)
            .hvis(new SjekkOmTapendePeriode(), sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse())
            .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErUtsettelse() {
        if (sjekkOmPeriodeErUtsettelse == null) {
            sjekkOmPeriodeErUtsettelse = RS.hvisRegel(SjekkOmPeriodeErUtsettelse.ID, SjekkOmPeriodeErUtsettelse.BESKRIVELSE)
                .hvis(new SjekkOmPeriodeErUtsettelse(), kreverBehandlingenSammenhengendeUttak())
                .ellers(sjekkOmManglendeSøktPeriode());
        }
        return sjekkOmPeriodeErUtsettelse;
    }

    private static Specification<FastsettePeriodeGrunnlag> kreverBehandlingenSammenhengendeUttak() {
        return RS.hvisRegel(SjekkOmPeriodenKreverSammenhengendeUttak.ID, SjekkOmPeriodenKreverSammenhengendeUttak.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenKreverSammenhengendeUttak(), new UtsettelseDelregelSammenhengendeUttak().getSpecification())
            .ellers(new UtsettelseDelregel().getSpecification());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmManglendeSøktPeriode() {

        var sjekkOmSøktOverføringAvKvoteNode = RS.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Er det søkt om overføring av kvote")
            .hvis(new SjekkOmSøktOmOverføringAvKvote(), sjekkOmSøktGradering())
            .ellers(IkkeOppfylt.opprett("UT1160", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));

        var sjekkOmTomForAlleSineKontoerNode = RS.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
            .hvis(new SjekkOmTomForAlleSineKontoer(), sjekkOmSøktOverføringAvKvoteNode)
            .ellers(sjekkOmSøktGradering());

        var sjekkOmForeldrepengerFørFødselNode = RS.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
            .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmSøktGradering())
            .ellers(sjekkOmTomForAlleSineKontoerNode);

        var sjekkKontoErOpprettet = RS.hvisRegel(SjekkOmKontoErOpprettet.ID,
                "Er det opprettet stønadskonto som tilsvarer stønadskonto i uttaksperioden?")
            .hvis(new SjekkOmKontoErOpprettet(), sjekkOmForeldrepengerFørFødselNode)
            .ellers(Manuellbehandling.opprett("UT1290", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true,
                false));

        return RS.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
            .hvis(new SjekkOmManglendeSøktPeriode(), MSP_DELREGEL)
            .ellers(sjekkKontoErOpprettet);
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering() {
        if (sjekkOmSøktGradering == null) {
            sjekkOmSøktGradering = RS.hvisRegel(SjekkOmSøktGradering.ID, "Er det søkt om gradering?")
                .hvis(new SjekkOmSøktGradering(), sjekkOmSøktGradering100ProsentEllerMer())
                .ellers(sjekkOmPeriodeErStebarnsadopsjon());
        }
        return sjekkOmSøktGradering;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering100ProsentEllerMer() {
        return RS.hvisRegel(SjekkOmSøktGraderingHundreProsentEllerMer.ID, "Er søkt arbeid 100 prosent eller mer i perioden?")
            .hvis(new SjekkOmSøktGraderingHundreProsentEllerMer(),
                Manuellbehandling.opprett("UT1180", IkkeOppfyltÅrsak.ARBEID_HUNDRE_PROSENT_ELLER_MER, Manuellbehandlingårsak.AVKLAR_ARBEID, true,
                    false))
            .ellers(sjekkOmPeriodeErStebarnsadopsjon());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErStebarnsadopsjon() {
        if (sjekkOmPeriodeErStebarnsadopsjon == null) {
            sjekkOmPeriodeErStebarnsadopsjon = RS.hvisRegel(SjekkOmDetErAdopsjonAvStebarn.ID, "Er det adopsjon av stebarn?")
                .hvis(new SjekkOmDetErAdopsjonAvStebarn(), new StebarnsadopsjonDelRegel().getSpecification())
                .ellers(sjekkOmPeriodeErMødrekvote());
        }
        return sjekkOmPeriodeErStebarnsadopsjon;
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErMødrekvote() {
        return RS.hvisRegel(SjekkOmPeriodeErMødrekvote.ID, "Er det søkt om uttak av mødrekvote?")
            .hvis(new SjekkOmPeriodeErMødrekvote(), new MødrekvoteDelregel().getSpecification())
            .ellers(sjekkOmFedrekvote());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmFedrekvote() {
        return RS.hvisRegel(SjekkOmPeriodeErFedrekvote.ID, "Er det søkt om uttak av fedrekvote?")
            .hvis(new SjekkOmPeriodeErFedrekvote(), new FedrekvoteDelregel().getSpecification())
            .ellers(sjekkOmFellesperiode());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmFellesperiode() {
        return RS.hvisRegel(SjekkOmPeriodeErFellesperiode.ID, "Er det søkt om uttak av fellesperiode?")
            .hvis(new SjekkOmPeriodeErFellesperiode(), new FellesperiodeDelregel().getSpecification())
            .ellers(sjekkOmPeriodeErForeldrepengerFørFødsel());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForeldrepengerFørFødsel() {
        return RS.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
            .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmFPFFGjelderFødsel())
            .ellers(new ForeldrepengerDelregel().getSpecification());
    }

    private static Specification<FastsettePeriodeGrunnlag> sjekkOmFPFFGjelderFødsel() {
        return RS.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, GJELDER_FPFF_PERIODE_FØDSEL)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), new ForeldrepengerFørFødselDelregel().getSpecification())
            .ellers(Manuellbehandling.opprett("UT1092", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }
}
