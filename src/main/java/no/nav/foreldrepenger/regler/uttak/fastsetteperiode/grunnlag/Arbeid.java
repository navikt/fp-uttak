package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class Arbeid {

    private final Set<Arbeidsforhold> arbeidsforholdListe =
            new HashSet<>(); // Arbeidsforhold brukt i beregning, tilkommet dersom gradering eller
    // refusjon
    private final Set<EndringAvStilling> endringAvStillingListe =
            new HashSet<>(); // Tidslinje for sum stillingsprosent fra AAregister

    private Arbeid() {}

    public BigDecimal getStillingsprosent(LocalDate dato) {
        return endringAvStillingListe.stream()
                .filter(eas -> !eas.dato().isAfter(dato))
                .max(Comparator.comparing(EndringAvStilling::dato))
                .map(EndringAvStilling::summertStillingsprosent)
                .orElseGet(() -> BigDecimal.valueOf(100));
    }

    /**
     * ALLE aktiviteter, som regel burde aktivitene hentes fra {@link OppgittPeriode ()} mtp at alle
     * perioder ikke har samme aktivieter
     */
    public Set<AktivitetIdentifikator> getAktiviteter() {
        return arbeidsforholdListe.stream()
                .map(Arbeidsforhold::identifikator)
                .collect(Collectors.toSet());
    }

    public Set<Arbeidsforhold> getArbeidsforhold() {
        return arbeidsforholdListe;
    }

    public static class Builder {

        private final Arbeid kladd = new Arbeid();

        public Builder arbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforholdListe.add(arbeidsforhold);
            return this;
        }

        public Builder endringAvStilling(EndringAvStilling endringAvStilling) {
            kladd.endringAvStillingListe.add(endringAvStilling);
            return this;
        }

        public Arbeid build() {
            return kladd;
        }
    }
}
