package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareMorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmEtterNesteStønadsperiodeHarDisponibleDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFarsUttakRundtFødselTilgjengeligeDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmMinsterettBalansertUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmMinsterettHarDisponibleDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenGjelderFlerbarnsdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenInnenforUkerReservertMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderTerminEllerFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUføreUtenAktivitetskravHarDisponibleDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerEtterDeFørsteUkene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.ConditionalOrSpecification;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = ForeldrepengerDelregel.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class ForeldrepengerDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK XX10";
    private static final String FØDSEL = "Er det fødsel?";
    private static final String ELLER = " eller ";

    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();
    private Specification<FastsettePeriodeGrunnlag> bareFarRettIkkeAleneomsorgHarDisponibleDager;
    private Specification<FastsettePeriodeGrunnlag> dagerIgjenPåMinsterett;

    ForeldrepengerDelregel() {
        // For dokumentasjonsgenerering
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, SjekkOmSøkerErMor.BESKRIVELSE)
            .hvis(new SjekkOmSøkerErMor(), sjekkOmDetErFødselMor())
            .ellers(sjekkOmDetErFødselFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødselMor() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, FØDSEL)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmUttaketStarterFørLovligUttakFørFamiliehendelse())
            .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelseMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelseMor() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                IkkeOppfylt.opprett("UT1236", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(sjekkOmMorHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødselFar() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, FØDSEL)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmUttakSkalVæreFørFamileHendelse())
            .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelseFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelseFar() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                IkkeOppfylt.opprett("UT1234", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(sjekkErDetAleneomsorgFar());
    }


    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaketStarterFørLovligUttakFørFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.ID, SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin(),
                IkkeOppfylt.opprett("UT1185", IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL, false, false))
            .ellers(sjekkErDetAleneomsorgMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetAleneomsorgMor() {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, SjekkOmErAleneomsorg.BESKRIVELSE)
            .hvis(new SjekkOmErAleneomsorg(), sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel())
            .ellers(sjekkErDetBareMorSomHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetBareMorSomHarRett() {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, SjekkOmBareMorHarRett.BESKRIVELSE)
            .hvis(new SjekkOmBareMorHarRett(), sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel())
            .ellers(Manuellbehandling.opprett("UT1209", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel.ID, "Starter perioden før 3 uker før termin/fødsel?")
            .hvis(new SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel(), sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor())
            .ellers(sjekkOmPeriodenStarterFørFamilieHendelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.BESKRIVELSE)
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPeriodenFørXUkerEtterFamiliehendelseMor())
            .ellers(
                Manuellbehandling.opprett("UT1205", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenFørXUkerEtterFamiliehendelseMor() {
        var erDetBareMorSomHarRettUtenGradering = erDetAleneomsorgSjekk(Oppfylt.opprett("UT1186", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true),
            Oppfylt.opprett("UT1211", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true));
        var erDetBareMorSomHarRettVedGradering = erDetAleneomsorgSjekk(
            Oppfylt.opprett("UT1187", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true),
            Oppfylt.opprett("UT1212", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), erDetBareMorSomHarRettVedGradering)
            .ellers(erDetBareMorSomHarRettUtenGradering);
    }

    private Specification<FastsettePeriodeGrunnlag> erDetAleneomsorgSjekk(FastsettePeriodeUtfall utfallJa, FastsettePeriodeUtfall utfallNei) {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, SjekkOmErAleneomsorg.BESKRIVELSE).hvis(new SjekkOmErAleneomsorg(), utfallJa).ellers(utfallNei);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørFamilieHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før termin/fødsel?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), sjekkOmGraderingIPeriodenForbeholdMorFørFødsel())
            .ellers(sjekkErPeriodenInnenforUkerReservertMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenForbeholdMorFørFødsel() {
        var sjekkErDetAleneomsorg = erDetAleneomsorgSjekk(Oppfylt.opprett("UT1197", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true, true),
            Oppfylt.opprett("UT1192", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), Manuellbehandling.opprett("UT1066", null, Manuellbehandlingårsak.AVKLAR_ARBEID, true, true))
            .ellers(sjekkErDetAleneomsorg);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErPeriodenInnenforUkerReservertMor() {
        return rs.hvisRegel(SjekkOmPeriodenInnenforUkerReservertMor.ID, "Er perioden innenfor 6 uker etter fødsel?")
            .hvis(new SjekkOmPeriodenInnenforUkerReservertMor(), sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor())
            .ellers(sjekkOmMorHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorHarOmsorgForBarnet() {
        var sjekkOmTilgjengeligeDager = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.BESKRIVELSE)
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPeriodenXUkerEtterFamilieHendelseForMor())
            .ellers(Manuellbehandling.opprett("UT1188", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmTilgjengeligeDager)
            .ellers(IkkeOppfylt.opprett("UT1191", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenXUkerEtterFamilieHendelseForMor() {
        var erDetBareMorSomHarRettSjekkIkkeGradering = erDetAleneomsorgSjekk(
            Oppfylt.opprett("UT1190", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true),
            Oppfylt.opprett("UT1214", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true));
        var erDetBareMorSomHarRettSjekkGradering = erDetAleneomsorgSjekk(Oppfylt.opprett("UT1210", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true),
            Oppfylt.opprett("UT1213", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), erDetBareMorSomHarRettSjekkGradering)
            .ellers(erDetBareMorSomHarRettSjekkIkkeGradering);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkalVæreFørFamileHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttak være før termin/fødsel?")
            .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(), sjekkOmUttakFørFødselErFarRundtFødsel())
            .ellers(sjekkErDetAleneomsorgFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakFørFødselErFarRundtFødsel() {
        return rs.hvisRegel(SjekkOmFarsUttakRundtFødselTilgjengeligeDager.ID, "Er det hjemlet fars uttak rundt fødsel?")
            .hvis(new SjekkOmFarsUttakRundtFødselTilgjengeligeDager(), sjekkErDetAleneomsorgFar())
            .ellers(Manuellbehandling.opprett("UT1193", IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL, Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL, false,
                false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetAleneomsorgFar() {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, SjekkOmErAleneomsorg.BESKRIVELSE)
            .hvis(new SjekkOmErAleneomsorg(), sjekkOmFarMedAleneomsorgHarOmsorgForBarnet())
            .ellers(sjekkErDetBareFarMedmorSomHarRett());
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmFarMedAleneomsorgHarDisponibleDager())
            .ellers(IkkeOppfylt.opprett("UT1194", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgHarDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.BESKRIVELSE)
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), sjekkOmFarMedAleneomsorgGraderingIPerioden())
            .ellers(Manuellbehandling.opprett("UT1195", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprett("UT1196", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true))
            .ellers(Oppfylt.opprett("UT1198", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetBareFarMedmorSomHarRett() {
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
            .hvis(new SjekkOmBareFarHarRett(), sjekkOmFarUtenAleneomsorgHarOmsorgForBarnet())
            .ellers(Manuellbehandling.opprett("UT1204", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarUtenAleneomsorgHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmPeriodenGjelderFlerbarnsdager())
            .ellers(IkkeOppfylt.opprett("UT1199", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenGjelderFlerbarnsdager() {
        var sjekkOmGraderingIPerioden = rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprett("UT1267", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT, true))
            .ellers(Oppfylt.opprett("UT1266", InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT, true));

        var sjekkOmTilgjengeligeDager = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.BESKRIVELSE)
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPerioden)
            .ellers(Manuellbehandling.opprett("UT1269", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        var minsterettBalansertUttak = rs.hvisRegel(SjekkOmMinsterettBalansertUttak.ID, SjekkOmMinsterettBalansertUttak.BESKRIVELSE)
            .hvis(new SjekkOmMinsterettBalansertUttak(), sjekkOmDagerIgjenPåMinsterett())
            .ellers(sjekkOmTilgjengeligeDager);

        return rs.hvisRegel(SjekkOmPeriodenGjelderFlerbarnsdager.ID, "Gjelder perioden flerbarnsdager?")
            .hvis(new SjekkOmPeriodenGjelderFlerbarnsdager(), minsterettBalansertUttak)
            .ellers(sjekkOmDetErFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, FØDSEL)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmUttakSkjerFørDeFørsteUkene())
            .ellers(sjekkFarUtenAleneomsorgHarDisponibleDager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerFørDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
            .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(), sjekkFarUtenAleneomsorgHarDisponibleDager())
            .ellers(sjekkOmUttakFørsteSeksUkerErFarMedFABalansertUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakFørsteSeksUkerErFarMedFABalansertUttak() {
        return rs.hvisRegel(SjekkOmMinsterettBalansertUttak.ID, SjekkOmMinsterettBalansertUttak.BESKRIVELSE)
            .hvis(new SjekkOmMinsterettBalansertUttak(), sjekkFarUtenAleneomsorgHarDisponibleDager())
            .ellers(sjekkOmGyldigGrunnForTidligOppstart());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigGrunnForTidligOppstart() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID, "Foreligger et gyldig grunn for hele perioden for tidlig oppstart?")
            .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), sjekkFarUtenAleneomsorgHarDisponibleDager())
            .ellers(Manuellbehandling.opprett("UT1200", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDagerIgjenPåMinsterett() {
        if (dagerIgjenPåMinsterett == null) {
            dagerIgjenPåMinsterett = rs.hvisRegel(
                    SjekkOmMinsterettHarDisponibleDager.ID + ELLER + SjekkOmEtterNesteStønadsperiodeHarDisponibleDager.ID + ELLER
                        + SjekkOmUføreUtenAktivitetskravHarDisponibleDager.ID,
                    SjekkOmMinsterettHarDisponibleDager.BESKRIVELSE + ELLER + SjekkOmEtterNesteStønadsperiodeHarDisponibleDager.BESKRIVELSE + ELLER
                        + SjekkOmUføreUtenAktivitetskravHarDisponibleDager.BESKRIVELSE)
                .hvis(new SjekkOmMinsterettHarDisponibleDager().eller(new SjekkOmEtterNesteStønadsperiodeHarDisponibleDager())
                    .eller(new SjekkOmUføreUtenAktivitetskravHarDisponibleDager()), sjekkGraderingVedKunFarMedmorRettMinsterett())
                .ellers(new AvslagAktivitetskravDelregel().getSpecification());
        }
        return dagerIgjenPåMinsterett;
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkGraderingVedKunFarMedmorRettMinsterett() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(),
                Oppfylt.opprett("UT1318", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, true))
            .ellers(Oppfylt.opprett("UT1317", InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAktivitetskravErOppfylt() {
        return rs.hvisRegel(SjekkOmMorErIAktivitet.ID, SjekkOmMorErIAktivitet.BESKRIVELSE)
            .hvis(new SjekkOmMorErIAktivitet(), sjekkGraderingVedKunFarMedmorRett())
            .ellers(sjekkOmDagerIgjenPåMinsterett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkGraderingVedKunFarMedmorRett() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), sjekkOmMorErIArbeidMedStillingprosentUnder75ProsentMedGradering())
            .ellers(sjekkOmMorErIArbeidMedStillingprosentUnder75Prosent());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorErIArbeidMedStillingprosentUnder75ProsentMedGradering() {
        return rs.hvisRegel(SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.ID, SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.BESKRIVELSE)
            .hvis(new SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent(), sjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent())
            .ellers(Oppfylt.opprett("UT1315", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent() {
        return rs.hvisRegel(SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent.ID, SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent.BESKRIVELSE)
            .hvis(new SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent(), Oppfylt.opprettMedAvslåttGradering("UT1331", InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT, GraderingIkkeInnvilgetÅrsak.MOR_OPPFYLLER_IKKE_AKTIVITETSKRAV, true))
            .ellers(Manuellbehandling.opprett("UT1329", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT, Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID, true, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorErIArbeidMedStillingprosentUnder75Prosent() {
        return rs.hvisRegel(SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.ID, SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.BESKRIVELSE)
            .hvis(new SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent(), Manuellbehandling.opprett("UT1328", InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT, Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID, true, true))
            .ellers(Oppfylt.opprett("UT1316", InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkFarUtenAleneomsorgHarDisponibleDager() {
        if (bareFarRettIkkeAleneomsorgHarDisponibleDager == null) {
            bareFarRettIkkeAleneomsorgHarDisponibleDager = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                    SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), sjekkOmAktivitetskravErOppfylt())
                .ellers(Manuellbehandling.opprett("UT1203", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
        }
        return bareFarRettIkkeAleneomsorgHarDisponibleDager;
    }
}
