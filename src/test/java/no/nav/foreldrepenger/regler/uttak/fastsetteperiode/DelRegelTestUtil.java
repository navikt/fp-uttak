package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.util.List;

import no.nav.foreldrepenger.regler.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

final class DelRegelTestUtil {

    private static final FastsettePeriodeRegel REGEL = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    private DelRegelTestUtil() {
    }

    static Regelresultat kjørRegel(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return kjørRegel(uttakPeriode, grunnlag, List.of());
    }

    static Regelresultat kjørRegel(UttakPeriode uttakPeriode, RegelGrunnlag grunnlag, List<FastsattUttakPeriode> søkersFastsattePerioder) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder,
                List.of(), grunnlag.getKontoer(), uttakPeriode.getFom(), grunnlag.getArbeid().getAktiviteter());
        uttakPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        return new Regelresultat(REGEL.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), uttakPeriode)));
    }

}
