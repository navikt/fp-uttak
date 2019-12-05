package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Arbeidsforhold {

    private static final LocalDate DEFAULT_STARTDATO = LocalDate.MIN;

    private final AktivitetIdentifikator identifikator;
    private final Set<EndringAvStilling> endringAvStillingListe = new HashSet<>();
    private final Kontoer kontoer;
    private final LocalDate startdato;

    public Arbeidsforhold(AktivitetIdentifikator identifikator, Kontoer kontoer, LocalDate startdato) {
        this.identifikator = identifikator;
        this.kontoer = kontoer;
        this.startdato = startdato == null ? DEFAULT_STARTDATO : startdato;
    }

    public Arbeidsforhold(AktivitetIdentifikator identifikator, Kontoer.Builder kontoer, LocalDate startdato) {
        this(identifikator, kontoer.build(), startdato);
    }

    public Arbeidsforhold(AktivitetIdentifikator identifikator, Kontoer.Builder kontoer) {
        this(identifikator, kontoer.build());
    }

    public Arbeidsforhold(AktivitetIdentifikator identifikator, Kontoer kontoer) {
        this(identifikator, kontoer, null);
    }

    public AktivitetIdentifikator getIdentifikator() {
        return identifikator;
    }

    public Kontoer getKontoer() {
        return kontoer;
    }

    public LocalDate getStartdato() {
        return startdato;
    }

    public Arbeidsforhold leggTilEndringIStilling(EndringAvStilling endringAvStilling) {
        if (getStillingsprosent(endringAvStilling.getDato()).compareTo(endringAvStilling.getStillingsprosent()) != 0) {
            endringAvStillingListe.add(endringAvStilling);
        }
        return this;
    }

    public BigDecimal getStillingsprosent(LocalDate dato) {
        return endringAvStillingListe.stream()
                .sorted(Comparator.comparing(EndringAvStilling::getDato))
                .filter(endring -> !dato.isBefore(endring.getDato()))
                .findFirst()
                .map(EndringAvStilling::getStillingsprosent)
                .orElse(BigDecimal.valueOf(100));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbeidsforhold that = (Arbeidsforhold) o;
        return Objects.equals(identifikator, that.identifikator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifikator);
    }
}
