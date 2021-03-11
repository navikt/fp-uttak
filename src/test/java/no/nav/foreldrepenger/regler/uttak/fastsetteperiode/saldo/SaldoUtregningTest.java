package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;


import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class SaldoUtregningTest {

    private static final AktivitetIdentifikator AKTIVITET1_SØKER = AktivitetIdentifikator.forArbeid("111", "222");
    private static final AktivitetIdentifikator AKTIVITET2_SØKER = AktivitetIdentifikator.forArbeid("333", "444");
    private static final AktivitetIdentifikator AKTIVITET1_ANNENPART = AktivitetIdentifikator.forArbeid("555", "666");
    private static final AktivitetIdentifikator AKTIVITET2_ANNENPART = AktivitetIdentifikator.forArbeid("777", "888");
    private final LocalDate enTirsdag = LocalDate.of(2019, 2, 19);


    private Stønadskonto stønadskonto(Stønadskontotype kontoType, int trekkdager) {
        return new Stønadskonto(kontoType, new Trekkdager(trekkdager));
    }

    @Test
    public void ingen_max_dag_og_ingen_trekkdager_skal_alltid_gi_0_i_saldo() {
        FastsattUttakPeriode fastsattUttakPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 0)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isZero();
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isZero();
    }

    @Test
    public void max_dager_minus_trekkdag_skal_bli_saldo() {
        FastsattUttakPeriode fastsattUttakPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(10 - 5);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(10 - 5);
    }

    @Test
    public void flere_trekk_gir_riktig_saldo() {
        FastsattUttakPeriode fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        FastsattUttakPeriode fastsattUttakPeriode2 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode1, fastsattUttakPeriode2);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 12)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(12 - 5 - 5);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(12 - 5 - 5);
    }

    @Test
    public void skal_runde_opp_hvis_ikke_brukt_mer_enn_maks() {
        FastsattUttakPeriode fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10.5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode1);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 12)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(2);
    }

    @Test
    public void skal_runde_opp_hvis_brukt_mer_enn_maks() {
        FastsattUttakPeriode fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(13.5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode1);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 12)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(-1);
    }

    @Test
    public void for_stort_trekk_gir_riktig_saldo() {
        FastsattUttakPeriode fastsattUttakPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), MØDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(10 - 15);
    }

    @Test
    public void for_stort_trekk_på_flere_aktiviteter_gir_riktig_saldo() {
        FastsattUttakPeriode fastsattUttakPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), MØDREKVOTE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(20), MØDREKVOTE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET2_SØKER)).isEqualTo(10 - 20);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(10 - 15);
    }

    @Test
    public void flere_trekk_på_forskjellig_aktivitet_gir_forskjellig_saldo() {
        FastsattUttakPeriode fastsattUttakPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), MØDREKVOTE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), MØDREKVOTE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(fastsattUttakPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(5);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET2_SØKER)).isZero();
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(5);
    }

    @Test
    public void trekkdager_på_annen_part_skal_telle_med_i_saldo() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(3);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(-2);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    public void minste_trekkdager_på_annen_part_skal_telle_med_i_saldo() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(7), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(8);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(3);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(8);
    }

    @Test
    public void flere_trekk_på_annen_part_skal_telle_med_i_saldo() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        FastsattUttakPeriode annenpartPeriode1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        FastsattUttakPeriode annenpartPeriode2 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode1, annenpartPeriode2);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(11);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(6);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(11);
    }

    @Test
    public void ikke_stjele_men_summere_begge_parter_hvis_tapende_behandling() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .medSamtidigUttak(false)
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                true, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    public void stjele_fra_annenpart_hvis_ikke_tapende_behandling_og_ikke_samtidig_uttak() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .medSamtidigUttak(false)
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(15);
    }

    @Test
    public void skal_summere_trekkdager_for_begge_parter_hvis_overlapp_og_samtidig_uttak_og_ikke_tapende_behandling() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .medSamtidigUttak(true)
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    public void skal_trekke_virkedager_fra_oppholdsperioder() {
        FastsattUttakPeriode periodeSøker1 = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15))
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode periodeSøker2 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(LocalDate.of(2019, 2, 18), LocalDate.of(2019, 2, 18))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker1, periodeSøker2);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(10 - 5);
    }

    @Test
    public void skal_trekke_oppholdsperioder_for_annenpart() {
        FastsattUttakPeriode periodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        FastsattUttakPeriode oppholdAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 22))
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode periodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(LocalDate.of(2019, 2, 25), LocalDate.of(2019, 2, 25))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(oppholdAnnenpart, periodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 5 - 7);
    }

    @Test
    public void skal_ikke_stjele_fra_annenpart_hvis_søker_har_oppholdsperiode() {
        FastsattUttakPeriode oppholdsperiodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .medOppholdÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode periodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .build();
        FastsattUttakPeriode periodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker, oppholdsperiodeSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20), stønadskonto(MØDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 2);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(20 - 5);
    }

    @Test
    public void skal_ikke_knekke_hvis_periode_er_i_en_helg() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 16), LocalDate.of(2019, 2, 16))
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(LocalDate.of(2019, 2, 17), LocalDate.of(2019, 2, 17))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(3);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(-2);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    public void overlappende_oppholdsperioder_skal_trekke_fra_annenparts_periode_ved() {
        FastsattUttakPeriode periodeSøker1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        FastsattUttakPeriode oppholdsperiodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode oppholdsperiodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .medOppholdÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode periodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 18), LocalDate.of(2019, 2, 18))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker1, oppholdsperiodeSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(oppholdsperiodeAnnenpart, periodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20), stønadskonto(MØDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 2);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(20 - 3);
    }

    @Test
    public void overlapp_med_avslått_perioder_på_søker_skal_telles_dobbelt() {
        FastsattUttakPeriode søkerPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        FastsattUttakPeriode annenpartPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkerPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 12);
    }

    @Test
    public void hvis_søkers_innvilget_periode_overlapper_med_annenparts_oppholdsperiode_skal_det_ikke_trekkes_dobbelt() {
        FastsattUttakPeriode periodeSøker1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), MØDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        FastsattUttakPeriode oppholdsperiodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .medOppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode oppholdsperiodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .medOppholdÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                .build();
        FastsattUttakPeriode periodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker1, oppholdsperiodeSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(oppholdsperiodeAnnenpart, periodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 20), stønadskonto(FEDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isEqualTo(20 - 2);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(20 - 3);
    }

    @Test
    public void hvis_alle_søkers_perioder_er_etter_annenpart_skal_det_ikke_være_nok_dager_å_frigi() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15))
                .build();
        FastsattUttakPeriode periode2Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 16), LocalDate.of(2019, 2, 16))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 13), LocalDate.of(2019, 2, 13))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 14))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker, periode2Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    public void hvis_ikke_alle_søkers_perioder_er_etter_annenpart_skal_det_være_nok_dager_å_frigi() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 20))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 10), LocalDate.of(2019, 2, 14))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 20))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    public void annenpart_har_ikke_nok_dager_å_frigi_selv_med_perioder_etter_søkers_siste_periode_med_trekkdager() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    public void annenpart_har_ikke_nok_dager_å_frigi_selv_med_perioder_etter_søkers_siste_periode_med_trekkdager_flere_arbeidsforhold_hos_annenpart() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    public void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    public void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager_hvis_søkers_siste_periode_starter_samme_dag_som_annenparts_siste_periode() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 5))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(30), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 12))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 30)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    public void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager_flere_arbeidsforhold_hos_annenpart() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    public void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager_oppholdsperiode() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .medTidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 25))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    public void annenpart_har_ikke_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        FastsattUttakPeriode periode2Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FEDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 4))
                .build();

        FastsattUttakPeriode periode1Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        FastsattUttakPeriode periode2Annenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker, periode2Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20), stønadskonto(FEDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    public void har_søkt_samtidig_uttak() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medSamtidigUttak(true)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.søktSamtidigUttak(FELLESPERIODE)).isTrue();
        assertThat(saldoUtregning.søktSamtidigUttak(FEDREKVOTE)).isFalse();
    }

    @Test
    public void har_ikke_søkt_samtidig_uttak() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.søktSamtidigUttak(FELLESPERIODE)).isFalse();
        assertThat(saldoUtregning.søktSamtidigUttak(FEDREKVOTE)).isFalse();
    }

    @Test
    public void skal_støtte_en_søknadsperiode_overlapper_med_flere_oppholdsperioder_hos_annenpart() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 10))
                .build();

        FastsattUttakPeriode opphold1 = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 7))
                .build();
        FastsattUttakPeriode opphold2 = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 8), LocalDate.of(2019, 10, 8))
                .build();
        FastsattUttakPeriode uttakAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 11), LocalDate.of(2019, 10, 11))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(opphold1, opphold2, uttakAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        //1 per søknadsperioder + 1 for resterende opphold der det ikke er søkt
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(16);
    }

    @Test
    public void skal_støtte_flere_søknadsperioder_overlapper_med_opphold_hos_annenpart() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 7))
                .build();

        FastsattUttakPeriode periode2Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 8), LocalDate.of(2019, 10, 10))
                .build();

        FastsattUttakPeriode opphold = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 9))
                .build();
        FastsattUttakPeriode uttakAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 11), LocalDate.of(2019, 10, 11))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker, periode2Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(opphold, uttakAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(16);
    }

    @Test
    public void skal_ikke_telle_dobbelt_når_oppholdsperiode_annenpart_overlapper_mer_flere_søknadsperioder_i_tapende_behandling() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(42), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 1, 2), LocalDate.of(2020, 3, 1))
                .build();
        FastsattUttakPeriode periode2Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 3, 2), LocalDate.of(2020, 3, 5))
                .build();
        FastsattUttakPeriode periode3Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 3, 6), LocalDate.of(2020, 3, 12))
                .build();
        FastsattUttakPeriode oppholdAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(LocalDate.of(2020, 1, 2), LocalDate.of(2020, 3, 12))
                .build();
        FastsattUttakPeriode uttakAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 3, 13), LocalDate.of(2020, 3, 20))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker, periode2Søker, periode3Søker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(oppholdAnnenpart, uttakAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(
                Set.of(stønadskonto(FELLESPERIODE, 51), stønadskonto(FEDREKVOTE, 75), stønadskonto(MØDREKVOTE, 75)), perioderSøker,
                perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(0);
    }

    @Test
    public void skal_ikke_frigi_dager_fra_oppholdsperiode_hvis_overlapp_med_avslått_periode_annenpart() {
        FastsattUttakPeriode periode1Søker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(6), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 21), LocalDate.of(2019, 10, 23))
                .build();
        FastsattUttakPeriode oppholdSøker = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 24), LocalDate.of(2019, 10, 25))
                .build();
        FastsattUttakPeriode innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 24), LocalDate.of(2019, 10, 24))
                .build();
        FastsattUttakPeriode avslåttPeriodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.AVSLÅTT)
                .medTidsperiode(LocalDate.of(2019, 10, 25), LocalDate.of(2019, 10, 25))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periode1Søker, oppholdSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart, avslåttPeriodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isEqualTo(9);
    }

    @Test
    public void skal_ikke_trekke_dager_for_oppholdsperioder_på_annenpart_som_ligger_etter_søkers_siste_periode() {
        FastsattUttakPeriode periodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FEDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 29), LocalDate.of(2019, 10, 30))
                .build();
        FastsattUttakPeriode innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 28), LocalDate.of(2019, 10, 28))
                .build();
        FastsattUttakPeriode oppholdAnnenpart1 = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 30), LocalDate.of(2019, 10, 30))
                .build();
        FastsattUttakPeriode oppholdAnnenpart2 = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medOppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 31), LocalDate.of(2019, 10, 31))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart, oppholdAnnenpart1, oppholdAnnenpart2);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10), stønadskonto(MØDREKVOTE, 10)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isZero();
        SaldoUtregning saldoUtregningTapende = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10), stønadskonto(MØDREKVOTE, 10)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregningTapende.saldo(FEDREKVOTE)).isZero();
    }

    @Test
    public void riktig_saldo_ved_delvis_overlapp_og_gradering_på_annenpart_der_annenpart_har_flere_arbeidsforhold() {
        var periodeSøker = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 1, 27), LocalDate.of(2020, 1, 31))
                .build();
        var innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET2_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(8), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 29))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of(periodeSøker);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 100)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldoITrekkdager(FELLESPERIODE)).isEqualTo(new Trekkdager(92));
    }

    @Test
    public void skal_ikke_finnes_nok_dager_å_frigi_ved_flere_arbeidsforhold_der_bare_det_ene_arbeidsforholdet_har_nok_dager() {
        var aktivitet1 = new FastsattUttakPeriodeAktivitet(new Trekkdager(8), FELLESPERIODE, AKTIVITET1_SØKER);
        var aktivitet2 = new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET2_SØKER);
        FastsattUttakPeriode søkersPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(List.of(aktivitet1, aktivitet2))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag, enTirsdag)
                .build();
        FastsattUttakPeriode annenpartsPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkersPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartsPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));
        SaldoUtregning saldoUtregningTapenendeBehandling = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker,
                perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
        assertThat(saldoUtregningTapenendeBehandling.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    public void skal_finne_saldo_for_søker_uten_uttaksperioder() {
        FastsattUttakPeriode innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 28), LocalDate.of(2019, 10, 28))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of();
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));
        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isEqualTo(10);
    }

    //FAGSYSTEM-81103
    @Test
    public void skal_finnes_nok_dager_å_frigi_hvis_annenparts_uttaksperiode_starter_før_men_overlapper_med_søkers_periode() {
        FastsattUttakPeriode søkersPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(32), FEDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 17), LocalDate.of(2019, 11, 29))
                .build();
        FastsattUttakPeriode annenpartsPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75.2), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkersPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartsPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 75)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FEDREKVOTE)).isTrue();
    }

    @Test
    public void skal_ikke_finnes_nok_dager_å_frigi_hvis_annenparts_uttaksperiode_starter_før_men_overlapper_med_søkers_periode_og_ikke_nok_dager_etter_søkers_fom() {
        FastsattUttakPeriode søkersPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(32), FEDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 2, 10))
                .build();
        FastsattUttakPeriode annenpartsPeriode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75.2), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .build();
        List<FastsattUttakPeriode> perioderSøker = List.of(søkersPeriode);
        List<FastsattUttakPeriode> perioderAnnenpart = List.of(annenpartsPeriode);
        SaldoUtregning saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 75)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER));

        //-20 dager på saldo, bare 19 dager igjen å frigi fra annenpart
        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FEDREKVOTE)).isFalse();
    }

    @Test
    public void skal_telle_riktig_antall_dager_på_annenpart_når_det_er_tilkommet_nytt_arbeidsforhold() {
        var annenpartsPeriode1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(80), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .build();
        var annenpartsPeriode2 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75), MØDREKVOTE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(75), MØDREKVOTE, AKTIVITET2_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var perioderAnnenpart = List.of(annenpartsPeriode1, annenpartsPeriode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 75), stønadskonto(FELLESPERIODE, 80)), List.of(),
                perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isZero();
    }

    @Test
    public void skal_telle_riktig_antall_dager_når_det_er_tilkommet_nytt_arbeidsforhold_og_siste_periode_før_tilkommet_er_opphold() {
        var periodeUtenNyttArbeidsforhold = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(40), MØDREKVOTE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 1), LocalDate.of(2020, 10, 13))
                .build();
        var opphold = new FastsattUttakPeriode.Builder().medAktiviteter(List.of())
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .medOppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .build();
        var periodeMedNyttArbeidsforhold = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(35), MØDREKVOTE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(35), MØDREKVOTE, AKTIVITET2_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var søkersPerioder = List.of(periodeUtenNyttArbeidsforhold, opphold, periodeMedNyttArbeidsforhold);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 75)), søkersPerioder, List.of(), false,
                Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER));

        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isZero();
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET2_SØKER)).isZero();
    }

    @Test
    public void innvilget_utsettelse_overlapper_med_annenpart() {
        var periode = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), null, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var søkersPerioder = List.of(periode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 75)), søkersPerioder, List.of(periodeAnnenpart),
                false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(75);
        assertThat(saldoUtregning.saldo(null)).isZero();
    }

    @Test
    public void skal_ta_med_overlappende_perioder_i_utregningen_av_dager_å_frigi_på_annenpart() {
        var periode1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 5, 17), LocalDate.of(2020, 5, 17))
                .build();
        var periode2 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 5, 18), LocalDate.of(2020, 5, 22))
                .build();
        var periodeAnnenpart1 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 5, 18), LocalDate.of(2020, 5, 20))
                .build();
        var periodeAnnenpart2 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 5, 21), LocalDate.of(2020, 5, 22))
                .build();
        var periodeAnnenpart3 = new FastsattUttakPeriode.Builder().medAktiviteter(
                List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .medPeriodeResultatType(Perioderesultattype.INNVILGET)
                .medTidsperiode(LocalDate.of(2020, 5, 23), LocalDate.of(2020, 5, 23))
                .build();
        var søkersPerioder = List.of(periode1, periode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 5)), søkersPerioder,
                List.of(periodeAnnenpart1, periodeAnnenpart2, periodeAnnenpart3), false, Set.of(AKTIVITET1_SØKER));

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(-10);
        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }
}
