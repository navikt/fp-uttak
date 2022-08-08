package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.PrematurukerUtil;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettKontoer.ID)
class OpprettKontoer extends LeafSpecification<BeregnKontoerGrunnlag> {

    private static final String KONTOER = "KONTOER";
    private static final String ANTALL_FLERBARN_DAGER = "ANTALL_FLERBARN_DAGER";
    private static final String ANTALL_PREMATUR_DAGER = "ANTALL_PREMATUR_DAGER";

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
        if (Arrays.stream(kontokonfigurasjoner).findAny().isEmpty()) {
            return manglerOpptjening();
        }
        Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        var antallPrematurDager = 0;

        // Finn antall ekstra dager først.
        var antallExtraBarnDager = finnEkstraFlerbarnsdager(grunnlag);

        // Opprette alle kontoer utenom samtidig uttak
        for (var kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.getStønadskontotype() != StønadskontoBeregningStønadskontotype.FLERBARNSDAGER) {
                var antallDager = konfigurasjon.getParameter(kontokonfigurasjon.getParametertype(),
                        grunnlag.getFamiliehendelsesdato());
                if (antallExtraBarnDager > 0) {
                    // Legg ekstra dager til foreldrepenger eller fellesperiode.
                    if ((kontokonfigurasjon.getStønadskontotype().equals(StønadskontoBeregningStønadskontotype.FORELDREPENGER))) {
                        antallDager += antallExtraBarnDager;
                        if (kunFarRettIkkeAleneomsorgFlerbarnsdager(grunnlag) && !grunnlag.isMinsterett()) {
                            kontoerMap.put(StønadskontoBeregningStønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
                        }
                    } else if (kontokonfigurasjon.getStønadskontotype().equals(StønadskontoBeregningStønadskontotype.FELLESPERIODE)) {
                        antallDager += antallExtraBarnDager;
                        kontoerMap.put(StønadskontoBeregningStønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
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
        return kontokonfigurasjon.getStønadskontotype().equals(StønadskontoBeregningStønadskontotype.FELLESPERIODE)
                || kontokonfigurasjon.getStønadskontotype().equals(StønadskontoBeregningStønadskontotype.FORELDREPENGER);
    }

    private int finnEkstraFlerbarnsdager(BeregnKontoerGrunnlag grunnlag) {
        for (var kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.getStønadskontotype() == StønadskontoBeregningStønadskontotype.FLERBARNSDAGER) {
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

    private Evaluation beregnetMedResultat(Map<StønadskontoBeregningStønadskontotype, Integer> kontoer,
                                           Integer antallExtraBarnDager,
                                           Integer antallPrematurDager) {
        var outcome = new KontoOutcome(kontoer)
            .medAntallExtraBarnDager(antallExtraBarnDager)
            .medAntallPrematurDager(antallPrematurDager);
        var eval = ja(outcome);
        eval.setEvaluationProperty(KONTOER, kontoer);
        eval.setEvaluationProperty(ANTALL_FLERBARN_DAGER, antallExtraBarnDager);
        eval.setEvaluationProperty(ANTALL_PREMATUR_DAGER, antallPrematurDager);

        return eval;
    }

    private Evaluation manglerOpptjening() {
        var utfall = KontoOutcome.ikkeOppfylt("Hverken far eller mor har opptjent rett til foreldrepenger.");
        return nei(utfall);
    }
}
