package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBehandlingKreverSammenhengendeUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmMorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOppholdFellesperiodeAnnenForelder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenGjelderFlerbarnsdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderTerminEllerFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerEtterDeFørsteUkene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent;
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
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsette uttak av fellesperiode.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Det er ikke nok dager igjen på stønadskontoen for fellesperioden.<br>
 * - Perioden starter for tidlig før familiehendelsen (termin/fødsel)
 * - Perioden starter i periode etter fødsel som er forbeholdt mor.<br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden starter før fødsel og det er nok dager på stønadskonto for fellesperiode. <br>
 * - Perioden er etter ukene etter fødsel som er forbeholdt mor og det er nok dager på stønadskontoen for fellesperiode.<br>
 */

@RuleDocumentation(value = FellesperiodeDelregel.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class FellesperiodeDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 19";

    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    FellesperiodeDelregel() {
        // For dokumentasjonsgenerering
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmOppholdFellesperiodeAnnenForelder.ID, "Er det søkt om opphold av fellesperioden?")
            .hvis(new SjekkOmOppholdFellesperiodeAnnenForelder(), sjekkOmNoenDisponibleDager())
            .ellers(sjekkOmMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmNoenDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                "Er det noen disponible stønadsdager på fellesperiode?")
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), Oppfylt.opprettForOppholds("UT1265", true, false))
            .ellers(Manuellbehandling.opprett("UT1264", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMor() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, "Gjelder søknaden fellesperiode for mor")
            .hvis(new SjekkOmSøkerErMor(), sjekkOmMorGjelderFødsel())
            .ellers(sjekkOmMorHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorHarRett() {
        return rs.hvisRegel(SjekkOmMorHarRett.ID, "Er det avklart at mor har rett?")
            .hvis(new SjekkOmMorHarRett(), sjekkOmFarGjelderFødsel())
            .ellers(IkkeOppfylt.opprett("UT1293", IkkeOppfyltÅrsak.MOR_IKKE_RETT_FP, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmUttakSkjerFørDeFørsteUkene())
            .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelseFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmPeriodenStarterFørLovligUttakFørFamiliehendelse())
            .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelseMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelseFar() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                IkkeOppfylt.opprett("UT1232", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(delFlytForVanligUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelseMor() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                IkkeOppfylt.opprett("UT1235", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(sjekkSaldoForMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørLovligUttakFørFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.ID, SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenStarterFørLovligUttakFørFødselTermin(),
                IkkeOppfylt.opprett("UT1040", IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL, false, false))
            .ellers(sjekkOmPeriodenStarterFørFamilieHendelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørFamilieHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, SjekkOmPeriodenStarterFørFamiliehendelse.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), sjekkOmGraderingIPerioden())
            .ellers(sjekkOmUttakSkjerEtterDeFørsteUkene());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, "Er perioden gradert?")
            .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprettMedAvslåttGradering("UT1064", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER,
                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true))
            .ellers(Oppfylt.opprett("UT1041", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerEtterDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
            .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(), sjekkSaldoForMor())
            .ellers(Manuellbehandling.opprett("UT1048", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkSaldoForMor() {
        var erDetGraderingIPeriodenMedDagerPåNoenAktiviteterNode = rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprett("UT1219", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, true))
            .ellers(Oppfylt.opprett("UT1047", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));

        var noenTilgjengligeDagerNode = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                "Er det tilgjengelige dager på fellesperioden?")
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), erDetGraderingIPeriodenMedDagerPåNoenAktiviteterNode)
            .ellers(
                Manuellbehandling.opprett("UT1043", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));

        var sjekkOmTrekkesDager = rs.hvisRegel(SjekkOmBehandlingKreverSammenhengendeUttak.ID, SjekkOmBehandlingKreverSammenhengendeUttak.BESKRIVELSE)
            .hvis(new SjekkOmBehandlingKreverSammenhengendeUttak(), IkkeOppfylt.opprett("UT1046", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, true, false))
            .ellers(IkkeOppfylt.opprett("UT1046", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenTilgjengligeDagerNode)
            .ellers(sjekkOmTrekkesDager);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerFørDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
            .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(), delFlytForVanligUttak())
            .ellers(sjekkOmPeriodenSlutterFørFamiliehendelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenGjelderFlerbarnsdager() {
        return rs.hvisRegel(SjekkOmPeriodenGjelderFlerbarnsdager.ID, SjekkOmPeriodenGjelderFlerbarnsdager.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenGjelderFlerbarnsdager(), delFlytForTidligUttak())
            .ellers(sjekkGyldigGrunnForTidligOppstartHelePerioden());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenSlutterFørFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttaksperioden være før termin/fødsel?")
            .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(),
                Manuellbehandling.opprett("UT1049", IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL, Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL, false,
                    false))
            .ellers(sjekkOmPeriodenGjelderFlerbarnsdager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkGyldigGrunnForTidligOppstartHelePerioden() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID, "Foreligger det gyldig grunn for hele perioden for tidlig oppstart?")
            .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), delFlytForTidligUttak())
            .ellers(Manuellbehandling.opprett("UT1050", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForTidligUttak() {
        var erDetGraderingIPeriodenMedDagerPåAlleAktiviteterNode = rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprett("UT1255", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, true))
            .ellers(Oppfylt.opprett("UT1256", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));

        var noenDisponibleDagerNode = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.BESKRIVELSE)
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), erDetGraderingIPeriodenMedDagerPåAlleAktiviteterNode)
            .ellers(
                Manuellbehandling.opprett("UT1257", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
            .ellers(IkkeOppfylt.opprett("UT1054", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGradertePeriodenGjelderFlerbarnsdager() {
        return rs.hvisRegel(SjekkOmPeriodenGjelderFlerbarnsdager.ID, SjekkOmPeriodenGjelderFlerbarnsdager.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenGjelderFlerbarnsdager(),
                Oppfylt.opprett("UT1270", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, true))
            .ellers(sjekkOmMorErIAktivitetIGradertPeriodeUtenFlerbarnsdager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorErIAktivitetIGradertPeriodeUtenFlerbarnsdager() {
        return rs.hvisRegel(SjekkOmMorErIAktivitet.ID, SjekkOmMorErIAktivitet.BESKRIVELSE)
            .hvis(new SjekkOmMorErIAktivitet(), sjekkOmMorErIArbeidMedStillingprosentUnder75Prosent())
            .ellers(new AvslagAktivitetskravDelregel().getSpecification());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorErIArbeidMedStillingprosentUnder75Prosent() {
        return rs.hvisRegel(SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.ID, SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.BESKRIVELSE)
            .hvis(new SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent(), sjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent())
            .ellers(Oppfylt.opprett("UT1272", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent() {
        return rs.hvisRegel(SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent.ID, SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent.BESKRIVELSE)
            .hvis(new SjekkOmSøktUttaksprosentErStørreEllerLikMorsStillingsprosent(), Oppfylt.opprettMedAvslåttGradering("UT1330", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, GraderingIkkeInnvilgetÅrsak.MOR_OPPFYLLER_IKKE_AKTIVITETSKRAV, true))
            .ellers(Manuellbehandling.opprett("UT1326", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID, true, true));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForVanligUttak() {
        var sjekkOmMorErIArbeidMedStillingprosentUnder75Prosent = rs.hvisRegel(SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.ID, SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent.BESKRIVELSE)
            .hvis(new SjekkOmMorErIArbeidMedStillingprosentUnder75Prosent(), Manuellbehandling.opprett("UT1327", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, Manuellbehandlingårsak.AKTIVITETSKRAV_DELVIS_ARBEID, true, true))
            .ellers(Oppfylt.opprett("UT1258", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));

        var sjekkOmMorErIAktivitetIPerioden = rs.hvisRegel(SjekkOmMorErIAktivitet.ID, SjekkOmMorErIAktivitet.BESKRIVELSE)
            .hvis(new SjekkOmMorErIAktivitet(), sjekkOmMorErIArbeidMedStillingprosentUnder75Prosent)
            .ellers(new AvslagAktivitetskravDelregel().getSpecification());

        var sjekkOmPeriodenGjelderFlerbarnsdager = rs.hvisRegel(SjekkOmPeriodenGjelderFlerbarnsdager.ID,
                SjekkOmPeriodenGjelderFlerbarnsdager.BESKRIVELSE)
            .hvis(new SjekkOmPeriodenGjelderFlerbarnsdager(), Oppfylt.opprett("UT1271", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true))
            .ellers(sjekkOmMorErIAktivitetIPerioden);


        var omGradertPeriodeNode = rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), sjekkOmGradertePeriodenGjelderFlerbarnsdager())
            .ellers(sjekkOmPeriodenGjelderFlerbarnsdager);

        var noenDisponibleDagerNode = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                "Er det disponibelt antall stønadsdager på fedrekvoten?")
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), omGradertPeriodeNode)
            .ellers(
                Manuellbehandling.opprett("UT1146", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));

        var sjekkOmTrekkesDager = rs.hvisRegel(SjekkOmBehandlingKreverSammenhengendeUttak.ID, SjekkOmBehandlingKreverSammenhengendeUttak.BESKRIVELSE)
            .hvis(new SjekkOmBehandlingKreverSammenhengendeUttak(), IkkeOppfylt.opprett("UT1060", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false))
            .ellers(IkkeOppfylt.opprett("UT1060", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
            .ellers(sjekkOmTrekkesDager);
    }
}
