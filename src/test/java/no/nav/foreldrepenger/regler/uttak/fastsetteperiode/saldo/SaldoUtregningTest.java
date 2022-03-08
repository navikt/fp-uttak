package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;


import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

class SaldoUtregningTest {

    private static final AktivitetIdentifikator AKTIVITET1_SØKER = AktivitetIdentifikator.forArbeid(new Orgnummer("111"), "222");
    private static final AktivitetIdentifikator AKTIVITET2_SØKER = AktivitetIdentifikator.forArbeid(new Orgnummer("333"), "444");
    private static final AktivitetIdentifikator AKTIVITET1_ANNENPART = AktivitetIdentifikator.forArbeid(new Orgnummer("555"), "666");
    private static final AktivitetIdentifikator AKTIVITET2_ANNENPART = AktivitetIdentifikator.forArbeid(new Orgnummer("777"), "888");
    private final LocalDate enTirsdag = LocalDate.of(2019, 2, 19);


    private Stønadskonto stønadskonto(Stønadskontotype kontoType, int trekkdager) {
        return new Stønadskonto(kontoType, new Trekkdager(trekkdager));
    }

    @Test
    void ingen_max_dag_og_ingen_trekkdager_skal_alltid_gi_0_i_saldo() {
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 0)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isZero();
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isZero();
    }

    @Test
    void max_dager_minus_trekkdag_skal_bli_saldo() {
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(10 - 5);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(10 - 5);
    }

    @Test
    void flere_trekk_gir_riktig_saldo() {
        var fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var fastsattUttakPeriode2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode1, fastsattUttakPeriode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 12)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(12 - 5 - 5);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(12 - 5 - 5);
    }

    @Test
    void skal_runde_opp_hvis_ikke_brukt_mer_enn_maks() {
        var fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10.5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode1);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 12)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(2);
    }

    @Test
    void skal_runde_opp_hvis_brukt_mer_enn_maks() {
        var fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(13.5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode1);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 12)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(-1);
    }

    @Test
    void for_stort_trekk_gir_riktig_saldo() {
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), MØDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(10 - 15);
    }

    @Test
    void returnerer_dagerUtenAktivitetskrav() {
        var fastsattUttakPeriode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FORELDREPENGER, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .resultatÅrsak(FastsattUttakPeriode.ResultatÅrsak.INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT)
                .build();
        var fastsattUttakPeriode2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FORELDREPENGER, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .resultatÅrsak(FastsattUttakPeriode.ResultatÅrsak.ANNET)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode1, fastsattUttakPeriode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FORELDREPENGER, 15)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null, new Trekkdager(10), new Trekkdager(10));
        assertThat(saldoUtregning.restSaldoDagerUtenAktivitetskrav(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(new Trekkdager(7));
        assertThat(saldoUtregning.saldo(FORELDREPENGER)).isEqualTo(2);
        assertThat(saldoUtregning.getMaxDagerUtenAktivitetskrav()).isEqualTo(new Trekkdager(10));
    }

    @Test
    void for_stort_trekk_som_ikke_bruker_minsterett_gir_riktig_saldo_minsterett() {
        // assert/netto: kanTrekkeAvMinsterett = false for MSP, Uttak, Opphold
        // Forbrukeravminsterett = innvilget utenom aktivitetskrav, avslag/søknadsfrist, eller manuellbehandling
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FORELDREPENGER, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .resultatÅrsak(FastsattUttakPeriode.ResultatÅrsak.INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FORELDREPENGER, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null, new Trekkdager(5), Trekkdager.ZERO);
        // Skal beholde dager pga minsterett 5 derfor 5-15
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, false).decimalValue().intValue()).isEqualTo(5-15);
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, true).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.restSaldoMinsterett(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(new Trekkdager(5));
        assertThat(saldoUtregning.restSaldoDagerUtenAktivitetskrav(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(Trekkdager.ZERO);
        assertThat(saldoUtregning.saldo(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(FORELDREPENGER)).isEqualTo(10 - 15);
    }

    @Test
    void for_stort_trekk_som_bruker_minsterett_gir_riktig_saldo_minsterett() {
        // assert/netto: kanTrekkeAvMinsterett = false for MSP, Uttak, Opphold
        // Forbrukeravminsterett = innvilget utenom aktivitetskrav, avslag/søknadsfrist, eller manuellbehandling
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FORELDREPENGER, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .resultatÅrsak(FastsattUttakPeriode.ResultatÅrsak.ANNET)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FORELDREPENGER, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null, new Trekkdager(5), Trekkdager.ZERO);
        // Skal forbruke minsterett
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, false).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, true).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.restSaldoMinsterett(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(Trekkdager.ZERO);
        assertThat(saldoUtregning.restSaldoDagerUtenAktivitetskrav(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(Trekkdager.ZERO);
        assertThat(saldoUtregning.saldo(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(FORELDREPENGER)).isEqualTo(10 - 15);
    }

    @Test
    void for_stort_trekk_som_ikke_bruker_minsterett_gir_riktig_saldo_utenAktivitetskrav() {
        // assert/netto: kanTrekkeAvMinsterett = false for MSP, Uttak, Opphold
        // Forbrukeravminsterett = innvilget utenom aktivitetskrav, avslag/søknadsfrist, eller manuellbehandling
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FORELDREPENGER, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .resultatÅrsak(FastsattUttakPeriode.ResultatÅrsak.INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FORELDREPENGER, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null, Trekkdager.ZERO, new Trekkdager(5));
        // Skal beholde dager pga minsterett 5 derfor 5-15
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, false).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, true).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.restSaldoMinsterett(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(Trekkdager.ZERO);
        assertThat(saldoUtregning.restSaldoDagerUtenAktivitetskrav(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(new Trekkdager(5));
        assertThat(saldoUtregning.saldo(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(FORELDREPENGER)).isEqualTo(10 - 15);
    }

    @Test
    void for_stort_trekk_som_bruker_minsterett_gir_riktig_saldo_utenAktivitetskrav() {
        // assert/netto: kanTrekkeAvMinsterett = false for MSP, Uttak, Opphold
        // Forbrukeravminsterett = innvilget utenom aktivitetskrav, avslag/søknadsfrist, eller manuellbehandling
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FORELDREPENGER, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .resultatÅrsak(FastsattUttakPeriode.ResultatÅrsak.ANNET)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FORELDREPENGER, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null, Trekkdager.ZERO, new Trekkdager(5));
        // Skal forbruke minsterett
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, false).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.nettoSaldoJustertForMinsterett(FORELDREPENGER, AKTIVITET1_SØKER, true).decimalValue().intValue()).isEqualTo(10-15);
        assertThat(saldoUtregning.restSaldoMinsterett(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(Trekkdager.ZERO);
        assertThat(saldoUtregning.restSaldoDagerUtenAktivitetskrav(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(Trekkdager.ZERO);
        assertThat(saldoUtregning.saldo(FORELDREPENGER, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(FORELDREPENGER)).isEqualTo(10 - 15);
    }

    @Test
    void for_stort_trekk_på_flere_aktiviteter_gir_riktig_saldo() {
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), MØDREKVOTE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(20), MØDREKVOTE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER), null, null);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(10 - 15);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET2_SØKER)).isEqualTo(10 - 20);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(10 - 15);
    }

    @Test
    void flere_trekk_på_forskjellig_aktivitet_gir_forskjellig_saldo() {
        var fastsattUttakPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), MØDREKVOTE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), MØDREKVOTE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(fastsattUttakPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER), null, null);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(5);
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET2_SØKER)).isZero();
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(5);
    }

    @Test
    void trekkdager_på_annen_part_skal_telle_med_i_saldo() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER),
                null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(3);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(-2);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    void minste_trekkdager_på_annen_part_skal_telle_med_i_saldo() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(7), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER),
                null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(8);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(3);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(8);
    }

    @Test
    void flere_trekk_på_annen_part_skal_telle_med_i_saldo() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var annenpartPeriode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        var annenpartPeriode2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode1, annenpartPeriode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER),
                null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(11);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(6);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(11);
    }

    @Test
    void ikke_stjele_men_summere_begge_parter_hvis_berørt_behandling() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .mottattDato(LocalDate.MAX)
                .samtidigUttak(false)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .mottattDato(LocalDate.MIN)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                true, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    void ikke_stjele_men_summere_begge_parter_hvis_tapende_periode() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .samtidigUttak(false)
                .mottattDato(LocalDate.MIN)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .mottattDato(LocalDate.MAX)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    void ikke_stjele_men_summere_begge_parter_hvis_tapende_periode_søkt_samme_dag_søker_søkt_først() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .samtidigUttak(false)
                .mottattDato(LocalDate.MIN)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .mottattDato(LocalDate.MIN)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), enTirsdag.atStartOfDay(), enTirsdag.atStartOfDay().plusHours(2));
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    void stjele_hvis_tapende_periode_søkt_samme_dag_og_annenpart_har_søkt_til_slutt() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .samtidigUttak(false)
                .mottattDato(LocalDate.MIN)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .mottattDato(LocalDate.MIN)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), enTirsdag.atStartOfDay().plusHours(2), enTirsdag.atStartOfDay());
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(15);
    }

    @Test
    void stjele_fra_annenpart_hvis_ikke_berørt_behandling_og_ikke_samtidig_uttak() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .samtidigUttak(false)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(15);
    }

    @Test
    void skal_summere_trekkdager_for_begge_parter_hvis_overlapp_og_samtidig_uttak_og_ikke_berørt_behandling() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .samtidigUttak(true)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    void skal_trekke_virkedager_fra_oppholdsperioder() {
        var periodeSøker1 = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15))
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .build();
        var periodeSøker2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(LocalDate.of(2019, 2, 18), LocalDate.of(2019, 2, 18))
                .build();
        var perioderSøker = List.of(periodeSøker1, periodeSøker2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(10 - 5);
    }

    @Test
    void skal_trekke_oppholdsperioder_for_annenpart() {
        var periodeSøker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        var oppholdAnnenpart = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 22))
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(LocalDate.of(2019, 2, 25), LocalDate.of(2019, 2, 25))
                .build();
        var perioderSøker = List.of(periodeSøker);
        var perioderAnnenpart = List.of(oppholdAnnenpart, periodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 5 - 7);
    }

    @Test
    void skal_ikke_stjele_fra_annenpart_hvis_søker_har_oppholdsperiode() {
        var oppholdsperiodeSøker = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .oppholdÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                .build();
        var periodeSøker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        var perioderSøker = List.of(periodeSøker, oppholdsperiodeSøker);
        var perioderAnnenpart = List.of(periodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20), stønadskonto(MØDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER), null,
                null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 2);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(20 - 5);
    }

    @Test
    void skal_ikke_knekke_hvis_periode_er_i_en_helg() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 16), LocalDate.of(2019, 2, 16))
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(LocalDate.of(2019, 2, 17), LocalDate.of(2019, 2, 17))
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 10), stønadskonto(FELLESPERIODE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER),
                null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(3);
        assertThat(saldoUtregning.saldo(FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(-2);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }

    @Test
    void overlappende_oppholdsperioder_skal_trekke_fra_annenparts_periode_ved() {
        var periodeSøker1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        var oppholdsperiodeSøker = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .build();
        var oppholdsperiodeAnnenpart = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .oppholdÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 18), LocalDate.of(2019, 2, 18))
                .build();
        var perioderSøker = List.of(periodeSøker1, oppholdsperiodeSøker);
        var perioderAnnenpart = List.of(oppholdsperiodeAnnenpart, periodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20), stønadskonto(MØDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER), null,
                null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 2);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(20 - 3);
    }

    @Test
    void overlapp_med_avslått_perioder_på_søker_skal_telles_dobbelt() {
        var søkerPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var annenpartPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(12), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var perioderSøker = List.of(søkerPeriode);
        var perioderAnnenpart = List.of(annenpartPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(20 - 12);
    }

    @Test
    void hvis_søkers_innvilget_periode_overlapper_med_annenparts_oppholdsperiode_skal_det_ikke_trekkes_dobbelt() {
        var periodeSøker1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), MØDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .build();
        var oppholdsperiodeSøker = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .oppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .build();
        var oppholdsperiodeAnnenpart = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 13))
                .oppholdÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER)
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 15))
                .build();
        var perioderSøker = List.of(periodeSøker1, oppholdsperiodeSøker);
        var perioderAnnenpart = List.of(oppholdsperiodeAnnenpart, periodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 20), stønadskonto(FEDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER), null,
                null);
        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isEqualTo(20 - 2);
        assertThat(saldoUtregning.saldo(MØDREKVOTE)).isEqualTo(20 - 3);
    }

    @Test
    void hvis_alle_søkers_perioder_er_etter_annenpart_skal_det_ikke_være_nok_dager_å_frigi() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15))
                .build();
        var periode2Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 16), LocalDate.of(2019, 2, 16))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 13), LocalDate.of(2019, 2, 13))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 14), LocalDate.of(2019, 2, 14))
                .build();

        var perioderSøker = List.of(periode1Søker, periode2Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    void hvis_ikke_alle_søkers_perioder_er_etter_annenpart_skal_det_være_nok_dager_å_frigi() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 20))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 10), LocalDate.of(2019, 2, 14))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 20))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    void annenpart_har_ikke_nok_dager_å_frigi_selv_med_perioder_etter_søkers_siste_periode_med_trekkdager() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    void annenpart_har_ikke_nok_dager_å_frigi_selv_med_perioder_etter_søkers_siste_periode_med_trekkdager_flere_arbeidsforhold_hos_annenpart() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager_hvis_søkers_siste_periode_starter_samme_dag_som_annenparts_siste_periode() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 5))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(30), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 12))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 30)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager_flere_arbeidsforhold_hos_annenpart() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    void annenpart_har_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager_oppholdsperiode() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .tidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 25))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    void annenpart_har_ikke_nok_dager_å_frigi_etter_søkers_siste_periode_med_trekkdager() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var periode2Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FEDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 4), LocalDate.of(2019, 3, 4))
                .build();

        var periode1Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 1))
                .build();
        var periode2Annenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 3), LocalDate.of(2019, 3, 3))
                .build();

        var perioderSøker = List.of(periode1Søker, periode2Søker);
        var perioderAnnenpart = List.of(periode1Annenpart, periode2Annenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20), stønadskonto(FEDREKVOTE, 20)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER), null,
                null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    void har_søkt_samtidig_uttak() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .samtidigUttak(true)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.søktSamtidigUttak(FELLESPERIODE)).isTrue();
        assertThat(saldoUtregning.søktSamtidigUttak(FEDREKVOTE)).isFalse();
    }

    @Test
    void har_ikke_søkt_samtidig_uttak() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 3, 2))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, List.of(), false,
                Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.søktSamtidigUttak(FELLESPERIODE)).isFalse();
        assertThat(saldoUtregning.søktSamtidigUttak(FEDREKVOTE)).isFalse();
    }

    @Test
    void skal_støtte_en_søknadsperiode_overlapper_med_flere_oppholdsperioder_hos_annenpart() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 10))
                .build();

        var opphold1 = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 7))
                .build();
        var opphold2 = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 8), LocalDate.of(2019, 10, 8))
                .build();
        var uttakAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 11), LocalDate.of(2019, 10, 11))
                .build();

        var perioderSøker = List.of(periode1Søker);
        var perioderAnnenpart = List.of(opphold1, opphold2, uttakAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        //1 per søknadsperioder + 1 for resterende opphold der det ikke er søkt
        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(16);
    }

    @Test
    void skal_støtte_flere_søknadsperioder_overlapper_med_opphold_hos_annenpart() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 7))
                .build();

        var periode2Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 8), LocalDate.of(2019, 10, 10))
                .build();

        var opphold = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 7), LocalDate.of(2019, 10, 9))
                .build();
        var uttakAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 11), LocalDate.of(2019, 10, 11))
                .build();

        var perioderSøker = List.of(periode1Søker, periode2Søker);
        var perioderAnnenpart = List.of(opphold, uttakAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 20)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(16);
    }

    @Test
    void skal_ikke_telle_dobbelt_når_oppholdsperiode_annenpart_overlapper_mer_flere_søknadsperioder_i_berørt_behandling() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(42), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 1, 2), LocalDate.of(2020, 3, 1))
                .build();
        var periode2Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 3, 2), LocalDate.of(2020, 3, 5))
                .build();
        var periode3Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 3, 6), LocalDate.of(2020, 3, 12))
                .build();
        var oppholdAnnenpart = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(LocalDate.of(2020, 1, 2), LocalDate.of(2020, 3, 12))
                .build();
        var uttakAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 3, 13), LocalDate.of(2020, 3, 20))
                .build();

        var perioderSøker = List.of(periode1Søker, periode2Søker, periode3Søker);
        var perioderAnnenpart = List.of(oppholdAnnenpart, uttakAnnenpart);
        var saldoUtregning = new SaldoUtregning(
                Set.of(stønadskonto(FELLESPERIODE, 51), stønadskonto(FEDREKVOTE, 75), stønadskonto(MØDREKVOTE, 75)), perioderSøker,
                perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(0);
    }

    @Test
    void skal_ikke_frigi_dager_fra_oppholdsperiode_hvis_overlapp_med_avslått_periode_annenpart() {
        var periode1Søker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(6), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 21), LocalDate.of(2019, 10, 23))
                .build();
        var oppholdSøker = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 24), LocalDate.of(2019, 10, 25))
                .build();
        var innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 24), LocalDate.of(2019, 10, 24))
                .build();
        var avslåttPeriodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .tidsperiode(LocalDate.of(2019, 10, 25), LocalDate.of(2019, 10, 25))
                .build();

        var perioderSøker = List.of(periode1Søker, oppholdSøker);
        var perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart, avslåttPeriodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isEqualTo(9);
    }

    @Test
    void skal_ikke_trekke_dager_for_oppholdsperioder_på_annenpart_som_ligger_etter_søkers_siste_periode() {
        var periodeSøker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FEDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 29), LocalDate.of(2019, 10, 30))
                .build();
        var innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 28), LocalDate.of(2019, 10, 28))
                .build();
        var oppholdAnnenpart1 = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 30), LocalDate.of(2019, 10, 30))
                .build();
        var oppholdAnnenpart2 = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .oppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 31), LocalDate.of(2019, 10, 31))
                .build();

        var perioderSøker = List.of(periodeSøker);
        var perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart, oppholdAnnenpart1, oppholdAnnenpart2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10), stønadskonto(MØDREKVOTE, 10)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isZero();
        var saldoUtregningBerørt = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10), stønadskonto(MØDREKVOTE, 10)),
                perioderSøker, perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregningBerørt.saldo(FEDREKVOTE)).isZero();
    }

    @Test
    void riktig_saldo_ved_delvis_overlapp_og_gradering_på_annenpart_der_annenpart_har_flere_arbeidsforhold() {
        var periodeSøker = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(5), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 1, 27), LocalDate.of(2020, 1, 31))
                .build();
        var innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET2_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(8), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 29))
                .build();

        var perioderSøker = List.of(periodeSøker);
        var perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 100)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldoITrekkdager(FELLESPERIODE)).isEqualTo(new Trekkdager(92));
    }

    @Test
    void skal_ikke_finnes_nok_dager_å_frigi_ved_flere_arbeidsforhold_der_bare_det_ene_arbeidsforholdet_har_nok_dager() {
        var aktivitet1 = new FastsattUttakPeriodeAktivitet(new Trekkdager(8), FELLESPERIODE, AKTIVITET1_SØKER);
        var aktivitet2 = new FastsattUttakPeriodeAktivitet(new Trekkdager(15), FELLESPERIODE, AKTIVITET2_SØKER);
        var søkersPeriode = new FastsattUttakPeriode.Builder().aktiviteter(List.of(aktivitet1, aktivitet2))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag, enTirsdag)
                .build();
        var annenpartsPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(enTirsdag.plusDays(1), enTirsdag.plusDays(1))
                .build();
        var perioderSøker = List.of(søkersPeriode);
        var perioderAnnenpart = List.of(annenpartsPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER), null, null);
        var saldoUtregningBerørtBehandling = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), perioderSøker,
                perioderAnnenpart, true, Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
        assertThat(saldoUtregningBerørtBehandling.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isFalse();
    }

    @Test
    void skal_finne_saldo_for_søker_uten_uttaksperioder() {
        var innvilgetPeriodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), MØDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 28), LocalDate.of(2019, 10, 28))
                .build();

        List<FastsattUttakPeriode> perioderSøker = List.of();
        var perioderAnnenpart = List.of(innvilgetPeriodeAnnenpart);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 10)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);
        assertThat(saldoUtregning.saldo(FEDREKVOTE)).isEqualTo(10);
    }

    //FAGSYSTEM-81103
    @Test
    void skal_finnes_nok_dager_å_frigi_hvis_annenparts_uttaksperiode_starter_før_men_overlapper_med_søkers_periode() {
        var søkersPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(32), FEDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 17), LocalDate.of(2019, 11, 29))
                .build();
        var annenpartsPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75.2), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .build();
        var perioderSøker = List.of(søkersPeriode);
        var perioderAnnenpart = List.of(annenpartsPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 75)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FEDREKVOTE)).isTrue();
    }

    @Test
    void skal_ikke_finnes_nok_dager_å_frigi_hvis_annenparts_uttaksperiode_starter_før_men_overlapper_med_søkers_periode_og_ikke_nok_dager_etter_søkers_fom() {
        var søkersPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(32), FEDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 2, 10))
                .build();
        var annenpartsPeriode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75.2), FEDREKVOTE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .build();
        var perioderSøker = List.of(søkersPeriode);
        var perioderAnnenpart = List.of(annenpartsPeriode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FEDREKVOTE, 75)), perioderSøker, perioderAnnenpart,
                false, Set.of(AKTIVITET1_SØKER), null, null);

        //-20 dager på saldo, bare 19 dager igjen å frigi fra annenpart
        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FEDREKVOTE)).isFalse();
    }

    @Test
    void skal_telle_riktig_antall_dager_på_annenpart_når_det_er_tilkommet_nytt_arbeidsforhold() {
        var annenpartsPeriode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(80), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .build();
        var annenpartsPeriode2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75), MØDREKVOTE, AKTIVITET1_ANNENPART),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(75), MØDREKVOTE, AKTIVITET2_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var perioderAnnenpart = List.of(annenpartsPeriode1, annenpartsPeriode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 75), stønadskonto(FELLESPERIODE, 80)), List.of(),
                perioderAnnenpart, false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isZero();
    }

    @Test
    void skal_telle_riktig_antall_dager_når_det_er_tilkommet_nytt_arbeidsforhold_og_siste_periode_før_tilkommet_er_opphold() {
        var periodeUtenNyttArbeidsforhold = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(40), MØDREKVOTE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 1), LocalDate.of(2020, 10, 13))
                .build();
        var opphold = new FastsattUttakPeriode.Builder().aktiviteter(List.of())
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2019, 10, 14), LocalDate.of(2020, 2, 20))
                .oppholdÅrsak(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER)
                .build();
        var periodeMedNyttArbeidsforhold = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(35), MØDREKVOTE, AKTIVITET1_SØKER),
                        new FastsattUttakPeriodeAktivitet(new Trekkdager(35), MØDREKVOTE, AKTIVITET2_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var søkersPerioder = List.of(periodeUtenNyttArbeidsforhold, opphold, periodeMedNyttArbeidsforhold);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(MØDREKVOTE, 75)), søkersPerioder, List.of(), false,
                Set.of(AKTIVITET1_SØKER, AKTIVITET2_SØKER), null, null);

        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET1_SØKER)).isZero();
        assertThat(saldoUtregning.saldo(MØDREKVOTE, AKTIVITET2_SØKER)).isZero();
    }

    @Test
    void innvilget_utsettelse_overlapper_med_annenpart() {
        var periode = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(0), null, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(75), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 2, 21), LocalDate.of(2020, 5, 5))
                .build();
        var søkersPerioder = List.of(periode);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 75)), søkersPerioder, List.of(periodeAnnenpart),
                false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(75);
        assertThat(saldoUtregning.saldo(null)).isZero();
    }

    @Test
    void skal_ta_med_overlappende_perioder_i_utregningen_av_dager_å_frigi_på_annenpart() {
        var periode1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 5, 17), LocalDate.of(2020, 5, 17))
                .build();
        var periode2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 5, 18), LocalDate.of(2020, 5, 22))
                .build();
        var periodeAnnenpart1 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(3), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 5, 18), LocalDate.of(2020, 5, 20))
                .build();
        var periodeAnnenpart2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(2), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 5, 21), LocalDate.of(2020, 5, 22))
                .build();
        var periodeAnnenpart3 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(10), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2020, 5, 23), LocalDate.of(2020, 5, 23))
                .build();
        var søkersPerioder = List.of(periode1, periode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 5)), søkersPerioder,
                List.of(periodeAnnenpart1, periodeAnnenpart2, periodeAnnenpart3), false, Set.of(AKTIVITET1_SØKER),
                null, null);

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(-10);
        assertThat(saldoUtregning.nokDagerÅFrigiPåAnnenpart(FELLESPERIODE)).isTrue();
    }

    @Test
    void samtidig_uttak_som_overlapper_delvis_med_oppholdsperioder_skal_trekke_dager_for_resten_av_oppholdet() {
        var samtidigUttak = new FastsattUttakPeriode.Builder()
                .samtidigUttak(true)
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2021, 8, 2), LocalDate.of(2021, 8, 6))
                .build();
        var oppholdAnnenpart = new FastsattUttakPeriode.Builder()
                .oppholdÅrsak(OppholdÅrsak.FELLESPERIODE_ANNEN_FORELDER)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2021, 8, 2), LocalDate.of(2021, 8, 13))
                .build();
        var søkersPeriode2 = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(4), FELLESPERIODE, AKTIVITET1_SØKER)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2021, 8, 10), LocalDate.of(2021, 8, 13))
                .build();
        var periodeAnnenpart = new FastsattUttakPeriode.Builder()
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(new Trekkdager(1), FELLESPERIODE, AKTIVITET1_ANNENPART)))
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .tidsperiode(LocalDate.of(2021, 8, 15), LocalDate.of(2021, 8, 15))
                .build();
        var søkersPerioder = List.of(samtidigUttak, søkersPeriode2);
        var saldoUtregning = new SaldoUtregning(Set.of(stønadskonto(FELLESPERIODE, 10)), søkersPerioder,
                List.of(oppholdAnnenpart, periodeAnnenpart), false, Set.of(AKTIVITET1_SØKER), null, null);

        assertThat(saldoUtregning.saldo(FELLESPERIODE)).isEqualTo(3);
    }
}
