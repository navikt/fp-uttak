package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePerioderRegelOrkestrering.mapTilÅrsak;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager.justerHelgTilMandag;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Virkedager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.FarUttakRundtFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;

public class TomKontoIdentifiserer {

    private TomKontoIdentifiserer() {
        //hindrer instansiering
    }

    public static Optional<TomKontoKnekkpunkt> identifiser(OppgittPeriode uttakPeriode,
                                                           List<AktivitetIdentifikator> aktiviteter,
                                                           SaldoUtregning saldoUtregning,
                                                           Stønadskontotype stønadskontotype,
                                                           LukketPeriode farRundtFødselIntervall,
                                                            boolean etterNesteStønadsperiode,
                                                            boolean skalTrekkeDager,
                                                            PeriodeResultatÅrsak periodeResultatÅrsak,
                                                            UtfallType utfallType,
                                                            boolean graderingAvslag,
                                                            SamtidigUttaksprosent samtidigUttaksprosent) {

        Map<LocalDate, TomKontoKnekkpunkt> knekkpunkter = new HashMap<>();
        for (var aktivitet : aktiviteter) {
            var redusertUttaksprosent = redusertUttaksprosent(uttakPeriode, aktivitet, graderingAvslag, samtidigUttaksprosent);
            var datoKontoGårTomIPeriode = finnDatoKontoGårTomIPeriode(uttakPeriode, aktivitet, saldoUtregning, stønadskontotype, skalTrekkeDager,
                redusertUttaksprosent);
            datoKontoGårTomIPeriode.ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            if (uttakPeriode.isFlerbarnsdager()) {
                var knekkpunktFlerbarnsdager = finnDatoKontoGårTomIPeriodeFlerbarnsdager(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager,
                    redusertUttaksprosent);
                knekkpunktFlerbarnsdager.ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }
            if (saldoUtregning.getMaxDagerMinsterett().merEnn0() && !etterNesteStønadsperiode) {
                finnDatoMinsterettOppbrukt(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager, utfallType, periodeResultatÅrsak,
                    redusertUttaksprosent).ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }
            if (saldoUtregning.getMaxDagerUtenAktivitetskrav().merEnn0()) {
                finnDatoDagerUtenAktivitetskravOppbrukt(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager, utfallType,
                    periodeResultatÅrsak, redusertUttaksprosent).ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }
            if (saldoUtregning.getMaxDagerEtterNesteStønadsperiode().merEnn0() && etterNesteStønadsperiode) {
                finnDatoNesteStønadsperiodeOppbrukt(uttakPeriode, aktivitet, saldoUtregning, skalTrekkeDager, utfallType,
                    periodeResultatÅrsak, redusertUttaksprosent).ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
            }
            if (farRundtFødselIntervall != null && FarUttakRundtFødsel.erKontoRelevant(stønadskontotype)
                && saldoUtregning.getFarUttakRundtFødselDager().merEnn0()) {
                // Fra lovendring 2022: Far kan ta ut 10 dager samtidig rundt fødsel (før termin, etter fødsel) + far alene kan ta ut fritt før uke 6.
                // Grensen på 10 samtidige dager rundt fødsel gjelder dermed kun tilfelle av kvoter og 1 barn
                finnDatoFarRundtFødselOppbrukt(uttakPeriode, aktivitet, farRundtFødselIntervall, saldoUtregning, skalTrekkeDager,
                    redusertUttaksprosent).ifPresent(dato -> knekkpunkter.put(dato, new TomKontoKnekkpunkt(dato)));
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
                                                                   boolean skalTrekkeDager,
                                                                   Optional<BigDecimal> redusertUttaksprosent) {
        if (!skalTrekkeDager) {
            return Optional.empty();
        }
        var saldo = saldoUtregning.nettoSaldoJustertForMinsterett(stønadskontotype, aktivitet, oppgittPeriode.kanTrekkeAvMinsterett());
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, saldo, redusertUttaksprosent);
    }

    private static Optional<LocalDate> finnDatoKontoGårTomIPeriodeFlerbarnsdager(OppgittPeriode oppgittPeriode,
                                                                                 AktivitetIdentifikator aktivitet,
                                                                                 SaldoUtregning saldoUtregning,
                                                                                 boolean skalTrekkeDager,
                                                                                 Optional<BigDecimal> redusertUttaksprosent) {
        if (!skalTrekkeDager) {
            return Optional.empty();
        }
        var saldo = saldoUtregning.restSaldoFlerbarnsdager(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, saldo, redusertUttaksprosent);
    }

    private static Optional<LocalDate> finnDatoMinsterettOppbrukt(OppgittPeriode oppgittPeriode,
                                                                  AktivitetIdentifikator aktivitet,
                                                                  SaldoUtregning saldoUtregning,
                                                                  boolean skalTrekkeDager,
                                                                  UtfallType utfallType,
                                                                  PeriodeResultatÅrsak periodeResultatÅrsak,
                                                                  Optional<BigDecimal> redusertUttaksprosent) {
        if (!trekkerMinsterett(oppgittPeriode, utfallType, periodeResultatÅrsak) || !skalTrekkeDager) {
            return Optional.empty();
        }
        var saldoMinsterett = saldoUtregning.restSaldoMinsterett(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, saldoMinsterett, redusertUttaksprosent);
    }

    private static Optional<LocalDate> finnDatoNesteStønadsperiodeOppbrukt(OppgittPeriode oppgittPeriode,
                                                                           AktivitetIdentifikator aktivitet,
                                                                           SaldoUtregning saldoUtregning,
                                                                           boolean skalTrekkeDager,
                                                                           UtfallType utfallType,
                                                                           PeriodeResultatÅrsak periodeResultatÅrsak,
                                                                           Optional<BigDecimal> redusertUttaksprosent) {
        if (!trekkerMinsterett(oppgittPeriode, utfallType, periodeResultatÅrsak) || !skalTrekkeDager) {
            return Optional.empty();
        }
        var saldoMinsterett = saldoUtregning.restSaldoEtterNesteStønadsperiode(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, saldoMinsterett, redusertUttaksprosent);
    }

    private static Optional<LocalDate> finnDatoDagerUtenAktivitetskravOppbrukt(OppgittPeriode oppgittPeriode,
                                                                               AktivitetIdentifikator aktivitet,
                                                                               SaldoUtregning saldoUtregning,
                                                                               boolean skalTrekkeDager,
                                                                               UtfallType utfallType,
                                                                               PeriodeResultatÅrsak periodeResultatÅrsak,
                                                                               Optional<BigDecimal> redusertUttaksprosent) {
        if (!trekkerMinsterett(oppgittPeriode, utfallType, periodeResultatÅrsak) || !skalTrekkeDager) {
            return Optional.empty();
        }

        var saldoMinsterett = saldoUtregning.restSaldoDagerUtenAktivitetskrav(aktivitet);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, saldoMinsterett, redusertUttaksprosent);
    }

