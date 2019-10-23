package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarMedmorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareMorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenGjelderFlerbarnsdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenInnenforUkerReservertMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerFørDeFørsteUkene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaketStarterFørLovligUttakFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.ConditionalOrSpecification;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = ForeldrepengerDelregel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=292407153")
public class ForeldrepengerDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK XX10";
    private static final String FØDSEL = "Er det fødsel?";

    private Konfigurasjon konfigurasjon;

    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public ForeldrepengerDelregel() {
        // For dokumentasjonsgenerering
    }

    ForeldrepengerDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, "Er søker mor?")
                .hvis(new SjekkOmSøkerErMor(), sjekkOmDetErFødselMor())
                .ellers(sjekkOmDetErFødselFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødselMor() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, FØDSEL)
            .hvis(new SjekkOmSøknadGjelderFødsel(), sjekkOmUttaketStarterFørLovligUttakFørFødsel())
            .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelseMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelseMor() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), IkkeOppfylt.opprett("UT1236", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(sjekkOmMorHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødselFar() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, FØDSEL)
            .hvis(new SjekkOmSøknadGjelderFødsel(), sjekkOmUttakSkalVæreFørFamileHendelse())
            .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelseFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelseFar() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), IkkeOppfylt.opprett("UT1234", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(sjekkErDetAleneomsorgFar());
    }


    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaketStarterFørLovligUttakFørFødsel() {
        return rs.hvisRegel(SjekkOmUttaketStarterFørLovligUttakFørFødsel.ID, "Skal uttaket starte tidligere enn 12 uker før termindato?")
                .hvis(new SjekkOmUttaketStarterFørLovligUttakFørFødsel(konfigurasjon), IkkeOppfylt.opprett("UT1185", IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL, false, false))
                .ellers(sjekkErDetAleneomsorgMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetAleneomsorgMor() {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, "Er det aleneomsorg?")
                .hvis(new SjekkOmErAleneomsorg(), sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel())
                .ellers(sjekkErDetBareMorSomHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetBareMorSomHarRett() {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Er det bare mor som har rett?")
                .hvis(new SjekkOmBareMorHarRett(), sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel())
                .ellers(Manuellbehandling.opprett("UT1209", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel.ID, "Starter perioden før 3 uker før termin/fødsel?")
                .hvis(new SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel(konfigurasjon), sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor())
                .ellers(sjekkOmPeriodenStarterFørFamilieHendelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.ID, SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPeriodenFørXUkerEtterFamiliehendelseMor())
                .ellers(Manuellbehandling.opprett("UT1205", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenFørXUkerEtterFamiliehendelseMor() {
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettUtenGradering = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprett("UT1211", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true),
                Oppfylt.opprett("UT1186", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettVedGradering = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprettMedAvslåttGradering("UT1212", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true),
                Oppfylt.opprettMedAvslåttGradering("UT1187", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), erDetBareMorSomHarRettVedGradering)
                .ellers(erDetBareMorSomHarRettUtenGradering);
    }

    private Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettSjekk(FastsettePeriodeUtfall utfallJa, FastsettePeriodeUtfall utfallNei) {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Er det bare mor som har rett?")
                .hvis(new SjekkOmBareMorHarRett(), utfallJa)
                .ellers(utfallNei);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørFamilieHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før termin/fødsel?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1192", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false))
                .ellers(sjekkErPeriodenInnenforUkerReservertMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErPeriodenInnenforUkerReservertMor() {
        return rs.hvisRegel(SjekkOmPeriodenInnenforUkerReservertMor.ID, "Er perioden innenfor 6 uker etter fødsel?")
                .hvis(new SjekkOmPeriodenInnenforUkerReservertMor(konfigurasjon), sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor())
                .ellers(sjekkOmMorHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorHarOmsorgForBarnet() {
        ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmTilgjengeligeDager =
                rs.hvisRegel(SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.ID, SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.BESKRIVELSE)
                        .hvis(new SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPeriodenXUkerEtterFamilieHendelseForMor())
                        .ellers(Manuellbehandling.opprett("UT1188", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmTilgjengeligeDager)
                .ellers(IkkeOppfylt.opprett("UT1191", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenXUkerEtterFamilieHendelseForMor() {
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettSjekkIkkeGradering = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprett("UT1214", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true),
                Oppfylt.opprett("UT1190", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettSjekkGradering = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprett("UT1213", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT, true),
                Oppfylt.opprett("UT1210", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), erDetBareMorSomHarRettSjekkGradering)
                .ellers(erDetBareMorSomHarRettSjekkIkkeGradering);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkalVæreFørFamileHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttak være før termin/fødsel?")
                .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(), Manuellbehandling.opprett("UT1193", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, false, false))
                .ellers(sjekkErDetAleneomsorgFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetAleneomsorgFar() {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, "Er det aleneomsorg?")
                .hvis(new SjekkOmErAleneomsorg(), sjekkOmFarMedAleneomsorgHarOmsorgForBarnet())
                .ellers(sjekkErDetBareFarMedmorSomHarRett());
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                    .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmFarMedAleneomsorgHarDisponibleDager())
                    .ellers(IkkeOppfylt.opprett("UT1194", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgHarDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.ID, SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto(), sjekkOmFarMedAleneomsorgGraderingIPerioden())
                .ellers(Manuellbehandling.opprett("UT1195", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprett("UT1196", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true))
                .ellers(Oppfylt.opprett("UT1198", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetBareFarMedmorSomHarRett() {
        return rs.hvisRegel(SjekkOmBareFarMedmorHarRett.ID, "Er det bare far/medmor som har rett?")
                .hvis(new SjekkOmBareFarMedmorHarRett(), sjekkOmFarUtenAleneomsorgHarOmsorgForBarnet())
                .ellers(Manuellbehandling.opprett("UT1204", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarUtenAleneomsorgHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmPeriodenGjelderFlerbarnsdager())
                .ellers(IkkeOppfylt.opprett("UT1199", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenGjelderFlerbarnsdager() {
        ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPerioden =
            rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprett("UT1267", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT, true))
                .ellers(Oppfylt.opprett("UT1266", InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT, true));

        ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmTilgjengeligeDager =
            rs.hvisRegel(SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.ID, SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPerioden)
                .ellers(Manuellbehandling.opprett("UT1269", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmPeriodenGjelderFlerbarnsdager.ID, "Gjelder perioden flerbarnsdager?")
            .hvis(new SjekkOmPeriodenGjelderFlerbarnsdager(), sjekkOmTilgjengeligeDager)
            .ellers(sjekkOmDetErFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, FØDSEL)
            .hvis(new SjekkOmSøknadGjelderFødsel(), sjekkOmUttakSkjerFørDeFørsteUkene())
            .ellers(sjekkFarUtenAleneomsorgHarDisponibleDager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerFørDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerFørDeFørsteUkene.ID, "Starter perioden før uke 7 etter termin/fødsel?")
                .hvis(new SjekkOmUttakSkjerFørDeFørsteUkene(konfigurasjon), sjekkOmGyldigGrunnForTidligOppstart())
                .ellers(sjekkFarUtenAleneomsorgHarDisponibleDager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigGrunnForTidligOppstart() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID, "Foreligger et gyldig grunn for hele perioden for tidlig oppstart?")
                .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), sjekkOmFarUtenAleneomsorgGraderingIPerioden())
                .ellers(Manuellbehandling.opprett("UT1200", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarUtenAleneomsorgGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), Manuellbehandling.opprett("UT1216", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false))
                .ellers(Manuellbehandling.opprett("UT1201", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkFarUtenAleneomsorgHarDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.ID, SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDagerPåAlleAktiviteteneForSøktStønadskonto(), sjekkOmFarUtenAleneomsorgGraderingIPerioden())
                .ellers(Manuellbehandling.opprett("UT1203", null, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }
}
