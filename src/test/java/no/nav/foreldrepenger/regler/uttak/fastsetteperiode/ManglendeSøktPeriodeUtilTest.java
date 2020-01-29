package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class ManglendeSøktPeriodeUtilTest {

    @Test
    public void skalFinneHullPåBegynnelsenAvEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
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
        return OppgittPeriode.forVanligPeriode(MØDREKVOTE, fom, tom, PeriodeKilde.SØKNAD, null, false, PeriodeVurderingType.IKKE_VURDERT);
    }

    @Test
    public void skalFinneEttHullInniEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                periode(start, start), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et msp her (f.o.m. i morgen t.o.m. i morgen) !!!
                periode(slutt, slutt) // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(msp.get(0).getTom()).isEqualTo(start.plusDays(1));
    }

    @Test
    public void skalFinneFlereHullInniEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(4);

        /* Fem dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                periode(start, start), // En uttaksperiode f.o.m. i dag t.o.m. i dag
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
    public void skalFinneHullPåSluttenAvEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                periode(start, start), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                periode(start.plusDays(1), start.plusDays(1)) // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                // !!! Skal finne et msp her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(slutt);
        assertThat(msp.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    public void skalIkkeFinneHullHvisDetIkkeErNoenHull() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                periode(start, start), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                periode(start.plusDays(1), start.plusDays(1)), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                periode(slutt, slutt)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                /// !!! Skal ikke finne noen msp !!!
        );


        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).isEmpty();
    }

    @Test
    public void skalHåndterePerioderSomBegynnnerFørPoi() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = List.of(
                periode(start.minusDays(1), slutt.minusDays(1)) // En uttaksperiode f.o.m. i går t.o.m. i morgen
                // !!! Skal finne et msp her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, poi);

        assertThat(msp).hasSize(1);
        assertThat(msp.get(0).getFom()).isEqualTo(slutt);
        assertThat(msp.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    public void skalHåndterePerioderSomSlutterEtterPoi() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
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
    public void enDagPerioderSkalIkkeGiHull() {
        LukketPeriode førPeriode = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        LukketPeriode enDagPeriode = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));
        LukketPeriode etterPeriode = new LukketPeriode(LocalDate.of(2019, 1, 2), LocalDate.of(2019, 1, 10));
        List<LukketPeriode> perioder = Arrays.asList(førPeriode, enDagPeriode, etterPeriode);
        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, new LukketPeriode(førPeriode.getFom(), etterPeriode.getTom()));

        assertThat(msp).isEmpty();
    }

    @Test
    public void skalFinneFlereHull() {
        LukketPeriode periode1 = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        LukketPeriode periode2 = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2));
        LukketPeriode periode3 = new LukketPeriode(LocalDate.of(2019, 1, 3), LocalDate.of(2019, 1, 10));
        LukketPeriode periode4 = new LukketPeriode(LocalDate.of(2019, 1, 15), LocalDate.of(2019, 1, 20));
        LukketPeriode periode5 = new LukketPeriode(LocalDate.of(2019, 1, 25), LocalDate.of(2019, 1, 30));
        List<LukketPeriode> perioder = Arrays.asList(periode1, periode2, periode3, periode4, periode5);
        var msp = ManglendeSøktPeriodeUtil.finnManglendeSøktePerioder(perioder, new LukketPeriode(periode1.getFom(), periode5.getTom()));

        assertThat(msp).hasSize(2);
        assertThat(msp.get(0).getFom()).isEqualTo(LocalDate.of(2019, 1, 11));
        assertThat(msp.get(0).getTom()).isEqualTo(LocalDate.of(2019, 1, 14));
        assertThat(msp.get(1).getFom()).isEqualTo(LocalDate.of(2019, 1, 21));
        assertThat(msp.get(1).getTom()).isEqualTo(LocalDate.of(2019, 1, 24));
    }
}
