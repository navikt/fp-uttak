package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import static no.nav.foreldrepenger.regler.uttak.felles.Virkedager.justerHelgTilMandag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;

public class TomKontoIdentifiserer {

    private TomKontoIdentifiserer() {
        //hindrer instansiering
    }

    public static Optional<TomKontoKnekkpunkt> identifiser(OppgittPeriode uttakPeriode,
                                                           List<AktivitetIdentifikator> aktiviteter,
                                                           SaldoUtregning saldoUtregning,
                                                           Stønadskontotype stønadskontotype,
                                                           boolean skalTrekkeDager) {

        Map<LocalDate, TomKontoKnekkpunkt> knekkpunkter = new HashMap<>();
        for (var aktivitet : aktiviteter) {
            var datoKontoGårTomIPeriode = finnDatoKontoGårTomIPeriode(uttakPeriode, aktivitet, saldoUtregning,
                    stønadskontotype, skalTrekkeDager);
            datoKontoGårTomIPeriode.ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            if (uttakPeriode.isFlerbarnsdager()) {
                var knekkpunktFlerbarnsdager = finnDatoKontoGårTomIPeriodeFlerbarnsdager(uttakPeriode, aktivitet, saldoUtregning,
                        skalTrekkeDager);
                knekkpunktFlerbarnsdager.ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }
            finnDatoMinsterettOppbrukt(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager)
                    .ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            finnDatoDagerUtenAktivitetskravOppbrukt(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager)
                    .ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            if (saldoUtregning.harFarUttakRundtFødselPeriode()) {
                finnDatoFarRundtFødselOppbrukt(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager, stønadskontotype)
                        .ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }

        }
        if (knekkpunkter.isEmpty()) {
            return Optional.empty();
        }

        var tidligstDato = tidligst(knekkpunkter.keySet());
        return Optional.of(knekkpunkter.get(tidligstDato));
    }