    private static boolean trekkerMinsterett(OppgittPeriode oppgittPeriode, UtfallType utfallType, PeriodeResultatÅrsak resultatÅrsak) {
        return FastsattUttakPeriode.trekkerMinsterett(Perioderesultattype.fra(utfallType), mapTilÅrsak(resultatÅrsak), oppgittPeriode.isUtsettelse());
    }

    private static Optional<LocalDate> finnDatoFarRundtFødselOppbrukt(OppgittPeriode oppgittPeriode,
                                                                      AktivitetIdentifikator aktivitet,
                                                                      LukketPeriode farRundtFødselIntervall,
                                                                      SaldoUtregning saldoUtregning,
                                                                      boolean skalTrekkeDager,
                                                                      Optional<BigDecimal> redusertUttaksprosent) {
        if (saldoUtregning.getFarUttakRundtFødselDager().equals(Trekkdager.ZERO) || !skalTrekkeDager || oppgittPeriode.isFlerbarnsdager()
            || !FarUttakRundtFødsel.erPeriodeRelevant(farRundtFødselIntervall, oppgittPeriode)) {
            return Optional.empty();
        }
        var saldoFarRundtFødsel = saldoUtregning.restSaldoFarUttakRundtFødsel(aktivitet, farRundtFødselIntervall);
        return datoHvisSaldoOppbruktIPeriode(oppgittPeriode, saldoFarRundtFødsel, redusertUttaksprosent);
    }

    private static Optional<LocalDate> datoHvisSaldoOppbruktIPeriode(OppgittPeriode oppgittPeriode,
                                                                     Trekkdager saldo,
                                                                     Optional<BigDecimal> redusertUttaksprosent) {
        var trekkdagerIPeriodeFom = justerHelgTilMandag(oppgittPeriode.getFom());
        var saldoTilVirkedager = saldoTilVirkedager(saldo, redusertUttaksprosent);
        var datoKontoGårTom = Virkedager.plusVirkedager(trekkdagerIPeriodeFom, saldoTilVirkedager);
        if (datoKontoGårTom.isAfter(trekkdagerIPeriodeFom) && !datoKontoGårTom.isAfter(oppgittPeriode.getTom())) {
            return Optional.of(datoKontoGårTom);
        }

        return Optional.empty();
    }

    private static LocalDate tidligst(Set<LocalDate> knekkpunkter) {
        return knekkpunkter.stream().min(Comparator.comparing(date -> date)).orElseThrow();
    }

    private static Optional<BigDecimal> redusertUttaksprosent(OppgittPeriode periode,
                                                              AktivitetIdentifikator aktivitet,
                                                              boolean graderingAvslag,
                                                              SamtidigUttaksprosent samtidigUttaksprosent) {
        if (periode.erSøktGradering(aktivitet) && !graderingAvslag) {
            return graderer100EllerMer(periode) ? Optional.empty() : Optional.of(BigDecimal.valueOf(100).subtract(periode.getArbeidsprosent()));
        }
        if (samtidigUttaksprosent != null && samtidigUttaksprosent.merEnn0()) {
            return Optional.of(samtidigUttaksprosent.decimalValue());
        }
        return Optional.empty();
    }

    private static int saldoTilVirkedager(Trekkdager saldo, Optional<BigDecimal> redusertUttaksprosent) {
        if (saldo.mindreEnn0()) {
            return 0;
        }
        return redusertUttaksprosent
            .map(prosent -> saldoTilVirkedagerVedRedusertUttak(saldo, prosent))
            .orElseGet(saldo::rundOpp);
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
