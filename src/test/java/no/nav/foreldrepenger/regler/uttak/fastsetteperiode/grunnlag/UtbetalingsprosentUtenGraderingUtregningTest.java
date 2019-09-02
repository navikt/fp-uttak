package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class UtbetalingsprosentUtenGraderingUtregningTest {

    @Test
    public void ettArbeidsforholdMed0ArbeidsprosentGir100Utbetalingsprosent() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent = BigDecimal.ZERO;
        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent, stillingsprosent))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forArbeid("orgnr", "id");
        arbeidsprosenter.leggTil(aktivitetIdentifikator, tidslinje);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator, periode).resultat()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void toArbeidsforholdMedRedusertArbeidsprosentGirRedusertUtbetalingsprosent() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent1 = BigDecimal.ZERO;
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(20);
        BigDecimal stillingsprosent1 = BigDecimal.valueOf(50);
        BigDecimal stillingsprosent2 = BigDecimal.valueOf(40);
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje1 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent1, stillingsprosent1))
                .build();
        ArbeidTidslinje tidslinje2 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent2, stillingsprosent2))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator1 = AktivitetIdentifikator.forArbeid("orgnr1", "id");
        AktivitetIdentifikator aktivitetIdentifikator2 = AktivitetIdentifikator.forArbeid("orgnr2", null);
        arbeidsprosenter.leggTil(aktivitetIdentifikator1, tidslinje1);
        arbeidsprosenter.leggTil(aktivitetIdentifikator2, tidslinje2);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator1, periode).resultat()).isEqualTo(new BigDecimal("100.00"));
        //redusert Utbetalingsgrad (i %) = (stillingsprosent – arbeidsprosent) / stillingsprosent
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator2, periode).resultat()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    public void redusertArbeidsprosentSkalGi2DesimalerOgHalfUpAvrunding() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent = BigDecimal.valueOf(4);
        BigDecimal stillingsprosent = BigDecimal.valueOf(21);
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje1 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent, stillingsprosent))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator1 = AktivitetIdentifikator.forArbeid("orgnr1", "id");
        arbeidsprosenter.leggTil(aktivitetIdentifikator1, tidslinje1);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator1, periode).resultat()).isEqualTo(new BigDecimal("80.95"));
    }

    @Test
    public void skalGi0UtbetalingsprosentHvisArbeidstidsprosentErStørreEnnStillingsprosent() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent = BigDecimal.valueOf(40);
        BigDecimal stillingsprosent = BigDecimal.valueOf(30);
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje1 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent, stillingsprosent))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator1 = AktivitetIdentifikator.forArbeid("orgnr1", "id");
        arbeidsprosenter.leggTil(aktivitetIdentifikator1, tidslinje1);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator1, periode).resultat()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void hvis0StillingsprosentSkalDetGis100Utbetaling() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent = BigDecimal.ZERO;
        BigDecimal stillingsprosent = BigDecimal.ZERO;
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent, stillingsprosent))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forArbeid("orgnr", "id");
        arbeidsprosenter.leggTil(aktivitetIdentifikator, tidslinje);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator, periode).resultat()).isEqualTo(new BigDecimal("100.00"));
    }


    private UtbetalingsprosentUtenGraderingUtregning utregning(Arbeidsprosenter arbeidsprosenter,
                                                               AktivitetIdentifikator aktivitetIdentifikator,
                                                               LukketPeriode periode) {
        return new UtbetalingsprosentUtenGraderingUtregning(arbeidsprosenter, aktivitetIdentifikator, periode);
    }


}