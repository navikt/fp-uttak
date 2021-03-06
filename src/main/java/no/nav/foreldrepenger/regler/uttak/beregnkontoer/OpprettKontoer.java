package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerPropertyType;
import no.nav.foreldrepenger.regler.uttak.felles.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettKontoer.ID)
class OpprettKontoer extends LeafSpecification<BeregnKontoerGrunnlag> {

    private final Konfigurasjon konfigurasjon;
    private final Kontokonfigurasjon[] kontokonfigurasjoner;
    public static final String ID = "Opprett kontoer";

    OpprettKontoer(Konfigurasjon konfigurasjon, Kontokonfigurasjon... kontokonfigurasjontoner) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
        this.kontokonfigurasjoner = kontokonfigurasjontoner;
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        Map<Stønadskontotype, Integer> kontoerMap = new EnumMap<>(Stønadskontotype.class);
        var antallPrematurDager = 0;

        // Finn antall ekstra dager først.
        var antallExtraBarnDager = finnEkstraFlerbarnsdager(grunnlag);

        // Opprette alle kontoer utenom samtidig uttak
        for (var kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.getStønadskontotype() != Stønadskontotype.FLERBARNSDAGER) {
                var antallDager = konfigurasjon.getParameter(kontokonfigurasjon.getParametertype(),
                        grunnlag.getFamiliehendelsesdato());
                if (antallExtraBarnDager > 0) {
                    // Legg ekstra dager til foreldrepenger eller fellesperiode.
                    if ((kontokonfigurasjon.getStønadskontotype().equals(Stønadskontotype.FORELDREPENGER))) {
                        antallDager += antallExtraBarnDager;
                        if (kunFarRettIkkeAleneomsorgFlerbarnsdager(grunnlag)) {
                            kontoerMap.put(Stønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
                        }
                    } else if (kontokonfigurasjon.getStønadskontotype().equals(Stønadskontotype.FELLESPERIODE)) {
                        antallDager += antallExtraBarnDager;
                        kontoerMap.put(Stønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
                    }
                }
                if (kontotypeSomKanHaEkstraFlerbarnsdager(kontokonfigurasjon) && skalLeggeTilPrematurUker(grunnlag)) {
                    antallPrematurDager = antallVirkedagerFomFødselTilTermin(grunnlag);
                    antallDager += antallPrematurDager;
                }
                kontoerMap.put(kontokonfigurasjon.getStønadskontotype(), antallDager);
            }
        }
        return beregnetMedResultat(kontoerMap, antallExtraBarnDager, antallPrematurDager);
    }

    private boolean kontotypeSomKanHaEkstraFlerbarnsdager(Kontokonfigurasjon kontokonfigurasjon) {
        return kontokonfigurasjon.getStønadskontotype().equals(Stønadskontotype.FELLESPERIODE)
                || kontokonfigurasjon.getStønadskontotype().equals(Stønadskontotype.FORELDREPENGER);
    }

    private int finnEkstraFlerbarnsdager(BeregnKontoerGrunnlag grunnlag) {
        for (var kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.getStønadskontotype() == Stønadskontotype.FLERBARNSDAGER) {
                return konfigurasjon.getParameter(kontokonfigurasjon.getParametertype(), grunnlag.getFamiliehendelsesdato());
            }
        }
        return 0;
    }

    private int antallVirkedagerFomFødselTilTermin(BeregnKontoerGrunnlag grunnlag) {
        //Fra termin, ikke inkludert termin
        return Virkedager.beregnAntallVirkedager(grunnlag.getFødselsdato().orElseThrow(),
                grunnlag.getTermindato().orElseThrow().minusDays(1));
    }

    private boolean skalLeggeTilPrematurUker(BeregnKontoerGrunnlag grunnlag) {
        if (!grunnlag.erFødsel()) {
            return false;
        }

        var fødselsdato = grunnlag.getFødselsdato();
        var termindato = grunnlag.getTermindato();
        return PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato.orElse(null), termindato.orElse(null), konfigurasjon);
    }

    private boolean kunFarRettIkkeAleneomsorgFlerbarnsdager(BeregnKontoerGrunnlag grunnlag) {
        return grunnlag.isFarRett() && !grunnlag.isMorRett() && !grunnlag.isFarAleneomsorg() && grunnlag.getAntallBarn() > 0;
    }

    private Evaluation beregnetMedResultat(Map<Stønadskontotype, Integer> kontoer,
                                           Integer antallExtraBarnDager,
                                           Integer antallPrematurDager) {
        var eval = ja();
        Map<String, Object> properties = new HashMap<>();
        properties.put(BeregnKontoerPropertyType.KONTOER, kontoer);
        properties.put(BeregnKontoerPropertyType.ANTALL_FLERBARN_DAGER, antallExtraBarnDager);
        properties.put(BeregnKontoerPropertyType.ANTALL_PREMATUR_DAGER, antallPrematurDager);

        eval.setEvaluationProperties(properties);
        return eval;
    }
}
