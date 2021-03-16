package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaIkkeRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmMorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOppholdKvoteAnnenForelder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenGjelderFlerbarnsdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørUke7;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderTerminEllerFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktOmOverføringAvKvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttaksperioder med fedrekvote.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Det er ikke nok dager igjen på stønadskontoen for fedrekvote.<br>
 * - Perioden starter før familiehendelsen (termin/fødsel).<br>
 * - Perioden starter i periode etter fødsel som er forbeholdt mor og har ikke gyldig grunn for dette. <br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden er etter ukene etter fødsel som er forbeholdt mor og det er nok dager på stønadskontoen for fedrekvote.<br>
 * - Perioden har gyldig grunn for å starte i ukene etter fødsel som er forbeholdt mor og det er nok dager på stønadskontoen for fedrekvote.<br>
 */

@RuleDocumentation(value = FedrekvoteDelregel.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class FedrekvoteDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13";

    private static final String ER_SØKER_FAR = "Er søker far?";
    private static final String OVERFØRING_SYKDOM_SKADE = "Er det søkt om overføring som følge av sykdom/skade?";
    private static final String OVERFØRING_INNLEGGELSE = "Er det søkt om overføring som følge av innleggelse på institusjon?";
    private static final String OVERFØRING_ALENEOMSORG_ELLER_IKKE_RETT = "Er det søkt om overføring som følge av aleneomsorg eller annen forelder ikke har rett?";

    private Konfigurasjon konfigurasjon;
    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public FedrekvoteDelregel() {
        // For dokumentasjonsgenerering
    }

    FedrekvoteDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmOppholdKvoteAnnenForelder.ID, "Er det søkt om opphold av fedrekvoten?")
                .hvis(new SjekkOmOppholdKvoteAnnenForelder(), sjekkOmNoenDisponibleDager())
                .ellers(sjekkOmSøkerErFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmNoenDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                "Er det noen disponible stønadsdager på fedrekvoten?")
                .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(),
                        Oppfylt.opprettForOppholds("UT1263", true, false))
                .ellers(Manuellbehandling.opprett("UT1262", null, Manuellbehandlingårsak.OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER, true,
                        false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørOmsorgsovertakelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                        IkkeOppfylt.opprett("UT1231", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
                .ellers(delFlytForVanligUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDetErFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
                .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmPeriodeStarterFørUke7EtterFamiliehendelse())
                .ellers(sjekkOmPeriodenStarterFørOmsorgsovertakelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErFar() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_FAR)
                .hvis(new SjekkOmSøkerErMor(), sjekkOmMorSøktOmOverføringAvFedrekvote())
                .ellers(sjekkOmMorHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorHarRett() {
        return rs.hvisRegel(SjekkOmMorHarRett.ID, "Er det avklart at mor har rett?")
                .hvis(new SjekkOmMorHarRett(), sjekkOmDetErFødsel())
                .ellers(IkkeOppfylt.opprett("UT1292", IkkeOppfyltÅrsak.MOR_IKKE_RETT_FK, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaInnleggelse() {
        return rs.hvisRegel(SjekkOmOverføringPgaInnleggelse.ID, OVERFØRING_INNLEGGELSE)
                .hvis(new SjekkOmOverføringPgaInnleggelse(), sjekkOmGyldigOverføringPgaInnleggelse())
                .ellers(sjekkOmOverføringPgaSykdomSkade());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaSykdomSkade() {
        return rs.hvisRegel(SjekkOmOverføringPgaSykdomSkade.ID, OVERFØRING_SYKDOM_SKADE)
                .hvis(new SjekkOmOverføringPgaSykdomSkade(), sjekkOmGyldigOverføringPgaSykdomSkade())
                .ellers(sjekkOmOverføringPgaAleneomsorgEllerIkkeRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaAleneomsorgEllerIkkeRett() {
        return rs.hvisRegel(SjekkOmOverføringPgaAleneomsorg.ID, OVERFØRING_ALENEOMSORG_ELLER_IKKE_RETT)
                .hvis(new SjekkOmOverføringPgaAleneomsorg(), sjekkOmGyldigOverføringPgaAleneomsorg())
                .ellers(sjekkOmGyldigOverføringPgaIkkeRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaInnleggelse() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaInnleggelse.ID,
                "Er det avklart at overføring av kvoten er gyldig grunn av innleggelse på institusjon?")
                .hvis(new SjekkOmGyldigOverføringPgaInnleggelse(), sjekkOmDetErFødsel())
                .ellers(Manuellbehandling.opprett("UT1033", IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaSykdomSkade() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaSykdomSkade.ID,
                "Er det avklart at overføring av kvoten er gyldig grunn av sykdom/skade?")
                .hvis(new SjekkOmGyldigOverføringPgaSykdomSkade(), sjekkOmDetErFødsel())
                .ellers(Manuellbehandling.opprett("UT1034", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaAleneomsorg() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaAleneomsorg.ID,
                "Er det avklart at overføring av kvoten er gyldig på grunn av aleneomsorg?")
                .hvis(new SjekkOmGyldigOverføringPgaAleneomsorg(), sjekkOmDetErFødsel())
                .ellers(Manuellbehandling.opprett("UT1296", IkkeOppfyltÅrsak.ALENEOMSORG_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaIkkeRett() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaIkkeRett.ID,
                "Er det avklart at overføring av kvoten er gyldig på grunn av annen forelder ikke har rett?")
                .hvis(new SjekkOmGyldigOverføringPgaIkkeRett(), sjekkOmDetErFødsel())
                .ellers(Manuellbehandling.opprett("UT1297", IkkeOppfyltÅrsak.DEN_ANDRE_PART_IKKE_RETT_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorSøktOmOverføringAvFedrekvote() {
        return rs.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Har mor søkt om overføring av fedrekvoten?")
                .hvis(new SjekkOmSøktOmOverføringAvKvote(), sjekkOmOverføringPgaInnleggelse()) // SKRIVE OM
                .ellers(Manuellbehandling.opprett("UT1032", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }


    public Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeStarterFørUke7EtterFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørUke7.ID, "Starter perioden før uke 7 etter termin/fødsel?")
                .hvis(new SjekkOmPeriodenStarterFørUke7(konfigurasjon), uttakFørTerminFødsel())
                .ellers(delFlytForVanligUttak());
    }

    public Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenGjelderFlerbarnsdager() {
        return rs.hvisRegel(SjekkOmPeriodenGjelderFlerbarnsdager.ID, "Gjelder perioden flerbarnsdager?")
                .hvis(new SjekkOmPeriodenGjelderFlerbarnsdager(), delFlytForTidligUttak())
                .ellers(gyldigGrunnForTidligUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> uttakFørTerminFødsel() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttaksperioden være før termin/fødsel?")
                .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(),
                        Manuellbehandling.opprett("UT1020", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG,
                                Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, false, false))
                .ellers(sjekkOmPeriodenGjelderFlerbarnsdager());
    }

    private Specification<FastsettePeriodeGrunnlag> gyldigGrunnForTidligUttak() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID,
                "Foreligger det gyldig grunn for hele perioden for tidlig oppstart?")
                .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), delFlytForTidligUttak())
                .ellers(Manuellbehandling.opprett("UT1021", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForTidligUttak() {

        var graderingIPeriodenNode = rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(),
                        Oppfylt.opprett("UT1217", InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE, true))
                .ellers(Oppfylt.opprett("UT1026", InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, true));

        var erSøkerFar = rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_FAR)
                .hvis(new SjekkOmSøkerErMor(), new OverføringDelregel().getSpecification())
                .ellers(graderingIPeriodenNode);

        var noenDisponibleDagerNode = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                "Er det disponibelt antall stønadsdager på fedrekvoten?")
                .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), erSøkerFar)
                .ellers(Manuellbehandling.opprett("UT1022", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                        Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
                .ellers(IkkeOppfylt.opprett("UT1025", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForVanligUttak() {

        var graderingIPeriodenNode = rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(),
                        Oppfylt.opprett("UT1218", InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE, true))
                .ellers(Oppfylt.opprett("UT1031", InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, true));

        var erSøkerFar = rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_FAR)
                .hvis(new SjekkOmSøkerErMor(), new OverføringDelregel().getSpecification())
                .ellers(graderingIPeriodenNode);

        var noenDisponibleDagerNode = rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID,
                "Er det disponibelt antall stønadsdager på fedrekvoten?")
                .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), erSøkerFar)
                .ellers(Manuellbehandling.opprett("UT1178", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN,
                        Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
                .ellers(IkkeOppfylt.opprett("UT1030", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }
}
