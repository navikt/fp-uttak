package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.regler.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmAlleBarnErDøde;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBareMorHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBådeMorOgFarHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmDekningsgradEr100;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmDødtBarn;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmFarHarAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmFødsel;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmMerEnnEttBarn;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmMorHarAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmToBarn;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Denne implementerer regeltjenesten som beregner antall stønadsdager for foreldrepenger.
 */
@RuleDocumentation(value = BeregnKontoer.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=174837789")
public class BeregnKontoer implements RuleService<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17";
    private static final String SJEKK_OM_MER_ENN_ETT_BARN = "Sjekk om det er mer enn ett barn?";
    private static final String SJEKK_OM_DET_ER_FØDSEL = "Sjekk om det er fødsel?";
    private static final String SJEKK_OM_DET_ER_DØDE_BARN = "Sjekk om der er døde barn?";
    private static final String SJEKK_OM_ALLE_BARN_ER_DØDE = "Sjekk om alle ban er døde?";
    private static final String SJEKK_OM_100_PROSENT_DEKNINGSGRAD = "Sjekk om det er 100% dekningsgrad?";
    private static final String SJEKK_OM_TO_BARN = "Sjekk om det er to barn?";


    private Konfigurasjon konfigurasjon;

    public BeregnKontoer() {
        //For dokumentasjonsgenerering
    }

    public BeregnKontoer(Konfigurasjon konfigurasjon) {
        Objects.requireNonNull(konfigurasjon);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluer(BeregnKontoerGrunnlag beregnKontoerGrunnlag) {
        return getSpecification().evaluate(beregnKontoerGrunnlag);
    }

    @Override
    public Specification<BeregnKontoerGrunnlag> getSpecification() {
        Ruleset<BeregnKontoerGrunnlag> rs = new Ruleset<>();

        return rs.hvisRegel(SjekkOmMorHarAleneomsorg.ID, "Sjekk om mor har aleneomsorg?")
            .hvis(new SjekkOmMorHarAleneomsorg(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.MOR)))
            .ellers(sjekkFarAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        RuleReasonRef ingenOpptjentRett = new RuleReasonRefImpl("", "Hverken far eller mor har opptjent rett til foreldrepenger.");
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, "Sjekk om kun far har rett til foreldrepenger?")
            .hvis(new SjekkOmBareFarHarRett(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.FAR))/*opprettKontoerForBareFarHarRettTilForeldrepenger(rs)*/)
            .ellers(new IkkeOppfylt<>(ingenOpptjentRett));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunMorRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Sjekk om kun mor har rett til foreldrepenger?")
            .hvis(new SjekkOmBareMorHarRett(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.MOR)))
            .ellers(sjekkKunFarRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBådeMorOgFarHarRett.ID, "Sjekk om begge har opptjent rett til foreldrepenger?")
            .hvis(new SjekkOmBådeMorOgFarHarRett(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.BEGGE)))
            .ellers(sjekkKunMorRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFarHarAleneomsorg.ID, "Sjekk om far har aleneomsorg?")
            .hvis(new SjekkOmFarHarAleneomsorg(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.FAR_ALENE)))
            .ellers(sjekkBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> opprettKontoer(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {

        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
            .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnNode(rs, konfigfaktorBuilder))
            .ellers(sjekkOmAlleBarnErDødeNode(rs, konfigfaktorBuilder));
    }

    private Kontokonfigurasjon[] byggKonfigurasjon(Konfigurasjonsfaktorer faktorer){

        if(faktorer.getAntallLevendeBarn() == null){
            throw new IllegalArgumentException("Antall levende barn er ikke oppgitt");
        }
        if(faktorer.erFødsel() == null){
            throw new IllegalArgumentException("Det er ikke oppgitt om dette gjelder fødsel");
        }
        if(faktorer.getBerettiget() == null){
            throw new IllegalArgumentException("Berettigede parter er ikke oppgitt");
        }
        if(faktorer.er100Prosent() == null){
            throw new IllegalArgumentException("dekningsgrad er ikke oppgitt");
        }
        if(faktorer.medDødtBarn() == null){
            throw new IllegalArgumentException("Det er ikke oppgitt hvorvidt der finnes døde barn i hendelsen");
        }
        List<Kontokonfigurasjon> konfigurasjoner = new ArrayList<>();

        int antallBarn = faktorer.getAntallLevendeBarn();

        if(antallBarn > 0) {

            if (faktorer.er100Prosent()) {
                konfigurasjoner.addAll(Konfigurasjonsfaktorer.KONFIGURASJONER_100_PROSENT.get(faktorer.getBerettiget()));
            } else {
                konfigurasjoner.addAll(Konfigurasjonsfaktorer.KONFIGURASJONER_80_PROSENT.get(faktorer.getBerettiget()));
            }

            if (antallBarn == 2) {
                if (faktorer.er100Prosent()) {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100));
                } else {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80));
                }
            } else if (antallBarn >= 3) {
                if (faktorer.er100Prosent()) {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100));
                } else {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80));
                }
            }

            if (faktorer.erFødsel()) {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL));
                if (faktorer.medDødtBarn() && antallBarn < 3) {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.STØNADSDAGER_ETTER_BARNS_DØD));
                }
            }

        } else if(faktorer.erFødsel() && faktorer.medDødtBarn()){
            if(faktorer.getBerettiget() == Konfigurasjonsfaktorer.Berettiget.BEGGE) {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.STØNADSDAGER_ETTER_BARNS_DØD));
            } else {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.STØNADSDAGER_ETTER_BARNS_DØD));
            }
        }

        return konfigurasjoner.toArray(Kontokonfigurasjon[]::new);
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), sjekkDødtBarnNode(rs,konfigfaktorBuilder.medErFødsel(true)))
            .ellers(new OpprettKontoer(konfigurasjon, byggKonfigurasjon(konfigfaktorBuilder.medErFødsel(false).medDødtBarn(false).build())));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkDødtBarnNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmDødtBarn.ID, SJEKK_OM_DET_ER_DØDE_BARN)
                .hvis(new SjekkOmDødtBarn(), new OpprettKontoer(konfigurasjon,
                        byggKonfigurasjon(konfigfaktorBuilder.medDødtBarn(true).build())))
                .ellers(new OpprettKontoer(konfigurasjon, byggKonfigurasjon(konfigfaktorBuilder.medDødtBarn(false).build())));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
                .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselNode(rs, konfigfaktorBuilder.medEr100Prosent(true)))
                .ellers(sjekkFødselNode(rs, konfigfaktorBuilder.medEr100Prosent(false)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmAlleBarnErDødeNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmAlleBarnErDøde.ID, SJEKK_OM_ALLE_BARN_ER_DØDE)
                .hvis(new SjekkOmAlleBarnErDøde(), sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(0)))
                .ellers(sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(1)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
            .hvis(new SjekkOmToBarn(), sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(2)))
            .ellers(sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(3)));
    }

}

