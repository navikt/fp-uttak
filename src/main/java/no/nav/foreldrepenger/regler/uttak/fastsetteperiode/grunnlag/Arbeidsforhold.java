package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.Objects;

public record Arbeidsforhold(AktivitetIdentifikator identifikator, LocalDate startdato) {

    private static final LocalDate DEFAULT_STARTDATO = LocalDate.MIN;

    public static Arbeidsforhold opprett(AktivitetIdentifikator identifikator, LocalDate startdato) {
        return new Arbeidsforhold(identifikator, startdato == null ? DEFAULT_STARTDATO : startdato);
    }

    public Arbeidsforhold(AktivitetIdentifikator identifikator) {
        this(identifikator, DEFAULT_STARTDATO);
    }

    public boolean erAktivtPåDato(LocalDate dato) {
        return !startdato().isAfter(dato);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Arbeidsforhold that && Objects.equals(identifikator, that.identifikator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifikator);
    }
}
