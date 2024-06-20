package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTomForAlleSineKontoer.ID)
public class SjekkOmTomForAlleSineKontoer extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.1";
    public static final String BESKRIVELSE = "Er søker tom for alle sine kontoer?";

    public SjekkOmTomForAlleSineKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var sakMedMinsterett = grunnlag.isSakMedMinsterett();
        var tomForAlleSineKontoer = true;
        for (var stønadskontotype : hentSøkerSineKontoer(grunnlag)) {
            for (var aktivitet : grunnlag.getAktuellPeriode().getAktiviteter()) {
                var nettosaldo = grunnlag.getSaldoUtregning()
                        .nettoSaldoJustertForMinsterett(
                                stønadskontotype,
                                aktivitet,
                                !sakMedMinsterett
                                        || grunnlag.getAktuellPeriode().kanTrekkeAvMinsterett());
                if (nettosaldo.merEnn0()) {
                    tomForAlleSineKontoer = false;
                }
            }
        }
        return tomForAlleSineKontoer ? ja() : nei();
    }

    static List<Stønadskontotype> hentSøkerSineKontoer(FastsettePeriodeGrunnlag grunnlag) {
        final List<Stønadskontotype> søkerSineKonto;
        if (!søkerOgAnnenForelderSineKontoer(grunnlag).contains(Stønadskontotype.FORELDREPENGER)) {
            if (grunnlag.isSøkerMor()) {
                søkerSineKonto = Arrays.asList(
                        Stønadskontotype.MØDREKVOTE,
                        Stønadskontotype.FELLESPERIODE,
                        Stønadskontotype.FORELDREPENGER); // 1 og 5
            } else {
                søkerSineKonto = Arrays.asList(
                        Stønadskontotype.FEDREKVOTE,
                        Stønadskontotype.FELLESPERIODE,
                        Stønadskontotype.FORELDREPENGER); // 3 og 7
            }
        } else { // en har rett
            søkerSineKonto = List.of(Stønadskontotype.FORELDREPENGER); // 2 4 6 og 8
        }
        return søkerSineKonto;
    }

    private static List<Stønadskontotype> søkerOgAnnenForelderSineKontoer(FastsettePeriodeGrunnlag grunnlag) {
        return new ArrayList<>(grunnlag.getGyldigeStønadskontotyper());
    }
}
