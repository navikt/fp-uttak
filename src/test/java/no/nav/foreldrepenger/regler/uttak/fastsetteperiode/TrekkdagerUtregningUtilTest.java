package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

import static org.assertj.core.api.Assertions.assertThat;

public class TrekkdagerUtregningUtilTest {

    @Test
    public void skal_runde_ned_ved_gradering() {

        LocalDate fom = LocalDate.of(2019, 3, 14);
        LocalDate tom = LocalDate.of(2019, 3, 15);
        //periode på 2 dager, 1% gradering
        BigDecimal arbeidstidsprosent = BigDecimal.valueOf(1);
        UttakPeriode periode = StønadsPeriode.medGradering(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,
                Collections.emptyList(), arbeidstidsprosent, PeriodeVurderingType.PERIODE_OK);
        Trekkdager trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, true, arbeidstidsprosent, null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(BigDecimal.valueOf(1.9)));
    }

    @Test
    public void skal_redusere_trekkdager_ved_samtidig_uttak_uten_gradering() {

        //10 virkdager
        LocalDate fom = LocalDate.of(2019, 4, 1);
        LocalDate tom = LocalDate.of(2019, 4, 12);

        BigDecimal samtidigUttaksprosent = BigDecimal.valueOf(50);
        UttakPeriode periode = new StønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, new SamtidigUttak(samtidigUttaksprosent), false);
        Trekkdager trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, false, null, samtidigUttaksprosent);

        assertThat(trekkdager).isEqualTo(new Trekkdager(BigDecimal.valueOf(5)));
    }
}
