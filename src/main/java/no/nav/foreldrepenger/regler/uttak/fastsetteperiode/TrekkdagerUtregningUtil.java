package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;

public final class TrekkdagerUtregningUtil {

    private TrekkdagerUtregningUtil() {
    }

    public static Trekkdager trekkdagerFor(Periode periode,
                                           boolean gradert,
                                           BigDecimal gradertArbeidstidsprosent,
                                           SamtidigUttaksprosent samtidigUttaksprosent,
                                           MorsStillingsprosent morsStillingsprosent) {
        var virkedager = Virkedager.beregnAntallVirkedager(periode);
        if (gradert && samtidigUttaksprosent != null) {
            //TODO
        }
        if (gradert) {
            return trekkdagerMedGradering(virkedager, gradertArbeidstidsprosent, morsStillingsprosent);
        }
        if (samtidigUttaksprosent != null) {
            //Samme utregning som med gradering
            return trekkdagerMedGradering(virkedager, BigDecimal.valueOf(100).subtract(samtidigUttaksprosent.decimalValue()), morsStillingsprosent);
        }
        return new Trekkdager(virkedager);
    }

    private static Trekkdager trekkdagerMedGradering(int trekkdagerUtenGradering,
                                                     BigDecimal gradertArbeidstidsprosent,
                                                     MorsStillingsprosent morsStillingsprosent) {

        if (gradertArbeidstidsprosent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return Trekkdager.ZERO;
        }
        var trekkprosent = utledTrekkprosent(morsStillingsprosent, gradertArbeidstidsprosent);
        var trekkdager = BigDecimal.valueOf(trekkdagerUtenGradering)
            .multiply(trekkprosent);
        return new Trekkdager(trekkdager);
    }

    private static BigDecimal utledTrekkprosent(MorsStillingsprosent morsStillingsprosent, BigDecimal gradertArbeidstidsprosent) {
        var søktUttaksprosent = BigDecimal.valueOf(100).subtract(gradertArbeidstidsprosent);
        if (morsStillingsprosent == null) {
            return søktUttaksprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_DOWN);
        }
        if (morsStillingsprosent.decimalValue().compareTo(søktUttaksprosent) > 0) {
            return søktUttaksprosent.divide(morsStillingsprosent.decimalValue(), 10, RoundingMode.HALF_DOWN);
        }
        return BigDecimal.ONE;
    }

}
