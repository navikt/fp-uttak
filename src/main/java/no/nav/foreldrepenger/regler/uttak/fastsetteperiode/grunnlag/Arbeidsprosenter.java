package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class Arbeidsprosenter {
    private final Map<AktivitetIdentifikator, ArbeidTidslinje> arbeidsprosentPrAktivitet = new HashMap<>();

    public Arbeidsprosenter leggTil(AktivitetIdentifikator aktivitetIdentifikator, ArbeidTidslinje arbeidTidslinje) {
        arbeidsprosentPrAktivitet.put(aktivitetIdentifikator, arbeidTidslinje);
        return this;
    }

    public BigDecimal getArbeidsprosent(AktivitetIdentifikator aktivitetIdentifikator, LukketPeriode periode) {
        ArbeidTidslinje arbeidTidslinje = getArbeidsprosentTidslinje(aktivitetIdentifikator);
        return arbeidTidslinje.getArbeidsprosent(periode).orElseGet(() -> BigDecimal.valueOf(100));
    }

    private ArbeidTidslinje getArbeidsprosentTidslinje(AktivitetIdentifikator aktivitetIdentifikator) {
        ArbeidTidslinje arbeidTidslinje = arbeidsprosentPrAktivitet.get(aktivitetIdentifikator);
        if (arbeidTidslinje == null) {
            throw new IllegalArgumentException("Utvikler-feil: det skal være lagt på arbeidsprosenter (evt tom liste) for alle arbeidsforhold");
        }
        return arbeidTidslinje;
    }

    public Set<LocalDate> getAlleEndringstidspunkter() {
        Set<LocalDate> endringstidspunkter = new HashSet<>();
        for (ArbeidTidslinje tidslinje : arbeidsprosentPrAktivitet.values()) {
            for (LocalDateInterval periode : tidslinje.getArbeid().getDatoIntervaller()) {
                endringstidspunkter.add(periode.getFomDato());
                endringstidspunkter.add(periode.getTomDato().plusDays(1));
            }
        }
        return endringstidspunkter;
    }

    public BigDecimal getStillingsprosent(AktivitetIdentifikator aktivitetIdentifikator, LukketPeriode periode) {
        ArbeidTidslinje arbeidTidslinje = getArbeidsprosentTidslinje(aktivitetIdentifikator);
        return arbeidTidslinje.getStillingsprosent(periode).orElseGet(() -> BigDecimal.valueOf(100));
    }

    public BigDecimal getStillingsprosent(LukketPeriode periode) {
        BigDecimal stillingsprosent = BigDecimal.ZERO;
        for (ArbeidTidslinje tidslinje : arbeidsprosentPrAktivitet.values()) {
            stillingsprosent = stillingsprosent.add(tidslinje.getStillingsprosent(periode).orElseGet(() -> BigDecimal.valueOf(100)));
        }
        return stillingsprosent;
    }

    public List<AktivitetIdentifikator> getAktiviteter() {
        return new ArrayList<>(arbeidsprosentPrAktivitet.keySet());
    }
}
