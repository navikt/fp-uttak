package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelseÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.FarUttakRundtFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt.FastsettePeriodeRegel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;

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
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder, List.of(), grunnlag,
            oppgittPeriode.getFom());
        oppgittPeriode.setAktiviteter(grunnlag.getArbeid().getAktiviteter());
        var farRundtFødselIntervall = FarUttakRundtFødsel.utledFarsPeriodeRundtFødsel(grunnlag).orElse(null);
        return new FastsettePerioderRegelresultat(REGEL.evaluer(
            new FastsettePeriodeGrunnlagImpl(grunnlag, farRundtFødselIntervall, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag),
                oppgittPeriode)));
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
        return OppgittPeriode.forGradering(stønadskontotype, fom, tom, BigDecimal.TEN, null, false, gradertAktiviteter, null, null, null,
            dokumentasjonVurdering);
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

    static OppgittPeriode utsettelsePeriode(LocalDate fom,
                                            LocalDate tom,
                                            UtsettelseÅrsak utsettelsesÅrsak,
                                            DokumentasjonVurdering dokumentasjonVurdering) {
        return OppgittPeriode.forUtsettelse(fom, tom, utsettelsesÅrsak, null, null, null, dokumentasjonVurdering);
    }
}