    private static Optional<LocalDate> finnDatoKontoGårTomIPeriode(OppgittPeriode oppgittPeriode,
                                                                   AktivitetIdentifikator aktivitet,
                                                                   SaldoUtregning saldoUtregning,
                                                                   Stønadskontotype stønadskontotype,
                                                                   boolean skalTrekkeDager) {
        if (!skalTrekkeDager) {
            return Optional.empty();
        }
        var saldo = saldoUtregning.nettoSaldoJustertForMinsterett(stønadskontotype, aktivitet, oppgittPeriode.kanTrekkeAvMinsterett());
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, aktivitet, saldo);
    }

    private static Optional<LocalDate> finnDatoKontoGårTomIPeriodeFlerbarnsdager(OppgittPeriode oppgittPeriode,
                                                                                 AktivitetIdentifikator aktivitet,
                                                                                 SaldoUtregning saldoUtregning,
                                                                                 boolean skalTrekkeDager) {
        if (!skalTrekkeDager) {
            return Optional.empty();
        }
        var saldo = saldoUtregning.restSaldoFlerbarnsdager(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, aktivitet, saldo);
    }

    private static Optional<LocalDate> finnDatoMinsterettOppbrukt(OppgittPeriode oppgittPeriode,
                                                                  AktivitetIdentifikator aktivitet,
                                                                  SaldoUtregning saldoUtregning,
                                                                  boolean skalTrekkeDager) {
        if (!oppgittPeriode.gjelderPeriodeMinsterett() || !skalTrekkeDager) {
            return Optional.empty();
        }

        var saldoMinsterett = saldoUtregning.restSaldoMinsterett(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, aktivitet, saldoMinsterett);
    }

    private static Optional<LocalDate> finnDatoDagerUtenAktivitetskravOppbrukt(OppgittPeriode oppgittPeriode,
                                                                               AktivitetIdentifikator aktivitet,
                                                                               SaldoUtregning saldoUtregning,
                                                                               boolean skalTrekkeDager) {
        if (!oppgittPeriode.gjelderPeriodeMinsterett() || !skalTrekkeDager) {
            return Optional.empty();
        }

        var saldoMinsterett = saldoUtregning.restSaldoDagerUtenAktivitetskrav(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, aktivitet, saldoMinsterett);
    }

    private static Optional<LocalDate> finnDatoFarRundtFødselOppbrukt(OppgittPeriode oppgittPeriode,
                                                                      AktivitetIdentifikator aktivitet,
                                                                      SaldoUtregning saldoUtregning, boolean skalTrekkeDager,
                                                                      Stønadskontotype stønadskontotype) {
        // Fra lovendring 2022: Far kan ta ut 10 dager samtidig rundt fødsel (før termin, etter fødsel) + far alene kan ta ut fritt før uke 6.
        // Regelen om at første 6 ukene er forbeholdt mor får dermed kun effekt for beggeRett, 1 barn, og utenom de 10 samtidige dagene
        if (saldoUtregning.getFarUttakRundtFødselDager().equals(Trekkdager.ZERO) || !skalTrekkeDager ||
                oppgittPeriode.isFlerbarnsdager() || !Stønadskontotype.FEDREKVOTE.equals(stønadskontotype) ||
                !saldoUtregning.erPeriodeRelevantForFarUttakRundtFødselDager(oppgittPeriode)) {
            return Optional.empty();
        }
        var saldoFarRundtFødsel = saldoUtregning.restSaldoFarUttakRundtFødsel(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, aktivitet, saldoFarRundtFødsel);
    }

    private static Optional<LocalDate> datoHvisSaldoOppbruktIPeriode(OppgittPeriode oppgittPeriode,
                                                                     AktivitetIdentifikator aktivitet,
                                                                     Trekkdager saldo) {
        var trekkdagerIPeriodeFom = justerHelgTilMandag(oppgittPeriode.getFom());
        var saldoTilVirkedager = saldoTilVirkedager(oppgittPeriode, aktivitet, saldo);
        var datoKontoGårTom = Virkedager.plusVirkedager(trekkdagerIPeriodeFom, saldoTilVirkedager);
        if (datoKontoGårTom.isAfter(trekkdagerIPeriodeFom) && !datoKontoGårTom.isAfter(oppgittPeriode.getTom())) {
            return Optional.of(datoKontoGårTom);
        }

        return Optional.empty();
    }

    private static LocalDate tidligst(Set<LocalDate> knekkpunkter) {
        return knekkpunkter.stream().min(Comparator.comparing(date -> date)).orElseThrow();
    }

    private static int saldoTilVirkedager(OppgittPeriode periode, AktivitetIdentifikator aktivitet, Trekkdager saldo) {
        if (saldo.mindreEnn0()) {
            return 0;
        }
        if (periode.erSøktGradering(aktivitet)) {
            return saldoTilVirkedagerGradering(periode, saldo);
        }
        if (periode.erSøktSamtidigUttak()) {
            return saldoTilVirkedagerSamtidigUttak(periode, saldo);
        }
        return saldo.decimalValue().setScale(0, RoundingMode.UP).intValue();
    }

    private static int saldoTilVirkedagerSamtidigUttak(OppgittPeriode periode, Trekkdager saldo) {
        return saldoTilVirkedagerVedRedusertUttak(saldo, periode.getSamtidigUttaksprosent().decimalValue());
    }

    private static int saldoTilVirkedagerGradering(OppgittPeriode periode, Trekkdager saldo) {
        if (graderer100EllerMer(periode)) {
            return saldo.rundOpp();
        }
        return saldoTilVirkedagerVedRedusertUttak(saldo, BigDecimal.valueOf(100).subtract(periode.getArbeidsprosent()));
    }

    private static int saldoTilVirkedagerVedRedusertUttak(Trekkdager saldo, BigDecimal uttaksprosent) {
        var trekkdagerPerVirkedag = uttaksprosent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        if (saldo.gårAkkuratOppIHeleVirkedager(trekkdagerPerVirkedag)) {
            return saldo.decimalValue().divide(trekkdagerPerVirkedag, 0, RoundingMode.DOWN).intValue();
        }
        var virkedager = saldo.decimalValue().add(BigDecimal.ONE).divide(trekkdagerPerVirkedag, 4, RoundingMode.DOWN);
        if (virkedager.remainder(BigDecimal.valueOf(virkedager.intValue(), 0)).compareTo(BigDecimal.ZERO) == 0) {
            return virkedager.subtract(BigDecimal.ONE).intValue();
        }
        return virkedager.intValue();
    }

    private static boolean graderer100EllerMer(OppgittPeriode periode) {
        return periode.getArbeidsprosent().compareTo(BigDecimal.valueOf(100)) >= 0;
    }
}
