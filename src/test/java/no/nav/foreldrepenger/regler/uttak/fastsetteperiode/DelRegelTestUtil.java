package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

final class DelRegelTestUtil {

    private static final FastsettePeriodeRegel REGEL = new FastsettePeriodeRegel();

    private DelRegelTestUtil() {
    }

    static FastsettePerioderRegelresultat kjørRegel(OppgittPeriode oppgittPeriode, RegelGrunnlag grunnlag) {
        return kjørRegel(oppgittPeriode, grunnlag, List.of());
    }

    static FastsettePerioderRegelresultat kjørRegel(OppgittPeriode oppgittPeriode,
                                                    RegelGrunnlag grunnlag,
                                                    List<FastsattUttakPeriode> søkersFastsattePerioder) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder, List.of(),
                grunnlag, oppgittPeriode.getFom());
        oppgittPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        return new FastsettePerioderRegelresultat(REGEL.evaluer(
                new FastsettePeriodeGrunnlagImpl(grunnlag, null, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag),
                        oppgittPeriode)));
    }

    static FastsettePerioderRegelresultat kjørRegel(OppgittPeriode oppgittPeriode,
                                                    RegelGrunnlag grunnlag,
                                                    List<FastsattUttakPeriode> søkersFastsattePerioder,
                                                    LukketPeriode farRundtFødselIntervall) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder, List.of(),
                grunnlag, oppgittPeriode.getFom());
        oppgittPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        return new FastsettePerioderRegelresultat(REGEL.evaluer(
                new FastsettePeriodeGrunnlagImpl(grunnlag, farRundtFødselIntervall,
                        SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag), oppgittPeriode)));
    }

    static OppgittPeriode overføringsperiode(Stønadskontotype stønadskontotype,
                                             LocalDate fom,
                                             LocalDate tom,
                                             OverføringÅrsak årsak,
                                             DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forOverføring(stønadskontotype, fom, tom, årsak, null, null, dokumentasjonVurdering);
    }

    static OppgittPeriode gradertPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return gradertPeriode(stønadskontotype, fom, tom, Set.of(AktivitetIdentifikator.forFrilans()));
    }

    static OppgittPeriode gradertPeriode(Stønadskontotype stønadskontotype,
                                         LocalDate fom,
                                         LocalDate tom,
                                         Set<AktivitetIdentifikator> gradertAktiviteter) {
        return gradertPeriode(stønadskontotype, fom, tom, gradertAktiviteter, null);
    }

    static OppgittPeriode gradertPeriode(Stønadskontotype stønadskontotype,
                                         LocalDate fom,
                                         LocalDate tom,
                                         Set<AktivitetIdentifikator> gradertAktiviteter,
                                         DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forGradering(stønadskontotype, fom, tom, BigDecimal.TEN, null, false, gradertAktiviteter,
                null, null, null, dokumentasjonVurdering);
    }

    static OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return oppgittPeriode(stønadskontotype, fom, tom, null);
    }

    static OppgittPeriode oppgittPeriode(Stønadskontotype stønadskontotype,
                                         LocalDate fom,
                                         LocalDate tom,
                                         DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forVanligPeriode(stønadskontotype, fom, tom, null, false, null, null, null, dokumentasjonVurdering);
    }

    static OppgittPeriode oppholdPeriode(LocalDate fom, LocalDate tom, OppholdÅrsak årsak) {
        return OppgittPeriode.forOpphold(fom, tom, årsak, null, null);
    }

    static OppgittPeriode utsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsak utsettelsesÅrsak, DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forUtsettelse(fom, tom, utsettelsesÅrsak, null, null, null, dokumentasjonVurdering);
    }
}
