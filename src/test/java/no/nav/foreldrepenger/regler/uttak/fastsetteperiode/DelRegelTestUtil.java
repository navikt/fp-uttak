package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningTjeneste;

final class DelRegelTestUtil {

    private static final FastsettePeriodeRegel REGEL = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    private DelRegelTestUtil() {
    }

    static Regelresultat kjørRegel(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), false,
                List.of(), grunnlag.getArbeid().getArbeidsforhold(), uttakPeriode.getFom());
        return kjørRegel(uttakPeriode, grunnlag, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag));
    }

    static Regelresultat kjørRegel(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag, SaldoUtregning saldoUtregning) {
        uttakPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        return new Regelresultat(REGEL.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, saldoUtregning, uttakPeriode)));
    }
}
