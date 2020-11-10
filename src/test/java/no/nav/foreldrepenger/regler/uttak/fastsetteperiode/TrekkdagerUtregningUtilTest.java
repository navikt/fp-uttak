package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class TrekkdagerUtregningUtilTest {

    @Test
    public void skal_runde_ned_ved_gradering() {

        LocalDate fom = LocalDate.of(2019, 3, 14);
        LocalDate tom = LocalDate.of(2019, 3, 15);
        //periode på 2 dager, 1% gradering
        BigDecimal arbeidstidsprosent = BigDecimal.valueOf(1);
        OppgittPeriode periode = OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, arbeidstidsprosent, null,
                false, Set.of(), PeriodeVurderingType.IKKE_VURDERT, null);
        Trekkdager trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, true, arbeidstidsprosent, null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(1.9));
    }

    @Test
    public void skal_redusere_trekkdager_ved_samtidig_uttak_uten_gradering() {

        //10 virkdager
        LocalDate fom = LocalDate.of(2019, 4, 1);
        LocalDate tom = LocalDate.of(2019, 4, 12);

        SamtidigUttaksprosent samtidigUttaksprosent = new SamtidigUttaksprosent(50);
        OppgittPeriode periode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, samtidigUttaksprosent,
                false, PeriodeVurderingType.IKKE_VURDERT, null);
        Trekkdager trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, false, null, samtidigUttaksprosent);

        assertThat(trekkdager).isEqualTo(new Trekkdager(5));
    }
}
