package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAdopsjonsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAlleBarnErDøde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBehandlingKreverSammenhengendeUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBerørtBehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDetErAdopsjonAvStebarn;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErGradertFørSøknadMottattdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmForeldreansvarsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFødselsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmHvisOverlapperSåSamtykkeMellomParter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmKontoErOpprettet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmManglendeSøktPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOpphørsdatoTrefferPerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOpptjeningsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFedrekvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFellesperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErMødrekvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeUavklartUtenomNoenTyper;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterMaksgrenseForUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterNyStønadsperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErFørGyldigDato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPleiepenger;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSamtidigUttak;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
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

    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();
    private Konfigurasjon konfigurasjon;
    private FeatureToggles featureToggles; //NOSONAR

    public FastsettePeriodeRegel(Konfigurasjon konfigurasjon, FeatureToggles featureToggles) {
        this.konfigurasjon = konfigurasjon;
        this.featureToggles = featureToggles;
    }

    public FastsettePeriodeRegel() {
        // For dokumentasjonsgenerering
    }

    @Override
    public Evaluation evaluer(FastsettePeriodeGrunnlag grunnlag) {
        return getSpecification().evaluate(grunnlag);
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmPeriodeErUtsettelse.ID, SjekkOmPeriodeErUtsettelse.BESKRIVELSE)
                .hvis(new SjekkOmPeriodeErUtsettelse(), sjekkPeriodeInnenforMaksgrense())
                .ellers(sjekkOmPeriodenErFørGyldigDato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenErFørGyldigDato() {
        return rs.hvisRegel(SjekkOmPeriodenErFørGyldigDato.ID, "Er uttaksperiode før \"gyldig dato\"?")
                .hvis(new SjekkOmPeriodenErFørGyldigDato(), sjekkOmManglendePeriode())
                .ellers(sjekkPeriodeInnenforMaksgrense());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaksperiodenEtterSøkersDødsdato() {
        return rs.hvisRegel(SjekkOmUttaksperiodenEtterSøkersDødsdato.ID, "Er uttaksperioden etter søkers dødsdato?")
                .hvis(new SjekkOmUttaksperiodenEtterSøkersDødsdato(),
                        Manuellbehandling.opprett("UT1275", IkkeOppfyltÅrsak.SØKER_DØD, Manuellbehandlingårsak.DØDSFALL, false, false))
                .ellers(sjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato() {
        return rs.hvisRegel(SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato.ID,
                "Er uttaksperioden etter senere enn 6 uker etter barns dødsdato?")
                .hvis(new SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato(konfigurasjon), sjekkOmAlleBarnErDøde())
                .ellers(sjekkOmOpphørsdatoTrefferPerioden());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAlleBarnErDøde() {
        return rs.hvisRegel(SjekkOmAlleBarnErDøde.ID, "Er alle barn døde?")
                .hvis(new SjekkOmAlleBarnErDøde(),
                        Manuellbehandling.opprett("UT1289", IkkeOppfyltÅrsak.BARN_DØD, Manuellbehandlingårsak.DØDSFALL, false, false))
                .ellers(sjekkOmOpphørsdatoTrefferPerioden());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOpphørsdatoTrefferPerioden() {
        return rs.hvisRegel(SjekkOmOpphørsdatoTrefferPerioden.ID, "Inneholder perioden opphørsdato for medlemskap")
                .hvis(new SjekkOmOpphørsdatoTrefferPerioden(),
                        IkkeOppfylt.opprett("UT1250", IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM, false, false))
                .ellers(sjekkOmFødselsvilkåretErOppfylt());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse() {
        return rs.hvisRegel(SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse.ID,
                "Sammenfaller uttaksperioden med en periode hos den andre parten som er en innvilget utsettelse?")
                .hvis(new SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse(), sjekkOmBehandlingKreverSammenhengendeUttak())
                .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBehandlingKreverSammenhengendeUttak() {
        return rs.hvisRegel(SjekkOmBehandlingKreverSammenhengendeUttak.ID, SjekkOmBehandlingKreverSammenhengendeUttak.BESKRIVELSE)
                .hvis(new SjekkOmBehandlingKreverSammenhengendeUttak(), ikkeOppfyltUT1166())
                .ellers(sjekkOmFødselTermin());
    }

    private FastsettePeriodeUtfall ikkeOppfyltUT1166() {
        return IkkeOppfylt.opprett("UT1166", IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE, false, false);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFødselTermin() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
                .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmTidsperiodeForbeholdtMor())
                .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTidsperiodeForbeholdtMor() {
        return rs.hvisRegel(SjekkOmTidsperiodeForbeholdtMor.ID, SjekkOmTidsperiodeForbeholdtMor.BESKRIVELSE)
                .hvis(new SjekkOmTidsperiodeForbeholdtMor(konfigurasjon), ikkeOppfyltUT1166())
                .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        return rs.hvisRegel(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID,
                "Sammenfaller uttaksperioden med en periode hos den andre parten som har utbetaling > 0?")
                .hvis(new SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad(), sjekkOmSamtidigUttak())
                .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSamtidigUttak() {
        return rs.hvisRegel(SjekkOmSamtidigUttak.ID, "Har en av foreldrene huket av for samtidig uttak?")
                .hvis(new SjekkOmSamtidigUttak(),
                        Manuellbehandling.opprett("UT1164", null, Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK, true, false))
                .ellers(IkkeOppfylt.opprett("UT1162", IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendePeriode() {
        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
                .hvis(new SjekkOmManglendeSøktPeriode(), new ManglendeSøktPeriodeDelregel(konfigurasjon).getSpecification())
                .ellers(sjekkOmSøknadGjelderTerminFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøknadGjelderTerminFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
                .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmPeriodeErForTidlig())
                .ellers(sjekkOmAdopsjonPeriodeErForTidlig());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAdopsjonPeriodeErForTidlig() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, SjekkOmPeriodenStarterFørFamiliehendelse.BESKRIVELSE)
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                        IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false))
                .ellers(sjekkOmTomPåKontoVedSøktPeriode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForTidlig() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.ID,
                SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.BESKRIVELSE)
                .hvis(new SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin(konfigurasjon),
                        IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false))
                .ellers(sjekkOmTomPåKontoVedSøktPeriode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTomPåKontoVedSøktPeriode() {
        return rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                .hvis(new SjekkOmTomForAlleSineKontoer(),
                        IkkeOppfylt.opprett("UT1081", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false))
                .ellers(IkkeOppfylt.opprett("UT1082", IkkeOppfyltÅrsak.SØKNADSFRIST, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkPeriodeInnenforMaksgrense() {
        return rs.hvisRegel(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID,
                "Er hele perioden innenfor maksimalgrense for foreldrepenger?")
                .hvis(new SjekkOmPeriodenErEtterMaksgrenseForUttak(konfigurasjon),
                        IkkeOppfylt.opprett("UT1085", IkkeOppfyltÅrsak.UTTAK_ETTER_3_ÅRSGRENSE, false, false))
                .ellers(sjekkOmPeriodeEtterNyStønadsperiode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeEtterNyStønadsperiode() {
        return rs.hvisRegel(SjekkOmPeriodenErEtterNyStønadsperiode.ID, "Er uttaksperioden etter start av ny stønadsperiode?")
            .hvis(new SjekkOmPeriodenErEtterNyStønadsperiode(),
                IkkeOppfylt.opprett("UT1086", IkkeOppfyltÅrsak.UTTAK_ETTER_NY_STØNADSPERIODE, false, false))
            .ellers(sjekkOmUttaksperiodenEtterSøkersDødsdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFødselsvilkåretErOppfylt() {
        return rs.hvisRegel(SjekkOmFødselsvilkåretErOppfylt.ID, "Er fødselsvilkåret oppfylt?")
                .hvis(new SjekkOmFødselsvilkåretErOppfylt(), sjekkOmAdopsjonsvilkåretErOppfylt())
                .ellers(IkkeOppfylt.opprett("UT1251", IkkeOppfyltÅrsak.FØDSELSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAdopsjonsvilkåretErOppfylt() {
        return rs.hvisRegel(SjekkOmAdopsjonsvilkåretErOppfylt.ID, "Er adopsjonsvilkåret oppfylt?")
                .hvis(new SjekkOmAdopsjonsvilkåretErOppfylt(), sjekkOmForeldreansvarsvilkåretErOppfylt())
                .ellers(IkkeOppfylt.opprett("UT1252", IkkeOppfyltÅrsak.ADOPSJONSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmForeldreansvarsvilkåretErOppfylt() {
        return rs.hvisRegel(SjekkOmForeldreansvarsvilkåretErOppfylt.ID, "Er foreldreansvarsvilkåret oppfylt?")
                .hvis(new SjekkOmForeldreansvarsvilkåretErOppfylt(), sjekkOmOpptjeningsvilkåretErOppfylt())
                .ellers(IkkeOppfylt.opprett("UT1253", IkkeOppfyltÅrsak.FORELDREANSVARSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOpptjeningsvilkåretErOppfylt() {
        return rs.hvisRegel(SjekkOmOpptjeningsvilkåretErOppfylt.ID, "Er opptjeningsvilkåret oppfylt?")
                .hvis(new SjekkOmOpptjeningsvilkåretErOppfylt(), sjekkOmPleiepenger())
                .ellers(IkkeOppfylt.opprett("UT1254", IkkeOppfyltÅrsak.OPPTJENINGSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPleiepenger() {
        return rs.hvisRegel(SjekkOmPleiepenger.ID, SjekkOmPleiepenger.BESKRIVELSE)
                .hvis(new SjekkOmPleiepenger(), sjekkOmBarnetsInnleggelse())
                .ellers(sjekkOmSamtykke());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBarnetsInnleggelse() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaBarnetsInnleggelse.ID, SjekkOmUtsettelsePgaBarnetsInnleggelse.BESKRIVELSE)
                .hvis(new SjekkOmUtsettelsePgaBarnetsInnleggelse(), sjekkOmSamtykke())
                .ellers(sjekkOmBarnInnlagt());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBarnInnlagt() {
        return rs.hvisRegel(SjekkOmBarnInnlagt.ID, SjekkOmBarnInnlagt.BESKRIVELSE)
                .hvis(new SjekkOmBarnInnlagt(), Manuellbehandling.opprett("UT1320", null, Manuellbehandlingårsak.OVERLAPPENDE_PLEIEPENGER_MED_INNLEGGELSE,
                        false, false))
                .ellers(Manuellbehandling.opprett("UT1321", null, Manuellbehandlingårsak.OVERLAPPENDE_PLEIEPENGER_UTEN_INNLEGGELSE,
                        false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSamtykke() {
        return rs.hvisRegel(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID, "Er det samtykke og overlappende periode?")
                .hvis(new SjekkOmHvisOverlapperSåSamtykkeMellomParter(), sjekkOmBerørtBehandling())
                .ellers(IkkeOppfylt.opprett("UT1063", IkkeOppfyltÅrsak.IKKE_SAMTYKKE, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBerørtBehandling() {
        return rs.hvisRegel(SjekkOmBerørtBehandling.ID, SjekkOmBerørtBehandling.BESKRIVELSE)
                .hvis(new SjekkOmBerørtBehandling(), sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse())
                .ellers(sjekkOmTapendePeriode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTapendePeriode() {
        return rs.hvisRegel(SjekkOmTapendePeriode.ID, SjekkOmTapendePeriode.BESKRIVELSE)
                .hvis(new SjekkOmTapendePeriode(), sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse())
                .ellers(sjekkOmGradertEtterSøknadMottattdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGradertEtterSøknadMottattdato() {
        return rs.hvisRegel(SjekkOmErGradertFørSøknadMottattdato.ID, "Er perioden gradert etter mottattdato?")
                .hvis(new SjekkOmErGradertFørSøknadMottattdato(),
                        Manuellbehandling.opprett("UT1165", null, Manuellbehandlingårsak.SØKNADSFRIST, true, false,
                                Optional.of(GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_SEN_SØKNAD)))
                .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErUtsettelse() {
        return rs.hvisRegel(SjekkOmPeriodeErUtsettelse.ID, SjekkOmPeriodeErUtsettelse.BESKRIVELSE)
                .hvis(new SjekkOmPeriodeErUtsettelse(), kreverBehandlingenSammenhengendeUttak())
                .ellers(sjekkOmManglendeSøktPeriode());
    }

    private Specification<FastsettePeriodeGrunnlag> kreverBehandlingenSammenhengendeUttak() {
        return rs.hvisRegel(SjekkOmBehandlingKreverSammenhengendeUttak.ID, SjekkOmBehandlingKreverSammenhengendeUttak.BESKRIVELSE)
                .hvis(new SjekkOmBehandlingKreverSammenhengendeUttak(), new UtsettelseDelregelSammenhengendeUttak(konfigurasjon).getSpecification())
                .ellers(new UtsettelseDelregel(konfigurasjon).getSpecification());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendeSøktPeriode() {
        var sjekkOmPeriodeUavklart = rs.hvisRegel(SjekkOmPeriodeUavklartUtenomNoenTyper.ID, "Er uttaksperioden uavklart?")
                .hvis(new SjekkOmPeriodeUavklartUtenomNoenTyper(konfigurasjon),
                        Manuellbehandling.opprett("UT1148", null, Manuellbehandlingårsak.PERIODE_UAVKLART, true, false))
                .ellers(sjekkOmSøktGradering());

        var sjekkOmSøktOverføringAvKvoteNode = rs.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Er det søkt om overføring av kvote")
                .hvis(new SjekkOmSøktOmOverføringAvKvote(), sjekkOmPeriodeUavklart)
                .ellers(IkkeOppfylt.opprett("UT1160", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));

        var sjekkOmTomForAlleSineKontoerNode = rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                .hvis(new SjekkOmTomForAlleSineKontoer(), sjekkOmSøktOverføringAvKvoteNode)
                .ellers(sjekkOmPeriodeUavklart);

        var sjekkOmForeldrepengerFørFødselNode = rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
                .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmPeriodeUavklart)
                .ellers(sjekkOmTomForAlleSineKontoerNode);

        var sjekkKontoErOpprettet = rs.hvisRegel(SjekkOmKontoErOpprettet.ID,
                "Er det opprettet stønadskonto som tilsvarer stønadskonto i uttaksperioden?")
                .hvis(new SjekkOmKontoErOpprettet(), sjekkOmForeldrepengerFørFødselNode)
                .ellers(Manuellbehandling.opprett("UT1290", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                        Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));

        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
                .hvis(new SjekkOmManglendeSøktPeriode(), new ManglendeSøktPeriodeDelregel(konfigurasjon).getSpecification())
                .ellers(sjekkKontoErOpprettet);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering() {
        return rs.hvisRegel(SjekkOmSøktGradering.ID, "Er det søkt om gradering?")
                .hvis(new SjekkOmSøktGradering(), sjekkOmSøktGradering100ProsentEllerMer())
                .ellers(sjekkOmPeriodeErStebarnsadopsjon());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering100ProsentEllerMer() {
        return rs.hvisRegel(SjekkOmSøktGraderingHundreProsentEllerMer.ID, "Er søkt arbeid 100 prosent eller mer i perioden?")
                .hvis(new SjekkOmSøktGraderingHundreProsentEllerMer(),
                        Manuellbehandling.opprett("UT1180", IkkeOppfyltÅrsak.ARBEID_HUNDRE_PROSENT_ELLER_MER,
                                Manuellbehandlingårsak.AVKLAR_ARBEID, true, false))
                .ellers(sjekkOmPeriodeErStebarnsadopsjon());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErStebarnsadopsjon() {
        return rs.hvisRegel(SjekkOmDetErAdopsjonAvStebarn.ID, "Er det adopsjon av stebarn?")
                .hvis(new SjekkOmDetErAdopsjonAvStebarn(), new StebarnsadopsjonDelRegel().getSpecification())
                .ellers(sjekkOmPeriodeErMødrekvote());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErMødrekvote() {
        return rs.hvisRegel(SjekkOmPeriodeErMødrekvote.ID, "Er det søkt om uttak av mødrekvote?")
                .hvis(new SjekkOmPeriodeErMødrekvote(), new MødrekvoteDelregel(konfigurasjon).getSpecification())
                .ellers(sjekkOmFedrekvote());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFedrekvote() {
        return rs.hvisRegel(SjekkOmPeriodeErFedrekvote.ID, "Er det søkt om uttak av fedrekvote?")
                .hvis(new SjekkOmPeriodeErFedrekvote(), new FedrekvoteDelregel(konfigurasjon).getSpecification())
                .ellers(sjekkOmFellesperiode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFellesperiode() {
        return rs.hvisRegel(SjekkOmPeriodeErFellesperiode.ID, "Er det søkt om uttak av fellesperiode?")
                .hvis(new SjekkOmPeriodeErFellesperiode(), new FellesperiodeDelregel(konfigurasjon).getSpecification())
                .ellers(sjekkOmPeriodeErForeldrepengerFørFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
                .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmFPFFGjelderFødsel())
                .ellers(new ForeldrepengerDelregel(konfigurasjon).getSpecification());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFPFFGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, GJELDER_FPFF_PERIODE_FØDSEL)
                .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(),
                        new ForeldrepengerFørFødselDelregel(konfigurasjon).getSpecification())
                .ellers(Manuellbehandling.opprett("UT1092", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }
}
