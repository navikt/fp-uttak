package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public final class TrekkdagerUtregningUtil {

    private TrekkdagerUtregningUtil() {
    }

    public static Trekkdager trekkdagerFor(Periode periode,
                                           boolean gradert,
                                           BigDecimal gradertArbeidstidsprosent,
                                           SamtidigUttaksprosent samtidigUttaksprosent) {
        int trekkdagerUtenGradering = Virkedager.beregnAntallVirkedager(periode);
        if (gradert) {
            return trekkdagerMedGradering(trekkdagerUtenGradering, gradertArbeidstidsprosent);
        } else if (samtidigUttaksprosent != null) {
            //Samme utregning som med gradering
            return trekkdagerMedGradering(trekkdagerUtenGradering,
                    BigDecimal.valueOf(100).subtract(samtidigUttaksprosent.decimalValue()));
        } else {
            return new Trekkdager(trekkdagerUtenGradering);
        }
    }

    private static Trekkdager trekkdagerMedGradering(int trekkdagerUtenGradering, BigDecimal gradertArbeidstidsprosent) {
        if (gradertArbeidstidsprosent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return Trekkdager.ZERO;
        }
        BigDecimal trekkdager = BigDecimal.valueOf(trekkdagerUtenGradering)
                .multiply(BigDecimal.valueOf(100).subtract(gradertArbeidstidsprosent))
                .divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
        return new Trekkdager(trekkdager);
    }

}
