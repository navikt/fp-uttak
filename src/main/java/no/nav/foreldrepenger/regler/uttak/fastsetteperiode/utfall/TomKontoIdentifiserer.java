package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class TomKontoIdentifiserer {

    private TomKontoIdentifiserer() {
        //hindrer instansiering
    }

    public static Optional<TomKontoKnekkpunkt> identifiser(UttakPeriode uttakPeriode,
                                                           List<AktivitetIdentifikator> aktiviteter,
                                                           Trekkdagertilstand trekkdagertilstand,
                                                           Stønadskontotype stønadskontotype) {

        Map<LocalDate, TomKontoKnekkpunkt> knekkpunkter = new HashMap<>();
        for (AktivitetIdentifikator aktivitet : aktiviteter) {
            Optional<LocalDate> datoKontoGårTomIPeriode = finnDatoKontoGårTomIPeriode(uttakPeriode, aktivitet, trekkdagertilstand, stønadskontotype);
            datoKontoGårTomIPeriode.ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            if (uttakPeriode.isFlerbarnsdager()) {
                Optional<LocalDate> knekkpunktFlerbarnsdager = finnDatoKontoGårTomIPeriode(uttakPeriode, aktivitet, trekkdagertilstand, Stønadskontotype.FLERBARNSDAGER);
                knekkpunktFlerbarnsdager.ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }
        }
        if (knekkpunkter.isEmpty()) {
            return Optional.empty();
        }

        LocalDate tidligstDato = tidligst(knekkpunkter.keySet());
        return Optional.of(knekkpunkter.get(tidligstDato));
    }

    private static Optional<LocalDate> finnDatoKontoGårTomIPeriode(UttakPeriode uttakPeriode,
                                                                   AktivitetIdentifikator aktivitet,
                                                                   Trekkdagertilstand trekkdagertilstand,
                                                                   Stønadskontotype stønadskontotype) {
        if (!uttakPeriode.getSluttpunktTrekkerDager(aktivitet)) {
            return Optional.empty();
        }

        Trekkdager saldo = trekkdagertilstand.saldo(aktivitet, stønadskontotype);
        int saldoTilVirkedager = saldoTilVirkedager(uttakPeriode, aktivitet, saldo);

        LocalDate datoKontoGårTom = Virkedager.plusVirkedager(uttakPeriode.getFom(), saldoTilVirkedager);
        if (datoKontoGårTom.isAfter(uttakPeriode.getFom()) && !datoKontoGårTom.isAfter(uttakPeriode.getTom())) {
            return Optional.of(datoKontoGårTom);
        }

        return Optional.empty();
    }

    private static LocalDate tidligst(Set<LocalDate> knekkpunkter) {
        return knekkpunkter.stream().min(Comparator.comparing(date -> date)).get();
    }

    private static int saldoTilVirkedager(UttakPeriode periode, AktivitetIdentifikator aktivitet, Trekkdager saldo) {
        if (saldo.mindreEnn0()) {
            return 0;
        }
        if (periode.isGradering(aktivitet)) {
            return saldoTilVirkedagerGradering(periode, saldo);
        }
        if (periode.isSamtidigUttak()) {
            return saldoTilVirkedagerSamtidigUttak(periode, saldo);
        }
        return saldo.decimalValue().setScale(0, RoundingMode.UP).intValue();
    }

    private static int saldoTilVirkedagerSamtidigUttak(UttakPeriode periode, Trekkdager saldo) {
        return saldoTilVirkedagerVedRedusertUttak(saldo, periode.getSamtidigUttaksprosent().get());
    }

    private static int saldoTilVirkedagerGradering(UttakPeriode periode, Trekkdager saldo) {
        if (graderer100EllerMer(periode)) {
            return saldo.rundOpp();
        }
        return saldoTilVirkedagerVedRedusertUttak(saldo, BigDecimal.valueOf(100).subtract(periode.getGradertArbeidsprosent()));
    }

    private static int saldoTilVirkedagerVedRedusertUttak(Trekkdager saldo, BigDecimal uttaksprosent) {
        BigDecimal trekkdagerPerVirkedag = uttaksprosent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        if (saldo.gårAkkuratOppIHeleVirkedager(trekkdagerPerVirkedag)) {
            return saldo.decimalValue().divide(trekkdagerPerVirkedag, 0, RoundingMode.DOWN).intValue();
        }
        BigDecimal virkedager = saldo.decimalValue().add(BigDecimal.ONE).divide(trekkdagerPerVirkedag, 4, RoundingMode.DOWN);
        if (virkedager.remainder(BigDecimal.valueOf(virkedager.intValue(), 0)).compareTo(BigDecimal.ZERO) == 0) {
            return virkedager.subtract(BigDecimal.ONE).intValue();
        }
        return virkedager.intValue();
    }

    private static boolean graderer100EllerMer(UttakPeriode periode) {
        return periode.getGradertArbeidsprosent().compareTo(BigDecimal.valueOf(100)) >= 0;
    }
}
