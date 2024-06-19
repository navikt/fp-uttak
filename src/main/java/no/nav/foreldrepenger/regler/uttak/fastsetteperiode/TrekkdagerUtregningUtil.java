package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;

public final class TrekkdagerUtregningUtil {

    private TrekkdagerUtregningUtil() {}

    public static Trekkdager trekkdagerFor(
            Periode periode,
            boolean gradert,
            BigDecimal gradertArbeidstidsprosent,
            SamtidigUttaksprosent samtidigUttaksprosent) {
        var trekkdagerUtenGradering = Virkedager.beregnAntallVirkedager(periode);
        if (gradert) {
            return trekkdagerMedGradering(trekkdagerUtenGradering, gradertArbeidstidsprosent);
        }
        if (samtidigUttaksprosent != null) {
            // Samme utregning som med gradering
            return trekkdagerMedGradering(
                    trekkdagerUtenGradering,
                    BigDecimal.valueOf(100).subtract(samtidigUttaksprosent.decimalValue()));
        }
        return new Trekkdager(trekkdagerUtenGradering);
    }

    private static Trekkdager trekkdagerMedGradering(
            int trekkdagerUtenGradering, BigDecimal gradertArbeidstidsprosent) {
        if (gradertArbeidstidsprosent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return Trekkdager.ZERO;
        }
        var trekkdager =
                BigDecimal.valueOf(trekkdagerUtenGradering)
                        .multiply(BigDecimal.valueOf(100).subtract(gradertArbeidstidsprosent))
                        .divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
        return new Trekkdager(trekkdager);
    }
}
