package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsStillingsprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

class TrekkdagerUtregningUtilTest {

    @Test
    void mors_stillingsprosent_øker_antall_trekkdager_ved_gradering() {
        var fom = LocalDate.of(2024, 6, 20);
        var tom = fom.plusWeeks(6).minusDays(1);
        var arbeidstidsprosent = BigDecimal.valueOf(80);
        var morsStillingsprosent = new MorsStillingsprosent(40);
        var periode = OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, arbeidstidsprosent, null, false, Set.of(), null, null,
            null, null, null);
        var trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, true, arbeidstidsprosent, null, morsStillingsprosent);

        assertThat(trekkdager).isEqualTo(new Trekkdager(15));
    }

    @Test
    void skal_runde_ned_ved_gradering() {

        var fom = LocalDate.of(2019, 3, 14);
        var tom = LocalDate.of(2019, 3, 15);
        //periode på 2 dager, 1% gradering
        var arbeidstidsprosent = BigDecimal.valueOf(1);
        var periode = OppgittPeriode.forGradering(Stønadskontotype.FORELDREPENGER, fom, tom, arbeidstidsprosent, null, false, Set.of(), null, null,
            null, null, null);
        var trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, true, arbeidstidsprosent, null, null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(1.9));
    }

    @Test
    void skal_redusere_trekkdager_ved_samtidig_uttak_uten_gradering() {

        //10 virkdager
        var fom = LocalDate.of(2019, 4, 1);
        var tom = LocalDate.of(2019, 4, 12);

        var samtidigUttaksprosent = new SamtidigUttaksprosent(50);
        var periode = OppgittPeriode.forVanligPeriode(Stønadskontotype.FORELDREPENGER, fom, tom, samtidigUttaksprosent, false, null, null, null, null,
            null);
        var trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(periode, false, null, samtidigUttaksprosent, null);

        assertThat(trekkdager).isEqualTo(new Trekkdager(5));
    }
}
