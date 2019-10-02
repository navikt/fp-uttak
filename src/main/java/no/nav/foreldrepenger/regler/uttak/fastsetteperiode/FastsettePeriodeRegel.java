package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAdopsjonsvilkåretErOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAlleBarnErDøde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDetErAdopsjonAvStebarn;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErGradertFørEndringssøknadMottattdato;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErFørGyldigDato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTapendeBehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadsperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktGradering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktGraderingHundreProsentEllerMer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktOmOverføringAvKvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDagerPåAlleAktivitetene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaketStarterFørLovligUttakFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaksperiodenEtterSøkersDødsdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
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

    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();
    private Konfigurasjon konfigurasjon;
    private FeatureToggles featureToggles = new FeatureToggles() {}; //NOSONAR

    public FastsettePeriodeRegel() {
        // For dokumentasjonsgenerering
    }

    public FastsettePeriodeRegel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    public FastsettePeriodeRegel(Konfigurasjon konfigurasjon, FeatureToggles featureToggles) {
        this(konfigurasjon);
        this.featureToggles = featureToggles;
    }

    @Override
    public Evaluation evaluer(FastsettePeriodeGrunnlag grunnlag) {
        return getSpecification().evaluate(grunnlag);
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøknadsperiode.ID, "Er perioden en søknadsperiode?")
                .hvis(new SjekkOmSøknadsperiode(), sjekkOmPeriodeFørGyldigDato())
                .ellers(sjekkOmUttaksperiodenEtterSøkersDødsdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaksperiodenEtterSøkersDødsdato() {
        return rs.hvisRegel(SjekkOmUttaksperiodenEtterSøkersDødsdato.ID, "Er uttaksperioden etter søkers dødsdato?")
                .hvis(new SjekkOmUttaksperiodenEtterSøkersDødsdato(), Manuellbehandling.opprett("UT1275", IkkeOppfyltÅrsak.SØKER_DØD, Manuellbehandlingårsak.DØDSFALL, false, false))
                .ellers(sjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato() {
        return rs.hvisRegel(SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato.ID, "Er uttaksperioden etter senere enn 6 uker etter barns dødsdato?")
                .hvis(new SjekkOmUttaksperiodenEtter6UkerEtterBarnsDødsdato(konfigurasjon), sjekkOmAlleBarnErDøde())
                .ellers(sjekkOmOpphørsdatoTrefferPerioden());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAlleBarnErDøde() {
        return rs.hvisRegel(SjekkOmAlleBarnErDøde.ID, "Er alle barn døde?")
                .hvis(new SjekkOmAlleBarnErDøde(), Manuellbehandling.opprett("UT1289", IkkeOppfyltÅrsak.BARN_DØD, Manuellbehandlingårsak.DØDSFALL, false, false))
                .ellers(sjekkOmOpphørsdatoTrefferPerioden());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOpphørsdatoTrefferPerioden() {
        return rs.hvisRegel(SjekkOmOpphørsdatoTrefferPerioden.ID, "Inneholder perioden opphørsdato for medlemskap")
                .hvis(new SjekkOmOpphørsdatoTrefferPerioden(), IkkeOppfylt.opprett("UT1250", IkkeOppfyltÅrsak.SØKER_IKKE_MEDLEM, false, false))
                .ellers(sjekkOmFødselsvilkåretErOppfylt());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeFørGyldigDato() {
        return rs.hvisRegel(SjekkOmPeriodenErFørGyldigDato.ID, "Er uttaksperiode før \"gyldig dato\"?")
                .hvis(new SjekkOmPeriodenErFørGyldigDato(), sjekkOmManglendePeriode())
                .ellers(sjekkPeriodeInnenforMaksgrense());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse() {
        return rs.hvisRegel(SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse.ID, "Sammenfaller uttaksperioden med en periode hos den andre parten som er en innvilget utsettelse?")
                .hvis(new SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse(), IkkeOppfylt.opprett("UT1166", IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE, false, false))
                .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        return rs.hvisRegel(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID, "Sammenfaller uttaksperioden med en periode hos den andre parten som har utbetaling > 0?")
                .hvis(new SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad(), sjekkOmSamtidigUttak())
                .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSamtidigUttak() {
        return rs.hvisRegel(SjekkOmSamtidigUttak.ID, "Har en av foreldrene huket av for samtidig uttak?")
                .hvis(new SjekkOmSamtidigUttak(), Manuellbehandling.opprett("UT1164", null, Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK, true, false))
                .ellers(IkkeOppfylt.opprett("UT1162", IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendePeriode() {
        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
                .hvis(new SjekkOmManglendeSøktPeriode(), IkkeOppfylt.opprett("UT1084", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, true, false))
                .ellers(sjekkOmPeriodeErForTidlig());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForTidlig() {
        return rs.hvisRegel(SjekkOmUttaketStarterFørLovligUttakFørFødsel.ID, "Gjelder det periode tidligere enn 12 uker før fødsel/termin?")
                .hvis(new SjekkOmUttaketStarterFørLovligUttakFørFødsel(konfigurasjon), IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false))
                .ellers(sjekkOmNoenDagerIgjen());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmNoenDagerIgjen() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåAlleAktivitetene.ID, SjekkOmTilgjengeligeDagerPåAlleAktivitetene.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDagerPåAlleAktivitetene(), IkkeOppfylt.opprett("UT1082", IkkeOppfyltÅrsak.SØKNADSFRIST, true, false))
                .ellers(IkkeOppfylt.opprett("UT1081", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkPeriodeInnenforMaksgrense() {
        return rs.hvisRegel(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID, "Er hele perioden innenfor maksimalgrense for foreldrepenger?")
                .hvis(new SjekkOmPeriodenErEtterMaksgrenseForUttak(konfigurasjon), IkkeOppfylt.opprett("UT1085", IkkeOppfyltÅrsak.UTTAK_ETTER_3_ÅRSGRENSE, false, false))
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
                .hvis(new SjekkOmOpptjeningsvilkåretErOppfylt(), sjekkOmSamtykke())
                .ellers(IkkeOppfylt.opprett("UT1254", IkkeOppfyltÅrsak.OPPTJENINGSVILKÅRET_IKKE_OPPFYLT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSamtykke() {
        return rs.hvisRegel(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID, "Er det samtykke og overlappende periode?")
                .hvis(new SjekkOmHvisOverlapperSåSamtykkeMellomParter(), sjekkOmTapendeBehandling())
                .ellers(IkkeOppfylt.opprett("UT1063", IkkeOppfyltÅrsak.IKKE_SAMTYKKE, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTapendeBehandling() {
        return rs.hvisRegel(SjekkOmTapendeBehandling.ID, SjekkOmTapendeBehandling.BESKRIVELSE)
                .hvis(new SjekkOmTapendeBehandling(), sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse())
                .ellers(sjekkOmGradertEtterEndringssøknadMottattdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGradertEtterEndringssøknadMottattdato() {
        return rs.hvisRegel(SjekkOmErGradertFørEndringssøknadMottattdato.ID, "Er perioden gradert etter mottattdato?")
                .hvis(new SjekkOmErGradertFørEndringssøknadMottattdato(), Manuellbehandling.opprett("UT1165",
                        null,
                        Manuellbehandlingårsak.SØKNADSFRIST,
                        true, false,
                        Optional.of(GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_SEN_SØKNAD)))
                .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErUtsettelse() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelseFørFamiliehendelse = rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Er utsettelse før familiehendelse?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1151", IkkeOppfyltÅrsak.UTSETTELSE_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false))
                .ellers(new UtsettelseDelregel(konfigurasjon).getSpecification());

        return rs.hvisRegel(SjekkOmPeriodeErUtsettelse.ID, "Er det utsettelse?")
                .hvis(new SjekkOmPeriodeErUtsettelse(), sjekkOmUtsettelseFørFamiliehendelse)
                .ellers(sjekkOmManglendeSøktPeriode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
                .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmFPFFGjelderFødsel())
                .ellers(sjekkOmTomPåAlleSineKonto());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTomPåAlleSineKonto() {
        return rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                .hvis(new SjekkOmTomForAlleSineKontoer(), IkkeOppfylt.opprett("UT1088", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, false, false))
                .ellers(IkkeOppfylt.opprett("UT1087", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendeSøktPeriode() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeUavklart =
                rs.hvisRegel(SjekkOmPeriodeUavklartUtenomNoenTyper.ID, "Er uttaksperioden uavklart?")
                        .hvis(new SjekkOmPeriodeUavklartUtenomNoenTyper(konfigurasjon), Manuellbehandling.opprett("UT1148", null, Manuellbehandlingårsak.PERIODE_UAVKLART, true, false))
                        .ellers(sjekkOmSøktGradering());

        Specification<FastsettePeriodeGrunnlag> sjekkOmSøktOverføringAvKvoteNode =
                rs.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Er det søkt om overføring av kvote")
                        .hvis(new SjekkOmSøktOmOverføringAvKvote(), Manuellbehandling.opprett("UT1161", null, Manuellbehandlingårsak.VURDER_OVERFØRING, true, false))
                        .ellers(IkkeOppfylt.opprett("UT1160", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmTomForAlleSineKontoerNode =
                rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                        .hvis(new SjekkOmTomForAlleSineKontoer(), sjekkOmSøktOverføringAvKvoteNode)
                        .ellers(sjekkOmPeriodeUavklart);

        Specification<FastsettePeriodeGrunnlag> sjekkOmForeldrepengerFørFødselNode =
                rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
                        .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmPeriodeUavklart)
                        .ellers(sjekkOmTomForAlleSineKontoerNode);

        Specification<FastsettePeriodeGrunnlag> sjekkKontoErOpprettet = rs.hvisRegel(SjekkOmKontoErOpprettet.ID, "Er det opprettet stønadskonto som tilsvarer stønadskonto i uttaksperioden?")
                .hvis(new SjekkOmKontoErOpprettet(), sjekkOmForeldrepengerFørFødselNode)
                .ellers(Manuellbehandling.opprett("UT1290", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));

        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
                .hvis(new SjekkOmManglendeSøktPeriode(), sjekkOmForeldrepengerFørFødsel())
                .ellers(sjekkKontoErOpprettet);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering() {
        return rs.hvisRegel(SjekkOmSøktGradering.ID, "Er det søkt om gradering?")
                .hvis(new SjekkOmSøktGradering(), sjekkOmSøktGradering100ProsentEllerMer())
                .ellers(sjekkOmPeriodeErStebarnsadopsjon());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering100ProsentEllerMer() {
        return rs.hvisRegel(SjekkOmSøktGraderingHundreProsentEllerMer.ID, "Er søkt arbeid 100 prosent eller mer i perioden?")
                .hvis(new SjekkOmSøktGraderingHundreProsentEllerMer(), Manuellbehandling.opprett("UT1180", IkkeOppfyltÅrsak.ARBEID_HUNDRE_PROSENT_ELLER_MER, Manuellbehandlingårsak.AVKLAR_ARBEID, true, false))
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
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, GJELDER_FPFF_PERIODE_FØDSEL)
                .hvis(new SjekkOmSøknadGjelderFødsel(), new ForeldrepengerFørFødselDelregel(konfigurasjon).getSpecification())
                .ellers(Manuellbehandling.opprett("UT1092", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }
}
