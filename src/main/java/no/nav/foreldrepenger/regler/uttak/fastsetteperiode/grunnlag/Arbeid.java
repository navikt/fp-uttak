package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class Arbeid {

    private Set<Arbeidsforhold> arbeidsforholdListe = new HashSet<>();

    private Arbeid() {
    }

    public BigDecimal getStillingsprosent(LocalDate dato) {
        return arbeidsforholdListe.stream()
                .map(arbeidsforhold -> getStillingsprosent(dato, arbeidsforhold.getIdentifikator()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(100));
    }

    public BigDecimal getStillingsprosent(LocalDate dato, AktivitetIdentifikator aktivitet) {
        var arbeidsforhold = arbeidsforholdListe.stream()
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

    public static class Builder {

        private final Arbeid kladd = new Arbeid();

        public Builder leggTilArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforholdListe.add(arbeidsforhold);
            return this;
        }

        public Arbeid build() {
            return kladd;
        }
    }
}
