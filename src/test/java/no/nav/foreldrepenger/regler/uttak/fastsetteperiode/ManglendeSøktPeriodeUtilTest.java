package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

class ManglendeSøktPeriodeUtilTest {

    @Test
    void skalFinneHullPåBegynnelsenAvEnPeriodOfInterest() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                // !!! Skal finne et msp her (f.o.m. i dag t.o.m. i dag) !!!
                periode(start.plusDays(1), start.plusDays(1)), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                periode(slutt, slutt)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(start);
        assertThat(msp.get(0).getTom()).isEqualTo(start);
    }

    private OppgittPeriode periode(LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forVanligPeriode(MØDREKVOTE, fom, tom, null, false, null, null,
                null, null);
    }

    @Test
    void skalFinneEttHullInniEnPeriodOfInterest() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(periode(start, start),
                // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et msp her (f.o.m. i morgen t.o.m. i morgen) !!!
                periode(slutt, slutt) // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(start.plusDays(1));
    }

    @Test
    void skalFinneFlereHullInniEnPeriodOfInterest() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(4);

        /* Fem dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(periode(start, start),
                // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et msp her (f.o.m. i morgen t.o.m. i morgen) !!!
                periode(start.plusDays(2), start.plusDays(2)), // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                // !!! Skal finne et msp her (f.o.m. om tre dager t.o.m. om tre dager) !!!
                periode(slutt, slutt) // En uttaksperiode f.o.m. om fire dager t.o.m. om fire dager
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(start.plusDays(1));
        assertThat(msp.get(1).getFom()).isEqualTo(start.plusDays(3));
        assertThat(msp.get(1).getTom()).isEqualTo(start.plusDays(3));
    }

    @Test
    void skalFinneHullPåSluttenAvEnPeriodOfInterest() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(periode(start, start),
                // En uttaksperiode f.o.m. i dag t.o.m. i dag
                periode(start.plusDays(1), start.plusDays(1)) // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                // !!! Skal finne et msp her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(slutt);
        assertThat(msp.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    void skalIkkeFinneHullHvisDetIkkeErNoenHull() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(periode(start, start),
                // En uttaksperiode f.o.m. i dag t.o.m. i dag
                periode(start.plusDays(1), start.plusDays(1)), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                periode(slutt, slutt)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                /// !!! Skal ikke finne noen msp !!!
        );


        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).isEmpty();
    }

    @Test
    void skalHåndterePerioderSomBegynnnerFørPoi() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = List.of(periode(start.minusDays(1), slutt.minusDays(1))
                // En uttaksperiode f.o.m. i går t.o.m. i morgen
                // !!! Skal finne et msp her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(slutt);
        assertThat(msp.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    void skalHåndterePerioderSomSlutterEtterPoi() {
        var start = LocalDate.of(2018, 6, 4);
        var slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        var poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = List.of(
                // !!! Skal finne et msp her (f.o.m. i dag t.o.m. i dag) !!!
                periode(start.plusDays(1), slutt.plusDays(1)) // En uttaksperiode f.o.m. i morgen t.o.m. om tre dager
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(start);
        assertThat(msp.get(0).getTom()).isEqualTo(start);
    }

    @Test
    void enDagPerioderSkalIkkeGiHull() {
        var førPeriode = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        var enDagPeriode = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));
        var etterPeriode = new LukketPeriode(LocalDate.of(2019, 1, 2), LocalDate.of(2019, 1, 10));
        var perioder = Arrays.asList(førPeriode, enDagPeriode, etterPeriode);
        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder,
                new LukketPeriode(førPeriode.getFom(), etterPeriode.getTom()));

        assertThat(msp).isEmpty();
    }

    @Test
    void skalFinneFlereHull() {
        var periode1 = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        var periode2 = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2));
        var periode3 = new LukketPeriode(LocalDate.of(2019, 1, 3), LocalDate.of(2019, 1, 10));
        var periode4 = new LukketPeriode(LocalDate.of(2019, 1, 15), LocalDate.of(2019, 1, 20));
        var periode5 = new LukketPeriode(LocalDate.of(2019, 1, 25), LocalDate.of(2019, 1, 30));
        var perioder = Arrays.asList(periode1, periode2, periode3, periode4, periode5);
        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder,
                new LukketPeriode(periode1.getFom(), periode5.getTom()));

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(LocalDate.of(2019, 1, 11));
        assertThat(msp.get(0).getTom()).isEqualTo(LocalDate.of(2019, 1, 14));
        assertThat(msp.get(1).getFom()).isEqualTo(LocalDate.of(2019, 1, 21));
        assertThat(msp.get(1).getTom()).isEqualTo(LocalDate.of(2019, 1, 24));
    }
}
