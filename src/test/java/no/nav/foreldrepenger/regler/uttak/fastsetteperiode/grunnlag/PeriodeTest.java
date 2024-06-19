package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PeriodeTest {

    @Test
    void periode_med_start_og_slutt_og_dato_utenfor_skal_ikke_overlappe() {
        var testPeriode = new Periode(LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(testPeriode.overlapper(LocalDate.of(2019, 1, 1))).isFalse();
    }

    @Test
    void periode_uten_start_og_slutt_skal_overlappe() {
        var testPeriode = new Periode(null, null);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    void periode_med_start_og_slutt_skal_overlappe() {
        var testPeriode = new Periode(LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    void periode_med_bare_start_skal_overlappe() {
        var testPeriode = new Periode(LocalDate.of(2016, 1, 1), null);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    void periode_med_bare_start_og_dato_før_start_skal_ikke_overlappe() {
        var testPeriode = new Periode(LocalDate.of(2016, 1, 1), null);

        assertThat(testPeriode.overlapper(LocalDate.of(2015, 1, 1))).isFalse();
    }

    @Test
    void helePeriodenOverlapper() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(1), tom.plusDays(1))))
                .isTrue();
        assertThat(periode.overlapper(new LukketPeriode(fom.plusDays(1), tom.minusDays(1))))
                .isTrue();
        assertThat(periode.overlapper(periode)).isTrue();
    }

    @Test
    void begynnelsenAvPeriodenOverlapper() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(1), tom.minusDays(1))))
                .isTrue();
        assertThat(periode.overlapper(new LukketPeriode(fom, fom))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(1), fom))).isTrue();
    }

    @Test
    void sluttenAvPeriodenOverlapper() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.plusDays(1), tom.plusDays(1))))
                .isTrue();
        assertThat(periode.overlapper(new LukketPeriode(tom, tom))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(tom, tom.plusDays(1)))).isTrue();
    }

    @Test
    void periodenRettFørOverlapperIkke() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(10), fom.minusDays(1))))
                .isFalse();
    }

    @Test
    void periodenRettEtterOverlapperIkke() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(tom.plusDays(1), tom.plusDays(5))))
                .isFalse();
    }
}
