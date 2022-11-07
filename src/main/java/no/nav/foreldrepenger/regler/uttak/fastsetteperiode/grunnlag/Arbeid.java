package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class Arbeid {

    private final Set<Arbeidsforhold> arbeidsforholdListe = new HashSet<>(); // Arbeidsforhold brukt i beregning, tilkommet dersom greadering eller refusjon
    private final Set<Arbeidsforhold> arbeidsforholdFraRegister = new HashSet<>(); // Alle arbeidsforhold fra register

    private Arbeid() {
    }

    public BigDecimal getStillingsprosent(LocalDate dato) {
        return arbeidsforholdFraRegister.stream()
                .map(arbeidsforhold -> getStillingsprosent(dato, arbeidsforhold.getIdentifikator()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(100));
    }

    public BigDecimal getStillingsprosent(LocalDate dato, AktivitetIdentifikator aktivitet) {
        var arbeidsforhold = arbeidsforholdFraRegister.stream()
                .filter(a -> a.getIdentifikator().equals(aktivitet))
                .findFirst()
                .orElseThrow();
        return arbeidsforhold.getStillingsprosent(dato);
    }

    /**
     * ALLE aktiviteter, som regel burde aktivitene hentes fra {@link OppgittPeriode ()} mtp at alle perioder ikke har samme aktivieter
     */
    public Set<AktivitetIdentifikator> getAktiviteter() {
        return arbeidsforholdListe.stream().map(Arbeidsforhold::getIdentifikator).collect(Collectors.toSet());
    }

    public Set<Arbeidsforhold> getArbeidsforhold() {
        return arbeidsforholdListe;
    }

    public Set<AktivitetIdentifikator> getRegisterAktiviteter() {
        return arbeidsforholdFraRegister.stream().map(Arbeidsforhold::getIdentifikator).collect(Collectors.toSet());
    }

    public Set<Arbeidsforhold> getArbeidsforholdFraRegister() {
        return arbeidsforholdFraRegister;
    }

    public static class Builder {

        private final Arbeid kladd = new Arbeid();

        public Builder arbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforholdListe.add(arbeidsforhold);
            return this;
        }

        public Builder arbeidsforholdFraRegister(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforholdFraRegister.add(arbeidsforhold);
            return this;
        }

        public Arbeid build() {
            return kladd;
        }
    }
}
