package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class FinnOppholdUtilTest {

    @Test
    public void skalFinneHullPåBegynnelsenAvEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                // !!! Skal finne et hull her (f.o.m. i dag t.o.m. i dag) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), start.plusDays(1), null, true), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, null, true)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(start);
        assertThat(hull.get(0).getTom()).isEqualTo(start);
    }

    @Test
    public void skalFinneEttHullInniEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, null, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et hull her (f.o.m. i morgen t.o.m. i morgen) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, null, true) // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(hull.get(0).getTom()).isEqualTo(start.plusDays(1));
    }

    @Test
    public void skalFinneFlereHullInniEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(4);

        /* Fem dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, null, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et hull her (f.o.m. i morgen t.o.m. i morgen) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(2), start.plusDays(2), null, true), // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                // !!! Skal finne et hull her (f.o.m. om tre dager t.o.m. om tre dager) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, null, true) // En uttaksperiode f.o.m. om fire dager t.o.m. om fire dager
        );

        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(hull.get(0).getTom()).isEqualTo(start.plusDays(1));
        assertThat(hull.get(1).getFom()).isEqualTo(start.plusDays(3));
        assertThat(hull.get(1).getTom()).isEqualTo(start.plusDays(3));
    }

    @Test
    public void skalFinneHullPåSluttenAvEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, null, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), start.plusDays(1), null, true) // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                // !!! Skal finne et hull her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(slutt);
        assertThat(hull.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    public void skalIkkeFinneHullHvisDetIkkeErNoenHull() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, null, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), start.plusDays(1), null, true), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, null, true)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                /// !!! Skal ikke finne noen hull !!!
        );


        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).isEmpty();
    }

    @Test
    public void skalHåndterePerioderSomBegynnnerFørPoi() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Collections.singletonList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.minusDays(1), slutt.minusDays(1), null, true) // En uttaksperiode f.o.m. i går t.o.m. i morgen
                // !!! Skal finne et hull her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(slutt);
        assertThat(hull.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    public void skalHåndterePerioderSomSlutterEtterPoi() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Collections.singletonList(
                // !!! Skal finne et hull her (f.o.m. i dag t.o.m. i dag) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), slutt.plusDays(1), null, true) // En uttaksperiode f.o.m. i morgen t.o.m. om tre dager
        );

        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(start);
        assertThat(hull.get(0).getTom()).isEqualTo(start);
    }

    @Test
    public void enDagPerioderSkalIkkeGiHull() {
        LukketPeriode førPeriode = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        LukketPeriode enDagPeriode = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));
        LukketPeriode etterPeriode = new LukketPeriode(LocalDate.of(2019, 1, 2), LocalDate.of(2019, 1, 10));
        List<LukketPeriode> perioder = Arrays.asList(førPeriode, enDagPeriode, etterPeriode);
        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, new LukketPeriode(førPeriode.getFom(), etterPeriode.getTom()));

        assertThat(hull).isEmpty();
    }

    @Test
    public void skalFinneFlereHull() {
        LukketPeriode periode1 = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        LukketPeriode periode2 = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2));
        LukketPeriode periode3 = new LukketPeriode(LocalDate.of(2019, 1, 3), LocalDate.of(2019, 1, 10));
        LukketPeriode periode4 = new LukketPeriode(LocalDate.of(2019, 1, 15), LocalDate.of(2019, 1, 20));
        LukketPeriode periode5 = new LukketPeriode(LocalDate.of(2019, 1, 25), LocalDate.of(2019, 1, 30));
        List<LukketPeriode> perioder = Arrays.asList(periode1, periode2, periode3, periode4, periode5);
        List<OppholdPeriode> hull = FinnOppholdUtil.finnOppholdIPeriode(perioder, new LukketPeriode(periode1.getFom(), periode5.getTom()));

        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(LocalDate.of(2019, 1, 11));
        assertThat(hull.get(0).getTom()).isEqualTo(LocalDate.of(2019, 1, 14));
        assertThat(hull.get(1).getFom()).isEqualTo(LocalDate.of(2019, 1, 21));
        assertThat(hull.get(1).getTom()).isEqualTo(LocalDate.of(2019, 1, 24));
    }
}