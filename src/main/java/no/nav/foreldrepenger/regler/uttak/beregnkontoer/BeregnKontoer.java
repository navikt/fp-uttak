package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.regler.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBareMorHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBådeMorOgFarHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmDekningsgradEr100;
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
            .hvis(new SjekkOmMorHarAleneomsorg(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.MOR).medErAleneomsorg(true)))
            .ellers(sjekkFarAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        RuleReasonRef ingenOpptjentRett = new RuleReasonRefImpl("", "Hverken far eller mor har opptjent rett til foreldrepenger.");
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, "Sjekk om kun far har rett til foreldrepenger?")
            .hvis(new SjekkOmBareFarHarRett(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.FAR).medErAleneomsorg(false))/*opprettKontoerForBareFarHarRettTilForeldrepenger(rs)*/)
            .ellers(new IkkeOppfylt<>(ingenOpptjentRett));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunMorRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Sjekk om kun mor har rett til foreldrepenger?")
            .hvis(new SjekkOmBareMorHarRett(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.MOR).medErAleneomsorg(false)))
            .ellers(sjekkKunFarRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBådeMorOgFarHarRett.ID, "Sjekk om begge har opptjent rett til foreldrepenger?")
            .hvis(new SjekkOmBådeMorOgFarHarRett(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.BEGGE).medErAleneomsorg(false)))
            .ellers(sjekkKunMorRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFarHarAleneomsorg.ID, "Sjekk om far har aleneomsorg?")
            .hvis(new SjekkOmFarHarAleneomsorg(), opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder().medBerettiget(Konfigurasjonsfaktorer.Berettiget.FAR).medErAleneomsorg(true)))
            .ellers(sjekkBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> opprettKontoer(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {

        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
            .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnNode(rs, konfigfaktorBuilder))
            .ellers(sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(1)));
    }

    private Kontokonfigurasjon[] byggKonfigurasjon(Konfigurasjonsfaktorer faktorer){
        List<Kontokonfigurasjon> konfigurasjoner = new ArrayList<>();

        if(faktorer.er100Prosent().isEmpty()){
            throw new IllegalArgumentException("dekningsgrad er ikke oppgitt");
        }
        if(faktorer.antallLevendeBarn().isEmpty()){
            throw new IllegalArgumentException("Antall levende barn er ikke oppgitt");
        }
        if(faktorer.erFødsel().isEmpty()){
            throw new IllegalArgumentException("Det er ikke oppgitt om dette gjelder fødsel");
        }
        if(faktorer.berettiget().isEmpty()){
            throw new IllegalArgumentException("Berettigede parter er ikke oppgitt");
        }
        if(faktorer.erAleneomsorg().isEmpty()){
            throw new IllegalArgumentException("Det er ikke oppgitt hvorvidt det dreier seg om aleneomsorg");
        }
        if(Konfigurasjonsfaktorer.Berettiget.MOR == faktorer.berettiget().orElseThrow()) {
            if (faktorer.er100Prosent().orElseThrow()) {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER));
            } else {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER));
            }
        } else if(Konfigurasjonsfaktorer.Berettiget.FAR == faktorer.berettiget().orElseThrow()) {
            if(faktorer.erAleneomsorg().orElseThrow()) {
                if (faktorer.er100Prosent().orElseThrow()) {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER));
                } else {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER));
                }
            } else {
                if (faktorer.er100Prosent().orElseThrow()) {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER));
                } else {
                    konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER));
                }
            }
        } else if(Konfigurasjonsfaktorer.Berettiget.BEGGE == faktorer.berettiget().orElseThrow()) {
            if (faktorer.er100Prosent().orElseThrow()) {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER));
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT));
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT));
            } else {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER));
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT));
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT));
            }
        }

        int antallBarn = faktorer.antallLevendeBarn().orElseThrow();
        if(antallBarn == 2){
            if(faktorer.er100Prosent().orElseThrow()) {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100));
            } else{
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80));
            }
        } else if(antallBarn >= 3){
            if(faktorer.er100Prosent().orElseThrow()) {
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100));
            } else{
                konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80));
            }
        }

        if(faktorer.erFødsel().orElseThrow()) {
            konfigurasjoner.add(new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL));
        }

        return konfigurasjoner.toArray(Kontokonfigurasjon[]::new);
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                    byggKonfigurasjon(konfigfaktorBuilder.medErFødsel(true).build())))
            .ellers(new OpprettKontoer(konfigurasjon, byggKonfigurasjon(konfigfaktorBuilder.medErFødsel(false).build())));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
                .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselNode(rs, konfigfaktorBuilder.medEr100Prosent(true)))
                .ellers(sjekkFødselNode(rs, konfigfaktorBuilder.medEr100Prosent(false)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnNode(Ruleset<BeregnKontoerGrunnlag> rs, Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
            .hvis(new SjekkOmToBarn(), sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(2)))
            .ellers(sjekk100ProsentNode(rs, konfigfaktorBuilder.medAntallLevendeBarn(3)));
    }

}

