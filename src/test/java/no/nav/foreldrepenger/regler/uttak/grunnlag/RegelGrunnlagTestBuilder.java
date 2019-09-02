package no.nav.foreldrepenger.regler.uttak.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class RegelGrunnlagTestBuilder {

    public static final AktivitetIdentifikator ARBEIDSFORHOLD_1 = AktivitetIdentifikator.forArbeid("000000001", null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_2 = AktivitetIdentifikator.forArbeid("000000002", null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_3 = AktivitetIdentifikator.forArbeid("000000003", null);

    public static RegelGrunnlag.Builder create() {
        return normal();
    }

    public static RegelGrunnlag.Builder normal() {
        ArbeidTidslinje tidslinje = new ArbeidTidslinje.Builder().build();
        ArbeidGrunnlag arbeidGrunnlag = new ArbeidGrunnlag.Builder()
                .medArbeidsprosenter(new Arbeidsprosenter().leggTil(ARBEIDSFORHOLD_1, tidslinje))
                .build();
        return new RegelGrunnlag.Builder()
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(LocalDate.MIN).build())
                .medBehandling(new Behandling.Builder().medSøkerErMor(true).medType(Behandlingtype.FØRSTEGANGSSØKNAD).build())
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true).medFarHarRett(true).medSamtykke(true).build())
                .medArbeid(arbeidGrunnlag)
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true)
                        .build());
    }

    public static RegelGrunnlag.Builder enGraderingsperiode(LocalDate fom, LocalDate tom, BigDecimal arbeidsprosent) {
        return enGraderingsperiode(fom, tom, Arbeid.forOrdinærtArbeid(arbeidsprosent, BigDecimal.valueOf(100)), Collections.singletonList(ARBEIDSFORHOLD_1));
    }

    public static RegelGrunnlag.Builder enGraderingsperiodeMedFlereAktiviteter(LocalDate fom, LocalDate tom,
                                                                               BigDecimal arbeidsprosent, List<AktivitetIdentifikator> aktivititeter) {
        return enGraderingsperiode(fom, tom, Arbeid.forOrdinærtArbeid(arbeidsprosent, BigDecimal.valueOf(100)), aktivititeter);
    }

    private static RegelGrunnlag.Builder enGraderingsperiode(LocalDate fom, LocalDate tom, Arbeid arbeid, List<AktivitetIdentifikator> aktiviteter) {
        ArbeidTidslinje tidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(fom, tom, arbeid)
                .medArbeid(tom.plusDays(1), LocalDateInterval.TIDENES_ENDE, Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100)))
                .build();
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        for (AktivitetIdentifikator aktivitet : aktiviteter) {
            arbeidsprosenter.leggTil(aktivitet, tidslinje);
        }
        ArbeidGrunnlag arbeidGrunnlag = new ArbeidGrunnlag.Builder().medArbeidsprosenter(arbeidsprosenter).build();
        return new RegelGrunnlag.Builder()
                .medArbeid(arbeidGrunnlag);
    }


}
