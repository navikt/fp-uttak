package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

final class DelRegelTestUtil {

    private static final FastsettePeriodeRegel REGEL = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    private DelRegelTestUtil() {
    }

    static FastsettePerioderRegelresultat kjørRegel(OppgittPeriode oppgittPeriode, RegelGrunnlag grunnlag) {
        return kjørRegel(oppgittPeriode, grunnlag, List.of());
    }

    static FastsettePerioderRegelresultat kjørRegel(OppgittPeriode oppgittPeriode, RegelGrunnlag grunnlag, List<FastsattUttakPeriode> søkersFastsattePerioder) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder,
                List.of(), grunnlag.getKontoer(), oppgittPeriode.getFom(), grunnlag.getArbeid().getAktiviteter());
        oppgittPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        return new FastsettePerioderRegelresultat(REGEL.evaluer(new FastsettePeriodeGrunnlagImpl(grunnlag, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), oppgittPeriode)));
    }

    static OppgittPeriode overføringsperiode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, OverføringÅrsak årsak, PeriodeVurderingType vurderingType) {
        return OppgittPeriode.forOverføring(stønadskontotype, fom, tom, PeriodeKilde.SØKNAD, vurderingType, årsak);
    }

    static OppgittPeriode gradertPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return gradertPeriode(stønadskontotype, fom, tom, Set.of(AktivitetIdentifikator.forFrilans()));
    }

    static OppgittPeriode gradertPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, Set<AktivitetIdentifikator> gradertAktiviteter) {
        return gradertPeriode(stønadskontotype, fom, tom, gradertAktiviteter, PeriodeVurderingType.IKKE_VURDERT);
    }

    static OppgittPeriode gradertPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, Set<AktivitetIdentifikator> gradertAktiviteter, PeriodeVurderingType vurderingType) {
        return OppgittPeriode.forGradering(stønadskontotype, fom, tom, PeriodeKilde.SØKNAD, BigDecimal.TEN, null,
                false, gradertAktiviteter, vurderingType);
    }

    static OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return oppgittPeriode(stønadskontotype, fom, tom, PeriodeVurderingType.IKKE_VURDERT);
    }

    static OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, PeriodeVurderingType vurderingType) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, PeriodeKilde.SØKNAD,
                null, false, vurderingType);
    }

    static OppgittPeriode oppholdPeriode(LocalDate fom, LocalDate tom, OppholdÅrsak årsak) {
        return OppgittPeriode.forOpphold(fom, tom, PeriodeKilde.SØKNAD, årsak);
    }

    static OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelsesÅrsak) {
        return OppgittPeriode.forUtsettelse(fom, tom, PeriodeKilde.SØKNAD, PeriodeVurderingType.IKKE_VURDERT, utsettelsesÅrsak);
    }
}
